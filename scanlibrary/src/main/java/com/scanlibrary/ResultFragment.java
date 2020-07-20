package com.scanlibrary;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
                    transformedBR = changeBitmapContrastBrightness(transformed, 1 + progress, 0);
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
                    transformedBR = changeBitmapContrastBrightness(transformed, 1, progress + 1);
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
            try {
                Bitmap bitmap = transformed;
                if (transformedBR != null) {
                    bitmap = transformedBR;
                }
                if (bitmap == null) {
                    bitmap = original;
                }
                final Bitmap finalBitmap = bitmap;
                Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_save);
                Button saveBtnImg = dialog.findViewById(R.id.btn_save_img);
                Button saveBtnPdf = dialog.findViewById(R.id.btn_save_pdf);
                final EditText docName = dialog.findViewById(R.id.et_file_name);
                saveBtnImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (docName.getText().toString().isEmpty()) {
                            docName.setError("Can't Empty");
                        } else {
                            Intent data = new Intent();
                            Uri uri = Utils.getUri(getActivity(), finalBitmap);
                            data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                            data.putExtra(ScanConstants.SELECTED_BITMAP_TYPE, "IMG");
                            data.putExtra(ScanConstants.SELECTED_BITMAP_NAME, docName.getText().toString());
                            getActivity().setResult(Activity.RESULT_OK, data);
                            original.recycle();
                            System.gc();
                            getActivity().finish();
                        }
                    }
                });
                saveBtnPdf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (docName.getText().toString().isEmpty()) {
                            docName.setError("Can't Empty");
                        } else {
                            Intent data = new Intent();
                            Uri uri = Utils.getUri(getActivity(), finalBitmap);
                            data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                            data.putExtra(ScanConstants.SELECTED_BITMAP_TYPE, "PDF");
                            data.putExtra(ScanConstants.SELECTED_BITMAP_NAME, docName.getText().toString());
                            getActivity().setResult(Activity.RESULT_OK, data);
                            original.recycle();
                            System.gc();
                            getActivity().finish();
                        }
                    }
                });
                dialog.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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

    /**
     * @param bmp        input bitmap
     * @param contrast   0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return new bitmap
     */
    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });
        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);
        return ret;
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