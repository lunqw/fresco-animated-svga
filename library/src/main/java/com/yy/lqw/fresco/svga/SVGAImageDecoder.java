package com.yy.lqw.fresco.svga;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.facebook.common.logging.FLog;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.animated.base.AnimatedImageResult;
import com.facebook.imagepipeline.animated.factory.AnimatedImageFactory;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.decoder.ImageDecoder;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.platform.PlatformDecoder;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by lunqingwen on 2016/8/30.
 */
public class SVGAImageDecoder extends ImageDecoder {
    private static final Class<?> TAG = SVGAImageDecoder.class;
    private static final String SVGA_IMAGE_FILE_EXTENSION = ".png";
    private static final String SVGA_DESCRIPTOR_FILE_NAME = "movie.spec";
    private static final Gson sGson = new Gson();

    public SVGAImageDecoder(AnimatedImageFactory animatedImageFactory,
                            PlatformDecoder platformDecoder,
                            Bitmap.Config bitmapConfig) {
        super(animatedImageFactory, platformDecoder, bitmapConfig);
    }

    @Override
    public CloseableImage decodeImage(EncodedImage encodedImage,
                                      int length,
                                      QualityInfo qualityInfo,
                                      ImageDecodeOptions options) {
        // Fresco默认不能处理的文件格式由这里进一步处理
        if (encodedImage.getImageFormat() == ImageFormat.UNKNOWN) {
            try {
                final SVGAImage image = decodeSVGAImage(encodedImage);
                if (image != null) {
                    final AnimatedImageResult result = AnimatedImageResult.newBuilder(image)
                            .build();
                    return new CloseableAnimatedImage(result);
                } else {
                    FLog.e(TAG, "Decode error: file was not SVGA format ?");
                    return super.decodeImage(encodedImage, length, qualityInfo, options);
                }
            } catch (Exception e) {
                FLog.w(TAG, "Decode error: %s", e.getMessage());
                return super.decodeImage(encodedImage, length, qualityInfo, options);
            }

        } else {
            return super.decodeImage(encodedImage, length, qualityInfo, options);
        }
    }

    /**
     * Decode SVGA image
     *
     * @param encodedImage
     * @return
     * @throws IOException
     */
    private SVGAImage decodeSVGAImage(EncodedImage encodedImage) throws IOException {
        SVGADescriptor descriptor = null;
        SVGAImage svgaImage = null;
        ZipEntry ze;
        String name;

        // 分两次读取zip文件，确认是SVGA才decode各个png文件
        ZipInputStream zin = new ZipInputStream(encodedImage.getInputStream());
        try {
            while ((ze = zin.getNextEntry()) != null) {
                name = ze.getName();
                if (name.equals(SVGA_DESCRIPTOR_FILE_NAME)) {
                    descriptor = decodeSVGADescriptor(new InputStreamReader(zin));
                    break;
                }
            }
        } finally {
            zin.close();
        }

        if (descriptor != null && descriptor.images != null) {
            zin = new ZipInputStream(encodedImage.getInputStream());
            try {
                while ((ze = zin.getNextEntry()) != null) {
                    name = ze.getName();
                    if (name.endsWith(SVGA_IMAGE_FILE_EXTENSION)) {
                        final String key = name.substring(0, name.indexOf('.'));
                        if (descriptor.images.containsKey(key)) {
                            final Bitmap bitmap = BitmapFactory.decodeStream(zin);
                            descriptor.cache.put(key, bitmap);
                        }
                    }
                }
            } finally {
                zin.close();
            }
            svgaImage = new SVGAImage(descriptor);
        }
        return svgaImage;
    }

    /**
     * Decode SVGA descriptor
     *
     * @param reader
     * @return
     */
    private SVGADescriptor decodeSVGADescriptor(Reader reader) {
        return reader != null ? sGson.fromJson(reader, SVGADescriptor.class) : null;
    }
}
