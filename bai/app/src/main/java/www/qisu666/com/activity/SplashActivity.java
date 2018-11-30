package www.qisu666.com.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import www.qisu666.com.R;
import www.qisu666.com.app.MyApplication;
import www.qisu666.com.callback.SplashAndActivityResp;
import www.qisu666.com.constant.Config;
import www.qisu666.com.constant.RequestUrls;
import www.qisu666.com.event.Message;
import www.qisu666.com.request.MyNetwork;
import www.qisu666.com.request.SplashAndActivityRequest;
import www.qisu666.com.request.utils.FlatFunction;
import www.qisu666.com.request.utils.MyMessageUtils;
import www.qisu666.com.request.utils.ResultSubscriber;
import www.qisu666.com.request.utils.RxNetHelper;
import www.qisu666.com.rx.RxBus;
import www.qisu666.com.rx.RxBusEvent;
import www.qisu666.com.rx.RxEventCodes;
import www.qisu666.com.rx.RxTimeCountDown;
import www.qisu666.com.sdk.stationMap.JsonUtil;
import www.qisu666.com.sdk.stationMap.StationLocation;
import www.qisu666.com.service.CIdHaveObserver;
import www.qisu666.com.service.IPushService;
import www.qisu666.com.service.MyLocationService;
import www.qisu666.com.utils.JsonUtils;
import www.qisu666.com.utils.Logger;
import www.qisu666.com.utils.SharedPreferencesUtils;
import www.qisu666.com.utils.StringUtils;
import www.qisu666.com.utils.UserUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.igexin.sdk.PushManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.util.LogUtil;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import me.iwf.photopicker.utils.PermissionsConstant;
import me.iwf.photopicker.utils.PermissionsUtils;
import rx.functions.Action1;

public class SplashActivity extends BaseActivity implements CIdHaveObserver {
    @BindView(R.id.ivSplashBg)
    ImageView ivSplashBg;
    private Intent intent;
    private static final int QUERY_SPLASH_AND_ACTIVITY = 1;
    private ArrayList<SplashAndActivityResp> splashAndActivityResps = new ArrayList<>();
    private double lat=-1;
    private double lon=-1;
    private boolean isRight=false;
    private boolean userGuide;

    @Override
    public void setView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
    }

    @Override
    public void initDatas() {
        querySplashAndActivity();

        IPushService.cidObserver = this;
        startService(new Intent(this, IPushService.class));

        observeRxEventCode();
        userGuide = SharedPreferencesUtils.getBoolean(application, Config.IS_SHOW_GUIDE,
                false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //请求定位权限。要放在onStart里，因为跳转到设置里设置权限完成后，需要在检测一次权限的授权情况
        if (PermissionsUtils.checkLocationPermission(this)) {
            startLocation();
            timeDown();
        }
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        intent = new Intent(this, MyLocationService.class);
        startService(intent);
    }

    /**
     * 倒计时
     */
    private void timeDown() {
        busSubscription = RxTimeCountDown.timeCountDown(3)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        if (integer <= 0) {
                            startMainActivity();
                        }
                    }
                });
    }



    private void observeRxEventCode() {
        busSubscription = RxBus.getInstance()
                .toObservable(RxBusEvent.class)
                .subscribe(new Action1<RxBusEvent>() {
                    @Override
                    public void call(RxBusEvent rxBusEvent) {
                        int eventCode = rxBusEvent.getEventCode();
                        d109();
                        switch (eventCode) {
                            case RxEventCodes.CODE_FINISH_LOAD_ACTIVITY:
                                Logger.i("关闭LoadActivity");
                                if (null != intent) {
                                    stopService(intent);
                                }
                                finish();
                                break;
                        }
                    }
                });
    }

    /**
     * 测试 d109 获取附近30km桩子的重载
     */
    public void d109() {
        if(!isRight){
            String url = "api/pile/station/map/query";
            HashMap<String, Object> map = new HashMap<>();
            map.put("latitude", application.latitude + "");
            map.put("longitude", application.longitude + "");

            MyNetwork.getMyApi()
                    .carRequest(url, MyMessageUtils.addBody(map))
                    .map(new FlatFunction<>(Object.class))
                    .compose(RxNetHelper.<Object>io_main())
                    .subscribe(new ResultSubscriber<Object>() {
                        @Override
                        public void onSuccessCode(www.qisu666.com.event.Message object) {

                        }

                        @Override
                        @SuppressWarnings("unchecked")
                        public void onSuccess(Object bean) {
                            // 对象转json
                            String s = JsonUtils.objectToJson(bean);
                            Log.e("aaaa", ":s" + s);
                            // json转 list
                            List<String> stringlist = JsonUtils.jsonToList(s);
                            try {
                                JSONArray array = new JSONArray(stringlist.toString());
                                int count = array.length();
                                LogUtil.e("aaa" + "返回结果-----数据大小----" + count);
                                if (count == 0) { //没有数据
                                    LogUtil.e("aaaa" + "返回结果-----没有数据");
                                } else {
                                    LogUtil.e("aaaa" + "返回结果-----有数据----");
                                    for (int i = 0; i < count; i++) {
                                        JSONObject object = array.getJSONObject(i);
                                        String station=object.optString("nameValuePairs");
                                        StationLocation stationLocation = JsonUtil.parse(station, StationLocation.class);
                                        LogUtil.e("aaaa" + i + "-每个对象station id: " + stationLocation.getStationId());
                                        try {
                                            MyApplication.getApplication().db.saveOrUpdate(stationLocation);
                                        } catch (Throwable t) {
                                            t.printStackTrace();
                                        }//保存数据库中
                                        LogUtil.e("aaaaa" + "0每个对象: " + object.toString());
                                        String oString = object.toString();
                                        LogUtil.e("aaaaa" + "每个对象: " + oString);
//                            list.add(JsonUtils.jsonToMap(oString));//添加桩子
                                    }
                                }
                                isRight=true;
//                                initIntent();
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(www.qisu666.com.event.Message<Object>bean) {
                            Log.e("aaaa", "获取失败：" + bean.toString());
                        }

                    });

//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("req_code", "D109");
//            //lon
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        HttpLogic_amap httpLogic_amap = new HttpLogic_amap(this);
//        httpLogic_amap.sendRequest(Config.REQUEST_URL, jsonObject, true, new AbstractResponseCallBack() {
//            @Override
//            public void onResponse(Map<String, Object> map, String tag) {
//                LogUtil.e(tag + "返回结果tag:" + tag);
//                LogUtil.e(tag + "返回结果:" + map.toString());
//                LogUtil.e(tag + "返回结果:" + map.get("record_list"));
////                List <StationLocation> sList = new ArrayList<StationLocation>();
//                try {
//                    JSONArray array = new JSONArray(map.get("record_list").toString());
//                    int count = array.length();
//                    LogUtil.e(tag + "返回结果-----数据大小----" + count);
//                    if (count == 0) { //没有数据
//                        LogUtil.e(tag + "返回结果-----没有数据");
//                    } else {
//                        LogUtil.e(tag + "返回结果-----有数据----");
//                        for (int i = 0; i < count; i++) {
//                            JSONObject object = array.getJSONObject(i);
//                            StationLocation stationLocation = JsonUtil.parse(object.toString(), StationLocation.class);
//                            LogUtil.e(tag + i + "-每个对象station id: " + stationLocation.getStation_id());
//                            try {
//                                IDianNiuApp.getInstance().db.saveOrUpdate(stationLocation);
//                            } catch (Throwable t) {
//                                t.printStackTrace();
//                            }//保存数据库中
//                            LogUtil.e(tag + "0每个对象: " + object.toString());
//                            String oString = object.toString();
//                            LogUtil.e(tag + "每个对象: " + oString);
////                            list.add(JsonUtils.jsonToMap(oString));//添加桩子
//                        }
//                    }
//                } catch (Throwable t) {
//                    t.printStackTrace();
//                }
////                countDown();
//            }
//        });
        }
    }

    @Override
    public void onComplete(String result, int type) {
        if (isSuccess(result)) {
            switch (type) {
                case QUERY_SPLASH_AND_ACTIVITY:
                    List<SplashAndActivityResp> data = getList(result, SplashAndActivityResp.class);
                    if (null != data && data.size() > 0) {
                        splashAndActivityResps.clear();
                        splashAndActivityResps.addAll(data);
                        //第一个是启动图片，后面的是活动图片
                        SplashAndActivityResp item = splashAndActivityResps.get(0);
                        if (null != item) {
                            String splashUrl = item.getAndroidImgUrl();
                            if (!StringUtils.isEmpty(splashUrl)) {
                                Glide.with(mContext).load(splashUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                                        ivSplashBg.setImageBitmap(resource);
                                    }
                                });
                            }
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(String msg, int type) {

    }

    private void startMainActivity() {
        Logger.e("进入app");
//         判断之前有没有显示过新手引导
        int old = SharedPreferencesUtils.getInt(mContext, "oldVersion", 1);
        PackageManager pm = application.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(application.getPackageName(), 0);
            if (pi.versionCode > old) {
                //更新版本要展示引导页
                userGuide = false;
            }
            initIntent();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initIntent(){
        synchronized (this){
//            if(isRight){
                Intent intentMain = new Intent();
                intentMain.putParcelableArrayListExtra("splashAndActivityResps", splashAndActivityResps);
                if (!userGuide) {
                    intentMain.setClass(mContext, GuideActivity.class);
                    Log.i("===intent","====GuideActivity---userGuide:"+userGuide+"---isRight:"+isRight);
                } else {
                    intentMain.setClass(mContext, ControlerActivity.class);
                    Log.i("===intent","====GuideActivity---userGuide:"+userGuide+"---isRight:"+isRight);
                }
                startActivity(intentMain);
//            }
        }
    }

    @Override
    public void afterCid() {
        if (!TextUtils.isEmpty(UserUtils.getPhone())) {
            PushManager.getInstance().bindAlias(application, UserUtils.getPhone());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            exitApplication();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void querySplashAndActivity() {
        SplashAndActivityRequest request = new SplashAndActivityRequest(UserUtils.getCityCode());
        request.setMethod(RequestUrls.QUERY_SPLASH_AND_ACTIVITY);
        doGet(request, QUERY_SPLASH_AND_ACTIVITY, "", false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PermissionsConstant.REQUEST_LOCATION) {
                startLocation();
                timeDown();
            }
        } else {
            if (requestCode == PermissionsConstant.REQUEST_LOCATION) {
                //提示打开定位权限
                toOpenLocatedPermission();
            }
        }
    }
}
