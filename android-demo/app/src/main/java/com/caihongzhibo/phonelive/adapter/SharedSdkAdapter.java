package com.caihongzhibo.phonelive.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caihongzhibo.phonelive.AppContext;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.bean.SharedSdkBean;
import com.caihongzhibo.phonelive.custom.CheckedImageView;
import com.caihongzhibo.phonelive.interfaces.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2017/9/28.
 */

public class SharedSdkAdapter extends RecyclerView.Adapter<SharedSdkAdapter.Vh> {

    private List<SharedSdkBean> mList;
    private LayoutInflater mInflater;
    private boolean mShowTitle;
    private boolean mShowChecked;
    private OnItemClickListener<SharedSdkBean> mOnItemClickListener;
    private int mCurPosition = -1;
    private String mType;

    public SharedSdkAdapter(String[] types, boolean showTitle,boolean showChecked) {
        mList = new ArrayList<>();
        for (String type : types) {
            mList.add(SharedSdkBean.create(type));
        }
        mInflater = LayoutInflater.from(AppContext.sInstance);
        mShowTitle = showTitle;
        mShowChecked=showChecked;
    }

    public void setOnItemClickListener(OnItemClickListener<SharedSdkBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public String getType() {
        return mType;
    }

    @Override
    public Vh onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_list_shared_sdk, parent, false));
    }

    @Override
    public void onBindViewHolder(Vh vh, int position) {
        vh.setData(mList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {
        CheckedImageView icon;
        TextView title;
        SharedSdkBean mBean;
        int mPosition;

        public Vh(View itemView) {
            super(itemView);
            icon = (CheckedImageView) itemView.findViewById(R.id.icon);
            title = (TextView) itemView.findViewById(R.id.title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mShowChecked){
                        if (mCurPosition == mPosition) {
                            mCurPosition = -1;
                            mType = null;
                            mBean.setChecked(false);
                            notifyItemChanged(mPosition);
                            return;
                        }
                        if (mCurPosition != -1) {
                            mList.get(mCurPosition).setChecked(false);
                            notifyItemChanged(mCurPosition);
                        }
                        mBean.setChecked(true);
                        notifyItemChanged(mPosition);
                        mType=mBean.getType();
                        mCurPosition = mPosition;
                    }
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mBean, mPosition);
                    }
                }
            });
        }

        void setData(SharedSdkBean bean, int position) {
            mBean = bean;
            mPosition = position;
            icon.setImageResource(bean.getDrawable(), bean.isChecked());
            if (mShowTitle) {
                title.setText(bean.getTitle());
            } else {
                if (title.getVisibility() == View.VISIBLE) {
                    title.setVisibility(View.GONE);
                }
            }
        }
    }
}
