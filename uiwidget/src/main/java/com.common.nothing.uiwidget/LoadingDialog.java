package com.common.nothing.uiwidget;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;




public class LoadingDialog extends AlertDialog {
    private String title;
    private String msg;
    private boolean canCancel = true;

    public LoadingDialog(Context context, String title, String msg) {
        super(context);
        this.title = title;
        this.msg = msg;
        canCancel = true;
    }

    public LoadingDialog(Context context, String title, String msg, boolean canCancel) {
        super(context);
        this.title = title;
        this.msg = msg;
        this.canCancel = canCancel;
    }

    TextView viewTitle;
    TextView viewMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_widget_dialog_loading);
        viewTitle = (TextView) findViewById(R.id.tv_title);
        viewMsg = (TextView) findViewById(R.id.tv_msg);
        if (TextUtils.isEmpty(title)){
            viewTitle.setVisibility(View.GONE);
        }else {
            viewTitle.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(msg)){
            viewMsg.setVisibility(View.GONE);
        }else {
            viewMsg.setVisibility(View.VISIBLE);
        }
        viewTitle.setText(title);
        viewMsg.setText(msg);
        setCanceledOnTouchOutside(canCancel);
        setCancelable(canCancel);
    }


    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        this.title = title.toString();
        if (viewTitle != null)
            viewTitle.setText(title);
    }

    @Override
    public void setMessage(CharSequence message) {
        super.setMessage(message);
        this.msg = message.toString();
        if (viewMsg != null)
            viewMsg.setText(message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER;

        getWindow().setAttributes(layoutParams);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}
