package com.oyoung.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import java.util.Objects;

/**
 * @author OyoungZh
 * @brief EditText with clear function
 * 1.has the flaw of screen adaptation(different screens has different resolution)
 * @date 2023-06-12
 */

public class ClearEditText extends AppCompatEditText {

    private Drawable iconDrawable;
    private static final int OFFSET = 20;
    private static final int HALF = 2;
    private static final int END_INDEX = 2;

    public ClearEditText(@NonNull Context context) {
        this(context, null);
    }

    public ClearEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化属性
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ClearEditText);
        try {
            int resourceId = array.getResourceId(R.styleable.ClearEditText_clear_icon, R.drawable.baseline_clear_24);
            iconDrawable = ContextCompat.getDrawable(context, resourceId);
        } finally {
            array.recycle();
        }
    }

    /**
     * The text of view is changing, trigger the method
     * 输入框的文本发生变化时候触发这个方法
     * @param text The text the TextView is displaying
     * @param start The offset of the start of the range of the text that was
     * modified
     * @param lengthBefore The length of the former text that has been replaced
     * @param lengthAfter The length of the replacement modified text
     */
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        triggerClearIcon();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        triggerClearIcon();
    }

    /**
     * When the view is touched, trigger the method
     * 当输入框被触摸的时候触发这个方法
     * @param event The motion event.
     * @return
     * SuppressLint("ClickableViewAccessibility")  Custom view ClearEditText overrides onTouchEvent but not performClick
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!Objects.isNull(event) && !Objects.isNull(iconDrawable)) {
            float eventX = event.getX(), eventY = event.getY();
            int viewWidth = getWidth(), viewHalfHeight = getHeight() / HALF;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (eventX > viewWidth - getIntrinsicWidth() - OFFSET
                        && eventX < viewWidth + OFFSET
                        && eventY > viewHalfHeight - getIntrinsicHalfHeight() - OFFSET
                        && eventY < viewHalfHeight + getIntrinsicHalfHeight() + OFFSET) {
                    Objects.requireNonNull(getText()).clear();
                }
            }
        }
        performClick();
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * 获取清除图标的宽度
     * @return 内在宽度
     */
    private int getIntrinsicWidth() {
        int intrinsicWidth = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intrinsicWidth = getTextCursorDrawable().getIntrinsicWidth();
        } else {
            if (!Objects.isNull(getCompoundDrawablesRelative()[END_INDEX])) {
                intrinsicWidth = getCompoundDrawablesRelative()[END_INDEX].getIntrinsicWidth();
            }
        }
        return intrinsicWidth;
    }

    /**
     * 获取清除图标的高度
     * @return 内在宽度
     */
    private int getIntrinsicHalfHeight() {
        int intrinsicHalfHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intrinsicHalfHeight = getTextCursorDrawable().getIntrinsicHeight() / HALF;
        } else {
            if (!Objects.isNull(getCompoundDrawablesRelative()[END_INDEX])) {
                intrinsicHalfHeight = getCompoundDrawablesRelative()[END_INDEX].getIntrinsicHeight() / HALF;
            }

        }
        return intrinsicHalfHeight;
    }

    /**
     * trigger the clear icon showing
     * 触发清除按钮Icon显示
     */
    private void triggerClearIcon() {
        String currentText = Objects.requireNonNull(getText()).toString();// 获取当前输入的text
        Drawable icon = isFocused() && !currentText.isEmpty() ? iconDrawable : null;// 设置icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null);// 设置icon位置
        } else {
            if (icon != null) {
                icon.setBounds(0, 0, iconDrawable.getIntrinsicWidth(), iconDrawable.getIntrinsicHeight());
            }
            setCompoundDrawablesRelative(null, null , icon, null);
        }
    }
}
