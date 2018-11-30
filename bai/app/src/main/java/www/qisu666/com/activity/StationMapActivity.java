package www.qisu666.com.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.NaviPara;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.squareup.picasso.Picasso;
import com.ta.utdid2.android.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.util.LogUtil;
import org.xutils.x;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import www.qisu666.com.R;
import www.qisu666.com.adapter.StationListAdapter;
import www.qisu666.com.anim.CloseEvarlutor;
import www.qisu666.com.anim.OpenEvarlutor;
import www.qisu666.com.droid.Activity_SelectCity;
import www.qisu666.com.event.MarkerModel;
import www.qisu666.com.logic.AbstractResponseCallBack;
import www.qisu666.com.logic.HttpLogic;
import www.qisu666.com.request.utils.Config;
import www.qisu666.com.utils.CacheUtils;
import www.qisu666.com.utils.ConstantCode;
import www.qisu666.com.utils.DialogHelper;
import www.qisu666.com.utils.Distance;
import www.qisu666.com.utils.JsonUtils;
import www.qisu666.com.utils.LogUtils;
import www.qisu666.com.utils.MapUtils;
import www.qisu666.com.utils.ToastUtil;
import www.qisu666.com.widget.LoadingDialog;


//桩点地图  废弃
public class StationMapActivity extends BaseActivity implements LocationSource,
        AMapLocationListener, AMap.OnMarkerClickListener, GeocodeSearch.OnGeocodeSearchListener, View.OnClickListener, AMap.OnCameraChangeListener, AMap.OnMapTouchListener {
    private PopupWindow mapPopupWindow;
    private TextView title,current_city;//当前城市
    private String current_city_str="";

    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private GeocodeSearch geocoderSearch;

    private View p_station_window;
    private boolean isFirst = true;

    private RelativeLayout layout_map, rl_list_frame;
    private ListView lv_station_list;
    private RelativeLayout layout_main;
    private StationListAdapter adapter;
    private List<Map<String, Object>> list;

    private boolean isShowList = false;

    //动画
    private Animator mRightOutSet;
    private Animator mLeftInSet;

    public static final String TYPE_FAST = "2", TYPE_SLOW = "1", TYPE_MIX = "3";

    private PopupWindow pop;

    private ImageView right_btn;
    private ImageView img_location;

    private MapView map_view;

    private AMap aMap;

    private double curLat, curLon, markerLat, markerLon;
    private double longitude_start, longitude_end, latitude_start, latitude_end;
    private boolean refreshFlag = true;

    private String area_code = "5810";//定位城市code
    private String cityCode = "";

    private String station_id; //选中的站点id
    private String station_name;//选中的站点名称
    private RelativeLayout.LayoutParams lp;//桩点列表列表的布局参数

    //筛选条件
    private String charge_interface,charge_carr,charge_method,charge_pile_bel,parking_free,service_time;
//    private String pile_state,charging_gun;

    private boolean finishLocation = false; //是否已定位完毕
    private boolean isTouch = false;        //手指是否触摸地图
    private boolean favorFlag = false;      //当前站点是否被收藏
    private MarkerModel curMarkerData;      //当前点击marker的数据
    private Marker curMarker;               //当前点击的marker
    private Map<String, String> filterMap;  //当前筛选条件集合
    private LoadingDialog dialog;           //请求定位的Dialog

    Map<String,Object> maps_info;//全部桩子缓存


    //当前是否geo定位
    LatLonPoint afterGeoPoint;     //geo编码后的点
    private   boolean isGeo=false; //是否切换城市


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setView(R.layout.activity_station_map);
        initView();
        initFilter();
        initMap(savedInstanceState);
//        setAnimators();
//        setCameraDistance();
    }

    /** 初始化筛选条件 */
    private void initFilter() {
        filterMap = new HashMap<>();
        filterMap.put("charge_interface", null);
        filterMap.put("charge_carr", null);
        filterMap.put("charge_method", null);
        filterMap.put("charge_pile_bel", null);
//        filterMap.put("pile_state", null);
//        filterMap.put("charging_gun", null);
        filterMap.put("parking_free", null);
        filterMap.put("service_time", null);
    }

    private void initMap(Bundle savedInstanceState) {
        map_view = (MapView) findViewById(R.id.map_view);
        map_view.onCreate(savedInstanceState);
        if(aMap == null){ aMap = map_view.getMap();  }
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        aMap.getUiSettings().setRotateGesturesEnabled(true);//是否可以旋转
        setUpMap();
    }

    private void initView() {
        layout_map = (RelativeLayout) findViewById(R.id.layout_map);
        layout_main = (RelativeLayout) findViewById(R.id.layout_main);

        rl_list_frame = (RelativeLayout) findViewById(R.id.rl_list_frame);

        img_location = (ImageView) findViewById(R.id.img_location);
        img_location.setOnClickListener(this);

        current_city =(TextView)findViewById(R.id.current_city);//当前城市
        current_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCity();//测试移动地图
            }
        });

        lv_station_list = (ListView) findViewById(R.id.lv_station_list);
        lv_station_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                station_id = list.get(position).get("station_id").toString();
                station_name = list.get(position).get("station_name").toString();
                intoDetail();
            }
        });

        initTitleBar();
        initPopupWindow();
        initMapPopupWindow();
    }

    private void initTitleBar() {
        title = (TextView) findViewById(R.id.tv_title);
        title.setText("桩点地图");
        View left_btn = findViewById(R.id.img_title_left);
        left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        right_btn = (ImageView) findViewById(R.id.img_title_right);
        ImageView right_btn2 = (ImageView) findViewById(R.id.img_title_right2);
        right_btn.setImageResource(R.mipmap.ic_station_list);
        right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHideList();
            }
        });
        right_btn2.setImageResource(R.mipmap.ic_station_filter);
        right_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StationMapActivity.this, ChargingFilterActivity.class);
                intent.putExtra("filterMap", (HashMap)filterMap);
                startActivityForResult(intent, ConstantCode.REQ_FILTER);
            }
        });
    }

    private void showOrHideList() {
        try {
            adapter.refreshData(list);
            adapter.notifyDataSetChanged();
        }catch (Throwable t){t.printStackTrace();}//刷新列表数据
        if (!isShowList) {//显示列表
//            lv_station_list.setVisibility(View.VISIBLE);
            title.setText(getResources().getString(R.string.station_list_title));
            right_btn.setImageResource(R.mipmap.ic_station_map);
            ValueAnimator va = ValueAnimator.ofObject(new OpenEvarlutor(), 0, layout_map.getHeight());
            va.setDuration(800);
            va.setInterpolator(new LinearInterpolator());
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    rl_list_frame.setTranslationY(-Float.parseFloat(animation.getAnimatedValue().toString()));
//                    layout_main.postInvalidate();
                }
            });
            va.start();
        } else {//隐藏列表
//            lv_station_list.setVisibility(View.GONE);
            title.setText(getResources().getString(R.string.station_map_title));
            right_btn.setImageResource(R.mipmap.ic_station_list);
            ValueAnimator va = ValueAnimator.ofObject(new CloseEvarlutor(), layout_map.getHeight(), 0);
            va.setInterpolator(new AccelerateInterpolator(1.5f));
            va.setDuration(650);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
//                    lp.bottomMargin = Integer.parseInt(animation.getAnimatedValue().toString());
//                    LogUtils.e(lp.bottomMargin+"bottom-margin");
                    rl_list_frame.setTranslationY(-Float.parseFloat(animation.getAnimatedValue().toString()));
//                    layout_main.postInvalidate();
                }
            });
            va.start();
        }
        LogUtils.e(layout_map.getHeight()+"height");
        isShowList = !isShowList;
    }

//    // 设置动画
//    private void setAnimators() {
//        mRightOutSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.anim_rotate_out);
//        mLeftInSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.anim_rotate_in);
//
//        // 设置点击事件
//        mRightOutSet.addListener(new AnimatorListenerAdapter() {
//            @Override public void onAnimationStart(Animator animation) {
//                super.onAnimationStart(animation);
//                right_btn.setClickable(false);
//            }
//        });
//        mLeftInSet.addListener(new AnimatorListenerAdapter() {
//            @Override public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                right_btn.setClickable(true);
//            }
//        });
//    }

    // 改变视角距离, 贴近屏幕
    private void setCameraDistance() {
        int distance = 16000;
        float scale = getResources().getDisplayMetrics().density * distance;
        layout_map.setCameraDistance(scale);
        lv_station_list.setCameraDistance(scale);
    }

    @Override
    protected void onResume() {
        super.onResume();
        map_view.onResume();

        aMap.setLocationSource(this);                          // 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置定位按钮是否显示  系统自带
        aMap.getUiSettings().setZoomControlsEnabled(false);    // 设置缩放按钮是否显示
//     aMap.setMyLocationEnabled(true);                        // 是否进行定位
    }

    @Override
    protected void onPause() {
        super.onPause();
        map_view.onPause();
        deactivate();
    }


    /**选择城市 按钮事件*/
    private void selectCity(){
        LogUtils.i("点击城市选择按钮");
        Intent intent = new Intent(StationMapActivity.this,Activity_SelectCity.class);//跳转城市选择页面  接收返回的结果
        startActivityForResult(intent,ConstantCode.SELECT_CITY);
    }


    /** 设置一些amap的属性 */
    private void setUpMap() {
//        dialog = DialogHelper.loadingDialog(this, getString(R.string.dialog_map_location));
        dialog = DialogHelper.loadingDialog(this, getString(R.string.dialog_map_location));
//        MyLocationStyle myLocationStyle = new MyLocationStyle(); // 自定义系统定位小蓝点
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.my_location_marker));// 设置小蓝点的图标
//        myLocationStyle.strokeColor(android.R.color.transparent);// 设置圆形的边框颜色
//        myLocationStyle.radiusFillColor(Color.argb(100,55,115,203));// 设置圆形的填充颜色
//        myLocationStyle.anchor(0.5f, 0.5f);//设置小蓝点的锚点
//        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置定位按钮是否显示
        aMap.getUiSettings().setZoomControlsEnabled(false);// 设置缩放按钮是否显示
        aMap.setMyLocationEnabled(true);                      // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setOnMarkerClickListener(this);
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMapTouchListener(this);
        CameraPosition.Builder builder = CameraPosition.builder();
        builder.target(new LatLng(23.117055306224895, 113.2759952545166));//先移动地图到广州
        builder.zoom(10);
        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
        list = new ArrayList<>();
        adapter = new StationListAdapter(StationMapActivity.this, list, new StationListAdapter.OnChargeBtnClickListener() {
            @Override
            public void onChargeClick(LatLng latLng) {
                markerLat = latLng.latitude;
                markerLon = latLng.longitude;
                mapPopupWindow.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM, 0, 0);
            }
        });
        lv_station_list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //模拟高德mark点数据
    @NonNull
    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> l = new ArrayList<>();
        for(int i = 0; i < 2; i ++){
            Map<String, Object> m = new HashMap();
            m.put("station_name", "海心沙广场" + i);
            m.put("charge_fee_per", "2.4");
            m.put("pile_fast_num_free", String.valueOf(new Random().nextInt(6) + 1));
            m.put("pile_slow_num_free", String.valueOf(new Random().nextInt(6) + 1));
            m.put("total", String.valueOf(Integer.parseInt(m.get("pile_fast_num_free").toString()) + Integer.parseInt(m.get("pile_slow_num_free").toString())));
            m.put("charge_method", String.valueOf(new Random().nextInt(3) + 1));
            m.put("charge_distance", "1.6");
            m.put("charge_address", "定位中...");
            m.put("latitude", String.valueOf(23.110000 + Math.random() * 1 / 30));
            m.put("longitude", String.valueOf(113.320000 + Math.random() * 1 / 30));
            l.add(m);
        }
        return l;
    }

    //位置改变的监听
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {//定位回调
        LogUtils.e("位置改变:");
        dialog.cancel();
        finishLocation = true;

        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null&& aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                curLat = aMapLocation.getLatitude();
                curLon = aMapLocation.getLongitude();
                String curCityCode = aMapLocation.getCityCode();

                //这里不变 否则一直显示深圳
                current_city_str= aMapLocation.getCity().replace("市","");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        current_city.setText(current_city_str);//ToastUtil.showToast(city);
                    }
                });

                aMap.moveCamera(CameraUpdateFactory.zoomTo(15f));
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(curLat, curLon)));

                connToServer();
                //定位后请求桩点信息，如果跟上一次定位的城市不一样，才需要请求刷新站点信息
//                if(cityCode.e quals("")){
//                    cityCode = curCityCode;
//                    connToServer();
//                } else{
//                    if(!cityCode.equals(curCityCode)){
//                        connToServer();
//                        cityCode = curCityCode;
//                    }
//                }

            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                LogUtils.e(errText);
                if(NetworkUtils.isConnected(this)){
                    if(aMapLocation.getErrorCode() == 12){
                        ToastUtil.showToast(R.string.toast_permission_location);
                    }else{
                        ToastUtil.showToast(getString(R.string.toast_map_location_failed) + aMapLocation.getErrorInfo());
                    }
                }
            }
        }
    }

    //激活高德地图的回调
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            mlocationClient.setLocationListener(this);//设置定位监听
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置为高精度定位模式
            mlocationClient.setLocationOption(mLocationOption);//设置定位参数
            mLocationOption.setOnceLocation(true);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        }
        mlocationClient.startLocation();
    }

    //暂停高德地图sdk用  释放相关资源
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
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


    //sort排序自定义规则为 哪个点离当前的经纬度距离近
    class SortByDistance implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Map<String, Object> s1 =  (Map<String, Object>)o1;
            Map<String, Object> s2 =  (Map<String, Object>)o2;
            double s1_lat=0.0,s1_lon=0.0,s2_lat=0.0,s2_lon=0.0,s1_distance=0.0,s2_distance=0.0;

            try {
                s1_lat = Double.parseDouble(s1.get("latitude").toString());
                s1_lon = Double.parseDouble(s1.get("longitude").toString());
                s2_lat = Double.parseDouble(s2.get("latitude").toString());
                s2_lon = Double.parseDouble(s2.get("longitude").toString());

                s1_distance = Distance.getDistance(afterGeoPoint.getLatitude(),afterGeoPoint.getLongitude(),s1_lat,s1_lon);//
                s2_distance = Distance.getDistance(afterGeoPoint.getLatitude(),afterGeoPoint.getLongitude(),s2_lat,s2_lon);
            }catch (Throwable t){t.printStackTrace();}


            if (s1_distance>s2_distance){
                return 1;
            }
            return  -1;
//            return s1.getAge().compareTo(s2.getAge());
//          if (s1.getAge() > s2.getAge())
//           return 1;
//          return -1;
        }
    }

    //更新mark标志点
    private void setMarkerToMap( List<Map<String, Object>> l){
        aMap.clear(true);
        Collections.sort(list, new SortByDistance());//排序

        x.task().run(new Runnable() {
            @Override
            public void run() {
                int a =0;
                if (list.size()>25){  a=25; }else {a=list.size();}
                for ( ;a>=0;a--){//遍历各个桩子   控制个数25   int a=list.size()
                    try {
                        Map m = list.get(a);
                        MarkerOptions markerOption = new MarkerOptions();
                        markerOption.position(new LatLng(Double.valueOf(m.get("latitude").toString()), Double.valueOf(m.get("longitude").toString())));
//            markerOption.draggable(true);
                        int pile_fast_num_free = Integer.valueOf(m.get("pile_fast_num_free").toString());
                        int pile_slow_num_free = Integer.valueOf(m.get("pile_slow_num_free").toString());
//            markerOption.icon(BitmapDescriptorFactory.fromView(getMarkerView(m.get("charge_station_type").toString(), String.valueOf(pile_fast_num_free+pile_slow_num_free))));
//            View markView = getMarkerView(m.get("charge_station_type").toString(), String.valueOf(pile_fast_num_free+pile_slow_num_free));

                        markerOption.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_charging_fast));//
//                        markerOption.icons(getBitmapDscriptors(m.get("charge_station_type").toString(), String.valueOf(pile_fast_num_free + pile_slow_num_free)));
//                            markerOption.icons(lists); //重新添加
//                        String type = m.get("charge_station_type").toString();
//                    LogUtils.e("获取到的类型:"+m.get("charge_station_type").toString());
                        markerOption.period(10);//去掉动画
                        Marker marker = aMap.addMarker(markerOption);

//                        marker.setObject(new MarkerModel(
//                                m.get("station_id").toString(),
//                                m.get("station_name").toString(),
//                                m.get("pile_fast_num_free").toString(),
//                                m.get("pile_slow_num_free").toString(),
//                                m.get("charge_method").toString(),
//                                m.get("charge_address").toString(),
//                                m.get("charge_distance").toString(),
//                                marker.getPosition(),
//                                m.get("is_favor").toString(),
//                                m.get("pile_num").toString(),
//                                m.get("charge_station_type").toString(),
//                                m.get("ele_fee").toString(),
//                                m.get("service_fee").toString(),
//                                m.get("park_fee").toString()
//                        ));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }//遍历结束
//                list.clear();//进行清空数据 防止缓存导致城市切换不了  这里切换导致列表没有数据
            }
        });


    }

    //添加mark标识点的view
    private ArrayList<BitmapDescriptor> getBitmapDscriptors(String type, String count){
        ArrayList<BitmapDescriptor> lists = new ArrayList<>();
        for (int i = 1;i < 6;i++){
            lists.add( BitmapDescriptorFactory.fromView(getMarkerView(type,count ,i)) );
        }
        return lists;
    }

    private View getMarkerView(String type, String count, int position){
        View view = View.inflate(StationMapActivity.this, R.layout.marker_item, null);
        WeakReference<View> abcWeakRef = new WeakReference<View>(view);

        ImageView iv_marker_icon = (ImageView) view.findViewById(R.id.iv_marker_icon);
        if(type.equals(TYPE_FAST)){
            iv_marker_icon.setImageResource(R.mipmap.ic_marker_fast);
//            Picasso.with(StationMapActivity.this).load(R.mipmap.ic_marker_fast).noFade().into(viewHolder.iv_marker_icon);
        }else if(type.equals(TYPE_SLOW)){
            iv_marker_icon.setImageResource(R.mipmap.ic_marker_slow);
//            Picasso.with(StationMapActivity.this).load(R.mipmap.ic_marker_slow).noFade().into(viewHolder.iv_marker_icon);
        }else{
            iv_marker_icon.setImageResource(R.mipmap.ic_marker_mix);
//            Picasso.with(StationMapActivity.this).load(R.mipmap.ic_marker_mix).noFade().into(viewHolder.iv_marker_icon);
        }
//        viewHolder.tv_marker_num.setText(count);

        TextView tv_marker_num =  (TextView) view.findViewById(R.id.tv_marker_num);
        tv_marker_num.setText(count);
        ImageView imgRound = (ImageView) view.findViewById(R.id.iv_round_icon);
        if (position == 0){
            imgRound.setImageResource(R.mipmap.ripple_one);
//            Picasso.with(StationMapActivity.this).load(R.mipmap.ripple_one).noFade().into(viewHolder.imgRound);
        }else if (position == 1){
            imgRound.setImageResource(R.mipmap.ripple_two);
//            Picasso.with(StationMapActivity.this).load(R.mipmap.ripple_two).noFade().into(viewHolder.imgRound);
        }else if (position == 2){
            imgRound.setImageResource(R.mipmap.ripple_three);
//            Picasso.with(StationMapActivity.this).load(R.mipmap.ripple_three).noFade().into(viewHolder.imgRound);
        }else if (position == 3){
            imgRound.setImageResource(R.mipmap.ripple_four);
//            Picasso.with(StationMapActivity.this).load(R.mipmap.ripple_four).noFade().into(viewHolder.imgRound);
        }else if (position == 4){
            imgRound.setImageResource(R.mipmap.ripple_five);
//            Picasso.with(StationMapActivity.this).load(R.mipmap.ripple_five).noFade().into(viewHolder.imgRound);
        }
        return  view;//
    }
    private View getMarkerViewBig(String type, String count){
        View view = View.inflate(this, R.layout.marker_item_big, null);
        ImageView iv_marker_icon = (ImageView) view.findViewById(R.id.iv_marker_icon);
        if(type.equals(TYPE_FAST)){
            iv_marker_icon.setImageResource(R.mipmap.ic_marker_fast);
//            Picasso.with(StationMapActivity.this).load(R.mipmap.ic_marker_fast).noFade().into(iv_marker_icon);
        }else if(type.equals(TYPE_SLOW)){
            iv_marker_icon.setImageResource(R.mipmap.ic_marker_slow);
//            Picasso.with(StationMapActivity.this).load(R.mipmap.ic_marker_slow).noFade().into(iv_marker_icon);
        }else{
            iv_marker_icon.setImageResource(R.mipmap.ic_marker_mix);
//            Picasso.with(StationMapActivity.this).load(R.mipmap.ic_marker_mix).noFade().into(iv_marker_icon);
        }
        TextView tv_marker_num = (TextView) view.findViewById(R.id.tv_marker_num);
        tv_marker_num.setText(count);
        return view;
    }

    /** 响应逆地理编码 */
    public void getAddress(final LatLonPoint latLonPoint) {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        if (rCode == 1000) {
            try{ regeocodeResult.getRegeocodeAddress().getRoads().get(0).getLatLngPoint(); }catch (Throwable t){t.printStackTrace();}
            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null&& regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
                tv_map_address.setText(regeocodeResult.getRegeocodeAddress().getFormatAddress());
                float dis_m = AMapUtils.calculateLineDistance(new LatLng(curLat, curLon), new LatLng(markerLat, markerLon)) / 1000;
                DecimalFormat format = new DecimalFormat("##0.0");
                tv_map_distance.setText(format.format(dis_m) + "km");
            } else {
                ToastUtil.showToast(R.string.toast_map_regeocode_no_data);
            }
        } else {
            ToastUtil.showToast(R.string.toast_map_regeocode_no_data);
            LogUtils.d("获取地址失败，错误码："+String.valueOf(rCode));
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {//通过地址搜索
        if (geocodeResult!=null&&geocodeResult.getGeocodeAddressList().size()>0){
            LatLonPoint myPoint = geocodeResult.getGeocodeAddressList().get(0).getLatLonPoint();
            CameraPosition.Builder builder = CameraPosition.builder();
//      builder.target(new LatLng(113.2759952545166 , 23.117055306224895));//先移动地图到广州
            builder.target(new LatLng(myPoint.getLatitude(),myPoint.getLongitude()));// 移动地图
            builder.zoom(13);//
            aMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));///
//            aMap.moveCamera(CameraUpdateFactory.zoomTo(15f));
//            aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(myPoint.getLatitude(),myPoint.getLongitude())));
            afterGeoPoint = myPoint;//更新切换城市后 经纬度坐标问题
            isGeo=true;
//            curLat=myPoint.getLatitude();
//            curLon=myPoint.getLongitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{current_city.setText(current_city_str);}catch (Throwable t){t.printStackTrace();}
                }
            });

            connToServer();//不再重新请求点信息
//            updateToServer();

        }
    }

    /**重写mark点的点击事件*/
    @Override
    public boolean onMarkerClick(Marker marker) {
        refreshFlag = false;
//        marker.setZIndex(100);//设置z轴(远近)  坐标
        openMarker(marker);
        curMarker = marker;
        curMarkerData = (MarkerModel) marker.getObject();
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15), 500l, new AMap.CancelableCallback() {
            @Override
            public void onFinish() { LogUtil.e("高德地图 动画结束的监听"); }
            @Override
            public void onCancel() { LogUtil.e("高德地图 动画取消的监听"); }
        });
        markerLat = marker.getPosition().latitude;//mark点的经纬度
        markerLon = marker.getPosition().longitude;
//        getAddress(new LatLonPoint(marker.getPosition().latitude, marker.getPosition().longitude));
        showPopWindow((MarkerModel) marker.getObject());
        return true;
    }

    /**打开mark点*/
    private void openMarker(Marker marker){
        MarkerModel markerModel = (MarkerModel)(marker.getObject());
        int pile_fast_num_free = Integer.valueOf(markerModel.getCount_fast());
        int pile_slow_num_free = Integer.valueOf(markerModel.getCount_slow());
        marker.setIcon(BitmapDescriptorFactory.fromView(getMarkerViewBig(markerModel.getCharge_method(), String.valueOf(pile_fast_num_free + pile_slow_num_free))));
    }

    /**关闭mark点*/
    private void closeMarker(Marker marker){
        MarkerModel markerModel = (MarkerModel)(marker.getObject());
        int pile_fast_num_free = Integer.valueOf(markerModel.getCount_fast());
        int pile_slow_num_free = Integer.valueOf(markerModel.getCount_slow());
//        marker.setIcon(BitmapDescriptorFactory.fromView(getMarkerView(markerModel.getCharge_method(), String.valueOf(pile_fast_num_free + pile_slow_num_free))));
        marker.setIcons(getBitmapDscriptors(markerModel.getCharge_method(),String.valueOf(pile_fast_num_free + pile_slow_num_free)));
    }

    /**桩子信息等textview的声明*/
    private TextView tv_map_address, tv_map_title, tv_charging_fee, tv_service_fee, tv_station_parking_fee, tv_map_distance, tv_free_fast, tv_free_slow, tv_elec_type;
    private ImageView iv_collection;

    /** 站点详情PopupWindow */
    private void initPopupWindow() {
        View contentView = LayoutInflater.from(this).inflate( R.layout.popup_station_info, null); // 一个自定义的布局，作为显示的内容

        View btn_map_charging = contentView.findViewById(R.id.btn_map_charging);
        tv_map_address = (TextView) contentView.findViewById(R.id.tv_map_address);
        tv_map_title = (TextView) contentView.findViewById(R.id.tv_map_title);
        tv_charging_fee = (TextView) contentView.findViewById(R.id.tv_charging_fee);
        tv_service_fee = (TextView) contentView.findViewById(R.id.tv_service_fee);
        tv_station_parking_fee = (TextView) contentView.findViewById(R.id.tv_station_parking_fee);
        tv_map_distance = (TextView) contentView.findViewById(R.id.tv_map_distance);
        tv_free_fast = (TextView) contentView.findViewById(R.id.tv_free_fast);
        tv_free_slow = (TextView) contentView.findViewById(R.id.tv_free_slow);
        tv_elec_type = (TextView) contentView.findViewById(R.id.tv_elec_type);
        iv_collection = (ImageView) contentView.findViewById(R.id.iv_collection);
        if(TextUtils.isEmpty(CacheUtils.getIn().getUserInfo().getId())){
            iv_collection.setVisibility(View.GONE);
        }else{
            iv_collection.setVisibility(View.VISIBLE);
        }

        tv_map_address.setText("定位中...");

        p_station_window = contentView.findViewById(R.id.p_station_window);
        p_station_window.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intoDetail();
                pop.dismiss();
            }
        });

        iv_collection.setOnClickListener(this);
        btn_map_charging.setOnClickListener(this);

        pop = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        pop.setTouchable(true);
        pop.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        pop.setBackgroundDrawable(getResources().getDrawable(R.color.white));
        pop.setAnimationStyle(R.style.Popup_Anim_Bottom);
        pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closeMarker(curMarker);
            }
        });
    }

    /**跳转到桩子详情页面*/
    private void intoDetail() {
        Intent intent = new Intent(StationMapActivity.this, StationInfoActivity.class);
        intent.putExtra("station_id", station_id);
        intent.putExtra("station_name", station_name);
        startActivityForResult(intent, ConstantCode.REQ_OPEN_STATION_INFO);
    }

    /**桩点信息*/
    @SuppressLint("SetTextI18n")
    private void showPopWindow(MarkerModel markerModel){
        station_id = markerModel.getId();
        String is_favor = markerModel.getIs_favor();
        favorFlag = is_favor.equals("0");
        if(is_favor.equals("0")){
//            iv_collection.setImageResource(R.mipmap.ic_collection_yellow);
            Picasso.with(StationMapActivity.this).load(R.mipmap.ic_collection_yellow).noFade().into(iv_collection);
        }else if(is_favor.equals("1")){
//            iv_collection.setImageResource(R.mipmap.ic_collection_gray);
            Picasso.with(StationMapActivity.this).load(R.mipmap.ic_collection_gray).noFade().into(iv_collection);
        }else{
            iv_collection.setVisibility(View.GONE);
        }
        station_name = markerModel.getTitle();
        tv_map_title.setText(markerModel.getTitle());

        try {
            double chargingfee=Double.parseDouble(markerModel.getCharging_fee());
            double servicefee=Double.parseDouble(markerModel.getService_fee());
            tv_charging_fee.setText(new BigDecimal(chargingfee-servicefee).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+"");//充电费=chargingFee-服务费
        }catch (Throwable t){t.printStackTrace();}
//        tv_charging_fee.setText(markerModel.getCharging_fee());
        tv_service_fee.setText(markerModel.getService_fee());
        tv_station_parking_fee.setText(markerModel.getStation_parking_fee());
        double b = new BigDecimal(markerModel.getDistance()).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        tv_map_distance.setText(String.valueOf(b) + "km");
        tv_free_fast.setText("空闲" + markerModel.getCount_fast());
        tv_free_slow.setText("空闲" + markerModel.getCount_slow());
        tv_map_address.setText(markerModel.getAddress());
        // tv_elec_type.setText(markerModel.getElec_type().equals("00") ? "交流" : markerModel.getElec_type().toString().equals("01") ? "直流" : "直流/交流");
        tv_elec_type.setText("总桩数" + markerModel.getPile_num());
        pop.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM, 0, 0);
    }

    /**统一处理click事件*/
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_amap: mapPopupWindow.dismiss(); openAMapNavi(); break;//打开高德地图
            case R.id.tv_start_navi://开始导航
                Intent intent = new Intent(this, NaviActivity.class);
                intent.putExtra("current_lat",curLat+"");
                intent.putExtra("current_lon",curLon+"");
                intent.putExtra("target_lat",markerLat+"");
                intent.putExtra("target_lon",markerLon+"");
//                intent.putExtra("current", new NaviLatLng(curLat, curLon));
//                intent.putExtra("target", new NaviLatLng(markerLat, markerLon));
                startActivity(intent);
                mapPopupWindow.dismiss();
                break;
            case R.id.tv_baidu: mapPopupWindow.dismiss(); openBaiduMapNavi(); break;//打开百度地图
            case R.id.btn_map_charging:  pop.dismiss();  mapPopupWindow.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM, 0, 0); break;//点击充电 弹出桩子信息
            case R.id.iv_collection: favorAction(); break;//收藏or取消收藏
            case R.id.img_location://定位,点击蓝色原点
//                if(mlocationClient!=null && finishLocation){
//                    dialog.show();
//                    finishLocation = false;
//                    mlocationClient.startLocation();
                aMap.setMyLocationEnabled(true);//只进行位置移动,由于已经监听位置移动  可以实时重新刷新地图上桩子信息
//                }
                break;
        }
    }

    /**收藏 取消站点*/
    private void favorAction() {
        JSONObject jsonObject = new JSONObject();
        if(!favorFlag){//收藏站点
            try {
                jsonObject.put("req_code", "D104");
                jsonObject.put("user_id", CacheUtils.getIn().getUserInfo().getId());
                jsonObject.put("s_token", CacheUtils.getIn().getUserInfo().getToken());
                jsonObject.put("station_id", station_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{ //取消收藏站点
            try {
                jsonObject.put("req_code", "D106");
                jsonObject.put("user_id", CacheUtils.getIn().getUserInfo().getId());
                jsonObject.put("s_token", CacheUtils.getIn().getUserInfo().getToken());
                jsonObject.put("station_id", station_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        HttpLogic httpLogic = new HttpLogic(this);
        httpLogic.sendRequest(Config.REQUEST_URL, jsonObject, true, new AbstractResponseCallBack() {

            @Override
            public void onResponse(Map<String,Object> map, String tag) {
                favorFlag = !favorFlag;
                if(favorFlag){
                    ToastUtil.showToast(R.string.toast_D104);
                    iv_collection.setImageResource(R.mipmap.ic_collection_yellow);
                    curMarkerData.setIs_favor("0");
                }else {
                    ToastUtil.showToast(R.string.toast_D106);
                    iv_collection.setImageResource(R.mipmap.ic_collection_gray);
                    curMarkerData.setIs_favor("1");
                }
                curMarker.setObject(curMarkerData);
            }
        });
    }

    /**activity返回的结果result*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==ConstantCode.REQ_FILTER && resultCode==ConstantCode.RES_FILTER){
            LogUtils.d(data.getSerializableExtra("filter").toString());
            HashMap<String, String> map = (HashMap<String, String>) data.getSerializableExtra("filter");
            charge_interface = map.get("charge_interface");
            charge_carr = map.get("charge_carr");
            charge_method = map.get("charge_method");
            charge_pile_bel = map.get("charge_pile_bel");
//            pile_state = map.get("pile_state");
//            charging_gun = map.get("charging_gun");
            parking_free = map.get("parking_free");
            service_time = map.get("service_time");

            for(Map.Entry<String, String> entry1:filterMap.entrySet()){
                String m1value = entry1.getValue() == null?"":entry1.getValue();
                String m2value = map.get(entry1.getKey())==null?"":map.get(entry1.getKey());
                if (!m1value.equals(m2value)) { //若两个map中相同key对应的value不相等 ,筛选条件变更重新请求
                    filterMap.clear();
                    filterMap = map;
                    connToServer();
                    break;
                }
            }
        }else if(requestCode == ConstantCode.REQ_OPEN_STATION_INFO && resultCode == ConstantCode.RES_OPEN_STATION_INFO && data != null){
            if(curMarker != null){
                MarkerModel m = (MarkerModel) curMarker.getObject();
                boolean is_favor = data.getBooleanExtra("is_favor", false);
                m.setIs_favor(is_favor ? "0" : "1");
                curMarker.setObject(m);
            }
        }else if (requestCode==ConstantCode.SELECT_CITY){
            aMap.setLocationSource(this);// 设置定位监听
            LogUtils.d("获取到的城市---:"+ data);
            try{ LogUtils.d("获取到的城市未:"+ data.getStringExtra("city"));}catch (Throwable t){t.printStackTrace();}//防止异常
            String str ="";
            try{ str=data.getStringExtra("city"); }catch (Throwable t){t.printStackTrace();str="";}
            if (!str.equals("")) {
                current_city_str=str;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        current_city.setText(current_city_str);//更新当前城市名
                    }
                });
                GeocodeSearch geocodeSearch = new GeocodeSearch(this);//构造 GeocodeSearch 对象，并设置监听。
                geocodeSearch.setOnGeocodeSearchListener(this);
                //通过GeocodeQuery设置查询参数,调用getFromLocationNameAsyn(GeocodeQuery geocodeQuery) 方法发起请求。
                //address表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode都ok
                GeocodeQuery query = new GeocodeQuery(current_city_str, " ");//str 为城市  南京 北京
                geocoderSearch.getFromLocationNameAsyn(query);
            }

        }
    }

    /** 选择地图PopupWindow */
    private void initMapPopupWindow() {
        View contentView = LayoutInflater.from(this).inflate( R.layout.popup_choice_map, null);// 一个自定义的布局，作为显示的内容

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
        mapPopupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
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

    /** 打开高德地图 */
    public void openAMapNavi(){
        // 构造导航参数
        NaviPara naviPara = new NaviPara();
        // 设置终点位置
        LatLng latLng = new LatLng(markerLat, markerLon);
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

    /**  打开百度地图 */
    public void openBaiduMapNavi(){
        try {
            LogUtils.i("markerLat:" + markerLat + "||markerLon:" + markerLon);
            Intent intent = Intent.parseUri("intent://map/direction?destination=" + MapUtils.bd_encrypt(markerLat, markerLon) + "&mode=driving&src=iDianNiu|iDianNiu#Intent;scheme=b" +
                    "dapp;package=com.baidu.BaiduMap;end",0);
            if(isInstallByread("com.baidu.BaiduMap")){
                startActivity(intent); //启动调用
                LogUtils.e("百度地图客户端已经安装") ;
            }else{
                ToastUtil.showToast(R.string.toast_app_uninstall_baidu);
                LogUtils.e("没有安装百度地图客户端") ;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**包路径是否存在*/
    private boolean isInstallByread(String packageName) {
        return new File("/data/data/" + packageName).exists();
    }

    /**地图边缘 范围?*/
    private LatLngBounds getRange(CameraPosition cameraPosition){
        Projection projection = aMap.getProjection();
        return projection.getMapBounds(cameraPosition.target, cameraPosition.zoom);
    }

    /**高德地图 摄像头位置改变*/
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        LogUtil.e("摄像头位置改变:lon--"+cameraPosition.target.longitude+",lat--"+cameraPosition.target.latitude);
    }

    /**高德地图  摄像头位置改变 完毕*/
    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
//        if(refreshFlag) {
//            LogUtils.d("cameraPosition:" + cameraPosition.target.toString() + "," + cameraPosition.zoom + ",finishLocation=" + finishLocation);
//            LatLngBounds latLngBounds = getRange(cameraPosition);
//            latitude_start = latLngBounds.southwest.latitude;
//            latitude_end = latLngBounds.northeast.latitude;
//            longitude_start = latLngBounds.southwest.longitude;
//            longitude_end = latLngBounds.northeast.longitude;
//            if (finishLocation && !isTouch) {
//                connToServer();
//            }
//        }else{
//            refreshFlag = true;
//        }
//        LogUtils.i("西南角:" + latLngBounds.southwest.latitude + "," + latLngBounds.southwest.longitude + "||||--------------||||东北角:" + latLngBounds.northeast.latitude + "," + latLngBounds.northeast.longitude);
    }

    /**  发送 D101 请求，获取附近电站 */
    private void connToServer() {
        if (maps_info!=null){
            updateToServer();//用本地数据更新
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("req_code", "D101");
//            jsonObject.put("longitude_start", String.valueOf(longitude_start));
//            jsonObject.put("longitude_end", String.valueOf(longitude_end));
//            jsonObject.put("latitude_start", String.valueOf(latitude_start));
//            jsonObject.put("latitude_end", String.valueOf(latitude_end));

            jsonObject.put("area_code", area_code);//城市编码
            jsonObject.put("latitude", String.valueOf(curLat));
            jsonObject.put("longitude", String.valueOf(curLon));
            jsonObject.put("charge_interface", charge_interface);
            jsonObject.put("self_support", charge_carr);
            jsonObject.put("charge_method", charge_method);
            jsonObject.put("charge_pile_bel", charge_pile_bel);
//            jsonObject.put("pile_state", pile_state);
//            jsonObject.put("charging_gun", charging_gun);
            jsonObject.put("parking_free", parking_free);
            jsonObject.put("service_time", service_time);
            if(!TextUtils.isEmpty(CacheUtils.getIn().getUserInfo().getId())){
                jsonObject.put("user_id", CacheUtils.getIn().getUserInfo().getId());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        LogUtil.e("请求桩子地图时的参数 城市编码："+area_code);
        LogUtil.e("请求桩子地图时的参数 service_time："+service_time);              //服务时间  0全时段
        LogUtil.e("请求桩子地图时的参数 curlat："+curLat);//当前lat
        LogUtil.e("请求桩子地图时的参数 curLon："+curLon);//当前lon
        LogUtil.e("请求桩子地图时的参数 charge_pile_bel："+charge_pile_bel);        //桩子类型 01 公共桩  02个人
        LogUtil.e("请求桩子地图时的参数 user_id："+CacheUtils.getIn().getUserInfo().getId());//用户id
        LogUtil.e("请求桩子地图时的参数 charge_method："+charge_method);       //充电方式
        LogUtil.e("请求桩子地图时的参数 req_code："+"D101");                   //接口编码
        LogUtil.e("请求桩子地图时的参数 self_support："+charge_carr);          //0自营  1非自营
        LogUtil.e("请求桩子地图时的参数 charge_interface："+charge_interface); //充电接口 1国标  2特斯拉
        LogUtil.e("请求桩子地图时的参数 parking_free："+parking_free);          //是否免费停车 0免费停车
        LogUtil.e("请求桩子地图时的参数 distance："+"30");                      //公里数  30=30km
        LogUtil.e("请求桩子地图时的参数 城市编码："+area_code);
        LogUtil.e("请求桩子地图时的参数 城市编码："+area_code);


        //本地测试用的数据
//        list = getData();
//        setMarkerToMap(list);
//        adapter.refreshData(list);

        HttpLogic httpLogic = new HttpLogic(this);
        httpLogic.sendRequest(Config.REQUEST_URL, jsonObject, true, new AbstractResponseCallBack() {

            @Override
            public void onResponse(Map<String,Object> map, String tag) {
                aMap.clear(true);
                list.clear();
                maps_info=map;


                // if (map.get("record_list") != null && !"".equals(map.get("record_list".toString()))) {
                Object oRecordList = maps_info.get("record_list");	//

                if(null != oRecordList) {
                    String recordList = oRecordList.toString();
                    LogUtils.d("recordList: " + recordList);
                    try {
                        JSONArray array = new JSONArray(recordList);
                        int count = array.length();
                        if(count == 0) {
                            DialogHelper.alertDialog(StationMapActivity.this, getString(R.string.dialog_D101_no_data));
                        } else {
                            for (int i = 0; i < count; i++) {
                                JSONObject object = array.getJSONObject(i);
                                String oString = object.toString();
                                LogUtils.d("oString: " + oString);

//                                String distance_str= (String) object.get("charge_distance");//
//
//                                LogUtils.e("转换后的距离："+distance_str);
//                                double distance_doub = Double.parseDouble(distance_str);
//                                LogUtils.e("转换后的距离："+distance_doub);
//                                LogUtils.e("转换后的距离size-----："+list.size());


                                try {
                                    String lat_str = (String) object.get("latitude");
                                    double lat_doub = Double.parseDouble(lat_str);
                                    LogUtil.e("转换后的lat：" + lat_doub);
                                    String lon_str = (String) object.get("longitude");
                                    double lon_doub = Double.parseDouble(lon_str);
                                    LogUtil.e("转换后的lon：" + lon_doub);
                                    double dis = Distance.getDistance(afterGeoPoint.getLatitude(), afterGeoPoint.getLongitude(), lat_doub, lon_doub);
                                    LogUtil.e("dis转换 计算后距离：" + dis);
                                    if (dis < 100) {//100千米之内
                                        list.add(JsonUtils.jsonToMap(oString));//添加桩子
                                    }
                                }catch (Throwable t){
                                    t.printStackTrace();
                                    try {
                                        String distance_str = (String) object.get("charge_distance");
                                        LogUtils.e("转换后的距离：" + distance_str);
                                        double distance_doub = Double.parseDouble(distance_str);
                                        LogUtils.e("转换后的距离：" + distance_doub);
                                        LogUtils.e("转换后的距离size-----：" + list.size());
                                        if (distance_doub <= 200.00) {
                                            if (list.size() > 25) {
                                                continue;
                                            }//展示最近的10个
                                            list.add(JsonUtils.jsonToMap(oString));//station_name
                                        }
                                    }catch (Throwable t1){t1.printStackTrace();}
                                }



                            }
                            if (list.size()<=0){
                                DialogHelper.alertDialog(StationMapActivity.this, getString(R.string.dialog_D101_no_data));
                                return;
                            }
                            setMarkerToMap(list);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // should handle the case as the count == 0
                    DialogHelper.alertDialog(StationMapActivity.this, getString(R.string.dialog_D101_no_data));
                }
            }
        });
    }

    /**D101的本地数据更新 如果本地有数据不再调用connToServer()改为调用该函数 */
    private void updateToServer(){  //从本地数据进行更新
        aMap.clear(true);//清空缓冲数据   防止不更新
        list.clear();
        Object oRecordList = maps_info.get("record_list"); //
        if(null != oRecordList) {
            String recordList = oRecordList.toString();
            LogUtils.d("recordList: " + recordList);
            try {
                JSONArray array = new JSONArray(recordList);
                int count = array.length();
                if(count == 0) {
                    DialogHelper.alertDialog(StationMapActivity.this, getString(R.string.dialog_D101_no_data));
                } else {

                    for(int a=0;a<count;a++){

                    }

                    for (int i = 0; i < count; i++) {
                        JSONObject object = array.getJSONObject(i);
                        String oString = object.toString();
                        LogUtil.d("转换总数: " + i+"/"+count);
                        LogUtil.d("oString: " + oString);

                        String distance_str= (String) object.get("charge_distance");//
                        LogUtil.e("转换后的距离："+distance_str);
                        double distance_doub = Double.parseDouble(distance_str);
                        LogUtil.e("转换后的距离："+distance_doub);
                        LogUtil.e("转换后是否切换城市："+isGeo +maps_info.size());
//                        if (list.size()>25){   continue;  }// 跳过 执行后面逻辑    全部添加进去  控制最终个数
                        if(isGeo){//通过经纬度坐标计算
                            String lat_str= (String) object.get("latitude");
                            double lat_doub = Double.parseDouble(lat_str);
                            LogUtil.e("转换后的lat："+lat_doub);
                            String lon_str= (String) object.get("longitude");
                            double lon_doub = Double.parseDouble(lon_str);
                            LogUtil.e("转换后的lon："+lon_doub);
                            double dis =Distance.getDistance(afterGeoPoint.getLatitude(),afterGeoPoint.getLongitude(),lat_doub,lon_doub);
                            LogUtil.e("dis转换 计算后距离："+dis);
                            if(dis<100){//100千米之内
                                list.add(JsonUtils.jsonToMap(oString));//添加桩子
                            }
                        }else{
                            if (distance_doub<=200.00){ list.add(JsonUtils.jsonToMap(oString));  }// 距离 200km以内 就ok
                        }

                    }
                    isGeo=false;//重置切换标志位
                    setMarkerToMap(list);
                }
                adapter.notifyDataSetChanged();

            } catch (JSONException e) { e.printStackTrace(); }
        }
    }


    /**0325修改-获取30km内地图桩子信息 */
    private void connToServer2(){


    }




    /**获取当前的状态是否在触摸*/
    @Override
    public void onTouch(MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
//            LogUtils.d("onTouch:ACTION_DOWN");
            isTouch = true;
        } else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
//            LogUtils.d("onTouch:ACTION_UP");
            isTouch = false;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(isFirst){
//            lp = (RelativeLayout.LayoutParams) rl_list_frame.getLayoutParams();
//            lp.bottomMargin = layout_map.getHeight();
////            LogUtils.e("height:" + layout_map.getHeight());
////            rl_list_frame.setLayoutParams(lp);
//            isFirst = false;
            try{   rl_list_frame.setTranslationY(0 - layout_map.getHeight());}catch (Throwable t){t.printStackTrace();}
            isFirst = false;
        }
    }//没研究

    /**重写返回键,解决列表返回异常*/
    @Override
    public void onBackPressed() { //正在显示列表的话// isShowList = !isShowList;
        if (isShowList){ showOrHideList(); return ; }else{ super.onBackPressed();  }
    }

    /**对高德地图的view进行保存  及恢复*/
    @Override
    protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); map_view.onSaveInstanceState(outState); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map_view.onDestroy();
    }



}
