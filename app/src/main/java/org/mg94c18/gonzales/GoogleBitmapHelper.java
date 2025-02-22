package org.mg94c18.gonzales;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class GoogleBitmapHelper {
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (reqWidth == 0 || reqHeight == 0) {
            return inSampleSize;
        }

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Creates sampled bitmap from input stream, using given file for temp storage.
     * @param inputStream the stream to read from and consume; we don't call .close on the stream
     * @param tempFile the caller is responsible for deleting the file
     * @return the scaled bitmap from the input stream
     * @throws IOException in case reading from the stream or writing to file fails
     */
    static String decodeSampledBitmapFromStream(InputStream inputStream, File tempFile) throws IOException {
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream, fileOutputStream);
            fileOutputStream.close();
            return tempFile.getAbsolutePath();
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }
    }
}
