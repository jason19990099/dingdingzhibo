package com.caihongzhibo.phonelive.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.util.DateUtils;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.bean.UserBean;
import com.caihongzhibo.phonelive.glide.ImgLoader;
import com.caihongzhibo.phonelive.im.IM;

import java.util.Date;
import java.util.List;

/**
 * Created by cxf on 2017/7/1.
 */

public class EMChatRoomAdapter<C,M,I extends IM<C,M>> extends RecyclerView.Adapter<EMChatRoomAdapter.Vh> {

    private final int LEFT = 0;
    private final int RIGHT = 1;
    private List<M> mList;
    private Context mContext;
    private UserBean mToUser;
    private UserBean mUser;
    private LayoutInflater mInflater;
    private I mIM;

    public EMChatRoomAdapter(Context context, List<M> list, UserBean toUser, UserBean user, I im) {
        mList = list;
        mContext = context;
        mToUser = toUser;
        mUser = user;
        mInflater = LayoutInflater.from(mContext);
        mIM = im;
    }

    @Override
    public Vh onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == LEFT) {
            v = mInflater.inflate(R.layout.item_chat_msg_left, parent, false);
        } else {
            v = mInflater.inflate(R.layout.item_chat_msg_right, parent, false);
        }
        return new Vh(v);
    }

    @Override
    public void onBindViewHolder(EMChatRoomAdapter.Vh vh, int position) {
        vh.setData(mList.get(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mUser.getId().equals(mIM.getFrom(mList.get(position)))) {
            return RIGHT;
        } else {
            return LEFT;
        }
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {
        TextView timeStamp;
        TextView msg;
        ImageView headImg;
        M mEmMessage;
        int mPosition;

        public Vh(View itemView) {
            super(itemView);
            timeStamp = (TextView) itemView.findViewById(R.id.timestamp);
            msg = (TextView) itemView.findViewById(R.id.msg);
            headImg = (ImageView) itemView.findViewById(R.id.headImg);
        }

        void setData(M emMessage, int position) {
            mEmMessage = emMessage;
            mPosition = position;
            if (position > 0) {
                if (DateUtils.isCloseEnough(mIM.getMessageTime(mList.get(position - 1)), mIM.getMessageTime(emMessage))) {
                    if (timeStamp.getVisibility() == View.VISIBLE) {
                        timeStamp.setVisibility(View.GONE);
                    }
                } else {
                    if (timeStamp.getVisibility() == View.GONE) {
                        timeStamp.setVisibility(View.VISIBLE);
                    }
                    timeStamp.setText(DateUtils.getTimestampString(new Date(mIM.getMessageTime(emMessage))));
                }
            } else if (position == 0) {
                if (timeStamp.getVisibility() == View.GONE) {
                    timeStamp.setVisibility(View.VISIBLE);
                }
                timeStamp.setText(DateUtils.getTimestampString(new Date(mIM.getMessageTime(emMessage))));
            }

            if (mIM.getFrom(emMessage).equals(mUser.getId())) {
                ImgLoader.displayCircle(mUser.getAvatar(), headImg);
            } else {
                ImgLoader.displayCircle(mToUser.getAvatar(), headImg);
            }
            msg.setText(mIM.getContent(emMessage));
        }
    }

}
