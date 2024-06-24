package com.nothing.commonutils.utils;

import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GsonUtils {


    @NonNull
    public static String toJson(Object obj){
        String json = "";
        try {
            json = new Gson().toJson(obj);
        }catch (Throwable e){
            e.printStackTrace();
        }
        return json;
    }


    @Nullable
    public static <T> T toModle(String json,Class<T> tClass){
        try{
            T t = new Gson().fromJson(json, tClass);
            return  t;
        }catch (Throwable t){
            t.printStackTrace();
        }
        return null;
    }
}
