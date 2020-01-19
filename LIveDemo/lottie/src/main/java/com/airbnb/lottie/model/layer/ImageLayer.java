package com.airbnb.lottie.model.layer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;

import java.net.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ImageLayer extends BaseLayer {

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
  private final Rect src = new Rect();
  private final Rect dst = new Rect();
  private final float density;

  private LottieComposition composition;

  ImageLayer(LottieDrawable lottieDrawable, Layer layerModel, LottieComposition composition) {
    super(lottieDrawable, layerModel);
    this.composition = composition;
    this.density = composition.getDpScale();
  }

  @Override public void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    Bitmap bitmap = getBitmap();
    if (bitmap == null) {
      return;
    }
    paint.setAlpha(parentAlpha);
    canvas.save();
    canvas.concat(parentMatrix);
    src.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    dst.set(0, 0, (int) (bitmap.getWidth() * density), (int) (bitmap.getHeight() * density));
    canvas.drawBitmap(bitmap, src, dst , paint);
    canvas.restore();
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    super.getBounds(outBounds, parentMatrix);
    Bitmap bitmap = getBitmap();
    if (bitmap != null) {
      outBounds.set(
          outBounds.left,
          outBounds.top,
          Math.min(outBounds.right, bitmap.getWidth()),
          Math.min(outBounds.bottom, bitmap.getHeight())
      );
      boundsMatrix.mapRect(outBounds);
    }

  }

  @Nullable
  private Bitmap getBitmap() {
    String refId = layerModel.getRefId();
    if(composition.getImages().get(refId).getFileName().startsWith("http")){
//      System.out.println("输出lottie----"+refId+"----"+composition.getImages().get(refId).getFileName());
      getNetWorkBitmap(refId);
      return lottieDrawable.getBitmapCatch().get(refId);
    }
    return lottieDrawable.getImageAsset(refId);
  }

  private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
      final int heightRatio = Math.round((float) height / (float) reqHeight);
      final int widthRatio = Math.round((float) width / (float) reqWidth);
      inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
    }
    return inSampleSize;
  }

  private synchronized void getNetWorkBitmap(final String id){
    if(lottieDrawable.getBitmapCatch().containsKey(id)){
      return;
    }
    lottieDrawable.threadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          URL url = new URL(composition.getImages().get(id).getFileName());
          BitmapFactory.Options options = new BitmapFactory.Options();
          options.inJustDecodeBounds = true;
          BitmapFactory.decodeStream(url.openStream(),null,options);
          options.inPreferredConfig = Bitmap.Config.RGB_565;
          options.inSampleSize = calculateInSampleSize(options, 1000, 1000);
          options.inJustDecodeBounds = false;
          lottieDrawable.setBitmapCatch(id,BitmapFactory.decodeStream(url.openStream(),null,options));
          url.openStream().close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override public void addColorFilter(@Nullable String layerName, @Nullable String contentName,
      @Nullable ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
  }
}
