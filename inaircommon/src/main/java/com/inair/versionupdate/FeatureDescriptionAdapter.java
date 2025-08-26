package com.inair.versionupdate;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inair.inaircommon.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FeatureDescriptionAdapter extends
        RecyclerView.Adapter<FeatureDescriptionAdapter.FeatureDescriptionViewHolder> {

    private List<String> descriptionList;
    private final boolean isDarkTheme;

    public FeatureDescriptionAdapter(List<String> descriptionList, boolean isDarkTheme) {
        this.descriptionList = descriptionList;
        this.isDarkTheme = isDarkTheme;
    }

    @NonNull
    @Override
    public FeatureDescriptionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        // 加载子布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_applicationi_feature_sub_child_des, parent, false);
        return new FeatureDescriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureDescriptionViewHolder holder, int position) {

        String description = descriptionList.get(position);
        holder.descriptionTextView.setText(description);
        // 绑定数据到视图
        if (isDarkTheme) {
            holder.descriptionTextView.setTextColor(Color.parseColor("#80FFFFFF"));
        } else {
            holder.descriptionTextView.setTextColor(Color.parseColor("#80000000"));
        }
    }

    @Override
    public int getItemCount() {
        return descriptionList == null ? 0 : descriptionList.size();
    }

    public static class FeatureDescriptionViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;

        public FeatureDescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            // 初始化视图组件
            descriptionTextView = itemView.findViewById(R.id.tv_feature_item_description);
        }
    }
}