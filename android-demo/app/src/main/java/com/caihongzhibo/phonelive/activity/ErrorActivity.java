package com.caihongzhibo.phonelive.activity;

import android.content.Intent;
import android.widget.TextView;

import com.caihongzhibo.phonelive.R;

/**
 * Created by cxf on 2018/8/29.
 */

public class ErrorActivity extends AbsActivity {

    private TextView mTextView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_error;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String error = intent.getStringExtra("error");
        setTitle(title);
        mTextView = (TextView) findViewById(R.id.text);
        mTextView.setText(error);
    }
}
