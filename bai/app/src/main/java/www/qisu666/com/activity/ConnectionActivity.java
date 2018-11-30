package www.qisu666.com.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import www.qisu666.com.R;
import www.qisu666.com.callback.UserInfoResp;
import www.qisu666.com.event.FinishActivityEvent;
import www.qisu666.com.request.MyNetwork;
import www.qisu666.com.request.utils.FlatFunction;
import www.qisu666.com.request.utils.MyMessageUtils;
import www.qisu666.com.request.utils.ResultSubscriber;
import www.qisu666.com.request.utils.RxNetHelper;
import www.qisu666.com.utils.CacheUtils;
import www.qisu666.com.utils.ChargeStatus;
import www.qisu666.com.utils.DialogHelper;
import www.qisu666.com.utils.JsonUtils;
import www.qisu666.com.utils.ToastUtil;
import www.qisu666.com.widget.AlertDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;


//正在连接
public class ConnectionActivity extends BaseActivity {

    private TextView tv_title;
    private TextView btn_reconnection;
    //    private TextView btn_cancel;
    private TextView tv_prompt;
    private GifImageView gifImageView;
    private ImageView iv_connect_fail;

    private View.OnClickListener onReconnectionClickListener;
    private View.OnClickListener onConfirmClickListener;

    //重复请求次数
    private int repeat_count = 0;
    //请求次数上限
    private static final int REPEAT_REQ_TIMES = 20;
    //请求间隔（上一次请求结束，到下一次请求开始的间隔）
    private static final int REPEAT_DURATION = 2000;
    //handler跳转的flag
    private static final int FLAG_FINISH = 1001;
    //handler重复请求的flag
    private static final int FLAG_REPEAT = 1002;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLAG_FINISH:
                    ChargeStatus.INSTANCE.setStatus(ChargeStatus.STATUS_CHARGING);
                    Intent i = new Intent(ConnectionActivity.this, ChargingActivity.class);
//                    Map map = (Map) msg.obj;
//                    Set set = map.keySet();
//                    Iterator<String> iterator = set.iterator();
//                    while (iterator.hasNext()){
//                        String key = iterator.next();
//                        if(!key.equals("return_msg") && !key.equals("return_code")) {
//                            String val = map.get(key).toString();
//                            i.putExtra(key, val);
//                        }
//                    }
                    startActivity(i);
                    finish();
                    break;
                case FLAG_REPEAT:
                    if (repeat_count < REPEAT_REQ_TIMES) {
                        connecting();
                        connectToServer();
                    } else {
                        connectFail();
                        tv_prompt.setText("连接超时！");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        initViews();
        // 请求订单下的钱包支付
        connectDingDan();
        EventBus.getDefault().post(new FinishActivityEvent());
//        testParams();
    }

    private void connectDingDan() {

        String url = "api/charge/wallet/order/pay";
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", CacheUtils.getIn().getUserInfo().getId());
        requestMap.put("gunCode", getIntent().getStringExtra("gunCode"));
        requestMap.put("chargePileSeri", getIntent().getStringExtra("chargePileSeri"));
        requestMap.put("orderType", "4");

        MyNetwork.getMyApi()
                .carRequest(url, MyMessageUtils.addBody(requestMap))
                .map(new FlatFunction<>(Object.class))
                .compose(RxNetHelper.<Object>io_main())
                .subscribe(new ResultSubscriber<Object>() {
                    @Override
                    public void onSuccessCode(www.qisu666.com.event.Message object) {

                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void onSuccess(Object bean) {
                        Log.e("aaaa", "onSuccess：" + bean.toString());

                        // 对象转json
                        String s = JsonUtils.objectToJson(bean);
                        // json转 map
                        Map jsonToMap = JsonUtils.jsonToMap(s);

                        UserInfoResp userInfoResp=CacheUtils.getIn().getUserInfo();
                        userInfoResp.setOut_trade_no(jsonToMap.get("outTradeNo").toString());
                        CacheUtils.getIn().save(userInfoResp);
                        Log.e("aaaa", "connectDingDan" + jsonToMap.get("outTradeNo").toString());
                        Log.e("aaaa", "connectDingDan + INSTANCE" + CacheUtils.getIn().getUserInfo().getOut_trade_no());
                        
                        // 调用成功后 查询订单状态
                        connectToServer();

                    }

                    @Override
                    public void onFail(www.qisu666.com.event.Message<Object> bean) {
                        ToastUtil.showToast(bean.msg);
                        Log.e("aaaa", "获取失败：" + bean.toString());
                    }


                });

    }

    private void testParams() {
        Message msg = handler.obtainMessage();
        msg.what = FLAG_FINISH;
        handler.sendMessageDelayed(msg, 3000);
    }

    /**
     * 查询用户状态
     */
    private String outTradeNo = "";

    private void connectToServer() {
        repeat_count++;
        String url = "api/charge/order/status/query";
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", CacheUtils.getIn().getUserInfo().getId());

        MyNetwork.getMyApi()
                .carRequest(url, MyMessageUtils.addBody(requestMap))
                .map(new FlatFunction<>(Object.class))
                .compose(RxNetHelper.<Object>io_main())
                .subscribe(new ResultSubscriber<Object>() {
                    @Override
                    public void onSuccessCode(www.qisu666.com.event.Message object) {

                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void onSuccess(Object bean) {
                        Log.e("aaaa", "onSuccess：" + bean.toString());
                        // 对象转json
                        String s = JsonUtils.objectToJson(bean);
                        // json 转map
                        Map jsonToMap = JsonUtils.jsonToMap(s);
                        outTradeNo = jsonToMap.get("outTradeNo").toString();                        

                        String charge_ret_39 = jsonToMap.get("orderStatus").toString();//返回03时提示插枪后再试
                        if ("03".equals(charge_ret_39)) {
                            connectWithoutInsert();
                            return;
                        }

                        Message msg = handler.obtainMessage();
                        if ("02".equals(charge_ret_39)) {
                            msg.what = FLAG_FINISH;
                            msg.obj = jsonToMap;
                            handler.sendMessage(msg);
                        } else if ("01".equals(charge_ret_39)) {
                            msg.what = FLAG_REPEAT;
                            handler.sendMessageDelayed(msg, REPEAT_DURATION);
                        }

                    }

                    @Override
                    public void onFail(www.qisu666.com.event.Message<Object> bean) {
                        ToastUtil.showToast(bean.msg);
                        Log.e("aaaa", "获取失败：" + bean.toString());
                    }


                });


//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("req_code", "E102");
//            jsonObject.put("user_id", UserParams.INSTANCE.getUser_id());
//            jsonObject.put("out_trade_no", UserParams.INSTANCE.getOut_trade_no());
//            jsonObject.put("s_token", UserParams.INSTANCE.getS_token() == null ? "" : UserParams.INSTANCE.getS_token());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        new HttpLogic(this).sendRequest(Config.REQUEST_URL, jsonObject, false, new AbstractResponseCallBack() {
//            @Override
//            public void onResponse(Map<String, Object> map, String tag) {
//                String order_state = map.get("order_state").toString();
//                String charge_ret_39 = map.get("charge_ret_39").toString();//返回03时提示插枪后再试
//                if ("03".equals(charge_ret_39)) {
//                    connectWithoutInsert();
//                    return;
//                }
//
//                Message msg = handler.obtainMessage();
//                if ("02".equals(order_state)) {
//                    msg.what = FLAG_FINISH;
//                    msg.obj = map;
//                    handler.sendMessage(msg);
//                } else if ("01".equals(order_state)) {
//                    msg.what = FLAG_REPEAT;
//                    handler.sendMessageDelayed(msg, REPEAT_DURATION);
//                }
//            }
//        });
    }

    private void initTitleBar() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(R.string.connection_title);
        View left_btn = findViewById(R.id.img_title_left);
        left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        initTitleBar();
        gifImageView = (GifImageView) findViewById(R.id.gifImageView);
        gifImageView.setImageResource(R.mipmap.img_loading_gif);

        tv_prompt = (TextView) findViewById(R.id.tv_prompt);
//        tv_prompt.setText("正在连接中...");
        tv_prompt.setText("");

        btn_reconnection = (TextView) findViewById(R.id.btn_reconnection);
//        btn_cancel = (TextView) findViewById(R.id.btn_cancel);

        iv_connect_fail = (ImageView) findViewById(R.id.iv_connect_fail);

        //重新链接
        onReconnectionClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToServer();
            }
        };

        //确认插入
        onConfirmClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToServer();
            }
        };

//        btn_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });
    }

    private void showConfirmDialog() {
        DialogHelper.confirmDialog(ConnectionActivity.this, getString(R.string.dialog_prompt_cancel_charge), new AlertDialog.OnDialogButtonClickListener() {
            @Override
            public void onConfirm() {
                finish();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
//        showConfirmDialog();
    }

    @Override
    protected void onDestroy() {
        handler.removeMessages(FLAG_REPEAT);
        handler = null;
        super.onDestroy();
    }

    private void connectFail() {
        repeat_count = 0;
        tv_title.setText(R.string.connection_failure);
        tv_prompt.setText(R.string.connection_failure_prompt);
        btn_reconnection.setVisibility(View.VISIBLE);
        btn_reconnection.setText(R.string.connection_reconnection);
        btn_reconnection.setOnClickListener(onReconnectionClickListener);
        gifImageView.setVisibility(View.GONE);
        iv_connect_fail.setVisibility(View.VISIBLE);
    }

    /**
     * 正在连接
     */
    private void connecting() {
        tv_title.setText(R.string.connection_title);
        tv_prompt.setText("");
        btn_reconnection.setVisibility(View.GONE);
        gifImageView.setVisibility(View.VISIBLE);
        iv_connect_fail.setVisibility(View.GONE);
    }

    /**
     * 未插入充电枪
     */
    private void connectWithoutInsert() {
        repeat_count = 0;
        tv_title.setText(R.string.connection_failure);
        tv_prompt.setText(R.string.connection_without_insert_prompt);
        btn_reconnection.setVisibility(View.VISIBLE);
        btn_reconnection.setText(R.string.connection_without_insert);
        btn_reconnection.setOnClickListener(onConfirmClickListener);
        gifImageView.setVisibility(View.GONE);
        iv_connect_fail.setVisibility(View.VISIBLE);
    }


    @Override
    public void setView() {

    }

    @Override
    public void initDatas() {

    }

    @Override
    public void onComplete(String result, int type) {

    }

    @Override
    public void onFailure(String msg, int type) {

    }
}
