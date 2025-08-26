package com.inair.versionupdate;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inair.inaircommon.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NewFeatureAdapter extends
        RecyclerView.Adapter<NewFeatureAdapter.NewFeatureViewHolder> {

    private List<NewFeatureModel> featureList;
    private final boolean isDarkTheme;

    public NewFeatureAdapter(List<NewFeatureModel> featureList, boolean isDarkTheme) {
        this.featureList = featureList;
        this.isDarkTheme = isDarkTheme;
    }

    @NonNull
    @Override
    public NewFeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application_feature_sub_title, parent, false);
        return new NewFeatureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewFeatureViewHolder holder, int position) {
        NewFeatureModel feature = featureList.get(position);
        holder.title.setText(feature.getTitle()); // 假设 NewFeatureModel 有 getTitle() 方法
        // 根据主题设置标题文字颜色
        if (isDarkTheme) {
            holder.title.setTextColor(Color.WHITE);
        } else {
            holder.title.setTextColor(Color.BLACK);
        }
        // 创建 FeatureDescriptionAdapter 并设置给子 RecyclerView
        FeatureDescriptionAdapter descriptionAdapter = new FeatureDescriptionAdapter(feature.getDesList(),isDarkTheme); // 假设 NewFeatureModel 有 getDesList() 方法
        holder.desRecyclerView.setLayoutManager(new LinearLayoutManager(holder.desRecyclerView.getContext()));
        holder.desRecyclerView.setAdapter(descriptionAdapter);
    }

    @Override
    public int getItemCount() {
        return featureList == null ? 0 : featureList.size();
    }

    public static class NewFeatureViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        RecyclerView desRecyclerView;

        public NewFeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_feature_item_title);
            desRecyclerView = itemView.findViewById(R.id.rv_feature_item_description);
        }
    }
}
