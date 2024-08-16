package com.inair.inaircommon.view;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class CrashConstraintLayout extends ConstraintLayout {
   public CrashConstraintLayout(@NonNull Context context) {
      super(context);
   }

   public CrashConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
      super(context, attrs);
   }

   public CrashConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
   }

   public CrashConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
      super(context, attrs, defStyleAttr, defStyleRes);
   }

   @Override
   public void draw(Canvas canvas) {
      try {
         super.draw(canvas);
      }catch (Throwable e){
         e.printStackTrace();
      }

   }

   @Override
   protected void onDraw(Canvas canvas) {
      try {
         super.onDraw(canvas);
      }catch (Throwable e){
         e.printStackTrace();
      }

   }
}
