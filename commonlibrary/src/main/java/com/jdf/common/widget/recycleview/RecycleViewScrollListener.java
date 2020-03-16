package com.jdf.common.widget.recycleview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.PathInterpolator;

import androidx.recyclerview.widget.RecyclerView;

import com.jdf.common.utils.JLog;


public class RecycleViewScrollListener extends RecyclerView.OnScrollListener {
    boolean firstScroll = true;
    boolean scrollDown;

    private Handler mHandler;

    private boolean isMoveRight;
    private ValueAnimator mScrollAnim;



    public RecycleViewScrollListener(Context context, Handler handler) {
        mHandler = handler;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        JLog.d(JLog.TAG_FILTER, "onScrollStateChanged newState[%d]", newState);

        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                //子布局宽度，也就是每个图片的宽度
                int childW = recyclerView.getChildAt(0).getWidth();
                //recyclerView的居中x位置
                int recyclerViewCenterX = (recyclerView.getWidth() >> 1);
                //中间选中框的x开始位置和结束位置
                int recycleViewCenterXStart = recyclerViewCenterX - (childW >> 1);
                int recycleViewCenterXEnd = recyclerViewCenterX + (childW >> 1);

                int count = recyclerView.getChildCount();
                int i = 0;
                while (i < count) {

                    float x = recyclerView.getChildAt(i).getX();

                    if (isMoveRight) {
                        //当向右滑动时，如果当前子布局处于中间选择框的中，则向右移动到与选择框重合
                        float xRight = x + childW;
                        if (xRight > recycleViewCenterXStart && xRight <= recycleViewCenterXEnd) {
                            int scrollX = (int) -(childW - (xRight - recycleViewCenterXStart));
                            scrollByAnim(recyclerView, scrollX);
                            break;
                        } else {
                            i++;
                        }
                    } else {
                        float xLeft = x;
                        if (xLeft > recycleViewCenterXStart && xLeft <= recycleViewCenterXEnd) {
                            int scrollX = (int) (xLeft - recycleViewCenterXStart);
                            scrollByAnim(recyclerView, scrollX);
                            break;
                        } else {
                            i++;
                        }
                    }
                }
                return;
            default:
                return;
        }
    }


    /**
     * 滑动时调用
     * @param recyclerView
     * @param dx
     * @param dy
     */
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        JLog.d(JLog.TAG_FILTER, "onScrolled dx[%d],dy[%d]", dx, dy);
        super.onScrolled(recyclerView, dx, dy);
        if(RecycleViewUtil.isClickSroll){
            return ;
        }
        if (dx < 0) {
            isMoveRight = true;
        } else {
            isMoveRight = false;
        }
        //子布局宽度，也就是每个图片的宽度
        int childW = recyclerView.getChildAt(0).getWidth();

        //recyclerView的居中x位置
        int recyclerViewCenterX = (recyclerView.getWidth() >> 1);
        //中间选中框的x开始位置和结束位置
        int recycleViewCenterXStart = recyclerViewCenterX - (childW >> 1);
        int recycleViewCenterXEnd = recyclerViewCenterX + (childW >> 1);
        int count = recyclerView.getChildCount();

        float effect_switch_threshold = childW * 0.7f;//滑动的位置，超过当前子布局的70%，切换到当前滤镜效果
        for (int i = 0; i < count; i++) {
            View childAt = recyclerView.getChildAt(i);
            float x = childAt.getX();
            if (isMoveRight) {
                //当向右滑动时，如果当前子布局处于中间选择框的中，则向右移动到与选择框重合
                float xRight = x + childW;
                if (xRight >= (recycleViewCenterXStart + effect_switch_threshold) && xRight <= recycleViewCenterXEnd) {
                    switchEffect(recyclerView, i, childAt);
                }
            } else {
                float xLeft = x;
                if (xLeft >= recycleViewCenterXStart && xLeft <= (recycleViewCenterXEnd - effect_switch_threshold)) {
                    switchEffect(recyclerView, i, childAt);

                }
            }
        }
    }

    private void switchEffect(RecyclerView recyclerView, int i, View childAt) {
        RecycleViewAdapter.ViewHolder childViewHolder = (RecycleViewAdapter.ViewHolder) recyclerView.getChildViewHolder(childAt);
        String name = childViewHolder.effectName;
        if (name.equals(RecycleViewUtil.lastEffectName)) {
            return;
        }
        JLog.d(JLog.TAG_FILTER, "change effect to:" + i + "---" + name);
        Message message = new Message();
        message.obj = new EffectData(childViewHolder.filterType,name,i);
        message.what = RecycleViewUtil.MSG_EFFECT_SWITCH_WITH;
        mHandler.sendMessage(message);
    }

    private void scrollByAnim(View view, int scrollX) {
        if (this.mScrollAnim != null && this.mScrollAnim.isRunning()) {
            this.mScrollAnim.cancel();
        }
        final View animView = view;
        this.mScrollAnim = ValueAnimator.ofInt(new int[]{0, scrollX});
        this.mScrollAnim.setDuration(500);
        this.mScrollAnim.setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.0f, 1.0f));
        this.mScrollAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int lastValue = 0;

            public void onAnimationUpdate(ValueAnimator animation) {
                int value = ((Integer) animation.getAnimatedValue()).intValue();
                animView.scrollBy(value - this.lastValue, 0);
                this.lastValue = value;
            }
        });
        this.mScrollAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }
        });
        this.mScrollAnim.start();
    }

}