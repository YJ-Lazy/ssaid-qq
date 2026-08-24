package com.example.ssaidhookQQ;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends Activity {
    private static final String QQ_PKG = "com.tencent.mobileqq";
    private TextView currentIdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF5F7FA);
        root.setPadding(dp(24), dp(40), dp(24), dp(24));
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("QQ SSAID 一键修改");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF1976D2);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("目标：com.tencent.mobileqq\n修改后请强制停止 QQ 再打开\n在「设置 → 关于QQ」可查看当前生效值");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        subtitle.setTextColor(0xFF757575);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(12), 0, dp(24));
        root.addView(subtitle);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.WHITE);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = dp(20);
        card.setLayoutParams(cardLp);

        TextView cardTitle = new TextView(this);
        cardTitle.setText("当前配置的 SSAID");
        cardTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        cardTitle.setTypeface(null, Typeface.BOLD);
        cardTitle.setTextColor(0xFF212121);
        card.addView(cardTitle);

        currentIdView = new TextView(this);
        currentIdView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        currentIdView.setTextColor(0xFF1976D2);
        currentIdView.setPadding(0, dp(8), 0, 0);
        currentIdView.setTextIsSelectable(true);
        card.addView(currentIdView);
        root.addView(card);

        Button oneClickBtn = makeButton("一键生成并应用随机 SSAID", 0xFF1976D2);
        oneClickBtn.setOnClickListener(v -> oneClickModify());
        root.addView(oneClickBtn);

        Button forceStopBtn = makeButton("结束 QQ 后台进程", 0xFFFB8C00);
        LinearLayout.LayoutParams forceLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        forceLp.topMargin = dp(12);
        forceStopBtn.setLayoutParams(forceLp);
        forceStopBtn.setOnClickListener(v -> forceStopQQ());
        root.addView(forceStopBtn);

        TextView tip = new TextView(this);
        tip.setText("使用步骤：\n1. 在 LSPosed 中启用本模块\n2. 作用域只勾选 QQ (com.tencent.mobileqq)\n3. 点击上方一键生成\n4. 结束 QQ 后台后重新打开\n5. 进入「设置 → 关于QQ」查看是否生效");
        tip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tip.setTextColor(0xFF757575);
        tip.setPadding(0, dp(24), 0, 0);
        root.addView(tip);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
        refreshCurrentId();
    }

    private void oneClickModify() {
        String newId = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toLowerCase();
        getSharedPreferences(App.PREF_NAME, MODE_PRIVATE).edit().putString(App.KEY_ID, newId).apply();
        refreshCurrentId();
        Toast.makeText(this, "已生成新 SSAID：\n" + newId + "\n请结束 QQ 后台后重启生效", Toast.LENGTH_LONG).show();
    }

    private void refreshCurrentId() {
        SharedPreferences prefs = getSharedPreferences(App.PREF_NAME, MODE_PRIVATE);
        String id = prefs.getString(App.KEY_ID, null);
        currentIdView.setText(id == null || id.isEmpty() ? "（尚未设置，点击下方按钮生成）" : id);
    }

    private void forceStopQQ() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            am.killBackgroundProcesses(QQ_PKG);
            Toast.makeText(this, "已尝试结束 QQ 后台进程\n请重新打开 QQ", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "结束失败，请手动在系统设置中强停 QQ", Toast.LENGTH_SHORT).show();
        }
    }

    private Button makeButton(String text, int bgColor) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(bgColor);
        btn.setAllCaps(false);
        btn.setPadding(dp(16), dp(14), dp(16), dp(14));
        return btn;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
