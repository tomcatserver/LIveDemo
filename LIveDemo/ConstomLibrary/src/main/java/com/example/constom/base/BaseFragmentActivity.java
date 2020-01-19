package com.example.constom.base;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.constom.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

public abstract class BaseFragmentActivity extends AppCompatActivity {
    protected Activity context;
    protected HeaderView mHeadView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(getContentView());
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT > LOLLIPOP) {
            if (Build.VERSION.SDK_INT > M) {
                changeLightStatusBar(window);
            } else {
                window.clearFlags(FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#121314"));
            }
        }
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        context = this;
        mHeadView = findViewById(R.id.header_view);
        if (mHeadView != null) {
            mHeadView.setTitle(getTitles());
        }
        findView();
        initData();
        setListener();
    }

    protected abstract void setListener();

    protected abstract void initData();

    protected abstract void findView();

    protected abstract CharSequence getTitles();

    private void changeLightStatusBar(Window window) {
        window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (DeviceUtil.checkIsMiuiRom()) {
            StatusBarUtil.MIUISetStatusBarLightMode(window, true);
        }
    }

    protected abstract int getContentView();

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (outState != null) {
            outState.putParcelable("android:support:fragments", outPersistentState);
        }
    }
}
