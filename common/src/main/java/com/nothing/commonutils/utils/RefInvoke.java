package com.nothing.commonutils.utils;


import android.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;

/**
 * 反射工具类封装
 * 支持Android final类型
 */
public class RefInvoke {

    private static final String TAG = "RefInvoke";

    /**
     * 根据类名 反射无参构造函数 获取实例
     *
     * @param className
     * @return
     */
    public static Object createObject(String className) {
        try {
            Class[] paramTypes = new Class[]{};
            Object[] paramValues = new Object[]{};
            Class<?> aClass = Class.forName(className);
            return createObject(aClass, paramTypes, paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Nullable
    public static Class getClass(String className){
        Class<?> aClass = null;
        try {
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return aClass;
    }


    /**
     * 根据类名、参数类型、参数值 反射创建出一个对象
     *
     * @param className
     * @param paramTypes
     * @param paramValues
     * @return
     */
    public static Object createObject(String className, Class[] paramTypes, Object[] paramValues) {
        try {
            Class<?> aClass = Class.forName(className);
            return createObject(aClass, paramTypes, paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据类类型、参数类型、参数值 反射创建出一个对象
     *
     * @param clazz
     * @param paramTypes
     * @param paramValues
     * @return
     */
    public static Object createObject(Class clazz, Class[] paramTypes, Object[] paramValues) {
        try {
            Constructor constructor = clazz.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据对象反射调用其实例方法（非静态方法）
     *
     * @param obj
     * @param methodName
     * @param paramTypes
     * @param paramValues
     * @return
     */
    public static Object invokeInstanceMethod(Object obj,
                                              String methodName,
                                              Class[] paramTypes,
                                              Object[] paramValues) {
        if (obj == null) {
            return null;
        }
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(obj, paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <O> Object invokeInstanceMethod(Object obj,
                                                  String methodName,
                                                  Pair<Class<O>, O>[] params) {
        if (obj == null) {
            return null;
        }
        try {
            List<Class<O>> classList = Arrays.stream(params).map(classObjectPair -> classObjectPair.first).collect(
                    Collectors.toList());
            Class<O>[] classArray = new Class[classList.size()];
            classList.toArray(classArray);
            Method method = obj.getClass().getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            Object[] invokeObj = Arrays.stream(params).map(classObjectPair -> classObjectPair.second).toArray();
            return method.invoke(obj, invokeObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据类名反射调用其静态方法
     *
     * @param className
     * @param methodName
     * @param paramTypes
     * @param paramValues
     * @return
     */
    public static Object invokeStaticMethod(String className,
                                            String methodName,
                                            @Nullable Class[] paramTypes,
                                            @Nullable Object[] paramValues) {
        try {
            Class<?> aClass = Class.forName(className);
            Method method = aClass.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(null, paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据类名、类的实例、属性名获取实例的属性
     *
     * @param className
     * @param obj
     * @param filedName
     * @return
     */
    public static Object getFieldObject(String className, Object obj, String filedName) {
        try {
            Class<?> aClass = Class.forName(className);
            Field field = aClass.getDeclaredField(filedName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据类名、实例反射设置属性值
     * note：如果实例为空且该属性为静态的，则为设置静态属性的值
     *
     * @param className
     * @param obj
     * @param filedName
     * @param fieldValues
     */
    public static void setFieldObject(String className,
                                      Object obj,
                                      String filedName,
                                      Object fieldValues) {
        try {
            Class<?> aClass = Class.forName(className);
            Field field = aClass.getDeclaredField(filedName);
            field.setAccessible(true);
            field.set(obj, fieldValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据类名获取静态属性
     *
     * @param className
     * @param fieldName
     * @return
     */
    public static Object getStaticFieldObject(String className, String fieldName) {
        return getFieldObject(className, null, fieldName);
    }

    /**
     * 根据类名反射设置静态属性的值
     *
     * @param className
     * @param fieldName
     * @param fieldValues
     */
    public static void setStaticFieldObject(String className,
                                            String fieldName,
                                            Object fieldValues) {
        setFieldObject(className, null, fieldName, fieldValues);
    }


    @Nullable
    public static Pair<Class<?>, Object> proxyTargetClassInstance(Object outProxy,
                                                                  String targetClassName) {
        return proxyTargetClassInstance(targetClassName,new InvokeHandler(outProxy));
    }

    @Nullable
    public static Pair<Class<?>, Object> proxyTargetClassInstance(String targetClassName,
                                                                  InvokeHandler invokeHandler) {
        try {
            Class<?> targetClass = Class.forName(targetClassName);
            Object cast = targetClass.cast(Proxy.newProxyInstance(targetClass.getClassLoader(),
                    new Class[]{targetClass}, invokeHandler));
            return new Pair<>(targetClass, cast);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class InvokeHandler implements InvocationHandler {

        Object outProxy;

        public InvokeHandler(Object outProxy) {
            this.outProxy = outProxy;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                Method foundMethod = outProxy.getClass().getMethod(method.getName(),
                        method.getParameterTypes());
                foundMethod.setAccessible(true);
                return foundMethod.invoke(outProxy, args);
            } catch (Exception e) {
                e.printStackTrace();
                return method.invoke(outProxy, args);
            }
        }
    }


    public static Object getEnum(Class<?> clazz, String name){
//name = "CLICK"
//ordinal = 0
        Object[] enumConstants = clazz.getEnumConstants();
        if (enumConstants == null){
            return null;
        }
        for (Object constant : enumConstants) {
            if (constant instanceof Enum) {
                if (((Enum<?>) constant).name().equals(name)) {
                    return constant;
                }
            }
        }
        return null;
    }

//    单单注解 某个接口或者是父类还不行 实例的对象又不能充分满足 所以放弃
//    @Retention(RetentionPolicy.RUNTIME)
//    public @interface IProxyTargetClass {
//        String name();
//    }


}