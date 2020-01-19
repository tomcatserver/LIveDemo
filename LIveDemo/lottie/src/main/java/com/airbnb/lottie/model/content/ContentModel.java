package com.airbnb.lottie.model.content;


import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.model.layer.BaseLayer;

import androidx.annotation.Nullable;

public interface ContentModel {
  @Nullable
  Content toContent(LottieDrawable drawable, BaseLayer layer);
}
