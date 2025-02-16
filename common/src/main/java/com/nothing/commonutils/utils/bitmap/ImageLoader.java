/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nothing.commonutils.utils.bitmap;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import androidx.exifinterface.media.ExifInterface;

public final class ImageLoader {

    private static final String LOGTAG = "ImageLoader";

    public static final String JPEG_MIME_TYPE = "image/jpeg";
    public static final int DEFAULT_COMPRESS_QUALITY = 95;

    public static final int ORI_NORMAL = ExifInterface.ORIENTATION_NORMAL;
    public static final int ORI_ROTATE_90 = ExifInterface.ORIENTATION_ROTATE_90;
    public static final int ORI_ROTATE_180 = ExifInterface.ORIENTATION_ROTATE_180;
    public static final int ORI_ROTATE_270 = ExifInterface.ORIENTATION_ROTATE_270;
    public static final int ORI_FLIP_HOR = ExifInterface.ORIENTATION_FLIP_HORIZONTAL;
    public static final int ORI_FLIP_VERT = ExifInterface.ORIENTATION_FLIP_VERTICAL;
    public static final int ORI_TRANSPOSE = ExifInterface.ORIENTATION_TRANSPOSE;
    public static final int ORI_TRANSVERSE = ExifInterface.ORIENTATION_TRANSVERSE;

    private static final int BITMAP_LOAD_BACKOUT_ATTEMPTS = 5;
    private static final float OVERDRAW_ZOOM = 1.2f;
    private ImageLoader() {}

    /**
     * Returns the Mime type for a Url.  Safe to use with Urls that do not
     * come from Gallery's content provider.
     */
    public static String getMimeType(Uri src) {
        String postfix = MimeTypeMap.getFileExtensionFromUrl(src.toString());
        String ret = null;
        if (postfix != null) {
            ret = MimeTypeMap.getSingleton().getMimeTypeFromExtension(postfix.toLowerCase());
        }
        return ret;
    }

    public static String getLocalPathFromUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        if (cursor == null) {
            return null;
        }
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(index);
    }

    /**
     * Returns the image's orientation flag.  Defaults to ORI_NORMAL if no valid
     * orientation was found.
     */
    public static int getMetadataOrientation(Context context, Uri uri) {
        if (uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to getOrientation");
        }

        // First try to find orientation data in Gallery's ContentProvider.
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri,
                    new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
                    null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                int ori = cursor.getInt(0);
                switch (ori) {
                    case 90:
                        return ORI_ROTATE_90;
                    case 270:
                        return ORI_ROTATE_270;
                    case 180:
                        return ORI_ROTATE_180;
                    default:
                        return ORI_NORMAL;
                }
            }
        } catch (SQLiteException e) {
            // Do nothing
        } catch (IllegalArgumentException e) {
            // Do nothing
        } catch (IllegalStateException e) {
            // Do nothing
        } finally {
            closeSilently(cursor);
        }
        ExifInterface exif = null;
        InputStream is = null;
        // Fall back to checking EXIF tags in file or input stream.
        try {
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
//                String mimeType = getMimeType(uri);
//                if (!JPEG_MIME_TYPE.equals(mimeType)) {
//                    return ORI_NORMAL;
//                }
                String path = uri.getPath();
                exif = new ExifInterface(new File(path));
            } else {
                is = context.getContentResolver().openInputStream(uri);
                exif = new ExifInterface(is);
            }
            return parseExif(exif);
        } catch (IOException e) {
            Log.w(LOGTAG, "Failed to read EXIF orientation", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Log.w(LOGTAG, "Failed to close InputStream", e);
            }
        }
        return ORI_NORMAL;
    }

    private static int parseExif(ExifInterface exif){
        int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch(orientation) {
            case ORI_NORMAL:
            case ORI_ROTATE_90:
            case ORI_ROTATE_180:
            case ORI_ROTATE_270:
            case ORI_FLIP_HOR:
            case ORI_FLIP_VERT:
            case ORI_TRANSPOSE:
            case ORI_TRANSVERSE:
                return orientation;
            default:
                return ORI_NORMAL;
        }
    }

    /**
     * Returns the rotation of image at the given URI as one of 0, 90, 180,
     * 270.  Defaults to 0.
     */
    public static int getMetadataRotation(Context context, Uri uri) {
        int orientation = getMetadataOrientation(context, uri);
        switch(orientation) {
            case ORI_ROTATE_90:
                return 90;
            case ORI_ROTATE_180:
                return 180;
            case ORI_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    /**
     * Takes an orientation and a bitmap, and returns the bitmap transformed
     * to that orientation.
     */
    public static Bitmap orientBitmap(Bitmap bitmap, int ori) {
        Matrix matrix = new Matrix();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (ori == ORI_ROTATE_90 ||
                ori == ORI_ROTATE_270 ||
                ori == ORI_TRANSPOSE ||
                ori == ORI_TRANSVERSE) {
            int tmp = w;
            w = h;
            h = tmp;
        }
        switch (ori) {
            case ORI_ROTATE_90:
                matrix.setRotate(90, w / 2f, h / 2f);
                break;
            case ORI_ROTATE_180:
                matrix.setRotate(180, w / 2f, h / 2f);
                break;
            case ORI_ROTATE_270:
                matrix.setRotate(270, w / 2f, h / 2f);
                break;
            case ORI_FLIP_HOR:
                matrix.preScale(-1, 1);
                break;
            case ORI_FLIP_VERT:
                matrix.preScale(1, -1);
                break;
            case ORI_TRANSPOSE:
                matrix.setRotate(90, w / 2f, h / 2f);
                matrix.preScale(1, -1);
                break;
            case ORI_TRANSVERSE:
                matrix.setRotate(270, w / 2f, h / 2f);
                matrix.preScale(1, -1);
                break;
            case ORI_NORMAL:
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
    }
    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException t) {
            Log.w(TAG, "close fail ", t);
        }
    }

    private static final String TAG = "ImageLoader";

    /**
     * Returns the bitmap for the rectangular region given by "bounds"
     * if it is a subset of the bitmap stored at uri.  Otherwise returns
     * null.
     */
    public static Bitmap loadRegionBitmap(Context context, BitmapCache cache,
                                          Uri uri, BitmapFactory.Options options,
                                          Rect bounds) {
        InputStream is = null;
        int w = 0;
        int h = 0;
        if (options.inSampleSize != 0) {
            return null;
        }
        try {
            is = context.getContentResolver().openInputStream(uri);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
            Rect r = new Rect(0, 0, decoder.getWidth(), decoder.getHeight());
            w = decoder.getWidth();
            h = decoder.getHeight();
            Rect imageBounds = new Rect(bounds);
            // return null if bounds are not entirely within the bitmap
            if (!r.contains(imageBounds)) {
                imageBounds.intersect(r);
                bounds.left = imageBounds.left;
                bounds.top = imageBounds.top;
            }
            Bitmap reuse = cache.getBitmap(imageBounds.width(),
                    imageBounds.height(), BitmapCache.REGION);
            options.inBitmap = reuse;
            Bitmap bitmap = decoder.decodeRegion(imageBounds, options);
            if (bitmap != reuse) {
                cache.cache(reuse); // not reused, put back in cache
            }
            return bitmap;
        } catch (FileNotFoundException e) {
            Log.e(LOGTAG, "FileNotFoundException for " + uri, e);
        } catch (IOException e) {
            Log.e(LOGTAG, "FileNotFoundException for " + uri, e);
        } catch (IllegalArgumentException e) {
            Log.e(LOGTAG, "exc, image decoded " + w + " x " + h + " bounds: "
                    + bounds.left + "," + bounds.top + " - "
                    + bounds.width() + "x" + bounds.height() + " exc: " + e);
        } finally {
            closeSilently(is);
        }
        return null;
    }

    /**
     * Returns the bounds of the bitmap stored at a given Url.
     */
    public static Rect loadBitmapBounds(Context context, Uri uri) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        loadBitmap(context, uri, o);
        return new Rect(0, 0, o.outWidth, o.outHeight);
    }

    /**
     * Loads a bitmap that has been downsampled using sampleSize from a given url.
     */
    public static Bitmap loadDownsampledBitmap(Context context, Uri uri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inSampleSize = sampleSize;
        return loadBitmap(context, uri, options);
    }


    /**
     * Returns the bitmap from the given uri loaded using the given options.
     * Returns null on failure.
     */
    public static Bitmap loadBitmap(Context context, Uri uri, BitmapFactory.Options o) {
        if (uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to loadBitmap");
        }
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(is, null, o);
        } catch (FileNotFoundException e) {
            Log.e(LOGTAG, "FileNotFoundException for " + uri, e);
        } finally {
            closeSilently(is);
        }
        return null;
    }

    /**
     * Loads a bitmap at a given URI that is downsampled so that both sides are
     * smaller than maxSideLength. The Bitmap's original dimensions are stored
     * in the rect originalBounds.
     *
     * @param uri URI of image to open.
     * @param context context whose ContentResolver to use.
     * @param maxSideLength max side length of returned bitmap.
     * @param originalBounds If not null, set to the actual bounds of the stored bitmap.
     * @param useMin use min or max side of the original image
     * @return downsampled bitmap or null if this operation failed.
     */
    public static Bitmap loadConstrainedBitmap(Uri uri, Context context, int maxSideLength,
            Rect originalBounds, boolean useMin) {
        if (maxSideLength <= 0 || uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to getScaledBitmap");
        }
        // Get width and height of stored bitmap
        Rect storedBounds = loadBitmapBounds(context, uri);
        if (originalBounds != null) {
            originalBounds.set(storedBounds);
        }
        int w = storedBounds.width();
        int h = storedBounds.height();

        // If bitmap cannot be decoded, return null
        if (w <= 0 || h <= 0) {
            return null;
        }

        // Find best downsampling size
        int imageSide = 0;
        if (useMin) {
            imageSide = Math.min(w, h);
        } else {
            imageSide = Math.max(w, h);
        }
        int sampleSize = 1;
        while (imageSide > maxSideLength) {
            imageSide >>>= 1;
            sampleSize <<= 1;
        }

        // Make sure sample size is reasonable
        if (sampleSize <= 0 ||
                0 >= (int) (Math.min(w, h) / sampleSize)) {
            return null;
        }
        return loadDownsampledBitmap(context, uri, sampleSize);
    }

    /**
     * Loads a bitmap at a given URI that is downsampled so that both sides are
     * smaller than maxSideLength. The Bitmap's original dimensions are stored
     * in the rect originalBounds.  The output is also transformed to the given
     * orientation.
     *
     * @param uri URI of image to open.
     * @param context context whose ContentResolver to use.
     * @param maxSideLength max side length of returned bitmap.
     * @param orientation  the orientation to transform the bitmap to.
     * @param originalBounds set to the actual bounds of the stored bitmap.
     * @return downsampled bitmap or null if this operation failed.
     */
    public static Bitmap loadOrientedConstrainedBitmap(Uri uri, Context context, int maxSideLength,
            int orientation, Rect originalBounds) {
        Bitmap bmap = loadConstrainedBitmap(uri, context, maxSideLength, originalBounds, false);
        if (bmap != null) {
            bmap = orientBitmap(bmap, orientation);
            if (bmap.getConfig()!= Bitmap.Config.ARGB_8888){
                bmap = bmap.copy( Bitmap.Config.ARGB_8888,true);
            }
        }
        return bmap;
    }

    public static Bitmap getScaleOneImageForPreset(Context context,
                                                   BitmapCache cache,
                                                   Uri uri, Rect bounds,
                                                   Rect destination) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        if (destination != null) {
            int thresholdWidth = (int) (destination.width() * OVERDRAW_ZOOM);
            if (bounds.width() > thresholdWidth) {
                int sampleSize = 1;
                int w = bounds.width();
                while (w > thresholdWidth) {
                    sampleSize *= 2;
                    w /= sampleSize;
                }
                options.inSampleSize = sampleSize;
            }
        }
        return loadRegionBitmap(context, cache, uri, options, bounds);
    }

    /**
     * Loads a bitmap that is downsampled by at least the input sample size. In
     * low-memory situations, the bitmap may be downsampled further.
     */
    public static Bitmap loadBitmapWithBackouts(Context context, Uri sourceUri, int sampleSize) {
        boolean noBitmap = true;
        int num_tries = 0;
        if (sampleSize <= 0) {
            sampleSize = 1;
        }
        Bitmap bmap = null;
        while (noBitmap) {
            try {
                // Try to decode, downsample if low-memory.
                bmap = loadDownsampledBitmap(context, sourceUri, sampleSize);
                noBitmap = false;
            } catch (OutOfMemoryError e) {
                // Try with more downsampling before failing for good.
                if (++num_tries >= BITMAP_LOAD_BACKOUT_ATTEMPTS) {
                    throw e;
                }
                bmap = null;
                System.gc();
                sampleSize *= 2;
            }
        }
        return bmap;
    }

    /**
     * Loads an oriented bitmap that is downsampled by at least the input sample
     * size. In low-memory situations, the bitmap may be downsampled further.
     */
    public static Bitmap loadOrientedBitmapWithBackouts(Context context, Uri sourceUri,
            int sampleSize) {
        Bitmap bitmap = loadBitmapWithBackouts(context, sourceUri, sampleSize);
        if (bitmap == null) {
            return null;
        }
        int orientation = getMetadataOrientation(context, sourceUri);
        bitmap = orientBitmap(bitmap, orientation);
        return bitmap;
    }

    /**
     * Loads bitmap from a resource that may be downsampled in low-memory situations.
     */
    public static Bitmap decodeResourceWithBackouts(Resources res, BitmapFactory.Options options,
            int id) {
        boolean noBitmap = true;
        int num_tries = 0;
        if (options.inSampleSize < 1) {
            options.inSampleSize = 1;
        }
        // Stopgap fix for low-memory devices.
        Bitmap bmap = null;
        while (noBitmap) {
            try {
                // Try to decode, downsample if low-memory.
                bmap = BitmapFactory.decodeResource(
                        res, id, options);
                noBitmap = false;
            } catch (OutOfMemoryError e) {
                // Retry before failing for good.
                if (++num_tries >= BITMAP_LOAD_BACKOUT_ATTEMPTS) {
                    throw e;
                }
                bmap = null;
                System.gc();
                options.inSampleSize *= 2;
            }
        }
        return bmap;
    }


}
