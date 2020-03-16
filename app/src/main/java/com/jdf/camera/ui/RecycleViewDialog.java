package com.jdf.camera.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jdf.camera.R;
import com.jdf.camera.util.GPUImageFilterTools;
import com.jdf.common.utils.JLog;
import com.jdf.common.widget.ImageRecyclerView;
import com.jdf.common.widget.recycleview.EffectData;
import com.jdf.common.widget.recycleview.FilterList;
import com.jdf.common.widget.recycleview.RecycleViewAdapter;
import com.jdf.common.widget.recycleview.RecycleViewScrollListener;
import com.jdf.common.widget.recycleview.OnChildAttachStateChangeListener;
import com.jdf.common.widget.recycleview.RecycleViewUtil;
import com.jdf.gpufilter.GPUImage;
import com.qiku.android.app.QKAlertDialog;


public class RecycleViewDialog {

    public TextView mShowEffectTextView;
    RecycleViewAdapter adapter;
    ImageRecyclerView recyclerView;
    GPUImageFilterTools.OnGpuImageFilterChosenListener mListener;
    private Context mContext;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RecycleViewUtil.MSG_EFFECT_SWITCH_WITH:
                    EffectData data = (EffectData) msg.obj;
                    mShowEffectTextView.setText(data.effectName);
                    RecycleViewUtil.lastEffectName = data.effectName;
                    if (data.filterType != null)
                        mListener.onGpuImageFilterChosenListener(
                                GPUImageFilterTools.createFilterForType(mContext, data.filterType), data.effectName);
                    break;

                case RecycleViewUtil.MSG_EFFECT_SWITCH_WITH_NONE:
                    adapter.switchToChild(recyclerView.getChildAt(0), false);
                    break;

            }
        }
    };


    public void showDialog(Activity context, FilterList filters, GPUImageFilterTools.OnGpuImageFilterChosenListener listener) {
        JLog.d(JLog.TAG_FILTER, "showDialog:" + "---" + filters.filters.size());
        mListener = listener;
        mContext = context;
        View view = context.findViewById(R.id.effect_layout);
        recyclerView = (ImageRecyclerView) view.findViewById(R.id.image_recyclerView);
        mShowEffectTextView = (TextView) view.findViewById(R.id.effect_name);
        adapter = new RecycleViewAdapter(context, recyclerView, filters, mHandler);
        recyclerView.addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                int recycleViewW = recyclerView.getMeasuredWidth();
                int itemWidth = recyclerView.getChildAt(0).getWidth();
                int paddingEdge = (recycleViewW >> 1) - (itemWidth >> 1);
                //make recycleView first and last chlild can move to center location.
                JLog.d(JLog.TAG_FILTER, "setPadding:" + "---" + recycleViewW + "---" + itemWidth);
                //保证reclerView可以拖动到中间位置
                recyclerView.setPadding(paddingEdge, 0, paddingEdge, 0);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(new RecycleViewScrollListener(context, mHandler));
        view.setVisibility(View.VISIBLE);
        //默认第一个为正常滤镜，将第一个滤镜效果居中
        mHandler.sendEmptyMessage(RecycleViewUtil.MSG_EFFECT_SWITCH_WITH_NONE);

    }


}
