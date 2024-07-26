package com.nothing.commonutils.utils.mediacodec.filter;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class BlackScreenFilter extends AbstractFboFilter {
    private static final String BLACK_SCREEN_FRAG
            = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 v_texPosition;\nuniform samplerExternalOES sTexture;\n\nuniform highp float S;\nuniform highp float H;\nuniform highp float L;\n\nhighp vec3 rgb2hsv(highp vec3 c){\n    highp vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);\n    highp vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));\n    highp vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));\n    highp float d = q.x - min(q.w, q.y);\n    highp float e = 1.0e-10;\n    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);\n}\n\nhighp vec3 hsv2rgb(highp vec3 c){\n    highp vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);\n    highp vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);\n    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);\n}\n\nvoid main() {\n    highp vec4 rgba = texture2D(sTexture, v_texPosition);\n    highp vec3 hsl = rgb2hsv(rgba.xyz);\n    if (S != 1.0) hsl.y = hsl.y * S;\n    if (H != 0.0) hsl.x = H;\n    if (hsl.x < 0.0) hsl.x = hsl.x + 1.0;\n    else if (hsl.x > 1.0) hsl.x = hsl.x-1.0;\n    if (L != 1.0) hsl.z = hsl.z * L;\n    highp vec3 rgb = hsv2rgb(hsl);\n    gl_FragColor = vec4(rgb, rgba.w);\n}";
    private static final float DEFAULT_HUE = 0.0f;
    private static final float DEFAULT_LIGHT = 1.0f;
    private static final float DEFAULT_LIGHT_MAX = 20.0f;
    private static final float DEFAULT_SATURATION = 1.0f;
    private int hHandle;
    private boolean isBlackScreen = false;
    private int lHandle;
    private int sHandle;
    private int textureId_filter;

    @Override
    public void initOpenGl() {
        super.initOpenGl(
                "attribute vec4 av_Position;\nattribute vec2 af_Position;\nvarying vec2 v_texPosition;\nuniform mat4 mvpMatrix;\n\nvoid main() {\n    v_texPosition = vec2(af_Position.x,1.0 - af_Position.y);\n    gl_Position = mvpMatrix * av_Position;\n}",
                BLACK_SCREEN_FRAG);
        Matrix.setIdentityM(this.modelMatrix, 0);
    }

    @Override // com.nothing.commonutils.utils.mediacodec.filter.AbstractFilter
    public void initGL(String str, String str2) {
        super.initGL(str, str2);
        this.mvpMatrixLoc = GLES20.glGetUniformLocation(this.program, "mvpMatrix");
        this.sHandle      = GLES20.glGetUniformLocation(this.program, "S");
        this.hHandle      = GLES20.glGetUniformLocation(this.program, "H");
        this.lHandle      = GLES20.glGetUniformLocation(this.program, "L");
    }

    public int onDrawTexture(int i) {
        this.textureId_filter = i;
        return onDrawTexture();
    }

    public void setBlackScreen(boolean z) {
        this.isBlackScreen = z;
    }

    private float getLight() {
        if (this.isBlackScreen) {
            return DEFAULT_LIGHT_MAX;
        }
        return 1.0f;
    }

    @Override // com.nothing.commonutils.utils.mediacodec.filter.AbstractFilter
    public int onDraw() {
        glUsrProgram();
        GLES20.glUniform1f(this.sHandle, 1.0f);
        GLES20.glUniform1f(this.hHandle, 0.0f);
        GLES20.glUniform1f(this.lHandle, getLight());
        GLES20.glUniformMatrix4fv(this.mvpMatrixLoc, 1, false, this.modelMatrix, 0);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(36197, this.textureId_filter);
        GLES20.glDrawArrays(5, 0, 4);
        return this.textureId_filter;
    }
}
