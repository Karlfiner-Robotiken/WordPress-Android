package org.wordpress.android.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodecInfo;
import android.os.Build;

import org.m4m.AudioFormat;
import org.m4m.MediaComposer;
import org.m4m.MediaFileInfo;
import org.m4m.Uri;
import org.m4m.VideoFormat;
import org.m4m.android.AndroidMediaObjectFactory;
import org.m4m.android.AudioFormatAndroid;
import org.m4m.android.VideoFormatAndroid;

import java.io.IOException;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class WPVideoUtils {

    // Default parameters for the video encoder
    private static final String VIDEO_MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 30;               // 30fps
    private static final int IFRAME_INTERVAL = 2;           // 2 seconds between I-frames

    private static final int OUTPUT_WIDTH = 1280;
    private static final int OUTPUT_HEIGHT = 720;
    private static final int OUTPUT_BITRATE_KB = 2000;

    // Default parameters for the audio encoder
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final int AUDIO_OUTPUT_BIT_RATE = 96 * 1024;

    public static MediaComposer getVideoOptimizationComposer(Context ctx, String inputFile, String outFile, org.m4m.IProgressListener listener) {
        if (ctx == null || inputFile == null || listener == null) {
            AppLog.e(AppLog.T.MEDIA, "Input parameters must be not null!!");
            return null;
        }

        AndroidMediaObjectFactory factory = new AndroidMediaObjectFactory(ctx);

        Uri m4mUri = new Uri(inputFile);
        MediaFileInfo mediaFileInfo = new MediaFileInfo(factory);
        try {
            mediaFileInfo.setUri(m4mUri);
        } catch (IOException e) {
            AppLog.e(AppLog.T.MEDIA, "Cannot access the input file at " + inputFile, e);
            return null;
        }

        // Check the video resolution
        VideoFormat videoFormat = (VideoFormat) mediaFileInfo.getVideoFormat();
        if (videoFormat == null) {
            AppLog.w(AppLog.T.MEDIA, "Input file doesn't contain a video track?");
            return null;
        }
        if (videoFormat.getVideoFrameSize().width() < OUTPUT_WIDTH ||
                videoFormat.getVideoFrameSize().height() < OUTPUT_HEIGHT) {
            AppLog.w(AppLog.T.MEDIA, "Input file resolution is lower than than 1280x720. Keeping the original file");
            return null;
        }

        AudioFormat audioFormat = (AudioFormat) mediaFileInfo.getAudioFormat();
        boolean isAudioAvailable = audioFormat != null;

        MediaComposer mediaComposer = new MediaComposer(factory, listener);
        try{
            mediaComposer.addSourceFile(inputFile);
        } catch (IOException e) {
            AppLog.e(AppLog.T.MEDIA, "Cannot access the input file at " + inputFile, e);
            return null;
        }

        try {
            mediaComposer.setTargetFile(outFile, mediaFileInfo.getRotation());
        } catch (IOException e) {
            AppLog.e(AppLog.T.MEDIA, "Cannot access/write the output file at " + outFile, e);
            return null;
        }

        configureVideoEncoderWithDefaults(mediaComposer);

        if (isAudioAvailable) {
            configureAudioEncoder(mediaComposer, audioFormat);
        }

        return mediaComposer;
    }

    private static void configureVideoEncoderWithDefaults(MediaComposer mediaComposer) {
        VideoFormatAndroid videoFormat = new VideoFormatAndroid(VIDEO_MIME_TYPE, OUTPUT_WIDTH, OUTPUT_HEIGHT);
        videoFormat.setVideoBitRateInKBytes(OUTPUT_BITRATE_KB);
        videoFormat.setVideoFrameRate(FRAME_RATE);
        videoFormat.setVideoIFrameInterval(IFRAME_INTERVAL);
        mediaComposer.setTargetVideoFormat(videoFormat);
    }

    private static void configureAudioEncoder(org.m4m.MediaComposer mediaComposer, AudioFormat audioFormat) {
        /**
         * TODO: Audio resampling is unsupported by current m4m release
         * Output sample rate and channel count are the same as for input.
         */
        AudioFormatAndroid aFormat = new AudioFormatAndroid(AUDIO_MIME_TYPE, audioFormat.getAudioSampleRateInHz(), audioFormat.getAudioChannelCount());

        aFormat.setAudioBitrateInBytes(AUDIO_OUTPUT_BIT_RATE);
        aFormat.setAudioProfile(MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        mediaComposer.setTargetAudioFormat(aFormat);
    }
}
