package com.caihongzhibo.phonelive2.bean;

import java.io.Serializable;
import java.util.List;

public class IParray implements Serializable {


    /**
     * ret : 200
     * data : ["dd7666.com","dd1666.com","dd9666.com","dd9995.com","dd7771.com","dd5557.com","dd5551.com","dd3339.com","dd3337.com"]
     */

    private int ret;
    private List<String> data;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
