package com.ping.android.presentation.view.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ping.android.R;

public class CustomSwitch extends LinearLayout {

    private String mSwitchLeftText;
    private String mSwitchRightText;
    private SwitchToggleListener mSwitchToggleListener;
    private SwitchToggleState mSwitchToggleState = SwitchToggleState.LEFT;

    private OnClickListener buttonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();

            switch (id) {
                case R.id.leftButton:
                    mSwitchToggleState = SwitchToggleState.LEFT;
                    break;
                case R.id.rightButton:
                    mSwitchToggleState = SwitchToggleState.RIGHT;
                    break;
            }

            toggleSwitch();
        }
    };

    public CustomSwitch(Context context) {
        super(context);
        initialize();
        toggleSwitch();
        invalidate();
    }

    public CustomSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();

        TypedArray styleAttrs = getContext().obtainStyledAttributes(
                attrs, R.styleable.MyCustomSwitch);

        String leftSwitch = styleAttrs.getString(R.styleable.MyCustomSwitch_leftSwitchText);
        String rightSwitch = styleAttrs.getString(R.styleable.MyCustomSwitch_rightSwitchText);
        setSwitches(leftSwitch, rightSwitch);
        toggleSwitch();
        invalidate();

        styleAttrs.recycle();
    }

    public void setSwitchToggleListener(SwitchToggleListener switchToggleListener) {
        mSwitchToggleListener = switchToggleListener;
    }

    public void setSwitches(String switchLeftText, String switchRightText) {

        mSwitchLeftText = switchLeftText;
        mSwitchRightText = switchRightText;

        if (switchLeftText != null && !switchLeftText.isEmpty())
            mSwitchLeftText = switchLeftText;
        else
            throw new IllegalArgumentException("switchLeftText cannot be empty");

        if (switchRightText != null && !switchRightText.isEmpty())
            mSwitchRightText = switchRightText;
        else
            throw new IllegalArgumentException("switchRightText cannot be empty");

        buildSwitch();
    }

    private void initialize() {
        setOrientation(LinearLayout.HORIZONTAL);
        setWeightSum(2);
    }

    private void buildSwitch() {
        buildLeftButton();
        buildRightButton();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void buildLeftButton() {
        TextView leftButton = new TextView(getContext());
        leftButton.setOnClickListener(buttonClickListener);
        leftButton.setLayoutParams(getButtonLayoutParams());
        leftButton.setId(R.id.leftButton);
        leftButton.setGravity(Gravity.CENTER);
        leftButton.setText(mSwitchLeftText);
        leftButton.setTextColor(getResources().getColor(R.color.white));
        leftButton.setTransformationMethod(null);
        leftButton.setBackground(getContext().getDrawable(R.drawable.switch_left_enabled));
        addView(leftButton);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void buildRightButton() {
        TextView rightButton = new TextView(getContext());
        rightButton.setOnClickListener(buttonClickListener);
        rightButton.setLayoutParams(getButtonLayoutParams());
        rightButton.setId(R.id.rightButton);
        rightButton.setGravity(Gravity.CENTER);
        rightButton.setText(mSwitchRightText);
        rightButton.setTextColor(getResources().getColor(R.color.orange_dark));
        rightButton.setTransformationMethod(null);
        rightButton.setBackground(getContext().getDrawable(R.drawable.switch_right_disabled));
        addView(rightButton);
    }

    private LayoutParams getButtonLayoutParams() {
        LayoutParams layoutParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        return layoutParams;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void toggleSwitch() {
        TextView leftButton = findViewById(R.id.leftButton);
        TextView rightButton = findViewById(R.id.rightButton);

        if (mSwitchToggleState == SwitchToggleState.LEFT) {
            leftButton.setTextColor(getResources().getColor(R.color.white));
            rightButton.setTextColor(getResources().getColor(R.color.white));
            leftButton.setBackground(getContext().getDrawable(R.drawable.switch_left_enabled));
            rightButton.setBackground(getContext().getDrawable(R.drawable.switch_right_disabled));
        } else {
            leftButton.setTextColor(getResources().getColor(R.color.white));
            rightButton.setTextColor(getResources().getColor(R.color.white));
            leftButton.setBackground(getContext().getDrawable(R.drawable.switch_left_disabled));
            rightButton.setBackground(getContext().getDrawable(R.drawable.switch_right_enabled));
        }

        if (mSwitchToggleListener != null)
            mSwitchToggleListener.onSwitchToggle(mSwitchToggleState);
    }

    public enum SwitchToggleState {
        LEFT, RIGHT
    }

    public interface SwitchToggleListener {
        void onSwitchToggle(SwitchToggleState switchToggleState);
    }
}
