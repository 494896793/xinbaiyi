package www.qisu666.com.logic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonStringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.util.LogUtil;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import www.qisu666.com.BuildConfig;
import www.qisu666.com.R;
import www.qisu666.com.activity.LoginActivity;

import www.qisu666.com.app.MyApplication;
import www.qisu666.com.net.ResponseListener;
import www.qisu666.com.request.utils.Config;
import www.qisu666.com.security.Base64Utils;
import www.qisu666.com.utils.CacheUtils;
import www.qisu666.com.utils.DialogHelper;
import www.qisu666.com.utils.HttpRequestManager;
import www.qisu666.com.utils.JsonUtils;
import www.qisu666.com.utils.SPParams;
import www.qisu666.com.utils.ToastUtil;
import www.qisu666.com.widget.AlertDialog;
import www.qisu666.com.widget.LoadingDialog;

/**
 * http逻辑  加密等操作
 *
 * @author lp
 */
public class HttpLogic implements ResponseListener {

    private Handler handler;
    private ResponseCallBack callBack;
    private LoadingDialog dialog;

    private Context context;

    private JsonStringRequest request;

    /**
     * 解密后的响应字符串
     */
    private String result;
    private String tag;

    public HttpLogic(Context context){
        handler = new Handler(MyApplication.getApplication().getMainLooper());
        this.context = context;
    }

    @Override
    public void onResponse(String response) {

        try {
            //防止dialog某些情况下无法绑定窗口而闪退
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            LogUtil.e("返回结果:"+response);
        }catch (Exception e){e.printStackTrace(); }

        try {
            if(response!=null && response.length()>0) {
                result = new String(Base64Utils.decode(response));
                LogUtil.e(tag+"返回结果 response:"+result);
                final Map<String,Object> responseMap = JsonUtils.jsonToMap(result);
                String return_code = responseMap.get("return_code").toString();

                //返回码为0000时，或者部分需单独处理的协议号
                if(tag.equals("D107") || tag.equals("A106") || return_code.equals("0000")){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callBack!=null){
                                callBack.onResponse(responseMap, tag);
                            }
                        }
                    });
                } else if(return_code.equals("1001")){
                    if(!DialogHelper.isShowLoginDialog) {
                        alertLogin(responseMap.get("return_msg").toString());
                    }
                } else{
                    ToastUtil.showToast(responseMap.get("return_msg").toString());
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(VolleyError error) {
        if(dialog!=null && dialog.isShowing()){
            dialog.dismiss();
        }
        if(error!=null){
            if(BuildConfig.DEBUG){
                ToastUtil.showToast("err:"+error.getLocalizedMessage());
            }
            LogUtil.e("进入异常VolleyError:"+error.getMessage());
            if(error.networkResponse!=null){
                LogUtil.e("进入异常VolleyError:"+error.networkResponse.statusCode);
            }
            ToastUtil.showToast(R.string.toast_network_server_outage);
        } else {
            ToastUtil.showToast(R.string.toast_network_interrupt);
        }

        if(callBack!=null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onError(tag);
                }
            });
        }
    }

    /**
     * 发送请求
     * @param url
     * @param jsonObject
     * @param callBack
     */
    public void sendRequest(String url, JSONObject jsonObject, ResponseCallBack callBack){
        sendRequest(url, jsonObject, true, true, LoadingDialog.TYPE_GIF, callBack);
    }

    /**
     * 发送请求
     * @param url
     * @param jsonObject
     * @param flag 是否显示LoadingDialog
     * @param callBack
     */
    public void sendRequest(String url, JSONObject jsonObject, boolean flag, ResponseCallBack callBack){
        sendRequest(url, jsonObject, flag, true, LoadingDialog.TYPE_GIF, callBack);
    }

    /**
     * 发送请求
     * @param url
     * @param jsonObject
     * @param flag 是否显示LoadingDialog
     * @param callBack
     */
    public void sendRequest(String url, JSONObject jsonObject, boolean flag, String type, ResponseCallBack callBack){
        sendRequest(url, jsonObject, flag, true, type, callBack);
    }

    /**
     * 发送请求
     * @param url
     * @param jsonObject
     * @param callBack
     * @param flag 是否显示LoadingDialog
     * @param cancelable 返回键能否关闭LoadingDialog
     * @param type LoadingDialog样式
     */
    public void sendRequest(String url, JSONObject jsonObject, boolean flag, boolean cancelable, String type, ResponseCallBack callBack){
//        if(!NetworkUtils.isConnected(context)){//如果没有网络，不发送请求
//            return;
//        }
        LogUtil.e("请求网络 :"+url);
        LogUtil.e("请求网络 参数:"+jsonObject.toString());
        this.callBack = callBack;
        try {
            // 将协议号作为tag
            tag = String.valueOf(jsonObject.get("req_code"));

            if(jsonObject==null){
                request = HttpRequestManager.newGetStringRequest(url,this);
            } else {
                jsonObject.put("req_chanl", Config.REG_CHANL);
                jsonObject.put("s_token", CacheUtils.getIn().getUserInfo().getToken());
                String jsonRequest = jsonObject.toString();
                LogUtil.e(tag+"加密后请求参数 request:"+jsonRequest);
                jsonRequest = Base64Utils.encoded(jsonRequest);
                LogUtil.e(tag+"加密后请求参数 request:"+jsonRequest);
                request = HttpRequestManager.newPostStringRequest(url,jsonRequest,this);
            }
            MyApplication.addRequest(request, tag);

            if(flag) {
                if(dialog!=null){
                    dialog.dismiss();
                }
                dialog = new LoadingDialog(context, type);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        request.cancel();
                    }
                });
                dialog.setCancelable(cancelable);
                dialog.show();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            LogUtil.e("请求字符串Base64加密失败");
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtil.e("无法从json中获取请求的协议号");
        }
    }


    /** 返回码：0006时，token过期，进行弹窗提示，并清除SharePreferences文件的部分属性，点击确定打开登录界面 */
    private void alertLogin(String return_msg) {
        final String login_phone = CacheUtils.getIn().getUserInfo().getPhone() == null ? "" : CacheUtils.getIn().getUserInfo().getPhone();
        SharedPreferences share = context.getSharedPreferences(SPParams.USER_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.clear().commit();

        AlertDialog dialog = new AlertDialog(context, return_msg, false);
        dialog.setCancelable(false);
        dialog.setSampleDialogListener(new AlertDialog.OnDialogButtonClickListener() {
            @Override
            public void onConfirm() {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.putExtra("login_phone", login_phone);
                context.startActivity(intent);
            }

            @Override
            public void onCancel() {

            }
        });
        dialog.show();
        DialogHelper.isShowLoginDialog = true;
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                DialogHelper.isShowLoginDialog = false;
            }
        });
    }
}
