package com.caihongzhibo.phonelive.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.bean.UserBean;
import com.caihongzhibo.phonelive.glide.ImgLoader;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.utils.ToastUtil;
import com.caihongzhibo.phonelive.utils.IconUitl;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by cxf on 2017/8/16.
 * 修改个人资料页面
 */

public class EditProfileActivity extends AbsActivity {

    private ImageView mHeadImg;
    private TextView mName;
    private TextView mSignature;
    private TextView mBirthday;
    private ImageView mSex;
    private UserBean mUserBean;
    private final int SET_AVATAR = 100;
    private final int SET_NICKNAME = 200;
    private final int SET_SIGNATURR = 300;
    private final int SET_SEX = 400;
    private final int TYPE_NICKNAME = 1000;
    private final int TYPE_SIGNATURR = 2000;
    private String mBirthdayVal;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_profile;
    }

    @Override
    protected void main() {
        setTitle(getString(R.string.edit_profile));
        mHeadImg = (ImageView) findViewById(R.id.headImg);
        mName = (TextView) findViewById(R.id.name);
        mSignature = (TextView) findViewById(R.id.signature);
        mBirthday = (TextView) findViewById(R.id.birthday);
        mSex = (ImageView) findViewById(R.id.sex);
        mUserBean = getIntent().getParcelableExtra("user");
        if (mUserBean == null) {
            return;
        }
        ImgLoader.displayCircle(mUserBean.getAvatar(), mHeadImg);
        mName.setText(mUserBean.getUser_nicename());
        mSignature.setText(mUserBean.getSignature());
        mBirthday.setText(mUserBean.getBirthday());
        mSex.setImageResource(IconUitl.getSexDrawable(mUserBean.getSex()));
    }


    public void editClick(View v) {
        switch (v.getId()) {
            case R.id.btn_head:
                setAvatar();
                break;
            case R.id.btn_name:
                setNickName(TYPE_NICKNAME);
                break;
            case R.id.btn_signature:
                setNickName(TYPE_SIGNATURR);
                break;
            case R.id.btn_birthday:
                setBirthday();
                break;
            case R.id.btn_sex:
                setSex();
                break;
        }
    }

    private void setNickName(int type) {
        Intent intent = new Intent(mContext, SetNickNameActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("name", mUserBean.getUser_nicename());
        intent.putExtra("signature", mUserBean.getSignature());
        startActivityForResult(intent, type == TYPE_NICKNAME ? SET_NICKNAME : SET_SIGNATURR);
    }

    private void setAvatar() {
        Intent intent = new Intent(mContext, SetAvatarActivity.class);
        intent.putExtra("avatar", mUserBean.getAvatar());
        startActivityForResult(intent, SET_AVATAR);
    }

    private void setSex() {
        Intent intent = new Intent(mContext, SetSexActivity.class);
        intent.putExtra("sex", mUserBean.getSex());
        startActivityForResult(intent, SET_SEX);
    }


    private void setBirthday() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                c.set(year, month, dayOfMonth);
                if (c.getTime().getTime() > new Date().getTime()) {
                    ToastUtil.show(getString(R.string.please_input_right_date));
                    return;
                }
                mBirthdayVal = DateFormat.format("yyyy-MM-dd", c).toString();
                updateBirthday();
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();

    }

    /**
     * 修改生日
     */
    private void updateBirthday() {
        HttpUtil.updateFields("{\"birthday\":\"" + mBirthdayVal + "\"}", mCallback);
    }

    private HttpCallback mCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                ToastUtil.show(JSON.parseObject(info[0]).getString("msg"));
                mBirthday.setText(mBirthdayVal);
            } else {
                ToastUtil.show(msg);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SET_AVATAR:
                    onSetAvatarResult(data);
                    break;
                case SET_NICKNAME:
                    onSetNameResult(data);
                    break;
                case SET_SIGNATURR:
                    onSetSignature(data);
                    break;
                case SET_SEX:
                    onSetSexResult(data);
                    break;
            }
        } else {
            switch (requestCode) {
                case SET_AVATAR:
                    ToastUtil.show(getString(R.string.cancel_set_avatar));
                    break;
                case SET_NICKNAME:
                    ToastUtil.show(getString(R.string.cancel_set_nickname));
                    break;
                case SET_SIGNATURR:
                    ToastUtil.show(getString(R.string.cancel_set_signature));
                    break;
                case SET_SEX:
                    ToastUtil.show(getString(R.string.cancel_set_sex));
                    break;
            }
        }
    }


    private void onSetAvatarResult(Intent data) {
        ImgLoader.displayCircle(new File(data.getStringExtra("path")), mHeadImg);
        mUserBean.setAvatar(data.getStringExtra("avatar"));
        mUserBean.setAvatar_thumb(data.getStringExtra("avatar_thumb"));
        ToastUtil.show(getString(R.string.success_set_avatar));
    }

    private void onSetSexResult(Intent data) {
        int sex = data.getIntExtra("sex", -1);
        if (sex != -1) {
            mSex.setImageResource(IconUitl.getSexDrawable(sex));
            mUserBean.setSex(sex);
        }
    }

    private void onSetNameResult(Intent data) {
        String name = data.getStringExtra("name");
        mName.setText(name);
        mUserBean.setUser_nicename(name);
    }

    private void onSetSignature(Intent data) {
        String signature = data.getStringExtra("signature");
        mSignature.setText(signature);
        mUserBean.setSignature(signature);
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.UPDATE_FIELDS);
        super.onDestroy();
    }
}
