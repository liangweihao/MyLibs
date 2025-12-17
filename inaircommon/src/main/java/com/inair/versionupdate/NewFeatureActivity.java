package com.inair.versionupdate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inair.inaircommon.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NewFeatureActivity extends FragmentActivity {

    private RecyclerView recyclerView;
    private NewFeatureAdapter adapter;
    private List<NewFeatureModel> featureList;
    private ImageView appIconImageView;
    private TextView experienceNowTextView;
    private TextView featureTitleTextView; // 新增 TextView 引用
    private LinearLayout mainContainer; // 新增 LinearLayout 引用
    private int appIconResId;
    // 新增字段，用于表示是否使用暗夜主题
    private boolean isDarkTheme;
    String useNowTitle = "";
    String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appIconResId = getIntent().getIntExtra("app_icon_res_id", -1);

        useNowTitle = getIntent().getStringExtra("useNowTitle");
        title = getIntent().getStringExtra("title");
        featureList = getIntent().getParcelableArrayListExtra("feature_list");
        // 从 Intent 中获取是否为暗夜主题的信息
        isDarkTheme = getIntent().getBooleanExtra("is_dark_theme", false);

        setContentView(R.layout.activity_update_feature);
        // 初始化视图组件
        initViews();

        // 初始化 RecyclerView
        initRecyclerView();
        // 初始化应用图标
        initAppIcon();
        // 根据主题设置 UI 样式
        applyTheme();
        // 设置立即体验按钮点击事件
        setExperienceNowClickListener();
    }

    private void applyTheme() {
        if (isDarkTheme) {
            // 实现暗夜主题的 UI 设置逻辑
            mainContainer.setBackgroundColor(Color.BLACK);
            featureTitleTextView.setTextColor(Color.WHITE);
        } else {
            // 实现白天主题的 UI 设置逻辑
            mainContainer.setBackgroundColor(Color.WHITE);
            featureTitleTextView.setTextColor(Color.BLACK);
        }
    }

    private void initViews() {
        appIconImageView = findViewById(R.id.iv_feature_icon);
        experienceNowTextView = findViewById(R.id.tv_experience_now);
        featureTitleTextView = findViewById(R.id.tv_feature_title); // 初始化标题 TextView
        mainContainer = findViewById(R.id.ll_main_container); // 初始化主容器 LinearLayout
        featureTitleTextView.setText(title);
        experienceNowTextView.setText(useNowTitle);
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.rv_feature_list);
        // 添加 40px 的条目间距
        int spacingInPixels = 40;
        recyclerView.addItemDecoration(new ItemSpacingDecoration(spacingInPixels));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewFeatureAdapter(featureList, isDarkTheme);
        recyclerView.setAdapter(adapter);
    }

    private void initAppIcon() {
        // 这里假设使用 R.drawable.app_icon 作为应用图标，你可根据实际情况修改
        appIconImageView.setImageResource(appIconResId);
    }

    private void setExperienceNowClickListener() {
        experienceNowTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里实现立即体验按钮点击后的逻辑
                // 例如，关闭当前页面
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    /**
     * 创建启动 NewFeatureActivity 的 Intent
     *
     * @param activity     启动该 Activity 的上下文
     * @param appIconResId 应用图标的资源 ID
     * @param featureList  新特性列表
     * @param isDarkTheme  是否使用暗夜主题
     * @return 用于启动 NewFeatureActivity 的 Intent
     */
    public static Intent createIntent(
            Activity activity,
            String title,
            String useNowTitle,
            int appIconResId,
            List<NewFeatureModel> featureList,
            boolean isDarkTheme
    ) {
        Intent intent = new Intent(activity, NewFeatureActivity.class);
        intent.putExtra("app_icon_res_id", appIconResId);
        intent.putExtra("title", title);
        intent.putExtra("useNowTitle", useNowTitle);
        intent.putParcelableArrayListExtra("feature_list", new ArrayList<>(featureList));
        intent.putExtra("is_dark_theme", isDarkTheme);
        return intent;
    }

    /**
     * 启动 NewFeatureActivity
     *
     * @param activity     启动该 Activity 的上下文
     * @param appIconResId 应用图标的资源 ID
     * @param featureList  新特性列表
     * @param isDarkTheme  是否使用暗夜主题
     */
    public static void start(
            Activity activity,
            String title,
            String useNowTitle,
            int appIconResId,
            List<NewFeatureModel> featureList,
            boolean isDarkTheme
    ) {
        Intent intent = createIntent(activity, title, useNowTitle, appIconResId, featureList, isDarkTheme);
        activity.startActivity(intent);
    }

    @VisibleForTesting
    public static void testNewFeatureActivityLaunch(Activity activity) {
        // 生成假的应用图标资源 ID，这里使用 Android 系统自带的图标示例
        int appIconResId = android.R.drawable.ic_menu_info_details;

        // 生成假的新特性列表
        List<NewFeatureModel> featureList = new ArrayList<>();
        // 第一个新特性
        List<String> desList1 = new ArrayList<>();
        desList1.add("测试新特性 1 的详细描述 1");
        featureList.add(new NewFeatureModel("测试新特性 1", desList1));

        // 第二个新特性
        List<String> desList2 = new ArrayList<>();
        desList2.add("测试新特性 2 的详细描述 1");
        desList2.add("测试新特性 2 的详细描述 2");
        featureList.add(new NewFeatureModel("测试新特性 2", desList2));

        List<String> desList3 = new ArrayList<>();
        desList3.add("测试新特性 2 的详细描述 1");
        desList3.add("测试新特性 2 的详细描述 2");
        desList3.add("测试新特性 3 的详细描述 3");
        featureList.add(new NewFeatureModel("测试新特性 3", desList3));


        List<String> desList4 = new ArrayList<>();
        desList4.add("测试新特性 2 的详细描述 1");
        desList4.add("测试新特性 2 的详细描述 2测试新特性 2 的详细描述 2测试新特性 2 的详细描述 2测试新特性 2 的详细描述 2测试新特性 2 的详细描述 2");
        desList4.add("测试新特性 3 \n 的详细描述 3");
        desList4.add("测试新特性 4 的详细描述 3");
        featureList.add(new NewFeatureModel("测试新特性 4", desList4));

        // 假设使用白天主题
        boolean isDarkTheme = false;

        // 调用 start 方法启动 NewFeatureActivity
        start(activity, "测试标题", "提交", appIconResId, featureList, isDarkTheme);
    }

}
