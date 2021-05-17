package com.scanlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class PickImageFragment extends Fragment {

    private Uri fileUri;
    private IScanner scanner;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof IScanner)) {
            throw new ClassCastException("Activity must implement IScanner");
        }
        this.scanner = (IScanner) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pick_image_fragment, container, false);
        init();
        return view;
    }

    private void init() {
        if (isIntentPreferenceSet()) {
            handleIntentPreference();
        } else {
            requireActivity().finish();
        }
    }

    private void clearTempImages() {
        try {
            File tempFolder = new File(requireActivity().getExternalFilesDir(null), Environment.DIRECTORY_PICTURES);
            for (File f : Objects.requireNonNull(tempFolder.listFiles()))
                f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleIntentPreference() {
        int preference = getIntentPreference();
        if (preference == ScanConstants.OPEN_CAMERA) {
            openCamera();
        } else if (preference == ScanConstants.OPEN_MEDIA) {
            openMediaContent();
        }
    }

    private boolean isIntentPreferenceSet() {
        int preference = 0;
        if (getArguments() != null) {
            preference = getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
        }
        return preference != 0;
    }

    private int getIntentPreference() {
        assert getArguments() != null;
        return getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
    }

    public void openMediaContent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE);
    }

    public void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File file = createImageFile();
        if (file.getParentFile() != null) {
            boolean isDirectoryCreated = file.getParentFile().mkdirs();
            Log.d("", "openCamera: isDirectoryCreated: " + isDirectoryCreated);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri tempFileUri = FileProvider.getUriForFile(requireActivity().getApplicationContext(),
                    "com.scanlibrary.provider1", // As defined in Manifest
                    file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri);
        } else {
            Uri tempFileUri = Uri.fromFile(file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri);
        }
        startActivityForResult(cameraIntent, ScanConstants.START_CAMERA_REQUEST_CODE);
    }

    private File createImageFile() {
        clearTempImages();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new
                Date());
        File file = new File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_" + timeStamp +
                ".jpg");
        fileUri = Uri.fromFile(file);
        return file;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("", "onActivityResult" + resultCode);
        Bitmap bitmap = null;
        if (resultCode == Activity.RESULT_OK) {
            try {
                switch (requestCode) {
                    case ScanConstants.START_CAMERA_REQUEST_CODE:
                        bitmap = BitmapUtils.INSTANCE.getBitmap(requireContext(),fileUri);
                        break;

                    case ScanConstants.PICKFILE_REQUEST_CODE:
                        bitmap = BitmapUtils.INSTANCE.getBitmap(requireContext(),data.getData());
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            requireActivity().finish();
        }
        if (bitmap != null) {
            postImagePick(bitmap);
        }
    }

    protected void postImagePick(Bitmap bitmap) {
        Uri uri = Utils.getUri(requireActivity(), bitmap);
        bitmap.recycle();
        scanner.onBitmapSelect(uri);
    }

}