package com.nothing.demo;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.nothing.commonutils.utils.Lg;

import org.opencv.android.CameraActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Puzzle15Activity extends CameraActivity
{
    private static final String TAG = "Puzzle15Activity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = createIntent(this, "*/*");
        startActivityForResult(intent,200);
    }

    public Intent createIntent(@NonNull Context context, @NonNull String input) {
        return new Intent(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType(input)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Lg.i(TAG,"activity result %d:%d:%s",requestCode,resultCode,String.valueOf(data.getData()));
    }
}
