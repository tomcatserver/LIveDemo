package com.airbnb.lottie.model.layer;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.DrawingContent;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.MaskKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.content.Mask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

public abstract class BaseLayer implements DrawingContent, BaseKeyframeAnimation.AnimationListener {
    private static final int SAVE_FLAGS = 0;

    @Nullable
    static BaseLayer forModel(
            Layer layerModel, LottieDrawable drawable, LottieComposition composition) {
        switch (layerModel.getLayerType()) {
            case Shape:
                return new ShapeLayer(drawable, layerModel);
            case PreComp:
                return new CompositionLayer(drawable, layerModel,
                        composition.getPrecomps(layerModel.getRefId()), composition);
            case Solid:
                return new SolidLayer(drawable, layerModel);
            case Image:
                return new ImageLayer(drawable, layerModel, composition);
            case Null:
                return new NullLayer(drawable, layerModel);
            case Text:
                return new TextLayer(drawable, layerModel);
            case Unknown:
            default:
                // Do nothing
                Log.w(L.TAG, "Unknown layer type " + layerModel.getLayerType());
                return null;
        }
    }

    private final Path path = new Path();
    private final Matrix matrix = new Matrix();
    private final Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mattePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint clearPaint = new Paint();
    private final RectF rect = new RectF();
    private final RectF maskBoundsRect = new RectF();
    private final RectF matteBoundsRect = new RectF();
    private final RectF tempMaskBoundsRect = new RectF();
    private final String drawTraceName;
    final Matrix boundsMatrix = new Matrix();
    final LottieDrawable lottieDrawable;
    final Layer layerModel;
    @Nullable
    private MaskKeyframeAnimation mask;
    @Nullable
    private BaseLayer matteLayer;
    @Nullable
    private BaseLayer parentLayer;
    private List<BaseLayer> parentLayers;

    private final List<BaseKeyframeAnimation<?, ?>> animations = new ArrayList<>();
    final TransformKeyframeAnimation transform;
    private boolean visible = true;

    BaseLayer(LottieDrawable lottieDrawable, Layer layerModel) {
        this.lottieDrawable = lottieDrawable;
        this.layerModel = layerModel;
        drawTraceName = layerModel.getName() + "#draw";
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        if (layerModel.getMatteType() == Layer.MatteType.Invert) {
            mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        } else {
            mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }

        this.transform = layerModel.getTransform().createAnimation();
        transform.addListener(this);
        transform.addAnimationsToLayer(this);

        if (layerModel.getMasks() != null && !layerModel.getMasks().isEmpty()) {
            this.mask = new MaskKeyframeAnimation(layerModel.getMasks());
            for (BaseKeyframeAnimation<?, Path> animation : mask.getMaskAnimations()) {
                addAnimation(animation);
                animation.addUpdateListener(this);
            }
            for (BaseKeyframeAnimation<Integer, Integer> animation : mask.getOpacityAnimations()) {
                addAnimation(animation);
                animation.addUpdateListener(this);
            }
        }
        setupInOutAnimations();
    }

    @Override
    public void onValueChanged() {
        invalidateSelf();
    }

    Layer getLayerModel() {
        return layerModel;
    }

    void setMatteLayer(@Nullable BaseLayer matteLayer) {
        this.matteLayer = matteLayer;
    }

    boolean hasMatteOnThisLayer() {
        return matteLayer != null;
    }

    void setParentLayer(@Nullable BaseLayer parentLayer) {
        this.parentLayer = parentLayer;
    }

    private void setupInOutAnimations() {
        if (!layerModel.getInOutKeyframes().isEmpty()) {
            final FloatKeyframeAnimation inOutAnimation =
                    new FloatKeyframeAnimation(layerModel.getInOutKeyframes());
            inOutAnimation.setIsDiscrete();
            inOutAnimation.addUpdateListener(new BaseKeyframeAnimation.AnimationListener() {
                @Override
                public void onValueChanged() {
                    setVisible(inOutAnimation.getValue() == 1f);
                }
            });
            setVisible(inOutAnimation.getValue() == 1f);
            addAnimation(inOutAnimation);
        } else {
            setVisible(true);
        }
    }

    private void invalidateSelf() {
        lottieDrawable.invalidateSelf();
    }

    public void addAnimation(BaseKeyframeAnimation<?, ?> newAnimation) {
        if (!(newAnimation instanceof StaticKeyframeAnimation)) {
            animations.add(newAnimation);
        }
    }

    @CallSuper
    @Override
    public void getBounds(RectF outBounds, Matrix parentMatrix) {
        boundsMatrix.set(parentMatrix);
        boundsMatrix.preConcat(transform.getMatrix());
    }

    @SuppressLint("WrongConstant")
    @Override
    public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        L.beginSection(drawTraceName);
        if (!visible) {
            L.endSection(drawTraceName);
            return;
        }
        buildParentLayerListIfNeeded();
        L.beginSection("Layer#parentMatrix");
        matrix.reset();
        matrix.set(parentMatrix);
        for (int i = parentLayers.size() - 1; i >= 0; i--) {
            matrix.preConcat(parentLayers.get(i).transform.getMatrix());
        }
        L.endSection("Layer#parentMatrix");
        int alpha = (int)
                ((parentAlpha / 255f * (float) transform.getOpacity().getValue() / 100f) * 255);
        if (!hasMatteOnThisLayer() && !hasMasksOnThisLayer()) {
            matrix.preConcat(transform.getMatrix());
            L.beginSection("Layer#drawLayer");
            drawLayer(canvas, matrix, alpha);
            L.endSection("Layer#drawLayer");
            recordRenderTime(L.endSection(drawTraceName));
            return;
        }

        L.beginSection("Layer#computeBounds");
        rect.set(0, 0, 0, 0);
        getBounds(rect, matrix);
        intersectBoundsWithMatte(rect, matrix);

        matrix.preConcat(transform.getMatrix());
        intersectBoundsWithMask(rect, matrix);

        rect.set(0, 0, canvas.getWidth(), canvas.getHeight());
        L.endSection("Layer#computeBounds");

        L.beginSection("Layer#saveLayer");
        canvas.saveLayer(rect, contentPaint, Canvas.ALL_SAVE_FLAG);
        L.endSection("Layer#saveLayer");

        // Clear the off screen buffer. This is necessary for some phones.
        clearCanvas(canvas);
        L.beginSection("Layer#drawLayer");
        drawLayer(canvas, matrix, alpha);
        L.endSection("Layer#drawLayer");

        if (hasMasksOnThisLayer()) {
            applyMasks(canvas, matrix);
        }

        if (hasMatteOnThisLayer()) {
            L.beginSection("Layer#drawMatte");
            L.beginSection("Layer#saveLayer");
            canvas.saveLayer(rect, mattePaint, SAVE_FLAGS);
            L.endSection("Layer#saveLayer");
            clearCanvas(canvas);
            //noinspection ConstantConditions
            matteLayer.draw(canvas, parentMatrix, alpha);
            L.beginSection("Layer#restoreLayer");
            canvas.restore();
            L.endSection("Layer#restoreLayer");
            L.endSection("Layer#drawMatte");
        }

        L.beginSection("Layer#restoreLayer");
        canvas.restore();
        L.endSection("Layer#restoreLayer");
        recordRenderTime(L.endSection(drawTraceName));
    }

    private void recordRenderTime(float ms) {
        lottieDrawable.getComposition()
                .getPerformanceTracker().recordRenderTime(layerModel.getName(), ms);

    }

    private void clearCanvas(Canvas canvas) {
        L.beginSection("Layer#clearLayer");
        // If we don't pad the clear draw, some phones leave a 1px border of the graphics buffer.
        canvas.drawRect(rect.left - 1, rect.top - 1, rect.right + 1, rect.bottom + 1, clearPaint);
        L.endSection("Layer#clearLayer");
    }

    private void intersectBoundsWithMask(RectF rect, Matrix matrix) {
        maskBoundsRect.set(0, 0, 0, 0);
        if (!hasMasksOnThisLayer()) {
            return;
        }
        //noinspection ConstantConditions
        int size = mask.getMasks().size();
        for (int i = 0; i < size; i++) {
            Mask mask = this.mask.getMasks().get(i);
            BaseKeyframeAnimation<?, Path> maskAnimation = this.mask.getMaskAnimations().get(i);
            Path maskPath = maskAnimation.getValue();
            path.set(maskPath);
            path.transform(matrix);

            switch (mask.getMaskMode()) {
                case MaskModeSubtract:
                    // If there is a subtract mask, the mask could potentially be the size of the entire
                    // canvas so we can't use the mask bounds.
                    return;
                case MaskModeIntersect:
                    // TODO
                    return;
                case MaskModeUnknown:
                    return;
                case MaskModeAdd:
                default:
                    path.computeBounds(tempMaskBoundsRect, false);
                    // As we iterate through the masks, we want to calculate the union region of the masks.
                    // We initialize the rect with the first mask. If we don't call set() on the first call,
                    // the rect will always extend to (0,0).
                    if (i == 0) {
                        maskBoundsRect.set(tempMaskBoundsRect);
                    } else {
                        maskBoundsRect.set(
                                Math.min(maskBoundsRect.left, tempMaskBoundsRect.left),
                                Math.min(maskBoundsRect.top, tempMaskBoundsRect.top),
                                Math.max(maskBoundsRect.right, tempMaskBoundsRect.right),
                                Math.max(maskBoundsRect.bottom, tempMaskBoundsRect.bottom)
                        );
                    }
            }
        }

        rect.set(
                Math.max(rect.left, maskBoundsRect.left),
                Math.max(rect.top, maskBoundsRect.top),
                Math.min(rect.right, maskBoundsRect.right),
                Math.min(rect.bottom, maskBoundsRect.bottom)
        );
    }

    private void intersectBoundsWithMatte(RectF rect, Matrix matrix) {
        if (!hasMatteOnThisLayer()) {
            return;
        }
        if (layerModel.getMatteType() == Layer.MatteType.Invert) {
            // We can't trim the bounds if the mask is inverted since it extends all the way to the
            // composition bounds.
            return;
        }
        //noinspection ConstantConditions
        matteLayer.getBounds(matteBoundsRect, matrix);
        rect.set(
                Math.max(rect.left, matteBoundsRect.left),
                Math.max(rect.top, matteBoundsRect.top),
                Math.min(rect.right, matteBoundsRect.right),
                Math.min(rect.bottom, matteBoundsRect.bottom)
        );
    }

    abstract void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha);

    @SuppressLint("WrongConstant")
    private void applyMasks(Canvas canvas, Matrix matrix) {
        L.beginSection("Layer#drawMask");
        L.beginSection("Layer#saveLayer");
        canvas.saveLayer(rect, maskPaint, SAVE_FLAGS);
        L.endSection("Layer#saveLayer");
        clearCanvas(canvas);

        //noinspection ConstantConditions
        int size = mask.getMasks().size();
        for (int i = 0; i < size; i++) {
            Mask mask = this.mask.getMasks().get(i);
            BaseKeyframeAnimation<?, Path> maskAnimation = this.mask.getMaskAnimations().get(i);
            Path maskPath = maskAnimation.getValue();
            path.set(maskPath);
            path.transform(matrix);

            switch (mask.getMaskMode()) {
                case MaskModeSubtract:
                    path.setFillType(Path.FillType.INVERSE_WINDING);
                    break;
                case MaskModeAdd:
                default:
                    path.setFillType(Path.FillType.WINDING);
            }
            BaseKeyframeAnimation<Integer, Integer> opacityAnimation =
                    this.mask.getOpacityAnimations().get(i);
            int alpha = contentPaint.getAlpha();
            contentPaint.setAlpha((int) (opacityAnimation.getValue() * 2.55f));
            canvas.drawPath(path, contentPaint);
            contentPaint.setAlpha(alpha);
        }
        L.beginSection("Layer#restoreLayer");
        canvas.restore();
        L.endSection("Layer#restoreLayer");
        L.endSection("Layer#drawMask");
    }

    boolean hasMasksOnThisLayer() {
        return mask != null && !mask.getMaskAnimations().isEmpty();
    }

    private void setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            invalidateSelf();
        }
    }

    void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (layerModel.getTimeStretch() != 0) {
            progress /= layerModel.getTimeStretch();
        }
        if (matteLayer != null) {
            // The matte layer's time stretch is pre-calculated.
            float matteTimeStretch = matteLayer.layerModel.getTimeStretch();
            matteLayer.setProgress(progress * matteTimeStretch);
        }
        for (int i = 0; i < animations.size(); i++) {
            animations.get(i).setProgress(progress);
        }
    }

    private void buildParentLayerListIfNeeded() {
        if (parentLayers != null) {
            return;
        }
        if (parentLayer == null) {
            parentLayers = Collections.emptyList();
            return;
        }

        parentLayers = new ArrayList<>();
        BaseLayer layer = parentLayer;
        while (layer != null) {
            parentLayers.add(layer);
            layer = layer.parentLayer;
        }
    }

    @Override
    public String getName() {
        return layerModel.getName();
    }

    @Override
    public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
        // Do nothing
    }

    @Override
    public void addColorFilter(@Nullable String layerName, @Nullable String contentName,
                               @Nullable ColorFilter colorFilter) {
        // Do nothing
    }
}
