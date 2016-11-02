package com.yy.lqw.fresco.svga;

import android.support.annotation.NonNull;

import com.facebook.imagepipeline.animated.base.AnimatedDrawableFrameInfo;
import com.facebook.imagepipeline.animated.base.AnimatedImage;

import java.util.Arrays;

/**
 * Created by lunqingwen on 2016/8/31.
 */
class SVGAImage implements AnimatedImage {
    private final SVGADescriptor mDescriptor;

    public SVGAImage(@NonNull SVGADescriptor descriptor) {
        mDescriptor = descriptor;
    }

    @Override
    public void dispose() {

    }

    @Override
    public int getWidth() {
        return mDescriptor.movie.viewBox.width;
    }

    @Override
    public int getHeight() {
        return mDescriptor.movie.viewBox.height;
    }

    @Override
    public int getFrameCount() {
        return mDescriptor.movie.frames;
    }

    @Override
    public int getDuration() {
        final float frames = (float) mDescriptor.movie.frames;
        final float fps = (float) mDescriptor.movie.fps;
        return (int) (frames / fps * 1000);
    }

    @Override
    public int[] getFrameDurations() {
        int[] durations = new int[mDescriptor.movie.frames];
        Arrays.fill(durations, 1000 / mDescriptor.movie.fps);
        return durations;
    }

    @Override
    public int getLoopCount() {
        return LOOP_COUNT_INFINITE;
    }

    @Override
    public SVGAFrame getFrame(int frameNumber) {
        return new SVGAFrame(frameNumber, mDescriptor);
    }

    @Override
    public boolean doesRenderSupportScaling() {
        return false;
    }

    @Override
    public int getSizeInBytes() {
        return 0;
    }

    @Override
    public AnimatedDrawableFrameInfo getFrameInfo(int frameNumber) {
        SVGAFrame frame = getFrame(frameNumber);
        try {
            return new AnimatedDrawableFrameInfo(
                    frameNumber,
                    frame.getXOffset(),
                    frame.getYOffset(),
                    frame.getWidth(),
                    frame.getHeight(),
                    AnimatedDrawableFrameInfo.BlendOperation.NO_BLEND,
                    AnimatedDrawableFrameInfo.DisposalMethod.DISPOSE_DO_NOT);
        } finally {
            frame.dispose();
        }
    }
}
