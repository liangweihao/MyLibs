package com.nothing.commonutils.utils;

import com.nothing.commonutils.inter.DynamicInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;

/**
 * --------------------
 * <p>Author：
 * lwh
 * <p>Created Time:
 * 2024/12/2
 * <p>Intro:
 *
 * <p>Thinking:
 *
 * <p>Problem:
 *
 * <p>Attention:
 * --------------------
 */

public class DynamicClassLoader extends java.lang.ClassLoader {

    private static final String TAG = "DynamicClassLoader";

    public Class<?> loadClassFromFile(String filePath) throws IOException {
        try {
            byte[] classData = readClassFileData(filePath);
            return defineClass(null, classData, 0, classData.length);
        } catch (IOException e) {
            throw e;
        }
    }

    private byte[] readClassFileData(String filePath) throws IOException {
        File file = new File(filePath);
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[(int) file.length()];
            int length;
            while ((length = fis.read(buffer))!= 0) {
                baos.write(buffer, 0, length);
            }
            return baos.toByteArray();
        }
    }

    public void callInterface(String filePath ) throws IOException {
        DynamicClassLoader loader = new DynamicClassLoader();
        Class<?> loadedClass = loader.loadClassFromFile(filePath);
        // 确保加载的类实现了指定的接口
        if (DynamicInterface.class.isAssignableFrom(loadedClass)) {
            DynamicInterface instance = (DynamicInterface) Proxy.newProxyInstance(
                    loader,
                    new Class[]{DynamicInterface.class},
                    (proxy, method, args) -> {
                        Object realObject = loadedClass.newInstance();
                        return method.invoke(realObject, args);
                    }
            );
            // 调用接口中的方法，实际会通过代理调用动态加载的类的方法
            Object called = instance.call();
            Lg.i(TAG,"Call DynamicInterface Result:%s",String.valueOf(called));
        }
    }

}
