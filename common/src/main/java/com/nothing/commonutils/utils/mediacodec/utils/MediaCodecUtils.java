package com.nothing.commonutils.utils.mediacodec.utils;

import android.graphics.Point;
import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import com.nothing.commonutils.utils.Lg;
import com.nothing.commonutils.utils.mediacodec.bean.CodecParameter;
import com.nothing.commonutils.utils.mediacodec.entity.H264Header;
import com.nothing.commonutils.utils.mediacodec.interfaces.OnScreenShareCallback;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME;

public class MediaCodecUtils {
    private static final String TAG = "MediaCodecUtils";
    private static final int REPEAT_INTERVAL = 1000000;
    private static final int TIMEOUT_US = 1000000;
    protected MediaCodec mEncoder;
    private H264Header mH264Header;
    private int mKeyFrameCounts;
    protected OnScreenShareCallback mOnScreenCallBack;
    private Surface mSurface;
    private int mTotalFrameCounts;
    private final CodecParameter parameter;
    private byte[] sps = null;
    private byte[] pps = null;
    protected AtomicBoolean mReset = new AtomicBoolean(false);
    protected MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private final boolean mAddHeader = true;

    public MediaCodecUtils(CodecParameter codecParameter) {
        this.parameter = codecParameter;
    }

    public void calcBitrate(int i) {
        MediaCodec mediaCodec = this.mEncoder;
        if (mediaCodec != null) {
            BitrateController.setBitrateOnFly(mediaCodec, i);
        }
    }

    public boolean prepareEncode() throws Exception {
        MediaFormat createVideoFormat = MediaFormat.createVideoFormat("video/avc", this.parameter.getMediaCodecWidth(), this.parameter.getMediaCodecHeight());
        createVideoFormat.setInteger("bitrate", this.parameter.getBitrate());
        createVideoFormat.setInteger("frame-rate", this.parameter.getFrameRate());
        createVideoFormat.setInteger("i-frame-interval", this.parameter.getIframeInterval());
        createVideoFormat.setInteger("color-format", 2130708361);
        createVideoFormat.setInteger("repeat-previous-frame-after", 1000000);
        createVideoFormat.setInteger("profile", 1);
        createVideoFormat.setInteger("level", 64);
        prepareStartEncoder(createVideoFormat);
        return true;
    }

    protected void prepareStartEncoder(MediaFormat mediaFormat) throws Exception {
        if (this.mEncoder != null) {
            try {
                Lg.i(TAG, "prepareEncoder not null");
                this.mEncoder.stop();
                this.mEncoder.release();
                this.mEncoder = null;
            } catch (Exception e) {
                this.mEncoder = null;
                String str = TAG;
                Lg.e(str, "prepareStartEncoder exception " + e.getMessage());
            }
        }
        MediaCodec createEncoderByType = MediaCodec.createEncoderByType("video/avc");
        this.mEncoder = createEncoderByType;
        createEncoderByType.configure(mediaFormat, (Surface) null, (MediaCrypto) null, 1);
        this.mSurface = this.mEncoder.createInputSurface();
        this.mEncoder.start();
        Lg.i(TAG, "Encoder start");
    }

    public Surface getSurface() {
        return this.mSurface;
    }

    public void encodeFrame() {
        try {
            int dequeueOutputBuffer = this.mEncoder.dequeueOutputBuffer(this.mBufferInfo, 1000000L);
            if (dequeueOutputBuffer == -2) {
                resetOutputFormat();
            } else if (dequeueOutputBuffer == -1) {
                Lg.i(TAG, "[mediaEncoder] retrieving buffers time out!");
            } else if (dequeueOutputBuffer >= 0) {
                if (Build.VERSION.SDK_INT >= 21) {
                    encodeToVideoTrack(this.mEncoder.getOutputBuffer(dequeueOutputBuffer));
                } else {
                    encodeToVideoTrack(this.mEncoder.getOutputBuffers()[dequeueOutputBuffer]);
                }
                this.mEncoder.releaseOutputBuffer(dequeueOutputBuffer, false);
            }
        } catch (Exception e) {
            String str = TAG;
            Lg.e(str, "encodeFrame exception : " + e.getMessage());
        }
    }

    protected void resetOutputFormat() {
        this.mReset.set(true);
        MediaFormat outputFormat = this.mEncoder.getOutputFormat();
        getSpsPpsByteBuffer(outputFormat);
        String str = TAG;
        Lg.i(str, "MediaProjection output format changed. new format: " + outputFormat.toString());
    }

    private void encodeToVideoTrack(ByteBuffer byteBuffer) {
        byte[] bArr;
        if ((this.mBufferInfo.flags & 2) != 0) {
            Lg.d(TAG, "[mediaEncoder] ignoring BUFFER_FLAG_CODEC_CONFIG");
            this.mBufferInfo.size = 0;
        }
        if (this.mBufferInfo.size == 0) {
            Lg.d(TAG, "[mediaEncoder] info.size == 0, drop it.");
            byteBuffer = null;
        }
        if (byteBuffer != null) {
             this.mTotalFrameCounts++;
            int mediaCodecWidth = this.parameter.getMediaCodecWidth();
            int mediaCodecHeight = this.parameter.getMediaCodecHeight();
            if (this.mH264Header == null) {
                this.mH264Header = new H264Header(new Point(mediaCodecWidth, mediaCodecHeight));
            }
            byteBuffer.position(this.mBufferInfo.offset);
            byteBuffer.limit(this.mBufferInfo.offset + this.mBufferInfo.size);
            int i = this.mAddHeader ? 20 : 0;
            boolean isKeyFrame = (this.mBufferInfo.flags & BUFFER_FLAG_KEY_FRAME) != 0;
            if (isKeyFrame) {
                this.mKeyFrameCounts++;
                int i2 = this.mBufferInfo.size;
                byte[] bArr2 = this.sps;
                bArr = new byte[i2 + bArr2.length + this.pps.length + i];
                if (this.mAddHeader) {
                    this.mH264Header.setData(mediaCodecWidth, mediaCodecHeight, 32, 1);
                    System.arraycopy(this.mH264Header.getData(), 0, bArr, 0, 20);
                    byte[] bArr3 = this.sps;
                    System.arraycopy(bArr3, 0, bArr, 20, bArr3.length);
                    byte[] bArr4 = this.pps;
                    System.arraycopy(bArr4, 0, bArr, this.sps.length + 20, bArr4.length);
                    byteBuffer.get(bArr, this.sps.length + 20 + this.pps.length, this.mBufferInfo.size);
                } else {
                    System.arraycopy(bArr2, 0, bArr, 0, bArr2.length);
                    byte[] bArr5 = this.pps;
                    System.arraycopy(bArr5, 0, bArr, this.sps.length, bArr5.length);
                    byteBuffer.get(bArr, this.sps.length + this.pps.length, this.mBufferInfo.size);
                }
                OnScreenShareCallback onScreenShareCallback = this.mOnScreenCallBack;
                if (onScreenShareCallback != null) {
                    onScreenShareCallback.onClearNeeded(this.mTotalFrameCounts, this.mKeyFrameCounts);
                }
            } else {
                bArr = new byte[this.mBufferInfo.size + i];
                if (this.mAddHeader) {
                    this.mH264Header.setData(mediaCodecWidth, mediaCodecHeight, 32, 3);
                    System.arraycopy(this.mH264Header.getData(), 0, bArr, 0, this.mH264Header.getDataLength());
                    byteBuffer.get(bArr, 20, this.mBufferInfo.size);
                } else {
                    byteBuffer.get(bArr, 0, this.mBufferInfo.size);
                }
             }
            OnScreenShareCallback onScreenShareCallback2 = this.mOnScreenCallBack;
            if (onScreenShareCallback2 != null) {
                onScreenShareCallback2.onH264Info(bArr, isKeyFrame);
            }
        }
    }

    public void setOnScreenShareCallBack(OnScreenShareCallback onScreenShareCallback) {
        this.mOnScreenCallBack = onScreenShareCallback;
    }

    private void getSpsPpsByteBuffer(MediaFormat mediaFormat) {
        this.sps = mediaFormat.getByteBuffer("csd-0").array();
        this.pps = mediaFormat.getByteBuffer("csd-1").array();
        String str = TAG;
        Lg.i(str, "[mediaEncoder] sps size:" + this.sps.length + ", pps size:" + this.pps.length);
    }

    public void release() {
        try {
            MediaCodec mediaCodec = this.mEncoder;
            if (mediaCodec != null) {
                mediaCodec.stop();
                this.mEncoder.release();
                this.mEncoder = null;
            }
            Surface surface = this.mSurface;
            if (surface != null) {
                surface.release();
                this.mSurface = null;
            }
        } catch (Exception e) {
            String str = TAG;
            Lg.i(str, "screen capture release. exception: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
