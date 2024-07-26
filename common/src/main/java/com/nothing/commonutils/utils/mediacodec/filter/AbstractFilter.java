package com.nothing.commonutils.utils.mediacodec.filter;

import android.opengl.GLES20;
import android.opengl.Matrix;
import com.nothing.commonutils.utils.mediacodec.utils.GlUtil;
import com.nothing.commonutils.utils.mediacodec.utils.SharderUtil;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class AbstractFilter {
    protected static final String BASE_MEDIA_CODEC_FRAG = "precision mediump float;\nvarying vec2 v_texPosition;\n\nuniform sampler2D  sTexture;\nvoid main() {\n    gl_FragColor = texture2D(sTexture, v_texPosition);\n}\n";
    protected static final String BASE_REVERT_FRAG = "attribute vec4 av_Position;\nattribute vec2 af_Position;\nvarying vec2 v_texPosition;\nuniform mat4 mvpMatrix;\n\nvoid main() {\n    v_texPosition = vec2(af_Position.x,1.0 - af_Position.y);\n    gl_Position = mvpMatrix * av_Position;\n}";
    protected static final String BASE_VERTEX = "attribute vec4 av_Position;\nattribute vec2 af_Position;\nvarying vec2 v_texPosition;\nuniform mat4 mvpMatrix;\n\nvoid main() {\n    v_texPosition = af_Position;\n    gl_Position = mvpMatrix * av_Position;\n}\n";
    int afPosition;
    int avPosition;
    float[] modelMatrix = new float[16];
    int mvpMatrixLoc;
    int program;
    FloatBuffer textureBuffer;
    FloatBuffer vertexBuffer;
    int videoHeight;
    int videoWidth;

    public abstract void initOpenGl();

    public abstract int onDraw();

    public void initOpenGl(String str, String str2) {
        FloatBuffer put = ByteBuffer.allocateDirect(GlUtil.VERTEX.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(GlUtil.VERTEX);
        this.vertexBuffer = put;
        put.position(0);
        FloatBuffer put2 = ByteBuffer.allocateDirect(GlUtil.TEXURE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(GlUtil.TEXURE);
        this.textureBuffer = put2;
        put2.position(0);
        initGL(str, str2);
    }

    public void initGL(String str, String str2) {
        int createProgram = SharderUtil.createProgram(str, str2);
        this.program = createProgram;
        this.avPosition = GLES20.glGetAttribLocation(createProgram, "av_Position");
        this.afPosition = GLES20.glGetAttribLocation(this.program, "af_Position");
    }

    public void updateScreen(int i, int i2) {
        this.videoWidth = i;
        this.videoHeight = i2;
    }

    public void updateViewport() {
        GLES20.glViewport(0, 0, this.videoWidth, this.videoHeight);
    }

    public int onDrawTexture() {
        return onDraw();
    }

    public void glUsrProgram() {
        GLES20.glUseProgram(this.program);
        GLES20.glEnableVertexAttribArray(this.avPosition);
        GLES20.glVertexAttribPointer(this.avPosition, 2, 5126, false, 8, (Buffer) this.vertexBuffer);
        GLES20.glEnableVertexAttribArray(this.afPosition);
        GLES20.glVertexAttribPointer(this.afPosition, 2, 5126, false, 8, (Buffer) this.textureBuffer);
    }

    public void loadTextureOesFilter() {
        GLES20.glTexParameteri(36197, 10242, 10497);
        GLES20.glTexParameteri(36197, 10243, 10497);
        GLES20.glTexParameteri(36197, 10241, 9729);
        GLES20.glTexParameteri(36197, 10240, 9729);
    }

    public void loadTexture2DFilter() {
        GLES20.glTexParameteri(3553, 10242, 10497);
        GLES20.glTexParameteri(3553, 10243, 10497);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10240, 9729);
    }

    public void glClearColor() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(16384);
    }

    public void rotateMatrix() {
        Matrix.rotateM(this.modelMatrix, 0, 180.0f, 1.0f, 0.0f, 0.0f);
    }

    public void release() {
        GLES20.glDeleteProgram(this.program);
    }
}
