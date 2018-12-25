package com.caihongzhibo.phonelive2.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.activity.UserInfoActivity;
import com.caihongzhibo.phonelive2.bean.AttentionBean;
import com.caihongzhibo.phonelive2.glide.ImgLoader;
import com.caihongzhibo.phonelive2.http.HttpUtil;
import com.caihongzhibo.phonelive2.interfaces.CommonCallback;
import com.caihongzhibo.phonelive2.utils.IconUitl;

import java.util.List;

/**
 * Created by cxf on 2017/8/11.
 * 搜索，个人主页的关注、粉丝列表
 */

public class AttentionAdapter extends RecyclerView.Adapter<AttentionAdapter.Vh> {

    private Context mContext;
    private List<AttentionBean> mList;
    private LayoutInflater mInflater;
    private String mAttentionStr;
    private String mNoAttentionStr;
    private Drawable mAttentionTextBg;
    private Drawable mNoAttentionTextBg;
    public static final String FLAG = "flag";

    public AttentionAdapter(Context context, List<AttentionBean> list) {
        mContext = context;
        mList = list;
        mInflater = LayoutInflater.from(mContext);
        mAttentionStr = mContext.getString(R.string.attention);
        mNoAttentionStr = mContext.getString(R.string.attention2);
        mAttentionTextBg = ContextCompat.getDrawable(mContext, R.drawable.bg_btn_attention);
        mNoAttentionTextBg = ContextCompat.getDrawable(mContext, R.drawable.bg_btn_no_attention);
    }

    public void setList(List<AttentionBean> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    @Override
    public Vh onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_list_attention, parent, false));
    }

    @Override
    public void onBindViewHolder(Vh vh, final int position) {

    }

    @Override
    public void onBindViewHolder(Vh vh, int position, List<Object> payloads) {
        String flag = payloads.size() > 0 ? (String) payloads.get(0) : null;
        vh.setData(mList.get(position), position, flag);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {
        ImageView headImg;
        TextView name;
        ImageView sex;
        ImageView anchorLevel;
        ImageView level;
        TextView signature;
        TextView attention;
        AttentionBean mBean;
        int mPosition;

        public Vh(View itemView) {
            super(itemView);
            headImg = (ImageView) itemView.findViewById(R.id.headImg);
            name = (TextView) itemView.findViewById(R.id.name);
            sex = (ImageView) itemView.findViewById(R.id.sex);
            anchorLevel = (ImageView) itemView.findViewById(R.id.anchor_level);
            level = (ImageView) itemView.findViewById(R.id.user_level);
            signature = (TextView) itemView.findViewById(R.id.signature);
            attention = (TextView) itemView.findViewById(R.id.attention);
            attention.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HttpUtil.setAttention(mBean.getId(), new CommonCallback<Integer>() {
                        @Override
                        public void callback(Integer isAttention) {
                            mBean.setIsattention(isAttention);
                            notifyItemChanged(mPosition, FLAG);
                        }
                    });
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, UserInfoActivity.class);
                    intent.putExtra("touid", mBean.getId());
                    mContext.startActivity(intent);
                }
            });
        }

        void setData(AttentionBean bean, int position, String flag) {
            mBean = bean;
            mPosition = position;
            if (flag == null) {
                ImgLoader.displayCircle(bean.getAvatar(), headImg);
                name.setText(bean.getUser_nicename());
                anchorLevel.setImageResource(IconUitl.getAnchorDrawable(bean.getLevel_anchor()));
                level.setImageResource(IconUitl.getAudienceDrawable(bean.getLevel()));
                sex.setImageResource(IconUitl.getSexDrawable(bean.getSex()));
                signature.setText(bean.getSignature());
            }
            if (bean.getIsattention() == 1) {
                attention.setText(mAttentionStr);
                attention.setBackground(mAttentionTextBg);
                attention.setTextColor(0xffffffff);
            } else {
                attention.setText(mNoAttentionStr);
                attention.setBackground(mNoAttentionTextBg);
                attention.setTextColor(0xff333333);
            }
        }
    }
}
