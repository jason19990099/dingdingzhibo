package com.caihongzhibo.phonelive.bean;

/**
 * Created by cxf on 2017/8/19.
 * 礼物列表的实体类
 */

public class GiftBean {
    private String id;
    private int type;
    private String giftname;
    private String needcoin;
    private String gifticon;
    private boolean checked;
    private int page;
    private int position;
    private String evensend;//连送的标识

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getGiftname() {
        return giftname;
    }

    public void setGiftname(String giftname) {
        this.giftname = giftname;
    }

    public String getNeedcoin() {
        return needcoin;
    }

    public void setNeedcoin(String needcoin) {
        this.needcoin = needcoin;
    }

    public String getGifticon() {
        return gifticon;
    }

    public void setGifticon(String gifticon) {
        this.gifticon = gifticon;
    }

    public String getEvensend() {
        return evensend;
    }

    public void setEvensend(String evensend) {
        this.evensend = evensend;
    }
}
