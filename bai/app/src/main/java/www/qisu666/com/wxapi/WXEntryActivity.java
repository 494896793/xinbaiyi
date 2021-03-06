/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package www.qisu666.com.wxapi;

import android.content.Intent;

import com.alibaba.fastjson.JSONObject;
import www.qisu666.com.activity.BaseActivity;
import www.qisu666.com.app.MyApplication;
import www.qisu666.com.constant.Config;
import www.qisu666.com.constant.RequestUrls;
import www.qisu666.com.request.WXAccessTokenRequest;
import www.qisu666.com.request.WXGetUserInfoRequest;
import www.qisu666.com.rx.RxBus;
import www.qisu666.com.rx.RxBusEvent;
import www.qisu666.com.rx.RxEventCodes;
import www.qisu666.com.utils.Logger;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信客户端回调activity示例
 */
public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler {
    private static final String TAG = WXEntryActivity.class.getSimpleName();
    private static final int QUERY_WX_ACCESS_TOKEN = 1;
    private static final int QUERY_WX_USER_INFO = 2;
    private static final String SECRET = "d54099cd05b6010ded23d697e6d48f47";
    private IWXAPI api;
    private String appId;

    @Override
    public void setView() {

    }

    @Override
    public void initDatas() {
        appId = MyApplication.getApplication().getAPP_ID();
        api = WXAPIFactory.createWXAPI(this, appId);
        api.handleIntent(getIntent(), this);
    }

    @Override
    public void onComplete(String result, int type) {
        if (type == QUERY_WX_ACCESS_TOKEN) {
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (null != jsonObject) {
                String token = jsonObject.getString("access_token");
                String openId = jsonObject.getString("openid");
                getUserInfo(token, openId);
            } else {
                finish();
            }
        } else if (type == QUERY_WX_USER_INFO) {
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (null != jsonObject) {
                String nikcName = jsonObject.getString("nickname");
                Logger.i(TAG, "nikcName=" + nikcName);
                RxBusEvent event = new RxBusEvent();
                event.setEventCode(RxEventCodes.CODE_WX_NICKNAME);
                Map<String,String> map = new HashMap<>();
                map.put("nickname",nikcName);
                map.put("openId",jsonObject.getString("openid"));
                event.setContent(map);
                RxBus.getInstance().post(event);
            }
            finish();
        }
    }

    @Override
    public void onFailure(String msg, int type) {
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {

    }


    /**
     * Title: onResp
     *
     * @param arg0
     */
    @Override
    public void onResp(BaseResp arg0) {
        Logger.i(TAG, "arg0.errCode=" + arg0.errCode);
        //获取到code之后，需要调用接口获取到access_token
        if (arg0.errCode == BaseResp.ErrCode.ERR_OK) {
            if (arg0 instanceof SendAuth.Resp) {
                SendAuth.Resp resp = ((SendAuth.Resp) arg0);
                String code = resp.code;
                getToken(code);
            } else {
                finish();
            }

        } else {
            WXEntryActivity.this.finish();
        }

    }

    //这个方法会取得accesstoken  和openID
    private void getToken(String code) {
        WXAccessTokenRequest request = new WXAccessTokenRequest();
        request.setMethod(RequestUrls.WX_ACCESS_TOKEN);
        request.setAppid(appId);
        request.setSecret(SECRET);
        request.setCode(code);
        request.setGrant_type("authorization_code");
        doGet(request, QUERY_WX_ACCESS_TOKEN, Config.LOADING_STRING, true);
    }

    //获取到token和openID之后，调用此接口得到身份信息
    private void getUserInfo(String accessToken, String openId) {
        WXGetUserInfoRequest request = new WXGetUserInfoRequest();
        request.setMethod(RequestUrls.WX_GET_USER_INFO);
        request.setOpenid(openId);
        request.setAccess_token(accessToken);
        doGet(request, QUERY_WX_USER_INFO, Config.LOADING_STRING, true);
    }
}
