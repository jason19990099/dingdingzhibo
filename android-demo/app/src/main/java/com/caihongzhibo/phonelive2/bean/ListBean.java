package com.caihongzhibo.phonelive2.bean;

/**
 * Created by cxf on 2018/2/2.
 */

public class ListBean {
    private String totalcoin;
    private String uid;
    private String user_nicename;
    private String avatar_thumb;
    private int levelAnchor;
    private int level;
    private int isAttention;

    public String getTotalcoin() {
        return totalcoin;
    }

    public void setTotalcoin(String totalcoin) {
        this.totalcoin = totalcoin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUser_nicename() {
        return user_nicename;
    }

    public void setUser_nicename(String user_nicename) {
        this.user_nicename = user_nicename;
    }

    public String getAvatar_thumb() {
        return avatar_thumb;
    }

    public void setAvatar_thumb(String avatar_thumb) {
        this.avatar_thumb = avatar_thumb;
    }

    public int getLevelAnchor() {
        return levelAnchor;
    }

    public void setLevelAnchor(int levelAnchor) {
        this.levelAnchor = levelAnchor;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getIsAttention() {
        return isAttention;
    }

    public void setIsAttention(int isAttention) {
        this.isAttention = isAttention;
    }

}
