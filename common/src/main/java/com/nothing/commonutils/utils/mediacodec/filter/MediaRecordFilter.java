package com.nothing.commonutils.utils.mediacodec.filter;

import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class MediaRecordFilter extends AbstractFilter {
    private int sTexture;
    private int textureId;

    @Override // com.nothing.commonutils.utils.mediacodec.filter.AbstractFilter
    public void initOpenGl() {
        initOpenGl("attribute vec4 av_Position;\nattribute vec2 af_Position;\nvarying vec2 v_texPosition;\nuniform mat4 mvpMatrix;\n\nvoid main() {\n    v_texPosition = af_Position;\n    gl_Position = mvpMatrix * av_Position;\n}\n", "precision mediump float;\nvarying vec2 v_texPosition;\n\nuniform sampler2D  sTexture;\nvoid main() {\n    gl_FragColor = texture2D(sTexture, v_texPosition);\n}\n");
    }

    @Override // com.nothing.commonutils.utils.mediacodec.filter.AbstractFilter
    public void initGL(String str, String str2) {
        super.initGL(str, str2);
        this.sTexture = GLES20.glGetUniformLocation(this.program, "sTexture");
        this.mvpMatrixLoc = GLES20.glGetUniformLocation(this.program, "mvpMatrix");
        Matrix.setIdentityM(this.modelMatrix, 0);
    }

    public void updateRect(Rect rect) {
        Matrix.setIdentityM(this.modelMatrix, 0);
        Matrix.translateM(this.modelMatrix, 0, -((rect.left * 2.0f) / this.videoWidth), -((((this.videoHeight - rect.height()) - rect.top) * 2.0f) / this.videoHeight), 0.0f);
    }

    public int onDrawTexture(int i) {
        this.textureId = i;
        return onDrawTexture();
    }

    @Override // com.nothing.commonutils.utils.mediacodec.filter.AbstractFilter
    public int onDraw() {
        GLES20.glViewport(0, 0, this.videoWidth, this.videoHeight);
        glUsrProgram();
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, this.textureId);
        GLES20.glUniform1i(this.sTexture, 0);
        GLES20.glUniformMatrix4fv(this.mvpMatrixLoc, 1, false, this.modelMatrix, 0);
        GLES20.glDrawArrays(5, 0, 4);
        GLES20.glBindTexture(3553, 0);
        return this.textureId;
    }
}
