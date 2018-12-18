package com.caihongzhibo.phonelive.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cxf on 2017/8/9.
 */

public class LiveBean implements Parcelable {
    private String uid;
    private String avatar;
    private String avatar_thumb;
    private String user_nicename;
    private String title;
    private String city;
    private String stream;
    private String pull;
    private String thumb;
    private String isvideo;
    private String nums;
    private String distance;
    private int level_anchor;
    private int type;
    private String type_val;
    private String goodnum;//主播的靓号
    private int game_action;//正在进行的游戏的标识
    private String game;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar_thumb() {
        return avatar_thumb;
    }

    public void setAvatar_thumb(String avatar_thumb) {
        this.avatar_thumb = avatar_thumb;
    }

    public String getUser_nicename() {
        return user_nicename;
    }

    public void setUser_nicename(String user_nicename) {
        this.user_nicename = user_nicename;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getPull() {
        return pull;
    }

    public void setPull(String pull) {
        this.pull = pull;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getIsvideo() {
        return isvideo;
    }

    public void setIsvideo(String isvideo) {
        this.isvideo = isvideo;
    }

    public String getNums() {
        return nums;
    }

    public void setNums(String nums) {
        this.nums = nums;
    }

    public int getLevel_anchor() {
        return level_anchor;
    }

    public void setLevel_anchor(int level_anchor) {
        this.level_anchor = level_anchor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getType_val() {
        return type_val;
    }

    public void setType_val(String type_val) {
        this.type_val = type_val;
    }

    public String getGoodnum() {
        return goodnum;
    }

    public void setGoodnum(String goodnum) {
        this.goodnum = goodnum;
    }

    public int getGame_action() {
        return game_action;
    }

    public void setGame_action(int game_action) {
        this.game_action = game_action;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public LiveBean() {

    }

    private LiveBean(Parcel in) {
        this.uid = in.readString();
        this.avatar = in.readString();
        this.avatar_thumb = in.readString();
        this.user_nicename = in.readString();
        this.title = in.readString();
        this.city = in.readString();
        this.stream = in.readString();
        this.pull = in.readString();
        this.thumb = in.readString();
        this.isvideo = in.readString();
        this.nums = in.readString();
        this.distance = in.readString();
        this.level_anchor = in.readInt();
        this.type = in.readInt();
        this.type_val = in.readString();
        this.goodnum=in.readString();
        this.game_action=in.readInt();
        this.game=in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.avatar);
        dest.writeString(this.avatar_thumb);
        dest.writeString(this.user_nicename);
        dest.writeString(this.title);
        dest.writeString(this.city);
        dest.writeString(this.stream);
        dest.writeString(this.pull);
        dest.writeString(this.thumb);
        dest.writeString(this.isvideo);
        dest.writeString(this.nums);
        dest.writeString(this.distance);
        dest.writeInt(this.level_anchor);
        dest.writeInt(this.type);
        dest.writeString(this.type_val);
        dest.writeString(this.goodnum);
        dest.writeInt(this.game_action);
        dest.writeString(this.game);
    }

    public static final Parcelable.Creator<LiveBean> CREATOR = new Creator<LiveBean>() {
        @Override
        public LiveBean[] newArray(int size) {
            return new LiveBean[size];
        }

        @Override
        public LiveBean createFromParcel(Parcel in) {
            return new LiveBean(in);
        }
    };
}
