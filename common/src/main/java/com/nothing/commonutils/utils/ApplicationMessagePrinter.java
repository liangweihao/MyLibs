package com.nothing.commonutils.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Printer;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 监听主线程 Handler 消息，捕获 View 点击事件并打印关键信息
 * 修复：正则匹配失败（matcher.find() 返回 null）问题
 */
public class ApplicationMessagePrinter implements Printer {
    // 点击事件关键词（兼容不同 Android 版本）
    private static final String[] CLICK_KEYWORDS = {"performClick", "onClick", "MSG_PERFORM_CLICK", "DispatchTouchEvent"};
    // 反射字段（提前初始化，避免重复反射）
    private  Field callbackField;
    private  Field objField;

    private static final String TAG = "ClickEvent";
    public ApplicationMessagePrinter() {
        // 初始化 Message 的 callback 和 obj 反射字段
        try {
            callbackField = Message.class.getDeclaredField("callback");
            callbackField.setAccessible(true);

            objField = Message.class.getDeclaredField("obj");
            objField.setAccessible(true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void println(String x) {
        // 仅处理主线程 Message 分发阶段的日志
        if (x == null || !x.startsWith(">>>>> Dispatching to")) {
            return;
        }

        try {
            // 1. 解析 Message 日志信息（修复正则匹配问题）
            MessageInfo messageInfo = parseMessageInfo(x);

            // 2. 判断是否为点击事件
            if (isClickEvent(messageInfo.msgStr) || isClickEvent(x)) {
                // 3. 提取点击的 View
                View clickView = findClickView(messageInfo.handler, messageInfo.what);
                if (clickView != null) {
                    // 4. 打印 View 关键信息
                    printViewInfo(clickView);
                }
            }
        } catch (Exception e) {

        }
    }

    /**
     * 修复正则匹配：适配所有 Dispatching to 日志格式
     */
    private MessageInfo parseMessageInfo(String log) {
        // 主正则：兼容数字在中间的场景（大部分情况）
        Pattern pattern = Pattern.compile(">>>>> Dispatching to (Handler\\s+\\(.*?\\)\\s+\\{[^}]+\\}).*?(\\d+):\\s*(.*)");
        Matcher matcher = pattern.matcher(log);

        String targetStr = null;
        int what = 0;
        String msgStr = "";

        // 场景1：数字在中间（如 ": 123: "）
        if (matcher.find()) {
            targetStr = matcher.group(1);
            what = Integer.parseInt(matcher.group(2));
            msgStr = matcher.group(3);
        } else {
            // 场景2：数字在最后（如 ": 0." 或 ": 0"）
            pattern = Pattern.compile(">>>>> Dispatching to (Handler\\s+\\(.*?\\)\\s+\\{[^}]+\\})\\s+.*?:\\s*(\\d+)[\\.]?\\s*(.*)");
            matcher = pattern.matcher(log);
            if (matcher.find()) {
                targetStr = matcher.group(1);
                what = Integer.parseInt(matcher.group(2));
                msgStr = matcher.group(3);
            } else {
                // 场景3：完全匹配不到 Handler，但仍需返回基础信息（避免 null）
                return new MessageInfo(null, 0, log);
            }
        }

        // 匹配对应的 Handler
        Handler target = findHandlerByTargetStr(targetStr);
        return new MessageInfo(target, what, msgStr);
    }

    /**
     * 判断是否为点击事件（兼容原始日志/解析后的日志）
     */
    private boolean isClickEvent(String msgStr) {
        if (msgStr == null) return false;
        for (String keyword : CLICK_KEYWORDS) {
            if (msgStr.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从 Message 中提取点击的 View（保留原逻辑）
     */
    private View findClickView(Handler handler, int what) {
        try {
            // 步骤1：获取主线程 Looper 的 mQueue
            Looper mainLooper = Looper.getMainLooper();
            Field queueField = Looper.class.getDeclaredField("mQueue");
            queueField.setAccessible(true);
            Object queue = queueField.get(mainLooper);

            // 步骤2：获取 Queue 中当前正在处理的 Message（mCurrentMessage）
            Field currentMessageField = queue.getClass().getDeclaredField("mMessages");
            currentMessageField.setAccessible(true);
            Message currentMsg = (Message) currentMessageField.get(queue);
            if (currentMsg == null) {
                return null;
            }

            // 步骤3：从 Message 的 callback/obj 中提取 View
            // 场景1：callback 是点击事件的 Runnable
            Runnable callback = (Runnable) callbackField.get(currentMsg);
            if (callback != null) {
                View view = findViewFromRunnable(callback);
                if (view != null) {
                    return view;
                }
            }

            // 场景2：obj 直接是 View
            Object obj = objField.get(currentMsg);
            if (obj instanceof View) {
                return (View) obj;
            }

            // 场景3：从 Handler 中溯源 View
            return findViewFromHandler(handler);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Runnable 反射提取 View（保留原逻辑）
     */
    private View findViewFromRunnable(Runnable runnable) {
        try {
            // 遍历 Runnable 的所有字段
            Field[] fields = runnable.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(runnable);

                // 字段是 View 类型
                if (value instanceof View) {
                    return (View) value;
                }

                // 兼容 OnClickListener 包装类
                if (value instanceof View.OnClickListener) {
                    // 遍历 OnClickListener 的字段找 View
                    Field[] listenerFields = value.getClass().getDeclaredFields();
                    for (Field listenerField : listenerFields) {
                        listenerField.setAccessible(true);
                        Object listenerValue = listenerField.get(value);
                        if (listenerValue instanceof View) {
                            return (View) listenerValue;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 从 Handler 中溯源关联的 View（保留原逻辑）
     */
    private View findViewFromHandler(Handler handler) {
        if (handler == null) {
            return null;
        }

        try {
            // 遍历 Handler 的所有字段找 View 引用
            Field[] fields = handler.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(handler);
                if (value instanceof View) {
                    return (View) value;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 打印 View 关键信息（保留原逻辑）
     */
    private void printViewInfo(View view) {
        // 1. View ID（含资源名）
        String viewId;
        if (view.getId() != View.NO_ID) {
            try {
                viewId = "id=" + view.getResources().getResourceName(view.getId());
            } catch (Exception e) {
                viewId = "id=" + view.getId() + "（无法获取资源名）";
            }
        } else {
            viewId = "id=NO_ID";
        }

        // 2. 文本内容（TextView 子类）
        String viewText;
        if (view instanceof TextView) {
            viewText = "text=" + ((TextView) view).getText();
        } else {
            viewText = "非文本View";
        }

        // 3. 其他关键信息
        String viewClass = " class=" + view.getClass().getSimpleName();
        String viewPosition = " (Left:" + view.getLeft() + ", Top:" + view.getTop() + ", Right:" + view.getRight() + ", Bottom:" + view.getBottom() + ")";

        String  text = view instanceof TextView? "text="+((TextView) view).getText().toString() : "";
        // 拼接日志
        StringBuilder log = new StringBuilder();
        log.append(viewClass)
                .append(viewId)
                .append(viewText)
                .append(viewPosition)
                .append(text);

        Lg.i(TAG, log.toString());
    }

    /**
     * 反射获取主线程所有活跃的 Handler（保留原逻辑）
     */
    private List<Handler> getMainThreadHandlers() {
        List<Handler> handlers = new ArrayList<>();
        try {
            Looper mainLooper = Looper.getMainLooper();
            // 1. 获取 Looper 的 mQueue
            Field queueField = Looper.class.getDeclaredField("mQueue");
            queueField.setAccessible(true);
            Object queue = queueField.get(mainLooper);

            // 2. 遍历 mQueue 中的所有 Message，提取 target（Handler）
            Field mMessagesField = queue.getClass().getDeclaredField("mMessages");
            mMessagesField.setAccessible(true);
            Message msg = (Message) mMessagesField.get(queue);

            while (msg != null) {
                Handler target = msg.getTarget();
                if (target != null && !handlers.contains(target)) {
                    handlers.add(target);
                }

                // 获取下一个 Message
                Field nextField = Message.class.getDeclaredField("next");
                nextField.setAccessible(true);
                msg = (Message) nextField.get(msg);
            }
        } catch (Exception e) {
        }
        return handlers;
    }

    /**
     * 从主线程 Handler 列表中匹配目标 Handler（保留原逻辑）
     */
    private Handler findHandlerByTargetStr(String targetStr) {
        if (targetStr == null) return null;

        List<Handler> mainHandlers = getMainThreadHandlers();
        if (mainHandlers.isEmpty()) {
            return null;
        }

        // 匹配 Handler 的 hashCode（如 {7d4c4e} 中的 7d4c4e）
        Pattern pattern = Pattern.compile("\\{(\\w+)\\}");
        Matcher matcher = pattern.matcher(targetStr);
        if (!matcher.find()) {
            return null;
        }

        String hashCode = matcher.group(1);
        for (Handler handler : mainHandlers) {
            if (handler.toString().contains(hashCode)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * 自定义实体类：封装 Message 解析结果
     */
    private static class MessageInfo {
        Handler handler;
        int what;
        String msgStr;

        public MessageInfo(Handler handler, int what, String msgStr) {
            this.handler = handler;
            this.what = what;
            this.msgStr = msgStr;
        }
    }

    /**
     * 模拟项目中的 Lg 日志工具
     */
    public static class Lg {
        public static void e(String tag, String msg) {
            Log.e(tag, msg);
        }

        public static void i(String tag, String msg) {
            Log.i(tag, msg);
        }
    }
}