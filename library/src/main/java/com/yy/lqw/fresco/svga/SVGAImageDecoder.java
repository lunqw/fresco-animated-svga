package com.yy.lqw.fresco.svga;

import android.content.Context;
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

import org.nustaq.serialization.FSTConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by lunqingwen on 2016/8/30.
 */
public class SVGAImageDecoder extends ImageDecoder {
    private static final Class<?> TAG = SVGAImageDecoder.class;
    private static final String IMAGE_FILE_EXTENSION = ".png";
    private static final String DESCRIPTOR_NAME = "movie.spec";
    private static final String CACHE_DIR_NAME = "svga";
    private static final String FMT_CACHE_NAME = "%x_%x_%x.fst";
    private static final String TMP_NAME_PREFIX = "cache_";
    private static final String TMP_NAME_SUFFIX = ".tmp";
    private static final Gson sGson = new Gson();
    private static final FSTConfiguration sFst = FSTConfiguration.createAndroidDefaultConfiguration();
    private final File mSVGACacheDirectory;

    public SVGAImageDecoder(Context context,
                            AnimatedImageFactory animatedImageFactory,
                            PlatformDecoder platformDecoder,
                            Bitmap.Config bitmapConfig) {
        super(animatedImageFactory, platformDecoder, bitmapConfig);
        mSVGACacheDirectory = new File(context.getCacheDir(), CACHE_DIR_NAME);
        if (!mSVGACacheDirectory.exists()) {
            mSVGACacheDirectory.mkdirs();
        }
    }

    @Override
    public CloseableImage decodeImage(EncodedImage encodedImage,
                                      int length,
                                      QualityInfo qualityInfo,
                                      ImageDecodeOptions options) {
        // Fresco默认不能处理的文件格式由这里进一步处理
        if (encodedImage.getImageFormat() == ImageFormat.UNKNOWN) {
            try {
                final long begin = System.currentTimeMillis();
                final SVGAImage image = decodeSVGAImage(encodedImage);
                final long end = System.currentTimeMillis();
                FLog.d(TAG, "SVGA image decoded, bein: %d, end: %d, diff: %d",
                        begin, end, end - begin);

                if (image != null) {
                    final AnimatedImageResult result = AnimatedImageResult.newBuilder(image)
                            .build();
                    return new CloseableAnimatedImage(result);
                } else {
                    FLog.e(TAG, "Decode error: file was not SVGA format ?");
                }
            } catch (Exception e) {
                FLog.e(TAG, e, "Decode error: %s");
            }
        }
        return super.decodeImage(encodedImage, length, qualityInfo, options);
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
        InputStream cacheIn = null;

        // 分两次读取zip文件，确认是SVGA才decode各个png文件
        ZipInputStream zin = new ZipInputStream(encodedImage.getInputStream());
        try {
            while ((ze = zin.getNextEntry()) != null
                    && !ze.getName().equals(DESCRIPTOR_NAME));

            if (ze != null) {
                File cacheFile = getCacheFile(ze.getSize(), ze.getTime(), ze.getCrc());
                if (cacheFile.exists()) {
                    cacheIn = new FileInputStream(cacheFile);
                    descriptor = decodeSVGADescriptorFromCache(cacheIn);
                }

                if (descriptor == null) {
                    descriptor = decodeSVGADescriptorFromJson(new InputStreamReader(zin));
                    saveSVGADescriptor(descriptor, cacheFile);
                }
            }
        } finally {
            zin.close();
            if (cacheIn != null) {
                cacheIn.close();
            }
        }

        if (descriptor != null && descriptor.images != null) {
            zin = new ZipInputStream(encodedImage.getInputStream());
            try {
                while ((ze = zin.getNextEntry()) != null) {
                    final String name = ze.getName();
                    if (ze.getName().endsWith(IMAGE_FILE_EXTENSION)) {
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
     * Decode descriptor from cache
     *
     * @param in
     * @return success return not null otherwise return null
     */
    private SVGADescriptor decodeSVGADescriptorFromCache(InputStream in) {
        SVGADescriptor descriptor = null;
        try {
            descriptor = (SVGADescriptor) sFst.decodeFromStream(in);
            if (descriptor.cache == null) {
                descriptor.cache = new HashMap<>();
            }
        } catch (Exception e) {
            FLog.e(TAG, e, "Decode descriptor from cache error");
        }
        return descriptor;
    }

    /**
     * Decode descriptor from json
     *
     * @param reader
     * @return success return not null otherwise return null
     */
    private SVGADescriptor decodeSVGADescriptorFromJson(Reader reader) {
        SVGADescriptor descriptor = null;
        try {
            descriptor = sGson.fromJson(reader, SVGADescriptor.class);
            if (descriptor.cache == null) {
                descriptor.cache = new HashMap<>();
            }
        } catch (Exception e) {
            FLog.e(TAG, e, "Decode descriptor from json error");
        }
        return descriptor;
    }

    /**
     * Cache descriptor in order to support fast serialization
     *
     * @param descriptor SVGA descriptor object
     * @param cacheFile  cache file name
     */
    private void saveSVGADescriptor(SVGADescriptor descriptor, File cacheFile) {
        if (descriptor != null) {
            FileOutputStream out = null;
            try {
                File tmpFile = File.createTempFile(TMP_NAME_PREFIX,
                        TMP_NAME_SUFFIX, mSVGACacheDirectory);
                out = new FileOutputStream(tmpFile);
                sFst.encodeToStream(out, descriptor);
                tmpFile.renameTo(cacheFile);
            } catch (IOException e) {
                FLog.e(TAG, e, "Cache descriptor error");
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception e) {
                    }
                }
            }

        }
    }

    /**
     * Get descriptor cache file by size, time, crc
     *
     * @param size descriptor file size
     * @param time descriptor file time
     * @param crc  descriptor file crc
     * @return
     */
    private File getCacheFile(long size, long time, long crc) {
        String name = String.format(FMT_CACHE_NAME, size, time, crc);
        return new File(mSVGACacheDirectory, name);
    }
}
