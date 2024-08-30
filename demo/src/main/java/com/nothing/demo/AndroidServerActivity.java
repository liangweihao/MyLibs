package com.nothing.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjectionManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.nothing.commonutils.opengl.OpenGLTest;
import com.nothing.commonutils.utils.Lg;
import com.nothing.demo.databinding.ActivityServerBinding;
import com.nothing.demo.databinding.FragmentTestBinding;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.features2d.SIFT;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_RGBA4;


public class AndroidServerActivity extends FragmentActivity {

    ActivityServerBinding binding;
    private static final String TAG = "AndroidServerActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityServerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        OpenGLTest.离屏渲染单线程模拟绘制三角形();
//        OpenGLTest.离屏渲染绘制一个矩形();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inTargetDensity = 160;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_img, options);
        Bitmap overBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_img2,
                                                         options);
        OpenGLTest.离屏渲染题图四边形(bitmap);
//        OpenGLTest.离屏渲染题图正方向居中裁剪_MAT(bitmap);
        OpenGLTest.贴图功能_附着到图片右下角XXX(bitmap, overBitmap);
//        OpenGLTest.贴图功能_上一个一直501_右下角(bitmap,overBitmap);

        if (OpenCVLoader.initLocal()) {
            Lg.d(TAG, "open cv loader suc");
        } else {
            Lg.d(TAG, "open cv loader fail");
        }
        // 检查是否有读取外部存储的权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) !=
            PackageManager.PERMISSION_GRANTED) {
            // 如果没有权限，则请求权限
            ActivityCompat.requestPermissions(this,
                                              new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                                              100);
        } else {

        }
//        Intent manageUnusedAppRestrictionsIntent
//                = IntentCompat.createManageUnusedAppRestrictionsIntent(this, getPackageName());
//        startActivity(manageUnusedAppRestrictionsIntent);
        try {
            String path = "/sdcard/DCIM/1080.jpg";
            Mat inputImg = Imgcodecs.imread(path);
            Mat edges = new Mat();
            Imgproc.Canny(inputImg, edges, 50, 200);
            Bitmap outBitmap = Bitmap.createBitmap(inputImg.width(), inputImg.height(),
                                                   Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(edges, outBitmap);
            System.out.println("compareHist");
//            String readWordDocx = readWordDocx("/sdcard/Download/testpdf.pdf");
//
//            System.out.println(readWordDocx);
        } catch (Throwable e) {
            e.printStackTrace();
        }


//        mediaProjectionManager
//                = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
//
//        Intent screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
//        startActivityForResult(screenCaptureIntent,1999);

//        FragmentUtils.replaceAddPop(getSupportFragmentManager(), this, android.R.id.content, new TestFragment(), "test");


        binding.sbA.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                canny(progress, binding.sbA.getProgress());

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.sbB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                canny(binding.sbA.getProgress(), progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.sbA.setProgress(1);
        binding.sbB.setProgress(1);
    }


    private Bitmap canny(double t1, double t2) {
        String path = "/sdcard/DCIM/1080.jpg";
//        String path = "/sdcard/download.jpeg";

        Mat img1 = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
        Mat img2 = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
        Mat dst1 = new Mat();
        Mat dst2 = new Mat();

        
        SIFT sift = SIFT.create();
        MatOfKeyPoint keypints1 = new MatOfKeyPoint();
        Mat des1 = new Mat();
        sift.detectAndCompute(img1, dst1, keypints1, des1);
        MatOfKeyPoint keypints2 = new MatOfKeyPoint();
        Mat des2 = new Mat();
        sift.detectAndCompute(img2, dst2, keypints2, des2);
        int FLANN_INDEX_KDTREE = 0;
        FlannBasedMatcher flannBasedMatcher = FlannBasedMatcher.create();
        List<MatOfDMatch> matches = new ArrayList<>();
        flannBasedMatcher.knnMatch(des1, des2, matches, 2);
        int MIN_MATCH_COUNT = 10;


        return null;
    }


    private Bitmap createBitmap(int w, int h) {
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    MediaProjectionManager mediaProjectionManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent intent = new Intent(this, ScreenService.class);
        intent.putExtra("data", data);
        startForegroundService(intent);
    }


    public static class TestFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState
        ) {
            FragmentTestBinding binding = FragmentTestBinding.inflate(inflater);
            return binding.getRoot();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            FragmentTestBinding binding = FragmentTestBinding.bind(view);

            binding.glSurface.setEGLContextClientVersion(2);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inTargetDensity = 160;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.xieti,
                                                         options);

            binding.glSurface.setRenderer(new GLSurfaceView.Renderer() {
                boolean contextCreated = false;

                int surfaceWidth = 1920;
                int surfaceHeight = 1080;
                int program = 0;
                int[] buffers = new int[2];
                int picPositionIndex = 0;
                int inputTexPositionIndex = 0;
                float[] pointVertex = new float[]{-1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f,};

                //                float[] texVertex = new float[]{0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f};
                float[] texVertex = new float[]{0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f};
                int[] textures = new int[1];
                int[] framebuffers = new int[1];
                int[] renderbuffers = new int[1];

                String vShader = "attribute vec2 picPosition;" +
                                 "attribute vec2 inputTexPosition;" + "varying vec2 texPosition;" +
                                 "void main(){" + "    gl_Position = vec4(picPosition,0.0,1.0);" +
                                 "    texPosition = inputTexPosition;" + "}";

                String fShader = "precision mediump float;" + "uniform sampler2D inputTexture;" +
                                 "varying vec2 texPosition;" + "void main(){" +
                                 "      gl_FragColor = texture2D(inputTexture,texPosition);" + "}";


                @Override
                public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                    program = GLES20.glCreateProgram();
                    binding.glSurface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

                    int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                    GLES20.glShaderSource(vertexShader, vShader);
                    GLES20.glAttachShader(program, vertexShader);
                    GLES20.glCompileShader(vertexShader);

                    int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                    GLES20.glShaderSource(fragmentShader, fShader);
                    GLES20.glAttachShader(program, fragmentShader);
                    GLES20.glCompileShader(fragmentShader);
                    GLES20.glLinkProgram(program);
                    GLES20.glGenBuffers(2, buffers, 0);

                    picPositionIndex = GLES20.glGetAttribLocation(program, "picPosition");
                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
                    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, pointVertex.length * 4,
                                        ByteBuffer.allocateDirect(pointVertex.length * 4)
                                                  .order(ByteOrder.nativeOrder())
                                                  .asFloatBuffer()
                                                  .put(pointVertex)
                                                  .position(0), GLES20.GL_STATIC_DRAW);
                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


                    inputTexPositionIndex = GLES20.glGetAttribLocation(program, "inputTexPosition");
                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
                    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, texVertex.length * 4,
                                        ByteBuffer.allocateDirect(texVertex.length * 4)
                                                  .order(ByteOrder.nativeOrder())
                                                  .asFloatBuffer()
                                                  .put(texVertex)
                                                  .position(0), GLES20.GL_STATIC_DRAW);


                    GLES20.glGenFramebuffers(1, framebuffers, 0);
                    GLES20.glGenRenderbuffers(1, renderbuffers, 0);
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffers[0]);
                    GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderbuffers[0]);
                    // 更新渲染缓冲区的参数
                    GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GL_RGBA4, surfaceWidth,
                                                 surfaceHeight);
                    // 首先获取 GL_FRAMEBUFFER 获取帧缓冲区 然后根据渲染缓冲区得到渲染对象 并且通过渲染对象得到采样， 然后将渲染的对象和采样 附加到帧缓冲区
                    GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                                                     GLES20.GL_COLOR_ATTACHMENT0,
                                                     GLES20.GL_RENDERBUFFER, renderbuffers[0]);
                    GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                    GLES20.glBindRenderbuffer(GL_RENDERBUFFER, 0);

                    GLES20.glGenTextures(1, textures, 0);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                                           GLES20.GL_MIRRORED_REPEAT);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                                           GLES20.GL_MIRRORED_REPEAT);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                                           GLES20.GL_LINEAR);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                                           GLES20.GL_LINEAR);
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
                    int inputTextureIndex = GLES20.glGetUniformLocation(program, "inputTexture");
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                    GLES20.glUniform1i(inputTextureIndex, 0);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

                    // 空间顶点
                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
                    GLES20.glEnableVertexAttribArray(picPositionIndex);
                    GLES20.glVertexAttribPointer(picPositionIndex, 2, GLES20.GL_FLOAT, false, 0, 0);
                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                    GLES20.glDisableVertexAttribArray(picPositionIndex);

                    // 纹理顶点
                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
                    GLES20.glEnableVertexAttribArray(inputTexPositionIndex);
                    GLES20.glVertexAttribPointer(inputTexPositionIndex, 2, GLES20.GL_FLOAT, false,
                                                 0, 0);
                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                    GLES20.glDisableVertexAttribArray(inputTexPositionIndex);

                    GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                    GLES20.glBindRenderbuffer(GL_RENDERBUFFER, 0);
                }

                @Override
                public void onSurfaceChanged(GL10 gl, int width, int height) {
                    surfaceWidth  = width;
                    surfaceHeight = height;
                    gl.glViewport(0, 0, width, height);
                }

                @Override
                public void onDrawFrame(GL10 gl) {

                    GLES20.glUseProgram(program);
                    GLES20.glEnableVertexAttribArray(picPositionIndex);
                    GLES20.glEnableVertexAttribArray(inputTexPositionIndex);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
                    GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                    GLES20.glBindRenderbuffer(GL_RENDERBUFFER, 0);
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, pointVertex.length / 2);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

                    Buffer pixels = IntBuffer.allocate(surfaceWidth * surfaceHeight);
                    GLES20.glReadPixels(0, 0, surfaceWidth, surfaceHeight, GLES20.GL_RGBA,
                                        GLES20.GL_UNSIGNED_BYTE, pixels);
                    Bitmap bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight,
                                                        Bitmap.Config.ARGB_8888, false);
                    bitmap.copyPixelsFromBuffer(pixels.rewind());
                    GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                    GLES20.glBindRenderbuffer(GL_RENDERBUFFER, 0);


                    GLES20.glDeleteProgram(program);
                    GLES20.glReleaseShaderCompiler();
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown() called with: keyCode = [" + keyCode + "], event = [" + event + "]");
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp() called with: keyCode = [" + keyCode + "], event = [" + event + "]");
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed() called");
    }

    /**
     * 阅读 Word 文档 - doc 格式文件
     *
     * @param filePath doc 文件路径
     * @return 仅文档内容
     */
    public static String readWordDoc(String filePath) {
        try {
            FileInputStream in = new FileInputStream(filePath);
            //PoiFs ：管理整个文件系统生命周期
            POIFSFileSystem pfs = new POIFSFileSystem(in);
            //获取文档所有的数据结构 : 文档对象
            HWPFDocument hwpfDocument = new HWPFDocument(pfs);
            return hwpfDocument.getText().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "请检查是否授予了权限 or Word文档依赖包";
        }
    }

    /**
     * 阅读 Word 文档 - docx 格式文件
     *
     * @param filePath docx 文件路径
     * @return 仅文档内容
     */
    public static String readWordDocx(String filePath) {
        try {
            InputStream is = new FileInputStream(filePath);
            XWPFDocument doc = new XWPFDocument(is);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            return extractor.getText();
        } catch (Exception e) {
            e.printStackTrace();
            return "请检查是否授予了权限 or Word文档依赖包";
        }
    }


}
