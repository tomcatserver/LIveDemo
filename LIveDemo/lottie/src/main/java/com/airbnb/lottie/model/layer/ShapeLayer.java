package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.RectF;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.model.content.ShapeGroup;

import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ShapeLayer extends BaseLayer {
  private final ContentGroup contentGroup;

  ShapeLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);

    ShapeGroup shapeGroup = new ShapeGroup(layerModel.getName(), layerModel.getShapes());
    contentGroup = new ContentGroup(lottieDrawable, this, shapeGroup);
    contentGroup.setContents(Collections.<Content>emptyList(), Collections.<Content>emptyList());
  }

  @Override void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    contentGroup.draw(canvas, parentMatrix, parentAlpha);
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    super.getBounds(outBounds, parentMatrix);
    contentGroup.getBounds(outBounds, boundsMatrix);
  }

  @Override public void addColorFilter(@Nullable String layerName, @Nullable String contentName,
                                       @Nullable ColorFilter colorFilter) {
    contentGroup.addColorFilter(layerName, contentName, colorFilter);
  }
}
