package com.caihongzhibo.phonelive.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.bean.UserBean;
import com.caihongzhibo.phonelive.bean.UserFunctionBean;
import com.caihongzhibo.phonelive.glide.ImgLoader;
import com.caihongzhibo.phonelive.interfaces.OnItemClickListener;
import com.caihongzhibo.phonelive.utils.IconUitl;
import com.caihongzhibo.phonelive.utils.WordUtil;

import java.util.List;

/**
 * Created by cxf on 2017/8/28.
 */

public class UserFunctionAdapter extends RecyclerView.Adapter {

    private static final int HEAD = 0;
    private static final int NORMAL = 1;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<UserFunctionBean> mList;
    private UserBean mUserBean;
    private View.OnClickListener mOnClickListener;
    private OnItemClickListener<UserFunctionBean> mOnItemClickListener;
    private HeadVh mHeadVh;

    public UserFunctionAdapter(Context context, UserBean userBean, List<UserFunctionBean> list) {
        mContext = context;
        mList = list;
        mUserBean = userBean;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setOnItemClickListener(OnItemClickListener<UserFunctionBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setData(UserBean userBean, List<UserFunctionBean> list) {
        mUserBean = userBean;
        mList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEAD;
        }
        return NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEAD) {
            if (mHeadVh == null) {
                mHeadVh = new HeadVh(mInflater.inflate(R.layout.view_list_head_user, parent, false));
            }
            return mHeadVh;
        } else {
            return new Vh(mInflater.inflate(R.layout.item_list_user_function, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof Vh) {
            ((Vh) holder).setData(mList.get(position - 1), position - 1);
        } else if (holder instanceof HeadVh) {
            ((HeadVh) holder).setData();
        }
    }


    @Override
    public int getItemCount() {
        return mList.size() + 1;
    }


    class HeadVh extends RecyclerView.ViewHolder {

        ImageView mHeadImg;
        TextView mName;
        TextView mId;
        ImageView mSex;
        ImageView mAnchorLevel;
        ImageView mUserLevel;
        TextView mLive;
        TextView mAttention;
        TextView mFans;

        public HeadVh(View itemView) {
            super(itemView);
            mHeadImg = (ImageView) itemView.findViewById(R.id.headImg);
            mName = (TextView) itemView.findViewById(R.id.name);
            mId = (TextView) itemView.findViewById(R.id.id_value);
            mSex = (ImageView) itemView.findViewById(R.id.sex);
            mAnchorLevel = (ImageView) itemView.findViewById(R.id.anchor_level);
            mUserLevel = (ImageView) itemView.findViewById(R.id.user_level);
            mLive = (TextView) itemView.findViewById(R.id.btn_live);
            mAttention = (TextView) itemView.findViewById(R.id.btn_attention);
            mFans = (TextView) itemView.findViewById(R.id.btn_fans);
            itemView.findViewById(R.id.btn_edit).setOnClickListener(mOnClickListener);
            mLive.setOnClickListener(mOnClickListener);
            mAttention.setOnClickListener(mOnClickListener);
            mFans.setOnClickListener(mOnClickListener);
        }

        void setData() {
            ImgLoader.displayCircle(mUserBean.getAvatar(), mHeadImg);
            mName.setText(mUserBean.getUser_nicename());
            mSex.setImageResource(IconUitl.getSexDrawable(mUserBean.getSex()));
            mAnchorLevel.setImageResource(IconUitl.getAnchorDrawable(mUserBean.getLevel_anchor()));
            mUserLevel.setImageResource(IconUitl.getAudienceDrawable(mUserBean.getLevel()));
            String liangNum = mUserBean.getLiang().getName();
            if (!"0".equals(liangNum)) {
                mId.setText(mContext.getString(R.string.liang) + ":" + liangNum);
            } else {
                mId.setText("ID:" + mUserBean.getId());
            }
            mLive.setText(Html.fromHtml(WordUtil.getString(R.string.live) + "&nbsp<font color='#ffd350'>" + mUserBean.getLives() + "</font>"));
            mAttention.setText(Html.fromHtml(WordUtil.getString(R.string.attention2) + "&nbsp<font color='#ffd350'>" + mUserBean.getFollows() + "</font>"));
            mFans.setText(Html.fromHtml(WordUtil.getString(R.string.fans) + "&nbsp<font color='#ffd350'>" + mUserBean.getFans() + "</font>"));
        }
    }

    class Vh extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        UserFunctionBean mBean;
        int mPosition;

        public Vh(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            name = (TextView) itemView.findViewById(R.id.name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mBean, mPosition);
                    }
                }
            });
        }

        void setData(UserFunctionBean bean, int position) {
            mBean = bean;
            mPosition = position;
            ImgLoader.display(bean.getThumb(), icon);
            name.setText(bean.getName());
        }
    }
}
