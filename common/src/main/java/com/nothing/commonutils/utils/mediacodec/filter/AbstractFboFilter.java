package com.nothing.commonutils.utils.mediacodec.filter;

import android.opengl.GLES20;
import com.nothing.commonutils.utils.mediacodec.utils.GlUtil;

public abstract class AbstractFboFilter extends AbstractFilter {
    int[] frameBuffer;
    int[] frameTextures;

    @Override // com.nothing.commonutils.utils.mediacodec.filter.AbstractFilter
    public int onDrawTexture() {
        GLES20.glBindFramebuffer(36160, this.frameBuffer[0]);
        onDraw();
        GLES20.glBindFramebuffer(36160, 0);
        return this.frameTextures[0];
    }

    public void updateFBO(int i, int i2) {
        deleteFBO();
        int[] iArr = new int[1];
        this.frameBuffer = iArr;
        this.frameTextures = new int[1];
        GLES20.glGenFramebuffers(1, iArr, 0);
        GlUtil.glGenTextures(this.frameTextures);
        GLES20.glBindTexture(3553, this.frameTextures[0]);
        loadTexture2DFilter();
        GLES20.glTexImage2D(3553, 0, 6408, i, i2, 0, 6408, 5121, null);
        GLES20.glBindFramebuffer(36160, this.frameBuffer[0]);
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.frameTextures[0], 0);
        GLES20.glBindTexture(3553, 0);
        GLES20.glBindFramebuffer(36160, 0);
    }

    public int[] createFBOTexture(int i, int i2) {
        int[] iArr = new int[1];
        GlUtil.glGenTextures(iArr);
        GLES20.glBindTexture(3553, iArr[0]);
        GLES20.glTexImage2D(3553, 0, 6408, i, i2, 0, 6408, 5121, null);
        return iArr;
    }

    public int[] createFrameBuffer() {
        int[] iArr = new int[1];
        GLES20.glGenFramebuffers(1, iArr, 0);
        return iArr;
    }

    public void bindFBO(int i, int i2) {
        GLES20.glBindFramebuffer(36160, i);
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, i2, 0);
    }

    public void unBindFBO() {
        GLES20.glBindFramebuffer(36160, 0);
        GLES20.glBindTexture(3553, 0);
    }

    @Override // com.nothing.commonutils.utils.mediacodec.filter.AbstractFilter
    public void release() {
        super.release();
        deleteFBO();
    }

    public void deleteFBO() {
        int[] iArr = this.frameTextures;
        if (iArr != null) {
            GLES20.glDeleteTextures(1, iArr, 0);
            this.frameTextures = null;
        }
        int[] iArr2 = this.frameBuffer;
        if (iArr2 != null) {
            GLES20.glDeleteFramebuffers(1, iArr2, 0);
        }
    }
}
