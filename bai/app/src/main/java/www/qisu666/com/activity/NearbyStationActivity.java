package www.qisu666.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.NaviPara;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.linfaxin.recyclerview.headfoot.LoadMoreView;
import com.linfaxin.recyclerview.headfoot.impl.DefaultLoadMoreView;
import com.ta.utdid2.android.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import www.qisu666.com.R;
import www.qisu666.com.adapter.NearbyStationAdapter;
import www.qisu666.com.event.Message;
import www.qisu666.com.request.MyNetwork;
import www.qisu666.com.request.utils.FlatFunction;
import www.qisu666.com.request.utils.MyMessageUtils;
import www.qisu666.com.request.utils.ResultSubscriber;
import www.qisu666.com.request.utils.RxNetHelper;
import www.qisu666.com.utils.DialogHelper;
import www.qisu666.com.utils.JsonUtils;
import www.qisu666.com.utils.LogUtils;
import www.qisu666.com.utils.MapUtils;
import www.qisu666.com.utils.OnLoadRefreshCallBack;
import www.qisu666.com.utils.ToastUtil;
import www.qisu666.com.widget.LoadingDialog;

//附近电站
public class NearbyStationActivity extends BaseActivity implements OnLoadRefreshCallBack, NearbyStationAdapter.OnGuideClickListener, View.OnClickListener, AMapLocationListener {

    private List<Map<String, Object>> list;
    private NearbyStationAdapter adapter;
    private PopupWindow mapPopupWindow;
    private PullToRefreshListView pull_refresh_load_recycler_view;
    private LoadMoreView loadMoreView;
    private int currentPage = 1;//当前数据分页
    private static final int PAGE_NUM = 10;//每页数据条数
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;

    //定位经纬度
    private double locationLat;
    private double locationLng;

    //点击位置经纬度
    private double clickLat;
    private double clickLng;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView(R.layout.activity_collection);
        initViews();
        initMapPopupWindow();
        setAdapter();
        startLocation();
    }

    /**
     * 发送 D103 请求，获取附近电站
     */
    private void connToServer(boolean flag) {

        String url = "api/pile/station/page/query";
        HashMap<String, Object> map = new HashMap<>();
        map.put("longitude", String.valueOf(locationLng));
        map.put("latitude", String.valueOf(locationLat));
        map.put("pageIndex", currentPage);
        map.put("pageNum", PAGE_NUM);
        map.put("distance", "60");  // (不传则全部)


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
                        pull_refresh_load_recycler_view.onRefreshComplete();
                        // 对象转json
                        String s = JsonUtils.objectToJson(bean);
                        // json转 list
                        List<String> strings = JsonUtils.jsonToList(s);
                        if (strings != null) {
                            Log.e("aaaa", "recordList: " + strings.toString());
                            try {
                                JSONArray array = new JSONArray(strings);
                                int count = array.length();
                                if (count == 0) {
                                    DialogHelper.alertDialog(NearbyStationActivity.this, getString(R.string.dialog_D101_no_data));
                                } else {
                                    List<Map<String, Object>> list_temp = new ArrayList<>();
                                    for (int i = 0; i < count; i++) {
                                        JSONObject object = array.getJSONObject(i);
                                        String oString = object.toString();
                                        LogUtils.e("oString: " + oString);
                                        list_temp.add(JsonUtils.jsonToMap(oString));
                                    }
                                    if(currentPage==1){
                                        if(list_temp.size()>0){
                                            list.clear();//先清空数据
                                            list.addAll(list_temp);//再进行添加
                                            adapter.setList(list_temp);
                                        }
                                    }else{
                                        if (list_temp.size() > 0) {
                                            list.addAll(list_temp);//再进行添加
                                            adapter.loadList(list_temp);
                                            Log.e("aaaa", "list.size():" + list.size());
                                        }//有数据就加载,不应先供后续逻辑
                                    }
                                    if (list_temp.size() < 10) {
                                        ToastUtil.showToast("已全部加载！");
                                        return;
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    @Override
                    public void onFail(Message<Object> bean) {
                        pull_refresh_load_recycler_view.onRefreshComplete();
                        Log.e("aaaa", "获取失败：" + bean.toString());
                    }

                });

//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("req_code", "D103");
//            jsonObject.put("longitude", String.valueOf(locationLng));
//            jsonObject.put("latitude", String.valueOf(locationLat));
//            jsonObject.put("page_index", currentPage);
//            jsonObject.put("page_num", PAGE_NUM);
////            LogUtils.d("距离："+AMapUtils.calculateLineDistance(new LatLng(locationLat, locationLng), new LatLng(locationLat-0.1,locationLng-0.1)));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        new HttpLogic(this).sendRequest(Config.REQUEST_URL, jsonObject, flag, new PageLoadResponseCallBack(currentPage, PAGE_NUM, pull_refresh_load_recycler_view, loadMoreView, adapter, list, "record_list"));
    }

    private void setAdapter() {
        list = new ArrayList<Map<String, Object>>();
        adapter = new NearbyStationAdapter(NearbyStationActivity.this, this, list);
        pull_refresh_load_recycler_view.setAdapter(adapter);
    }

    private void initViews() {
        pull_refresh_load_recycler_view = (PullToRefreshListView) findViewById(R.id.pull_refresh_load_recycler_view);
        pull_refresh_load_recycler_view.setMode(PullToRefreshBase.Mode.BOTH);
        pull_refresh_load_recycler_view.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            //下拉刷新时会回调的方法
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
                currentPage = 1;
                connToServer(false);
            }

            //上啦加载时执行的方法
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
                currentPage++;
                connToServer(false);
            }
        });
        loadMoreView = new DefaultLoadMoreView(this);
        initTitleBar();
    }

    private void initTitleBar() {
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText("附近电站");
        View left_btn = findViewById(R.id.img_title_left);
        left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 选择地图PopupWindow
     */
    private void initMapPopupWindow() {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.popup_choice_map, null);

        TextView tv_start_navi = (TextView) contentView.findViewById(R.id.tv_start_navi);
        TextView tv_amap = (TextView) contentView.findViewById(R.id.tv_amap);
        TextView tv_baidu = (TextView) contentView.findViewById(R.id.tv_baidu);
        TextView tv_cancel = (TextView) contentView.findViewById(R.id.tv_cancel);

        tv_start_navi.setOnClickListener(this);
        tv_amap.setOnClickListener(this);
        tv_baidu.setOnClickListener(this);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapPopupWindow.dismiss();
            }
        });

        mapPopupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        mapPopupWindow.setTouchable(true);
        mapPopupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        mapPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.white));
        mapPopupWindow.setAnimationStyle(R.style.Popup_Anim_Bottom);
    }

    @Override
    public void onRefresh() {
        currentPage = 1;
        connToServer(false);
    }

    @Override
    public void onLoadMore() {
        currentPage++;
        connToServer(false);
    }

    @Override
    public void onGuideClick(int position) {
        Map<String,Object> map=list.get(position);
        try{
            JSONObject jsonObject=new JSONObject(map.get("nameValuePairs").toString());
            clickLat = Double.valueOf(jsonObject.optString("latitude"));
            clickLng = Double.valueOf(jsonObject.optString("longitude"));
            mapPopupWindow.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM, 0, 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_start_navi:
                Intent intent = new Intent(this, NaviActivity.class);
                intent.putExtra("current_lat", locationLat + "");
                intent.putExtra("current_lon", locationLng + "");
                intent.putExtra("target_lat", clickLat + "");
                intent.putExtra("target_lon", clickLng + "");

//                intent.putExtra("current", new NaviLatLng(Double.valueOf(locationLat), Double.valueOf(locationLng)));
//                intent.putExtra("target", new NaviLatLng(Double.valueOf(clickLat), Double.valueOf(clickLng)));
                startActivity(intent);
                break;
            case R.id.tv_amap:
                openAMapNavi();
                break;
            case R.id.tv_baidu:
                openBaiduMapNavi();
                break;
            default:
                break;
        }
    }

    /**
     * 打开高德地图
     */
    public void openAMapNavi() {
        // 构造导航参数
        NaviPara naviPara = new NaviPara();
        // 设置终点位置
        LatLng latLng = new LatLng(clickLat, clickLng);
        naviPara.setTargetPoint(latLng);
        // 设置导航策略，这里是避免拥堵
        naviPara.setNaviStyle(AMapUtils.DRIVING_AVOID_CONGESTION);

        // 调起高德地图导航
        try {
            AMapUtils.openAMapNavi(naviPara, getApplicationContext());
        } catch (com.amap.api.maps.AMapException e) {

            // 如果没安装会进入异常，调起下载页面
            AMapUtils.getLatestAMapApp(getApplicationContext());

        }
    }

    /**
     * 打开百度地图
     */
    public void openBaiduMapNavi() {
        try {
            Intent intent = Intent.parseUri("intent://map/direction?destination=" + MapUtils.bd_encrypt(clickLat, clickLng) + "&mode=driving&src=iDianNiu|iDianNiu#Intent;scheme=b" +
                    "dapp;package=com.baidu.BaiduMap;end", 0);
            if (isInstallByread("com.baidu.BaiduMap")) {
                startActivity(intent); //启动调用
                LogUtils.e("百度地图客户端已经安装");
            } else {
                LogUtils.e("没有安装百度地图客户端");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否安装了某应用
     *
     * @param packageName
     * @return
     */
    private boolean isInstallByread(String packageName) {
        return new File("/data/data/" + packageName).exists();
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        locationClient = new AMapLocationClient(this);
        locationOption = new AMapLocationClientOption();
        //设置定位监听
        locationClient.setLocationListener(this);
        //设置为高精度定位模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否单次定位
        locationOption.setOnceLocation(true);
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        locationClient.startLocation();
        loadingDialog = DialogHelper.loadingDialog(this, LoadingDialog.TYPE_GIF, getString(R.string.dialog_map_location));
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
            LogUtils.d("当前位置：" + amapLocation.getLatitude() + "," + amapLocation.getLongitude() + "," + amapLocation.getAddress());
            locationLat = amapLocation.getLatitude();
            locationLng = amapLocation.getLongitude();
            connToServer(true);
        } else {
            String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
            LogUtils.e("AmapErr:" + errText);

            if (NetworkUtils.isConnected(this)) {
                if (amapLocation.getErrorCode() == 12) {
                    ToastUtil.showToast(R.string.toast_permission_location);
                } else {
                    ToastUtil.showToast(getString(R.string.toast_map_location_failed) + amapLocation.getErrorInfo());
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationClient != null) {
            locationClient.onDestroy();//销毁定位客户端
            locationClient = null;
            locationOption = null;
        }

        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
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
