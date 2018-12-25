package com.caihongzhibo.phonelive2.interfaces;

/**
 * Created by cxf on 2017/8/9.
 * RecyclerView的Adapter点击事件
 */

public interface OnItemClickListener<T> {
    void onItemClick(T item, int position);
}
