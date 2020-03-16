package com.jdf.common.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ImageRecyclerView extends RecyclerView {
    private OnFlingListener mOnFlingListener;

    public interface OnFlingListener {
        boolean onFlingIntercept(int i);
    }

    public ImageRecyclerView(Context context) {
        super(context);
    }

    public ImageRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    public ImageRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean fling(int velocityX, int velocityY) {
        return super.fling((int) (((float) velocityX) * 0.8f), velocityY);
    }

    public void setOnFlingListener(OnFlingListener inf) {
        this.mOnFlingListener = inf;
    }

    public void setAdapter(@Nullable Adapter adapter){
        super.setAdapter(adapter);
    }
}
