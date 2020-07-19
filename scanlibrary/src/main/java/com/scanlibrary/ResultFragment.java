package com.scanlibrary;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import java.io.IOException;

public class ResultFragment extends Fragment {

    private View view;
    private ImageView scannedImageView;
    private Bitmap original;
    private Bitmap transformed;
    private Bitmap transformedBR;
    private Button brightnessButton, originalButton,
            magicColorButton, grayModeButton, bwButton,
            leftRotate, rightRotate, mirror, doneButton,
            backButton;
    private LinearLayout brightnessPopup;
    private SeekBar mBrightnessSeekBar, mSharpnessSeekBar;
    private Boolean isBrightnessPopupVisible = false;
    private static ProgressDialogFragment progressDialogFragment;

    public ResultFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.result_layout, null);
        init();
        return view;
    }

    private void init() {
        scannedImageView = view.findViewById(R.id.scannedImage);
        originalButton = view.findViewById(R.id.original);
        magicColorButton = view.findViewById(R.id.magicColor);
        grayModeButton = view.findViewById(R.id.grayMode);
        bwButton = view.findViewById(R.id.BWMode);
        leftRotate = view.findViewById(R.id.rotate_left);
        rightRotate = view.findViewById(R.id.rotate_right);
        mirror = view.findViewById(R.id.mirror);
        doneButton = view.findViewById(R.id.doneButton);
        backButton = view.findViewById(R.id.backButton);
        brightnessButton = view.findViewById(R.id.brightnessButton);
        brightnessPopup = view.findViewById(R.id.brightnessPopup);
        mBrightnessSeekBar = view.findViewById(R.id.seek_brightness);
        mSharpnessSeekBar = view.findViewById(R.id.seek_sharpness);
        initClickListener();
        Bitmap bitmap = getBitmap();
        setScannedImage(bitmap);
    }

    private void initClickListener() {
        originalButton.setOnClickListener(new OriginalButtonClickListener());
        magicColorButton.setOnClickListener(new MagicColorButtonClickListener());
        grayModeButton.setOnClickListener(new GrayButtonClickListener());
        bwButton.setOnClickListener(new BWButtonClickListener());
        leftRotate.setOnClickListener(new LeftRotateButtonClickListener());
        rightRotate.setOnClickListener(new RightRotateButtonClickListener());
        mirror.setOnClickListener(new MirrorButtonClickListener());
        doneButton.setOnClickListener(new DoneButtonClickListener());
        backButton.setOnClickListener(new BackButtonClickListener());
        brightnessButton.setOnClickListener(new BrightnessButtonClickListener());
        initSeekBarListener();
    }

    private void initSeekBarListener() {

        mSharpnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    if (transformed == null)
                        transformed = original;
                    transformedBR = adjustedContrast(transformed, progress);
                } catch (Exception e) {
                    transformed = original;
                    scannedImageView.setImageBitmap(original);
                    e.printStackTrace();
                }
                scannedImageView.setImageBitmap(transformedBR);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBrightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                try {
                    if (transformed == null)
                        transformed = original;
                    transformedBR = changeBrightness(transformed, progress);
                } catch (Exception e) {
                    transformed = original;
                    scannedImageView.setImageBitmap(original);
                    e.printStackTrace();
                }
                scannedImageView.setImageBitmap(transformedBR);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private Bitmap getBitmap() {
        Uri uri = getUri();
        try {
            original = Utils.getBitmap(getActivity(), uri);
            getActivity().getContentResolver().delete(uri, null, null);
            return original;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri getUri() {
        return getArguments().getParcelable(ScanConstants.SCANNED_RESULT);
    }

    public void setScannedImage(Bitmap scannedImage) {
        scannedImageView.setImageBitmap(scannedImage);
    }

    private class DoneButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.loading));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent data = new Intent();
                        Bitmap bitmap = transformed;
                        if (transformedBR != null) {
                            bitmap = transformedBR;
                        }
                        if (bitmap == null) {
                            bitmap = original;
                        }
                        Uri uri = Utils.getUri(getActivity(), bitmap);
                        data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                        getActivity().setResult(Activity.RESULT_OK, data);
                        original.recycle();
                        System.gc();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissDialog();
                                getActivity().finish();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private class BackButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            original.recycle();
            System.gc();
            getActivity().finish();
        }
    }

    private class BWButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = ((ScanActivity) getActivity()).getBWBitmap(original);
                        ResetBrightness();
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }

    private class LeftRotateButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (transformed == null) {
                            transformed = original;
                        }
                        transformed = ((ScanActivity) getActivity()).getLeftRotateBitmap(transformed);
                        ResetBrightness();
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }

    private class RightRotateButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (transformed == null) {
                            transformed = original;
                        }
                        transformed = ((ScanActivity) getActivity()).getRightRotateBitmap(transformed);
                        ResetBrightness();
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }

    private class MirrorButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (transformed == null) {
                            transformed = original;
                        }
                        transformed = ((ScanActivity) getActivity()).getMirrorBitmap(transformed);
                        ResetBrightness();
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }

    private class MagicColorButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = ((ScanActivity) getActivity()).getMagicColorBitmap(original);
                        ResetBrightness();
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }

    private class OriginalButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                showProgressDialog(getResources().getString(R.string.applying_filter));
                transformed = original;
                scannedImageView.setImageBitmap(original);
                ResetBrightness();
                dismissDialog();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                dismissDialog();
            }
        }
    }

    private class GrayButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = ((ScanActivity) getActivity()).getGrayBitmap(original);
                        ResetBrightness();
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }

    private class BrightnessButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            if (!isBrightnessPopupVisible) {
                brightnessPopup.setVisibility(View.VISIBLE);
                isBrightnessPopupVisible = true;
            } else {
                brightnessPopup.setVisibility(View.GONE);
                isBrightnessPopupVisible = false;
            }
        }
    }

    private void ResetBrightness() {
        mBrightnessSeekBar.setProgress(0);
        mSharpnessSeekBar.setProgress(0);
        transformedBR = null;
    }

    public static Bitmap changeBrightness(Bitmap src, int value) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        int A, R, G, B;
        int pixel;
        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // increase/decrease each channel
                R += value;
                if (R > 255) {
                    R = 255;
                } else if (R < 0) {
                    R = 0;
                }
                G += value;
                if (G > 255) {
                    G = 255;
                } else if (G < 0) {
                    G = 0;
                }
                B += value;
                if (B > 255) {
                    B = 255;
                } else if (B < 0) {
                    B = 0;
                }
                // apply new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        // return final image
        return bmOut;
    }

    private Bitmap adjustedContrast(Bitmap src, double value) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // create a canvas so that we can draw the bmOut Bitmap from source bitmap
        Canvas c = new Canvas();
        c.setBitmap(bmOut);
        // draw bitmap to bmOut from src bitmap so we can modify it
        c.drawBitmap(src, 0, 0, new Paint(Color.BLACK));
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);
        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (R < 0) {
                    R = 0;
                } else if (R > 255) {
                    R = 255;
                }
                G = Color.green(pixel);
                G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (G < 0) {
                    G = 0;
                } else if (G > 255) {
                    G = 255;
                }
                B = Color.blue(pixel);
                B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (B < 0) {
                    B = 0;
                } else if (B > 255) {
                    B = 255;
                }
                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return bmOut;
    }

    protected synchronized void showProgressDialog(String message) {
        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment.dismissAllowingStateLoss();
        }
        progressDialogFragment = null;
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    protected synchronized void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }
}