package com.caihongzhibo.phonelive.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.utils.ToastUtil;

/**
 * Created by cxf on 2017/8/17.
 * 设置昵称
 */

public class SetNickNameActivity extends AbsActivity {

    private final int TYPE_NICKNAME = 1000;
    private final int TYPE_SIGNATURR = 2000;
    private EditText mInput;
    private TextView mTextView;
    private InputMethodManager imm;
    private int mType;
    private String mContent;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_set_nickname;
    }

    @Override
    protected void main() {
        mInput = (EditText) findViewById(R.id.input);
        mTextView = (TextView) findViewById(R.id.text);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Intent intent = getIntent();
        mType = intent.getIntExtra("type", 0);
        String title = "";
        String val = "";
        String text = "";
        if (mType == TYPE_NICKNAME) {
            val = intent.getStringExtra("name");
            text = getString(R.string.nickname_length);
            title = getString(R.string.update_nickname);
        } else if (mType == TYPE_SIGNATURR) {
            val = intent.getStringExtra("signature");
            text = getString(R.string.signature_length);
            title = getString(R.string.update_signature);
        }
        mTextView.setText(text);
        mInput.setText(val);
        setTitle(title);
    }

    public void setNickNameClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clear:
                clear();
                break;
            case R.id.btn_save:
                save();
                break;
        }
    }


    private void clear() {
        mInput.requestFocus();
        mInput.setText("");
        imm.showSoftInput(mInput, InputMethodManager.SHOW_FORCED);
    }

    private void save() {
        imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
        mContent = mInput.getText().toString();
        if ("".equals(mContent)) {
            ToastUtil.show(mType == TYPE_NICKNAME ? getString(R.string.nickname_empty) : getString(R.string.signature_empty));
            return;
        }
        if (mContent.length() > (mType == TYPE_NICKNAME ? 8 : 20)) {
            ToastUtil.show(mType == TYPE_NICKNAME ? getString(R.string.nickname_length_error) : getString(R.string.signature_length_error));
            return;
        }
        String fields = "{\"" + (mType == TYPE_NICKNAME ? "user_nicename" : "signature") + "\":\"" + mContent + "\"}";
        HttpUtil.updateFields(fields, mCallback);
    }

    private HttpCallback mCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                ToastUtil.show(JSON.parseObject(info[0]).getString("msg"));
                Intent intent = getIntent();
                if (mType == TYPE_NICKNAME) {
                    intent.putExtra("name", mContent);
                } else {
                    intent.putExtra("signature", mContent);
                }
                setResult(RESULT_OK, intent);
                finish();
            } else {
                ToastUtil.show(msg);
            }
        }
    };

    @Override
    public void onBackPressed() {
        imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.UPDATE_FIELDS);
        super.onDestroy();
    }
}
