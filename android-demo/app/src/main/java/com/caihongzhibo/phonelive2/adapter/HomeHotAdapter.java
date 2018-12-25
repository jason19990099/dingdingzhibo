package com.caihongzhibo.phonelive2.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.phonelive.game.GameIconUitl;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.activity.WebBannerActivity;
import com.caihongzhibo.phonelive2.bean.LiveBean;
import com.caihongzhibo.phonelive2.bean.SliderBean;
import com.caihongzhibo.phonelive2.glide.ImgLoader;
import com.caihongzhibo.phonelive2.interfaces.OnItemClickListener;
import com.caihongzhibo.phonelive2.utils.IconUitl;

import java.util.List;


/**
 * Created by cxf on 2017/8/9.
 */
public class HomeHotAdapter extends RecyclerView.Adapter {

    private final int HEADER_TYPE = 0;
    private final int NORMAL_TYPE = 1;

    private Context mContext;
    private List<SliderBean> mSliderList;
    private List<LiveBean> mItemList;
    private LayoutInflater mInflater;
    private OnItemClickListener<LiveBean> mOnItemClickListener;
    private Banner mBanner;

    public HomeHotAdapter(Context context, List<SliderBean> sliderList, List<LiveBean> itemList) {
        mContext = context;
        mSliderList = sliderList;
        mItemList = itemList;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setOnItemClickListener(OnItemClickListener<LiveBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setData(List<SliderBean> sliderList, List<LiveBean> itemList) {
        mSliderList = sliderList;
        mItemList = itemList;
        notifyDataSetChanged();
    }

    public void clearData() {
        mItemList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER_TYPE;
        } else {
            return NORMAL_TYPE;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER_TYPE) {
            HeadViewHolder holder;
            if (mBanner == null) {
                holder = new HeadViewHolder(mInflater.inflate(R.layout.view_home_banner, parent, false));
            } else {
                holder = new HeadViewHolder(mBanner);
            }
            return holder;
        } else {
            return new ViewHolder(mInflater.inflate(R.layout.item_list_home_hot, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder vh = (ViewHolder) holder;
            vh.setData(mItemList.get(position - 1), position);
        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size() + 1;
    }

    class HeadViewHolder extends RecyclerView.ViewHolder {
        public HeadViewHolder(View itemView) {
            super(itemView);
            if (mBanner == null) {
                mBanner = (Banner) this.itemView;
                mBanner.setImageLoader(new ImageLoader() {
                    @Override
                    public void displayImage(Context context, Object path, ImageView imageView) {
                        ImgLoader.display(((SliderBean) path).getSlide_pic(), imageView, R.mipmap.bg_home_placeholder2);
                    }
                });
                mBanner.setOnBannerListener(new OnBannerListener() {
                    @Override
                    public void OnBannerClick(int p) {
                        Intent intent = new Intent(mContext, WebBannerActivity.class);
                        intent.putExtra("url", mSliderList.get(p).getSlide_url());
                        mContext.startActivity(intent);
                    }
                });
                mBanner.setImages(mSliderList);
                mBanner.start();
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LiveBean mBean;
        int mPosition;
        ImageView headImg;
        ImageView img;
        TextView name;
        TextView city;
        TextView nums;
        TextView title;
        ImageView type;
        ImageView game;
        ImageView anchorLevel;//主播等级

        public ViewHolder(View itemView) {
            super(itemView);
            headImg = (ImageView) itemView.findViewById(R.id.headImg);
            img = (ImageView) itemView.findViewById(R.id.img);
            nums = (TextView) itemView.findViewById(R.id.nums);
            name = (TextView) itemView.findViewById(R.id.name);
            city = (TextView) itemView.findViewById(R.id.city);
            title = (TextView) itemView.findViewById(R.id.title);
            type = (ImageView) itemView.findViewById(R.id.live_type);
            game = (ImageView) itemView.findViewById(R.id.game);
            anchorLevel = (ImageView) itemView.findViewById(R.id.anchor_level);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mBean, mPosition);
                    }
                }
            });
        }

        void setData(LiveBean bean, int position) {
            mBean = bean;
            mPosition = position;
            name.setText(bean.getUser_nicename());
            city.setText(bean.getCity());
            nums.setText(bean.getNums());
            anchorLevel.setImageResource(IconUitl.getAnchorLiveDrawable(bean.getLevel_anchor()));
            if (!"".equals(bean.getTitle())) {
                if (title.getVisibility() == View.GONE) {
                    title.setVisibility(View.VISIBLE);
                }
                title.setText(bean.getTitle());
            } else {
                if (title.getVisibility() == View.VISIBLE) {
                    title.setVisibility(View.GONE);
                }
            }
            ImgLoader.displayCircle(bean.getAvatar_thumb(), headImg);
            ImgLoader.display(bean.getThumb(), img, R.mipmap.bg_home_placeholder);
            type.setImageResource(IconUitl.getLiveTypeDrawable(bean.getType()));
            if (bean.getGame_action() == 0) {
                game.setImageDrawable(null);
            } else {
                game.setImageResource(GameIconUitl.getLiveGame(bean.getGame_action()));
            }
        }
    }


}
