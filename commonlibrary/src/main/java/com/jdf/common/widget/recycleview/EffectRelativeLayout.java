package com.jdf.common.widget.recycleview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.jdf.common.R;
import com.jdf.common.utils.JLog;

public class EffectRelativeLayout extends RelativeLayout {
    public EffectRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        JLog.d("jiadongfeng","onTouchEvent:"+event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                View view = findViewById(R.id.effect_layout);
                if (view.getVisibility() == View.VISIBLE) {
                    view.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                break;
        }
        return true;

    }
}
