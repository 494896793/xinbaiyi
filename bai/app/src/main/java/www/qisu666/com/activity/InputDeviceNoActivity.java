package www.qisu666.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import www.qisu666.com.R;
import www.qisu666.com.event.Message;
import www.qisu666.com.request.MyNetwork;
import www.qisu666.com.request.utils.FlatFunction;
import www.qisu666.com.request.utils.MyMessageUtils;
import www.qisu666.com.request.utils.ResultSubscriber;
import www.qisu666.com.request.utils.RxNetHelper;
import www.qisu666.com.utils.CacheUtils;
import www.qisu666.com.utils.JsonUtils;
import www.qisu666.com.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;


//输入扫描终端号  与zxing对应
public class InputDeviceNoActivity extends BaseActivity implements View.OnClickListener {

    private View ll_device_no, ll_scan;
    private EditText et_device_no;
    private TextView btn_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_no);
        initView();
    }

    private void initView() {
        ll_device_no = findViewById(R.id.ll_device_no);
        ll_scan = findViewById(R.id.ll_scan);
        btn_submit = (TextView) findViewById(R.id.btn_submit);
        et_device_no = (EditText) findViewById(R.id.et_device_no);
        ll_device_no.setOnClickListener(this);
        ll_scan.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        initTitleBar();
    }

    private void initTitleBar() {
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText("输入编号充电");
        View leftBtn = findViewById(R.id.img_title_left);
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SpannableString ss = new SpannableString("请输入充电终端编号");//定义hint的值
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(17, true);//设置字体大小 true表示单位是sp
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        et_device_no.setHint(new SpannedString(ss));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                if (!TextUtils.isEmpty(et_device_no.getText().toString())) {
                    connToServer();
                } else {
                    ToastUtil.showToast(R.string.toast_device_no_is_null);
                }
                break;
            case R.id.ll_scan:
                startActivity(new Intent(this, CaptureActivity.class));
                finish();
                break;
            case R.id.ll_device_no:
                break;
            default:
                break;

        }
    }

    /**
     * 发送 D107 请求，获取电桩信息
     */
    private void connToServer() {

        String url = "api/pile/gun/info/query";
        HashMap<String, Object> map = new HashMap<>();
        map.put("gunCode", et_device_no.getText().toString());
        map.put("userId", CacheUtils.getIn().getUserInfo().getId());

        MyNetwork.getMyApi()
                .carRequest(url, MyMessageUtils.addBody(map))
                .map(new FlatFunction<>(Object.class))
                .compose(RxNetHelper.<Object>io_main())
                .subscribe(new ResultSubscriber<Object>() {
                    @Override
                    public void onSuccessCode(Message object) {

                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void onSuccess(Object bean) {
                        // 对象转json
                        String s = JsonUtils.objectToJson(bean);
//                        Log.e("aaaa", "s:" + s);
                        // json转 map
                        Map jsonToMap = JsonUtils.jsonToMap(s);
                        Log.e("aaaa", "jsonToMap:" + jsonToMap.toString());

                         // 充电站ID
                        String id = jsonToMap.get("stationId").toString();
                        if (!TextUtils.isEmpty(id)) {
                            Intent i = new Intent(InputDeviceNoActivity.this, DeviceDetailActivity.class);
                            i.putExtra("map", jsonToMap.toString());
                            i.putExtra("charge_pile_num", et_device_no.getText().toString());
                            startActivity(i);
                            finish();
                        } else {
                            ToastUtil.showToast(R.string.toast_D107_failed);
                        }
                    }

                    @Override
                    public void onFail(Message<Object> bean) {
                        ToastUtil.showToast(bean.msg);
                        Log.e("aaa", "msg:" + bean.msg);
                        Log.e("aaaa", "获取失败：" + bean.toString());
                    }

                });


//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("req_code", "D107");
////            jsonObject.put("charge_pile_seri", getIntent().getStringExtra("result"));
//            jsonObject.put("charge_pile_num", et_device_no.getText().toString());
//            jsonObject.put("user_id", UserParams.INSTANCE.getUser_id());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        HttpLogic httpLogic = new HttpLogic(this);
//        httpLogic.sendRequest(Config.REQUEST_URL, jsonObject, true, "rotate", new AbstractResponseCallBack() {
//
//            @Override
//            public void onResponse(Map<String, Object> map, String tag) {
//                /**
//                 * D107 response:{"station_name":"荷兰站","charge_pile_type":"01","return_msg":"操作成功.","charge_pile_num":"01","brand_id":"","charge_pile_seri":"000000012345","charge_pile_id":6,
//                 *      "time_list":[{"service_price":80,"end_time":"600","low_price":90,"start_time":"000","charge_price":90,"max_price":220,"avg_price":160,"high_price":200,"division_type":"low_price"},
//                 *                  {"service_price":80,"end_time":"1900","low_price":90,"start_time":"600","charge_price":90,"max_price":220,"avg_price":160,"high_price":200,"division_type":"max_price"},
//                 *                  {"service_price":80,"end_time":"2100","low_price":90,"start_time":"1900","charge_price":90,"max_price":220,"avg_price":160,"high_price":200,"division_type":"high_price"},
//                 *                  {"service_price":80,"end_time":"000","low_price":90,"start_time":"2100","charge_price":90,"max_price":220,"avg_price":160,"high_price":200,"division_type":"avg_price"}],
//                 *      "charge_pile_name":"超级快充桩","tariff_policy_id":6,"charge_interface":"01","charging_gun":"0","charge_method":"01","station_id":23,"pile_state":"00","return_code":"0000","charge_pile_bel":"01"}
//                 */
//                String return_code = map.get("return_code").toString();
//                if (return_code.equals("0000")) {
//                    Intent i = new Intent(InputDeviceNoActivity.this, DeviceDetailActivity.class);
//                    i.putExtra("map", map.toString());
//                    i.putExtra("charge_pile_num", et_device_no.getText().toString());
//                    startActivity(i);
//                    finish();
//                } else {
//                    ToastUtil.showToast(R.string.toast_D107_failed);
//                }
//            }
//        });
    }

    // 点击空白区域 自动隐藏软键盘
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            /**
             * 点击空白位置 隐藏软键盘
             */
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert mInputMethodManager != null;
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
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


