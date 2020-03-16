package com.jdf.common.widget.recycleview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.jdf.common.R;
import com.jdf.common.utils.JLog;
import com.jdf.common.widget.ImageRecyclerView;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> implements View.OnClickListener {
    private final LayoutInflater layoutInflater;
    private int[] effect_imageRes;
    private FilterList filterList;
    private ValueAnimator mScrollAnim;
    private RecyclerView mRecyclerView;
    private Handler mHandler;

    private boolean switchWithAnim = true;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public String effectName;
        public FilterType filterType;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public RecycleViewAdapter(Context context, ImageRecyclerView recyclerView, FilterList filterNames, Handler handler) {
        effect_imageRes = null;
        this.layoutInflater = LayoutInflater.from(context);
        filterList = filterNames;
        mRecyclerView = recyclerView;
        mHandler = handler;

    }

    int i = 0;

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.effect_recycleview_item, null);
        convertView.setOnClickListener(this);
        ViewHolder viewHolder = new ViewHolder(convertView);
        viewHolder.image = convertView.findViewById(R.id.effect_list_image);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecycleViewAdapter.ViewHolder holder, int position) {
        //holder.image.setImageResource(effect_imageRes[position]);
        holder.effectName = filterList.names.get(position);
        holder.filterType = filterList.filters.get(position);
        JLog.d(JLog.TAG_FILTER, "onBindViewHolder:" + filterList.names.get(position));
    }


    public int getItemCount() {

        return filterList.names.size();
    }

    public void onClick(View v) {
        JLog.d(JLog.TAG_FILTER, "onClick:" + v);
        switchToChild(v);
    }

    public void switchToChild(View view) {
        switchToChild(view, true);
    }

    public void switchToChild(View view, boolean withAnimation) {
        switchWithAnim = withAnimation;
        switchToChild(mRecyclerView.getChildAdapterPosition(view), view);
    }


    /**
     * 滤镜效果切换到position位置对应的效果
     *
     * @param position
     */
    private void switchToChild(int position, View childAt) {
        JLog.d(JLog.TAG_FILTER, "switchToChild cur:" + position + " last:" + RecycleViewUtil.lastEffectName);

        if (this.mScrollAnim != null
                && this.mScrollAnim.isRunning()) {
            return;
        }
        ViewHolder viewHolder = (ViewHolder) mRecyclerView.getChildViewHolder(childAt);

        if (!viewHolder.effectName.equals(RecycleViewUtil.lastEffectName)) {
            if (switchWithAnim) {
                float childX = childAt.getX();
                //子布局宽度，也就是每个图片的宽度
                int childW = mRecyclerView.getChildAt(0).getWidth();
                //recyclerView的居中x位置
                int recyclerViewCenterX = (mRecyclerView.getWidth() >> 1);
                //中间选中框的x开始位置和结束位置
                int recycleViewCenterXStart = recyclerViewCenterX - (childW >> 1);
                int scrollX = (int) (childX - recycleViewCenterXStart);
                JLog.d(JLog.TAG_FILTER, "scrollX:" + scrollX);

                scrollByAnim(scrollX);
            } else {
                mRecyclerView.scrollToPosition(position);
            }
            String name = ((ViewHolder) mRecyclerView.getChildViewHolder(childAt)).effectName;

            switchEffect(viewHolder.filterType, name, position);

        }
    }


    private void switchEffect(FilterType filterType, String name, int postion) {
        if (name.equals(RecycleViewUtil.lastEffectName)) {
            return;
        }
        JLog.d(JLog.TAG_FILTER, "change effect to adapter:" + "---" + name);
        Message message = new Message();
        message.obj = new EffectData(filterType, name, postion);
        message.what = RecycleViewUtil.MSG_EFFECT_SWITCH_WITH;
        mHandler.sendMessage(message);

    }

    private void scrollByAnim(int scrollX) {
        if (this.mScrollAnim != null && this.mScrollAnim.isRunning()) {
            this.mScrollAnim.cancel();
        }
        this.mScrollAnim = ValueAnimator.ofInt(new int[]{0, scrollX});
        this.mScrollAnim.setDuration(500);
        this.mScrollAnim.setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.0f, 1.0f));
        this.mScrollAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int lastValue = 0;

            public void onAnimationUpdate(ValueAnimator animation) {
                int value = ((Integer) animation.getAnimatedValue()).intValue();
                JLog.d(JLog.TAG_FILTER, "scrollBy:" + (value - this.lastValue));

                mRecyclerView.scrollBy(value - this.lastValue, 0);
                mRecyclerView.invalidate();
                this.lastValue = value;
            }
        });
        this.mScrollAnim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                RecycleViewUtil.isClickSroll = true;
            }

            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                RecycleViewUtil.isClickSroll = false;
            }

            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                RecycleViewUtil.isClickSroll = false;
            }
        });
        this.mScrollAnim.start();
    }
}