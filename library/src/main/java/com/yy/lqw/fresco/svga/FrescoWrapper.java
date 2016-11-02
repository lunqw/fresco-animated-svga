package com.yy.lqw.fresco.svga;

import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.animated.factory.AnimatedImageFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.platform.PlatformDecoder;

/**
 * Created by lunqingwen on 2016/10/14.
 */

public class FrescoWrapper {
    public static void initialize(Context context) {
        ImagePipelineFactory.initialize(context);
        ImagePipelineConfig.Builder builder = ImagePipelineConfig.newBuilder(context);
        AnimatedImageFactory factory = ImagePipelineFactory.getInstance()
                .getAnimatedFactory()
                .getAnimatedImageFactory();
        PlatformDecoder platformDecoder = ImagePipelineFactory.getInstance()
                .getPlatformDecoder();
        SVGAImageDecoder decoder = new SVGAImageDecoder(factory, platformDecoder,
                Bitmap.Config.ARGB_8888);
        builder.setImageDecoder(decoder);
        Fresco.initialize(context, builder.build());
    }
}
