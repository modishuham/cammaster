package com.scanlibrary;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
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
import java.nio.IntBuffer;

public class ResultFragment extends Fragment {

    private View view;
    private ImageView scannedImageView;
    private Bitmap original;
    private Bitmap transformed;
    private Bitmap transformedForMonoChrome;
    private Bitmap transformedBR;
    private Bitmap transformedOriginal;
    private Button brightnessButton, originalButton,
            magicColorButton, grayModeButton, bwButton,
            rotate, monochrome, doneButton, backButton, contrastButton,
            gammaEffect;
    private LinearLayout brightnessPopup;
    private LinearLayout contrastPopup;
    private SeekBar mBrightnessSeekBar, mSharpnessSeekBar;
    private Boolean isBrightnessPopupVisible = false;
    private Boolean isContrastPopupVisible = false;
    private static ProgressDialogFragment progressDialogFragment;
    private int rotationValue = 0;
    private Dialog dialog;

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
        monochrome = view.findViewById(R.id.monochrome);
        doneButton = view.findViewById(R.id.doneButton);
        backButton = view.findViewById(R.id.backButton);
        brightnessButton = view.findViewById(R.id.brightnessButton);
        brightnessPopup = view.findViewById(R.id.brightnessPopup);
        contrastPopup = view.findViewById(R.id.contrastPopup);
        gammaEffect = view.findViewById(R.id.gammaEffect);
        mBrightnessSeekBar = view.findViewById(R.id.seek_brightness);
        mSharpnessSeekBar = view.findViewById(R.id.seek_sharpness);
        initClickListener();
        getBitmap();
        gammaEffect.performClick();
    }

    private void initClickListener() {
        originalButton.setOnClickListener(new OriginalButtonClickListener());
        magicColorButton.setOnClickListener(new MagicColorButtonClickListener());
        grayModeButton.setOnClickListener(new GrayButtonClickListener());
        bwButton.setOnClickListener(new BWButtonClickListener());
        rotate.setOnClickListener(new RotateButtonClickListener());
        contrastButton.setOnClickListener(new ContrastButtonClickListener());
        monochrome.setOnClickListener(new MonoChromeButtonClickListener());
        doneButton.setOnClickListener(new DoneButtonClickListener());
        backButton.setOnClickListener(new BackButtonClickListener());
        brightnessButton.setOnClickListener(new BrightnessButtonClickListener());
        gammaEffect.setOnClickListener(new GammaEffectClickListener());
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

    private void getBitmap() {
        Uri uri = getUri();
        try {
            original = Utils.getBitmap(requireActivity(), uri);
            requireActivity().getContentResolver().delete(uri, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri getUri() {
        assert getArguments() != null;
        return getArguments().getParcelable(ScanConstants.SCANNED_RESULT);
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
                dialog = new Dialog(requireActivity());
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
                        transformed = ((ScanActivity) requireContext()).getBWBitmap(transformedOriginal);
                        setSelectedEffect(bwButton);
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
                isBrightnessPopupVisible = false;
            } else {
                contrastPopup.setVisibility(View.GONE);
                isContrastPopupVisible = false;
            }
        }
    }

    private class MonoChromeButtonClickListener implements View.OnClickListener {
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
                        transformedOriginal = ((ScanActivity) requireContext()).getRotateBitmap(original, rotationValue);
                        transformedForMonoChrome = ((ScanActivity) requireContext()).getMagicColorBitmap(transformedOriginal);
                        transformed = Bitmap.createBitmap(transformedForMonoChrome.getWidth(), transformedForMonoChrome.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(transformed);
                        ColorMatrix ma = new ColorMatrix();
                        ma.setSaturation(0);
                        Paint paint = new Paint();
                        paint.setColorFilter(new ColorMatrixColorFilter(ma));
                        canvas.drawBitmap(transformedForMonoChrome, 0, 0, paint);
                        setSelectedEffect(monochrome);
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
                        transformed = ((ScanActivity) requireContext()).getMagicColorBitmap(transformedOriginal);
                        setSelectedEffect(magicColorButton);
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
                transformed = original;
                scannedImageView.setImageBitmap(original);
                setSelectedEffect(originalButton);
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
                        transformed = ((ScanActivity) requireContext()).getGrayBitmap(transformedOriginal);
                        setSelectedEffect(grayModeButton);
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

    private class GammaEffectClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformedOriginal = ((ScanActivity) requireContext()).getRotateBitmap(original, rotationValue);
                        transformed = doGamma(transformedOriginal);
                        setSelectedEffect(gammaEffect);
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
                isContrastPopupVisible = false;
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

    public Bitmap doGamma(Bitmap src) {
        Bitmap bmGray = toGrayscale(src, 101 - 100); //101-i
        Bitmap bmInvert = toInverted(bmGray, 100); //i
        Bitmap bmBlur = toBlur(bmInvert, 100); //i
        return colorDodgeBlend(bmBlur, bmGray, 100);
    }

    private static Bitmap toGrayscale(Bitmap bmpOriginal, float saturation) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        cm.setSaturation(saturation / 100);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private static Bitmap toInverted(Bitmap src, float i) {
        ColorMatrix colorMatrix_Inverted =
                new ColorMatrix(new float[]{
                        -1, 0, 0, 0, 255,
                        0, -1, 0, 0, 255,
                        0, 0, -1, 0, 255,
                        0, 0, 0, i / 100, 0});

        ColorFilter colorFilter = new ColorMatrixColorFilter(
                colorMatrix_Inverted);

        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(src, 0, 0, paint);

        return bitmap;
    }

    private Bitmap toBlur(Bitmap input, float i) {
        try {
            RenderScript rsScript = RenderScript.create(requireContext());
            Allocation alloc = Allocation.createFromBitmap(rsScript, input);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript));
            blur.setRadius((i * 25) / 100);
            blur.setInput(alloc);

            Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

            blur.forEach(outAlloc);
            outAlloc.copyTo(result);

            rsScript.destroy();
            return result;
        } catch (Exception e) {
            // TODO: handle exception
            return input;
        }
    }

    /**
     * Blends 2 bitmaps to one and adds the color dodge blend mode to it.
     */
    public static Bitmap colorDodgeBlend(Bitmap source, Bitmap layer, float i) {
        Bitmap base = source.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap blend = layer.copy(Bitmap.Config.ARGB_8888, false);

        IntBuffer buffBase = IntBuffer.allocate(base.getWidth() * base.getHeight());
        base.copyPixelsToBuffer(buffBase);
        buffBase.rewind();

        IntBuffer buffBlend = IntBuffer.allocate(blend.getWidth() * blend.getHeight());
        blend.copyPixelsToBuffer(buffBlend);
        buffBlend.rewind();

        IntBuffer buffOut = IntBuffer.allocate(base.getWidth() * base.getHeight());
        buffOut.rewind();

        while (buffOut.position() < buffOut.limit()) {
            int filterInt = buffBlend.get();
            int srcInt = buffBase.get();

            int redValueFilter = Color.red(filterInt);
            int greenValueFilter = Color.green(filterInt);
            int blueValueFilter = Color.blue(filterInt);

            int redValueSrc = Color.red(srcInt);
            int greenValueSrc = Color.green(srcInt);
            int blueValueSrc = Color.blue(srcInt);

            int redValueFinal = colordodge(redValueFilter, redValueSrc, i);
            int greenValueFinal = colordodge(greenValueFilter, greenValueSrc, i);
            int blueValueFinal = colordodge(blueValueFilter, blueValueSrc, i);

            int pixel = Color.argb((int) (i * 255) / 100, redValueFinal, greenValueFinal, blueValueFinal);
            buffOut.put(pixel);
        }

        buffOut.rewind();

        base.copyPixelsFromBuffer(buffOut);
        blend.recycle();

        return base;
    }

    private static int colordodge(int in1, int in2, float i) {
        float image = (float) in2;
        float mask = (float) in1;
        return ((int) ((image == 255) ? image : Math.min(255, (((long) mask << (int) (i * 8) / 100) / (255 - image)))));
    }

    public static Bitmap sharpen(Bitmap src) {
        double[][] SharpConfig = new double[][]{
                {0, -2, 0},
                {-2, 11, -2},
                {0, -2, 0}
        };
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(SharpConfig);
        convMatrix.Factor = 3;
        return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
    }

    private void setSelectedEffect(Button selectedButton) {
        if (selectedButton.getId() == R.id.monochrome) {
            monochrome.setBackgroundColor(requireContext().getResources().getColor(R.color.colorEditActionBack));
            magicColorButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            originalButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            grayModeButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            bwButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            gammaEffect.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
        } else if (selectedButton.getId() == R.id.magicColor) {
            monochrome.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            magicColorButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorEditActionBack));
            originalButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            grayModeButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            bwButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            gammaEffect.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
        } else if (selectedButton.getId() == R.id.original) {
            monochrome.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            magicColorButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            originalButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorEditActionBack));
            grayModeButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            bwButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            gammaEffect.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
        } else if (selectedButton.getId() == R.id.grayMode) {
            monochrome.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            magicColorButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            originalButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            grayModeButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorEditActionBack));
            bwButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            gammaEffect.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
        } else if (selectedButton.getId() == R.id.BWMode) {
            monochrome.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            magicColorButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            originalButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            grayModeButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            bwButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorEditActionBack));
            gammaEffect.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
        } else if (selectedButton.getId() == R.id.gammaEffect) {
            monochrome.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            magicColorButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            originalButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            grayModeButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            bwButton.setBackgroundColor(requireContext().getResources().getColor(R.color.colorTransparent));
            gammaEffect.setBackgroundColor(requireContext().getResources().getColor(R.color.colorEditActionBack));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}