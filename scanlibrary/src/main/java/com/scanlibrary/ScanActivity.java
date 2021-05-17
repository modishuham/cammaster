package com.scanlibrary;

import android.app.Activity;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;

public class ScanActivity extends FragmentActivity implements IScanner, ComponentCallbacks2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_layout);
        handleIntentPreference();
    }

    private void handleIntentPreference() {
        int preference = getIntent().getIntExtra(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
        if (preference == ScanConstants.OPEN_CAMERA) {
            openCameraX();
        } else if (preference == ScanConstants.OPEN_MEDIA) {
            openMediaContent();
        }
    }

    private void openCameraX() {
        startActivityForResult(new Intent(this, CameraActivity.class),
                ScanConstants.START_CAMERA_REQUEST_CODE);
    }

    public void openMediaContent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, ScanConstants.PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanConstants.START_CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null || data.getExtras() == null) {
                    Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String uriString = data.getExtras().getString(ScanConstants.SELECTED_CAMERA_BITMAP);
                ScanFragment fragment = new ScanFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(ScanConstants.SELECTED_BITMAP, Uri.parse(uriString));
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.content, fragment);
                fragmentTransaction.addToBackStack(ScanFragment.class.toString());
                fragmentTransaction.commit();
            } else {
                finish();
            }
        }
        if (requestCode == ScanConstants.PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                if (data == null || data.getData() == null) {
                    Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bitmap bitmap = BitmapUtils.INSTANCE.getBitmap(this, data.getData());
                postImagePick(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void postImagePick(Bitmap bitmap) {
        Uri uri = Utils.getUri(this, bitmap);
        bitmap.recycle();
        ScanFragment fragment = new ScanFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ScanConstants.SELECTED_BITMAP, uri);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content, fragment);
        fragmentTransaction.addToBackStack(ScanFragment.class.toString());
        fragmentTransaction.commit();
    }

    @Override
    public void onBitmapSelect(Uri uri) {
        ScanFragment fragment = new ScanFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ScanConstants.SELECTED_BITMAP, uri);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content, fragment);
        fragmentTransaction.addToBackStack(ScanFragment.class.toString());
        fragmentTransaction.commit();
    }

    @Override
    public void onScanFinish(Uri uri) {
        ResultFragment fragment = new ResultFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ScanConstants.SCANNED_RESULT, uri);
        fragment.setArguments(bundle);
        androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content, fragment);
        fragmentTransaction.addToBackStack(ResultFragment.class.toString());
        fragmentTransaction.commit();
    }

    @Override
    public void onTrimMemory(int level) {
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */
                /*new AlertDialog.Builder(this)
                        .setTitle(R.string.low_memory)
                        .setMessage(R.string.low_memory_message)
                        .create()
                        .show();*/
                break;
            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }

    public native Bitmap getScannedBitmap(Bitmap bitmap, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4);

    public native Bitmap getGrayBitmap(Bitmap bitmap);

    public native Bitmap getMagicColorBitmap(Bitmap bitmap);

    public native Bitmap getBWBitmap(Bitmap bitmap);

    public native float[] getPoints(Bitmap bitmap);

    public Bitmap getRotateBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public Bitmap getRotateBitmap(Bitmap bitmap, int rotationValue) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationValue);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("Scanner");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}