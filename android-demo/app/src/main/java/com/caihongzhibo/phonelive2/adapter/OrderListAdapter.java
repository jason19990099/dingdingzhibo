package com.caihongzhibo.phonelive2.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caihongzhibo.phonelive2.AppContext;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.bean.ListBean;
import com.caihongzhibo.phonelive2.fragment.OrderListFragment;
import com.caihongzhibo.phonelive2.glide.ImgLoader;
import com.caihongzhibo.phonelive2.interfaces.OnItemClickListener;
import com.caihongzhibo.phonelive2.utils.DpUtil;
import com.caihongzhibo.phonelive2.utils.IconUitl;
import com.caihongzhibo.phonelive2.utils.WordUtil;

import java.util.List;

/**
 * Created by cxf on 2018/2/2.
 */

public class OrderListAdapter extends RecyclerView.Adapter {

    private String mAttentionNot;
    private String mAttention;
    private static final int HEAD = 1;
    private static final int NORMAL = 2;
    private static final String FLAG = "flag";
    private List<ListBean> mList;
    private int mType;
    private LayoutInflater mInflater;
    private RecyclerView mRecyclerView;
    private int mLoadMoreHeight;
    private HeadVh mHeadVh;
    private OnItemClickListener<ListBean> mItemClickListener;

    public OrderListAdapter(List<ListBean> list, int type) {
        mList = list;
        mType = type;
        mInflater = LayoutInflater.from(AppContext.sInstance);
        mLoadMoreHeight = DpUtil.dp2px(50);
        mAttentionNot = WordUtil.getString(R.string.attention3);
        mAttention = WordUtil.getString(R.string.attention);
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
                mHeadVh = new HeadVh(mInflater.inflate(R.layout.item_list_order_1, parent, false));
            }
            return mHeadVh;
        } else {
            return new Vh(mInflater.inflate(R.layout.item_list_order_2, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position, List payloads) {
        String flag = payloads.size() > 0 ? (String) payloads.get(0) : null;
        if (vh instanceof HeadVh) {
            ((HeadVh) vh).setData(mList.get(position), position, flag);
        } else {
            ((Vh) vh).setData(mList.get(position), position, flag);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    public void insertList(List<ListBean> list) {
        int p = mList.size();
        mList.addAll(list);
        notifyItemRangeInserted(p, list.size());
        notifyItemRangeChanged(p, list.size());
        //mRecyclerView.scrollToPosition(p);
        mRecyclerView.scrollBy(0, mLoadMoreHeight);
    }


    public void setData(List<ListBean> list) {
        mList = list;
        notifyDataSetChanged();
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }


    class HeadVh extends RecyclerView.ViewHolder {
        ImageView head;
        TextView name;
        ImageView level;
        TextView coin;
        TextView btnAttention;
        ListBean mListBean;
        int mPosition;
        TextView num;

        public HeadVh(View itemView) {
            super(itemView);
            head = (ImageView) itemView.findViewById(R.id.head);
            name = (TextView) itemView.findViewById(R.id.name);
            level = (ImageView) itemView.findViewById(R.id.level);
            coin = (TextView) itemView.findViewById(R.id.coin);
            num = (TextView) itemView.findViewById(R.id.num);
            btnAttention = (TextView) itemView.findViewById(R.id.btn_attention);
            btnAttention.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(mListBean, mPosition);
                    }
                }
            });
        }

        void setData(ListBean bean, int position, String flag) {
            mListBean = bean;
            mPosition = position;
            if (flag == null) {
                ImgLoader.display(bean.getAvatar_thumb(), head);
                name.setText(bean.getUser_nicename());
                coin.setText(bean.getTotalcoin());
                if (mType == OrderListFragment.CONSUME) {
                    level.setImageResource(IconUitl.getAudienceDrawable(bean.getLevel()));
                } else if (mType == OrderListFragment.PROFIT) {
                    level.setImageResource(IconUitl.getAnchorDrawable(bean.getLevelAnchor()));
                }
            }
            if (bean.getIsAttention() == 0) {
                btnAttention.setText(mAttentionNot);
                btnAttention.setEnabled(true);
            } else {
                btnAttention.setText(mAttention);
                btnAttention.setEnabled(false);
            }
        }
    }

    class Vh extends RecyclerView.ViewHolder {
        ImageView head;
        TextView name;
        ImageView level;
        TextView coin;
        TextView btnAttention;
        ListBean mListBean;
        int mPosition;
        ImageView wrap;
        TextView num;

        public Vh(View itemView) {
            super(itemView);
            head = (ImageView) itemView.findViewById(R.id.head);
            name = (TextView) itemView.findViewById(R.id.name);
            level = (ImageView) itemView.findViewById(R.id.level);
            coin = (TextView) itemView.findViewById(R.id.coin);
            wrap = (ImageView) itemView.findViewById(R.id.wrap);
            num = (TextView) itemView.findViewById(R.id.num);
            btnAttention = (TextView) itemView.findViewById(R.id.btn_attention);
            btnAttention.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(mListBean, mPosition);
                    }
                }
            });
        }

        void setData(ListBean bean, int position, String flag) {
            mListBean = bean;
            mPosition = position;
            if (flag == null) {
                ImgLoader.display(bean.getAvatar_thumb(), head);
                name.setText(bean.getUser_nicename());
                coin.setText(bean.getTotalcoin());
                if (mType == OrderListFragment.CONSUME) {
                    level.setImageResource(IconUitl.getAudienceDrawable(bean.getLevel()));
                } else if (mType == OrderListFragment.PROFIT) {
                    level.setImageResource(IconUitl.getAnchorDrawable(bean.getLevelAnchor()));
                }
                if (position == 1) {
                    wrap.setImageResource(R.mipmap.icon_list_02);
                    num.setText("");
                } else if (position == 2) {
                    wrap.setImageResource(R.mipmap.icon_list_03);
                    num.setText("");
                } else {
                    wrap.setImageDrawable(null);
                    num.setText("NO." + (position + 1));
                }
            }
            if (bean.getIsAttention() == 0) {
                btnAttention.setText(mAttentionNot);
                btnAttention.setEnabled(true);
            } else {
                btnAttention.setText(mAttention);
                btnAttention.setEnabled(false);
            }
        }
    }

    public void setItemClickListener(OnItemClickListener<ListBean> listener) {
        mItemClickListener = listener;
    }

    public void setAttention(String touid, int isAttention) {
        ListBean target = null;
        int position = 0;
        for (int i = 0; i < mList.size(); i++) {
            ListBean bean = mList.get(i);
            if (bean.getUid().equals(touid)) {
                target = bean;
                position = i;
                break;
            }
        }
        if (target != null) {
            target.setIsAttention(isAttention);
            notifyItemChanged(position, FLAG);
        }
    }
}
