package com.example.constom.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.constom.R;

public class HeaderView extends RelativeLayout {
    TextView textView;

    public HeaderView(Context context) {
        super(context);
        initView();
    }


    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @SuppressLint("ResourceType")
    private void initView() {
        if (getId() <= 0) {
            setId(R.id.header_view);
        }
    }

    public void setTitle(CharSequence title) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        params.addRule(CENTER_HORIZONTAL, TRUE);
        if (textView == null) {
            textView = new TextView(getContext());
            addView(textView);
        }
        textView.setText(title);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(18);
        textView.setTextColor(Color.parseColor("#333333"));

    }

    public TextView getTextView() {
        return textView;
    }

    public void setLeftButton(String title, OnClickListener listener) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        params.addRule(ALIGN_PARENT_LEFT, TRUE);
        Button button = new Button(getContext());
        button.setId(R.id.left_btn);
        button.setLayoutParams(params);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(15);
        button.setOnClickListener(listener);
        button.setTextColor(Color.parseColor("#333333"));
        addView(button);

    }

    public void setRightButton(String title, OnClickListener listener) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        Button button = new Button(getContext());
        params.addRule(ALIGN_PARENT_RIGHT, TRUE);
        button.setId(R.id.right_btn);
        button.requestLayout();
        button.setText(title);
        button.setOnClickListener(listener);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundResource(0);
        button.setTextColor(Color.parseColor("#333333"));
        addView(button);
    }
}
