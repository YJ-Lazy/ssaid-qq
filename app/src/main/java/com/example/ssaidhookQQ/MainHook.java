package com.example.ssaidhookQQ;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Color;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.api.XposedModule;

/**
 * QQ NT 专用增强版：
 * 1. Hook Settings.Secure.getString(android_id)
 * 2. 不再依赖 AboutActivity 类名
 * 3. 通过 QQ NT 实际渲染出来的 View 文本/Activity 页面特征识别“关于QQ”页面
 * 4. 使用 DecorView Overlay 显示状态，避免破坏 QQ NT RecyclerView/Compose/自定义布局
 */
public class MainHook extends XposedModule {
    private static final String TAG = "QQSSaidHook";
    private static final String TARGET_PKG = "com.tencent.mobileqq";
    private static final String SELF_PKG = "com.example.ssaidhookQQ";
    private static final String VIEW_TAG = "qqssaid_nt_status";

    @Override
    public void onPackageReady(PackageReadyParam param) {
        String packageName = param.getPackageName();
        if (!TARGET_PKG.equals(packageName) || SELF_PKG.equals(packageName)) return;

        hookAndroidId(param);
        hookQqNtLifecycle();
    }

    private volatile String lastSsaid;

    private void hookAndroidId(PackageReadyParam param) {
        try {
            ClassLoader cl = param.getClassLoader();
            if (cl == null) cl = ClassLoader.getSystemClassLoader();
            Class<?> secureClass = Class.forName("android.provider.Settings$Secure", false, cl);
            Method getString = secureClass.getDeclaredMethod(
                    "getString", ContentResolver.class, String.class);

            hook(getString).intercept(chain -> {
                try {
                    if (chain.getArgs() != null && chain.getArgs().size() >= 2
                            && Settings.Secure.ANDROID_ID.equals(chain.getArg(1))) {
                        Object resolverObj = chain.getArg(0);
                        if (resolverObj instanceof ContentResolver) {
                            String id = loadSsaid((ContentResolver) resolverObj);
                            if (id != null && !id.isEmpty()) {
                                lastSsaid = id;
                                return id;
                            }
                        }
                    }
                } catch (Throwable t) {
                    log(Log.WARN, TAG, "read configured SSAID failed: " + t.getMessage());
                }
                return chain.proceed();
            });
            log(Log.INFO, TAG, "ANDROID_ID hook installed (provider-backed config)");
        } catch (Throwable t) {
            log(Log.ERROR, TAG, "Failed to hook ANDROID_ID", t);
        }
    }

    /**
     * QQ NT 不依赖固定 AboutActivity。
     * 在 Activity.resume / window focus 后扫描实际 View 树。
     * QQ NT 的“关于QQ”页面即使 Activity/Fragment 名称变化，最终仍需要渲染标题文本，
     * 因此这个识别点比硬编码类名稳定得多。
     */
    private void hookQqNtLifecycle() {
        try {
            Method onResume = Activity.class.getDeclaredMethod("onResume");
            hook(onResume).intercept(chain -> {
                try {
                    Object obj = chain.getThisObject();
                    if (!(obj instanceof Activity)) return chain.proceed();
                    Activity activity = (Activity) obj;
                    scheduleDetect(activity, 250);
                    scheduleDetect(activity, 900);
                    scheduleDetect(activity, 1800);
                } catch (Throwable t) {
                    log(Log.WARN, TAG, "onResume detector failed: " + t.getMessage());
                }
                return chain.proceed();
            });

            Method onWindowFocusChanged = Activity.class.getDeclaredMethod(
                    "onWindowFocusChanged", boolean.class);
            hook(onWindowFocusChanged).intercept(chain -> {
                try {
                    if (!Boolean.TRUE.equals(chain.getArg(0))) return chain.proceed();
                    Object obj = chain.getThisObject();
                    if (obj instanceof Activity) scheduleDetect((Activity) obj, 120);
                } catch (Throwable ignored) { }
                return chain.proceed();
            });

            log(Log.INFO, TAG, "QQ NT view-based About hook installed");
        } catch (Throwable t) {
            log(Log.ERROR, TAG, "Failed to install QQ NT lifecycle hook", t);
        }
    }

    private void scheduleDetect(final Activity activity, long delay) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        activity.getWindow().getDecorView().postDelayed(() -> detectAboutPage(activity), delay);
    }

    private void detectAboutPage(Activity activity) {
        try {
            if (activity.isFinishing() || activity.isDestroyed()) return;
            View decor = activity.getWindow().getDecorView();
            if (decor == null) return;

            String activityName = activity.getClass().getName().toLowerCase();
            boolean classHint = activityName.contains("about")
                    || activityName.contains("version")
                    || activityName.contains("setting");

            boolean textHint = containsAboutText(decor);
            if (classHint || textHint) {
                addNtOverlay(activity);
                log(Log.INFO, TAG, "QQ NT About detected: " + activity.getClass().getName()
                        + ", classHint=" + classHint + ", textHint=" + textHint);
            } else {
                removeNtOverlay(activity);
            }
        } catch (Throwable t) {
            log(Log.WARN, TAG, "About detection failed: " + t.getMessage());
        }
    }

    private boolean containsAboutText(View root) {
        ArrayDeque<View> queue = new ArrayDeque<>();
        Set<View> seen = new HashSet<>();
        queue.add(root);

        int scanned = 0;
        while (!queue.isEmpty() && scanned++ < 2500) {
            View v = queue.removeFirst();
            if (v == null || !seen.add(v)) continue;

            if (v instanceof TextView) {
                CharSequence cs = ((TextView) v).getText();
                if (cs != null && isAboutText(cs.toString())) return true;
            }
            if (v instanceof ViewGroup) {
                ViewGroup g = (ViewGroup) v;
                for (int i = 0; i < g.getChildCount(); i++) queue.addLast(g.getChildAt(i));
            }
        }
        return false;
    }

    private boolean isAboutText(String text) {
        if (text == null) return false;
        String s = text.trim().toLowerCase();
        return s.equals("关于qq")
                || s.equals("关于qq")
                || s.contains("关于qq")
                || s.contains("about qq")
                || s.contains("aboutqq")
                || s.contains("版本信息")
                || s.contains("version information");
    }

    /**
     * 在 DecorView 的最外层 FrameLayout 上增加 Overlay。
     * 不改 QQ NT 原页面布局，因此 RecyclerView、Compose、自定义 View 均不会被挤乱。
     */
    private void addNtOverlay(Activity activity) {
        activity.runOnUiThread(() -> {
            try {
                View decor = activity.getWindow().getDecorView();
                if (!(decor instanceof ViewGroup)) return;
                ViewGroup root = (ViewGroup) decor;
                if (root.findViewWithTag(VIEW_TAG) != null) return;

                FrameLayout overlay = new FrameLayout(activity);
                overlay.setTag(VIEW_TAG);
                overlay.setClickable(false);
                overlay.setFocusable(false);

                String current = loadSsaid(activity.getContentResolver());
                if (current != null && !current.isEmpty()) lastSsaid = current;

                TextView tv = new TextView(activity);
                tv.setText("SSAID：" + safeSsaid());
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                tv.setTextColor(Color.WHITE);
                tv.setGravity(Gravity.CENTER);
                tv.setPadding(dp(activity, 14), dp(activity, 7), dp(activity, 14), dp(activity, 7));
                tv.setBackgroundColor(Color.argb(215, 30, 30, 30));

                FrameLayout.LayoutParams textLp = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                textLp.bottomMargin = dp(activity, 28);
                overlay.addView(tv, textLp);

                ViewGroup.LayoutParams overlayLp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                root.addView(overlay, overlayLp);
                log(Log.INFO, TAG, "QQ NT SSAID overlay added");
            } catch (Throwable t) {
                log(Log.ERROR, TAG, "Failed to add QQ NT overlay", t);
            }
        });
    }

    private void removeNtOverlay(Activity activity) {
        activity.runOnUiThread(() -> {
            try {
                View decor = activity.getWindow().getDecorView();
                if (!(decor instanceof ViewGroup)) return;
                View v = decor.findViewWithTag(VIEW_TAG);
                if (v != null && v.getParent() instanceof ViewGroup) {
                    ((ViewGroup) v.getParent()).removeView(v);
                }
            } catch (Throwable ignored) { }
        });
    }

    private String safeSsaid() {
        String id = lastSsaid;
        return id == null || id.isEmpty() ? "未配置" : id;
    }

    private String loadSsaid(ContentResolver resolver) {
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://com.example.ssaidhookQQ.config/config");
            cursor = resolver.query(uri, new String[]{"android_id"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("android_id");
                if (index >= 0 && !cursor.isNull(index)) {
                    String id = cursor.getString(index);
                    if (id != null) id = id.trim();
                    if (id != null && !id.isEmpty()) return id;
                }
            }
        } catch (Throwable t) {
            log(Log.WARN, TAG, "ConfigProvider read failed: " + t.getMessage());
        } finally {
            if (cursor != null) try { cursor.close(); } catch (Throwable ignored) { }
        }
        return null;
    }

    private int dp(Activity activity, int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value,
                activity.getResources().getDisplayMetrics());
    }
}
