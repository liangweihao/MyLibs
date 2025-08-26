package com.inair.versionupdate;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemSpacingDecoration extends RecyclerView.ItemDecoration {
   private final int spacing;

   public ItemSpacingDecoration(int spacing) {
      this.spacing = spacing;
   }

   @Override
   public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
      super.getItemOffsets(outRect, view, parent, state);
      // 为每个条目底部添加间距
      outRect.bottom = spacing;
      // 如果是第一个条目，为其顶部也添加间距
      if (parent.getChildAdapterPosition(view) == 0) {
         outRect.top = spacing;
      }
   }

   @Override
   public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
      super.onDraw(c, parent, state);
   }
}
