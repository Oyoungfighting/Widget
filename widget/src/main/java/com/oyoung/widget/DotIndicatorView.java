package com.oyoung.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author OyoungZh
 * @brief 配合ViewPager一起使用的圆点指示器
 * @date 2023-06-08
 */
public class DotIndicatorView extends View {
    private int mUnselectColor;
    private int mSelectColor;
    private int mMargin;
    private int mRadius;
    private int mCurItemPosition;
    private float mCurItemPositionOffset;
    private ShapeHolder moveItem;
    private List<ShapeHolder> mTabItems;
    private Mode mMode;
    private Gravity mGravity;

    private ViewPager mViewPager;


    /**
     * View的Default值
     */
    public static class DefaultConst {
        private static final int DEFAULT_RADIUS = 10;
        private static final int DEFAULT_MARGIN = 40;
        private static final int DEFAULT_UNSELECT_COLOR = Color.WHITE;
        private static final int DEFAULT_SELECT_COLOR = Color.BLACK;
        private static final int DEFAULT_INDICATOR_MODE = Mode.SOLO.ordinal();
        private static final int DEFAULT_INDICATOR_LAYOUT_GRAVITY = Gravity.CENTER.ordinal();
    }
    public enum Gravity {
        LEFT, CENTER, RIGHT;
    }

    public enum Mode {
        INSIDE, OUTSIDE, SOLO;
    }




    public DotIndicatorView(Context context) {
        this(context, null);
    }

    public DotIndicatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DotIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化View
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        mTabItems = new ArrayList<>();
        handleTypeArrays(context, attrs);
    }

    /**
     * 获取布局中设置的属性值
     * @param context
     * @param attrs
     */
    private void handleTypeArrays(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DotIndicatorView);
        mUnselectColor = array.getColor(R.styleable.DotIndicatorView_indicator_unselect_color, DefaultConst.DEFAULT_UNSELECT_COLOR);
        mSelectColor = array.getColor(R.styleable.DotIndicatorView_indicator_select_color, DefaultConst.DEFAULT_SELECT_COLOR);
        mMargin = (int) array.getDimension(R.styleable.DotIndicatorView_indicator_margin, DefaultConst.DEFAULT_MARGIN);
        mRadius = (int) array.getDimension(R.styleable.DotIndicatorView_indicator_radius, DefaultConst.DEFAULT_RADIUS);
        int layoutGravity = array.getInt(R.styleable.DotIndicatorView_indicator_gravity, DefaultConst.DEFAULT_INDICATOR_LAYOUT_GRAVITY);
        mGravity = Gravity.values()[layoutGravity];
        int layoutMode = array.getInt(R.styleable.DotIndicatorView_indicator_mode, DefaultConst.DEFAULT_INDICATOR_MODE);
        mMode = Mode.values()[layoutMode];
        array.recycle();
    }


    public void setViewPager(ViewPager viewPager) {
        if (null != mViewPager) {
            mViewPager = null;
            mTabItems.clear();
            moveItem = null;
            mCurItemPosition = 0;
            mCurItemPositionOffset = 0;
            requestLayout();
            invalidate();
        }
        mViewPager = viewPager;
        createTabItems();
        createMoveItems();
        setViewPagerListener();
    }

    private void setViewPagerListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mMode != Mode.SOLO) {
                    trigger(position, positionOffset);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (mMode == Mode.SOLO) {
                    trigger(position, 0);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void trigger(int position, float positionOffset) {
        this.mCurItemPosition = position;
        this.mCurItemPositionOffset = positionOffset;
        requestLayout();
        invalidate();
    }

    /**
     * 创建移动小圆点
     */
    private void createMoveItems() {
        OvalShape circle = new OvalShape();
        ShapeDrawable drawable = new ShapeDrawable(circle);
        moveItem = new ShapeHolder(drawable);

        Paint paint = drawable.getPaint();
        paint.setColor(mSelectColor);
        paint.setAntiAlias(true);

        switch (mMode) {
            case INSIDE:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                break;
            case OUTSIDE:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                break;
            case SOLO:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                break;
            default:
                break;
        }

        moveItem.setPaint(paint);
    }

    /**
     * 创建小圆点个数, 依赖ViewPager
     */
    private void createTabItems() {
        for (int i = 0; i < Objects.requireNonNull(mViewPager.getAdapter()).getCount(); i++) {
            OvalShape circle = new OvalShape();
            ShapeDrawable drawable = new ShapeDrawable(circle);
            ShapeHolder holder = new ShapeHolder(drawable);

            Paint paint = drawable.getPaint();
            paint.setColor(mUnselectColor);
            paint.setAntiAlias(true);

            holder.setPaint(paint);
            mTabItems.add(holder);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = getWidth();
        int height = getHeight();

        layoutItem(width, height);
        layoutMoveItem(mCurItemPosition, mCurItemPositionOffset);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int layer = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        for (ShapeHolder holder : mTabItems) {
            drawItem(canvas, holder);
        }
        if (null != moveItem) {
            drawItem(canvas, moveItem);
        }
        canvas.restoreToCount(layer);
    }

    /**
     * 开始绘画圆点
     *
     * @param canvas
     * @param holder
     */
    private void drawItem(@NonNull Canvas canvas, @NonNull ShapeHolder holder) {
        canvas.save();
        canvas.translate(holder.getX(), holder.getY());
        holder.getShape().draw(canvas);
        canvas.restore();
    }

    /**
     * 设置移动小圆点位置
     */
    private void layoutMoveItem(int curItemPosition, float curItemPositionOffset) {
        if (null == moveItem) {
            throw new IllegalArgumentException("forget to create moveitem?");
        }

        if (0 == mTabItems.size()) {
            return;
        }

        ShapeHolder holder = mTabItems.get(curItemPosition);
        moveItem.resizeShape(holder.getWidth(), holder.getHeight());

        float x = holder.getX() + (mMargin + mRadius * 2) * curItemPositionOffset;
        moveItem.setX(x);
        moveItem.setY(holder.getY());
    }

    /**
     * 计算每个小圆点位置
     * @param width
     * @param height
     */
    private void layoutItem(int width, int height) {
        if (null == mTabItems) {
            throw new IllegalArgumentException("forget to create items?");
        }
        float heightY = height * 0.5f;
        int startPosition = startDrawPosition(width);

        for (int i = 0; i < mTabItems.size(); i++) {
            ShapeHolder holder = mTabItems.get(i);
            holder.resizeShape(2 * mRadius, 2 * mRadius);
            holder.setY(heightY - mRadius);

            int x = startPosition + (2 * mRadius + mMargin) * i;
            holder.setX(x);
        }
    }

    /**
     * 设置小圆点起始位置
     *
     * @param width
     */
    private int startDrawPosition(int width) {
        if (mGravity == Gravity.LEFT) {
            return 0;
        }

        int tabItemLength = mTabItems.size() * (2 * mRadius + mMargin) - mMargin;
        if (width < tabItemLength) {
            return 0;
        }

        if (mGravity == Gravity.CENTER) {
            return (width - tabItemLength) / 2;
        }

        return width - tabItemLength;
    }

    /**
     * 暴露接口，可代码修改参数
     */
    public void setIndicatorMode(Mode indicatorMode) {
        this.mMode = indicatorMode;
    }

    public void setIndicatorMargin(int indicatorMargin) {
        this.mMargin = indicatorMargin;
    }

    public void setIndicatorRadius(int indicatorRadius) {
        this.mRadius = indicatorRadius;
    }

    public void setIndicatorBackground(int indicatorBackground) {
        this.mUnselectColor = indicatorBackground;
    }

    public void setIndicatorLayoutGravity(Gravity indicatorLayoutGravity) {
        this.mGravity = indicatorLayoutGravity;
    }

    public void setIndicatorSelectBackground(int indicatorSelectBackground) {
        this.mSelectColor = indicatorSelectBackground;
    }

    public static class ShapeHolder {
        private float x = 0, y = 0;
        private ShapeDrawable shape;
        private int color;
        private Paint paint;
        private float alpha = 1f;

        public void setPaint(Paint value) {
            paint = value;
        }

        public Paint getPaint() {
            return paint;
        }

        public void setX(float value) {
            x = value;
        }

        public float getX() {
            return x;
        }

        public void setY(float value) {
            y = value;
        }

        public float getY() {
            return y;
        }

        public void setShape(ShapeDrawable value) {
            shape = value;
        }

        public ShapeDrawable getShape() {
            return shape;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int value) {
            shape.getPaint().setColor(value);
            color = value;

        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
            shape.setAlpha((int) ((alpha * 255f) + .5f));
        }

        public float getWidth() {
            return shape.getShape().getWidth();
        }

        public void setWidth(float width) {
            Shape s = shape.getShape();
            s.resize(width, s.getHeight());
        }

        public float getHeight() {
            return shape.getShape().getHeight();
        }

        public void setHeight(float height) {
            Shape s = shape.getShape();
            s.resize(s.getWidth(), height);
        }

        public void resizeShape(final float width, final float height) {
            shape.getShape().resize(width, height);
        }

        public ShapeHolder(ShapeDrawable s) {
            shape = s;
        }
    }
}
