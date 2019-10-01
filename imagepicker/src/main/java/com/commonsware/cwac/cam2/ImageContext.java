/**
 * Copyright (c) 2015 CommonsWare, LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commonsware.cwac.cam2;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import com.android.mms.exif.ExifInterface;
import com.android.mms.exif.ExifTag;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Represents a picture taken by the camera, to be passed through
 * the ImageProcessor chain.
 *
 * The ImageContext should always hold the byte[] representing
 * the JPEG image. If an ImageProcessor needs a Bitmap, it can
 * call getBitmap(true) to force creation of a Bitmap for those
 * JPEG bytes, but this is memory-intensive and should be avoided
 * where possible.
 */
public class ImageContext {
  private static final double LOG_2=Math.log(2.0d);
  private Context ctxt;
  private byte[] jpegOriginal;
  private Bitmap bmp;
  private Bitmap thumbnail;
  private ExifInterface exif;

  ImageContext(Context ctxt, byte[] jpeg) {
    this.ctxt=ctxt.getApplicationContext();
    setJpeg(jpeg);
  }

  /**
   * @return an Android Context suitable for use in cases where
   * you need filesystem paths and the like
   */
  public Context getContext() {
    return(ctxt);
  }

  /**
   * @return the byte[] of JPEG-encoded data for the picture
   */
  public byte[] getJpeg() {
    return(jpegOriginal);
  }

  /**
   * Updates the JPEG data, invalidating any previous Bitmap.
   *
   * @param jpeg the new JPEG data
   */
  public void setJpeg(byte[] jpeg) {
    this.jpegOriginal=jpeg;
    this.bmp=null;
    this.thumbnail=null;
  }

  public ExifInterface getExifInterface() throws IOException {
    if (exif==null) {
      exif=new ExifInterface();

      exif.readExif(jpegOriginal);
    }

    return(exif);
  }

  public int getOrientation() throws IOException {
    ExifTag tag=getExifInterface().getTag(ExifInterface.TAG_ORIENTATION);

    return(tag==null ? -1 : tag.getValueAsInt(-1));
  }

  public byte[] getJpeg(boolean normalizeOrientation) {
    if (normalizeOrientation) {
      try {
        int orientation=getOrientation();

        if (needsNormalization(orientation)) {
          try {
            Bitmap original=
              BitmapFactory.decodeByteArray(jpegOriginal, 0,
                jpegOriginal.length);
            Bitmap rotated=rotateViaMatrix(original, orientation);

            exif.setTagValue(ExifInterface.TAG_ORIENTATION, 1);
            exif.removeCompressedThumbnail();

            ByteArrayOutputStream baos=new ByteArrayOutputStream();

            exif.writeExif(rotated, baos, 100);
            jpegOriginal=baos.toByteArray();

            return(jpegOriginal);
          }
          catch (OutOfMemoryError e) {
            AbstractCameraActivity.BUS
              .post(new CameraEngine.DeepImpactEvent(e));
          }
        }
      }
      catch (Exception e) {
        AbstractCameraActivity.BUS
          .post(new CameraEngine.DeepImpactEvent(e));
      }
    }

    return(getJpeg());
  }

  /**
   * Retrieve a Bitmap rendition of the picture. Try to avoid
   * this where possible, as it is memory-intensive.
   *
   * @param force true if you want to force creation of a Bitmap
   *              if there is none, false if you want the Bitmap
   *              but can live without it if it is unavailable
   * @return the Bitmap rendition of the picture
   */
  public Bitmap getBitmap(boolean force, boolean normalizeOrientation) {
    if (bmp==null && force) {
      updateBitmap(normalizeOrientation);
    }

    return(bmp);
  }

  public Bitmap buildPreviewThumbnail(Context ctxt, Float quality,
                                      boolean normalizeOrientation) {
    // TODO: move this into PictureTransaction work somewhere, so done
    // on a background thread

    if (thumbnail==null) {
      int limit=2000000;

      if (quality!=null && quality>0.0f && quality<1.0f) {
        ActivityManager am=(ActivityManager)ctxt.getSystemService(Context.ACTIVITY_SERVICE);
        int flags=ctxt.getApplicationInfo().flags;
        int memoryClass=am.getMemoryClass();

        if ((flags & ApplicationInfo.FLAG_LARGE_HEAP)!=0) {
          memoryClass=am.getLargeMemoryClass();
        }

        limit=(int)(1024*1024*memoryClass*quality);
      }

      thumbnail=createBitmap(null, limit, normalizeOrientation);
    }

    return(thumbnail);
  }

  public Bitmap buildResultThumbnail(boolean normalizeOrientation) {
    // TODO: move this onto background thread

    return(createBitmap(null, 750000, normalizeOrientation));
  }

  private Bitmap createBitmap(Bitmap inBitmap, int limit,
                              boolean normalizeOrientation) {
    double ratio=(double)jpegOriginal.length * 10.0d / (double)limit;
    int inSampleSize;

    if (ratio > 1.0d) {
      inSampleSize=1 << (int)(Math.ceil(Math.log(ratio) / LOG_2));
    } else {
      inSampleSize=1;
    }

    return(createBitmap(inSampleSize, inBitmap, limit,
      normalizeOrientation));
  }

  private Bitmap createBitmap(int inSampleSize, Bitmap inBitmap,
                              int limit,
                              boolean normalizeOrientation) {
    BitmapFactory.Options opts=new BitmapFactory.Options();

    opts.inSampleSize=inSampleSize;
    opts.inBitmap=inBitmap;

    Bitmap result;

    try {
      result=
         BitmapFactory.decodeByteArray(jpegOriginal, 0, jpegOriginal.length, opts);

      if (limit>0 && result.getByteCount()>limit) {
        return(createBitmap(inSampleSize+1, inBitmap,
          limit, normalizeOrientation));
      }
    }
    catch (OutOfMemoryError e) {
      return(createBitmap(inSampleSize+1, inBitmap,
        limit, normalizeOrientation));
    }

    try {
      if (normalizeOrientation) {
        int orientation=getOrientation();

        if (needsNormalization(orientation)) {
          result=rotateViaMatrix(result, orientation);
        }
      }
    }
    catch (IOException e) {
      AbstractCameraActivity.BUS.post(
        new CameraEngine.DeepImpactEvent(e));
    }

    return(result);
  }

  private void updateBitmap(boolean normalizeOrientation) {
    bmp=createBitmap(1, bmp, -1, normalizeOrientation); // no limit other than OOM
  }

  private boolean needsNormalization(int orientation) {
    return(orientation==8 || orientation==3 || orientation==6);
  }

  static private Bitmap rotateViaMatrix(Bitmap original, int orientation) {
    Matrix matrix=new Matrix();

    matrix.setRotate(degreesForRotation(orientation));

    return(Bitmap.createBitmap(original, 0, 0, original.getWidth(),
      original.getHeight(), matrix, true));
  }

  static private int degreesForRotation(int orientation) {
    int result;

    switch (orientation) {
      case 8:
        result=270;
        break;

      case 3:
        result=180;
        break;

      default:
        result=90;
    }

    return(result);
  }
}
