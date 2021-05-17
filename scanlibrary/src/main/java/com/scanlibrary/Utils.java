package com.scanlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Utils {

    private Utils() {

    }

    public static Uri getUri(Context context, Bitmap bitmap) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
            if (path == null || path.isEmpty()) {
                return BitmapUtils.INSTANCE.getUriFromBitmap(context, bitmap);
            }
            return Uri.parse(path);
        } catch (Exception ex) {
            return BitmapUtils.INSTANCE.getUriFromBitmap(context, bitmap);
        }
    }

    public static Bitmap getBitmap(Context context, Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            /*return ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                            context.getContentResolver(),
                            uri
                    )
            );*/
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } else {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        }
    }
}