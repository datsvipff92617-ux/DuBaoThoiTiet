package com.example.dubaothoitiet;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * BaseActivity: Cung cấp xử lý Edge-to-Edge và Safe Area cho toàn bộ 16 màn hình
 * Tự động thêm khoảng đệm phía trên (status bar, camera notch) và phía dưới (navigation bar)
 * giúp tất cả nút bấm, tiêu đề không bị che khuất hoặc đè lên tai thỏ/nốt ruồi.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        applyEdgeToEdgeInsets();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        applyEdgeToEdgeInsets();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        applyEdgeToEdgeInsets();
    }

    protected void applyEdgeToEdgeInsets() {
        ViewGroup content = findViewById(android.R.id.content);
        if (content != null && content.getChildCount() > 0) {
            View rootView = content.getChildAt(0);
            final int initialPaddingLeft = rootView.getPaddingLeft();
            final int initialPaddingTop = rootView.getPaddingTop();
            final int initialPaddingRight = rootView.getPaddingRight();
            final int initialPaddingBottom = rootView.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                );
                v.setPadding(
                        initialPaddingLeft + insets.left,
                        initialPaddingTop + insets.top,
                        initialPaddingRight + insets.right,
                        initialPaddingBottom + insets.bottom
                );
                return windowInsets;
            });
            ViewCompat.requestApplyInsets(rootView);
        }
    }
}
