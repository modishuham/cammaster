package com.scanlibrary;

import android.app.Activity;
import android.app.Dialog;
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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;

public class ResultFragment extends Fragment {

    private View view;
    private ImageView scannedImageView;
    private Bitmap original;
    private Bitmap transformed;
    private Bitmap transformedBR;
    private Bitmap transformedOriginal;
    private Button brightnessButton, originalButton,
            magicColorButton, grayModeButton, bwButton,
            rotate, mirror, doneButton, backButton, contrastButton;
    private LinearLayout brightnessPopup;
    private LinearLayout contrastPopup;
    private SeekBar mBrightnessSeekBar, mSharpnessSeekBar;
    private Boolean isBrightnessPopupVisible = false;
    private Boolean isContrastPopupVisible = false;
    private static ProgressDialogFragment progressDialogFragment;
    private int rotationValue = 0;
    private boolean mirrorValue = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.result_layout, container, false);
        init();
        return view;
    }

    private void init() {
        scannedImageView = view.findViewById(R.id.scannedImage);
        originalButton = view.findViewById(R.id.original);
        magicColorButton = view.findViewById(R.id.magicColor);
        grayModeButton = view.findViewById(R.id.grayMode);
        bwButton = view.findViewById(R.id.BWMode);
        rotate = view.findViewById(R.id.rotate);
        contrastButton = view.findViewById(R.id.contrastButton);
        mirror = view.findViewById(R.id.mirror);
        doneButton = view.findViewById(R.id.doneButton);
        backButton = view.findViewById(R.id.backButton);
        brightnessButton = view.findViewById(R.id.brightnessButton);
        brightnessPopup = view.findViewById(R.id.brightnessPopup);
        contrastPopup = view.findViewById(R.id.contrastPopup);
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
        rotate.setOnClickListener(new RotateButtonClickListener());
        contrastButton.setOnClickListener(new ContrastButtonClickListener());
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
                    if (progress > 1)
                        mBrightnessSeekBar.setProgress(0);
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
                    if (progress > 1)
                        mSharpnessSeekBar.setProgress(0);
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
            original = Utils.getBitmap(requireActivity(), uri);
            requireActivity().getContentResolver().delete(uri, null, null);
            return original;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri getUri() {
        assert getArguments() != null;
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
                Dialog dialog = new Dialog(requireActivity());
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
                            Uri uri = Utils.getUri(requireActivity(), finalBitmap);
                            data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                            data.putExtra(ScanConstants.SELECTED_BITMAP_TYPE, "IMG");
                            data.putExtra(ScanConstants.SELECTED_BITMAP_NAME, docName.getText().toString());
                            requireActivity().setResult(Activity.RESULT_OK, data);
                            original.recycle();
                            System.gc();
                            requireActivity().finish();
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
                            Uri uri = Utils.getUri(requireActivity(), finalBitmap);
                            data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                            data.putExtra(ScanConstants.SELECTED_BITMAP_TYPE, "PDF");
                            data.putExtra(ScanConstants.SELECTED_BITMAP_NAME, docName.getText().toString());
                            requireActivity().setResult(Activity.RESULT_OK, data);
                            original.recycle();
                            System.gc();
                            requireActivity().finish();
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
            requireActivity().finish();
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
                        transformedOriginal = ((ScanActivity) requireContext()).getRotateBitmap(original, rotationValue);
                        if (mirrorValue)
                            transformedOriginal = ((ScanActivity) requireContext()).getMirrorBitmap(transformedOriginal);
                        transformed = ((ScanActivity) requireContext()).getBWBitmap(transformedOriginal);
                        ResetBrightness();
                    } catch (final OutOfMemoryError e) {
                        requireActivity().runOnUiThread(new Runnable() {
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
                    requireActivity().runOnUiThread(new Runnable() {
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

    private class RotateButtonClickListener implements View.OnClickListener {
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
                        if (rotationValue > 360) {
                            rotationValue = 0;
                        }
                        rotationValue = rotationValue + 90;
                        transformed = ((ScanActivity) requireContext()).getRotateBitmap(transformed);
                        ResetBrightness();
                    } catch (final OutOfMemoryError e) {
                        requireActivity().runOnUiThread(new Runnable() {
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
                    requireActivity().runOnUiThread(new Runnable() {
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

    private class ContrastButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            if (!isContrastPopupVisible) {
                contrastPopup.setVisibility(View.VISIBLE);
                brightnessPopup.setVisibility(View.GONE);
                isContrastPopupVisible = true;
            } else {
                contrastPopup.setVisibility(View.GONE);
                isContrastPopupVisible = false;
            }
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
                        transformed = ((ScanActivity) requireContext()).getMirrorBitmap(transformed);
                        mirrorValue = !mirrorValue;
                        ResetBrightness();
                    } catch (final OutOfMemoryError e) {
                        requireActivity().runOnUiThread(new Runnable() {
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
                    requireActivity().runOnUiThread(new Runnable() {
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
                        transformedOriginal = ((ScanActivity) requireContext()).getRotateBitmap(original, rotationValue);
                        if (mirrorValue)
                            transformedOriginal = ((ScanActivity) requireContext()).getMirrorBitmap(transformedOriginal);
                        transformed = ((ScanActivity) requireContext()).getMagicColorBitmap(transformedOriginal);
                        ResetBrightness();
                    } catch (final OutOfMemoryError e) {
                        requireActivity().runOnUiThread(new Runnable() {
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
                    requireActivity().runOnUiThread(new Runnable() {
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
                rotationValue = 0;
                mirrorValue = false;
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
                        transformedOriginal = ((ScanActivity) requireContext()).getRotateBitmap(original, rotationValue);
                        if (mirrorValue)
                            transformedOriginal = ((ScanActivity) requireContext()).getMirrorBitmap(transformedOriginal);
                        transformed = ((ScanActivity) requireContext()).getGrayBitmap(transformedOriginal);
                        ResetBrightness();
                    } catch (final OutOfMemoryError e) {
                        requireActivity().runOnUiThread(new Runnable() {
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
                    requireActivity().runOnUiThread(new Runnable() {
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
                contrastPopup.setVisibility(View.GONE);
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
        FragmentManager fm = getChildFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    protected synchronized void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }
}