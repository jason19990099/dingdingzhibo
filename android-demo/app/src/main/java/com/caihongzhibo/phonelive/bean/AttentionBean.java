package com.caihongzhibo.phonelive.bean;

/**
 * Created by cxf on 2017/8/11.
 * 搜索结果列表和主页关注列表 数据实体类
 */

public class AttentionBean {
    private String id;
    private String user_nicename;
    private String avatar;
    private int sex;
    private String signature;
    private float votestotal;
    private int level;
    private int level_anchor;
    private int isattention;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_nicename() {
        return user_nicename;
    }

    public void setUser_nicename(String user_nicename) {
        this.user_nicename = user_nicename;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public float getVotestotal() {
        return votestotal;
    }

    public void setVotestotal(float votestotal) {
        this.votestotal = votestotal;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel_anchor() {
        return level_anchor;
    }

    public void setLevel_anchor(int level_anchor) {
        this.level_anchor = level_anchor;
    }

    public int getIsattention() {
        return isattention;
    }

    public void setIsattention(int isattention) {
        this.isattention = isattention;
    }
}
