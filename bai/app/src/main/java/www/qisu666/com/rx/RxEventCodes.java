package www.qisu666.com.rx;

/**
 * Created by wujiancheng on 2017/8/8.
 */

public class RxEventCodes {
    public static final int FINISH_LOGIN = 1;//关闭登录页面
    public static final int CHOOSE_COUPON = 2;//选择优惠券
    public static final int REFRESH_COUPON = 3;//刷新优惠券列表
    //充值
    public static final int CODE_RECHARGE_PAY_SUCCESS = 4;//余额充值成功
    public static final int CODE_PLEDGE_PAY_SUCCESS = 5;//押金充值成功
    public static final int CODE_ORDER_PAY_SUCCESS = 6;//订单(列表)支付成功
    public static final int CODE_USE_CAR_PAY_SUCCESS = 7;//用车支付成功
    public static final int CODE_LONG_USE_CAR_PAY_SUCCESS = 8;//短租用车支付成功

    //推送
    public static final int SERVER_PUSH_CLOSE_DOOR_SUCCESS = 9;//关门成功
    public static final int SERVER_PUSH_DETECTION_SUCCESS = 10;//状态检测成功
    public static final int SERVER_PUSH_ORDER_CANCEL = 11;//取消订单
    public static final int SERVER_PUSH_COUPON_SUCCESS = 12;//注册送优惠券
    public static final int SERVER_PUSH_BING_COUPON = 13;//绑定优惠券
    public static final int SERVER_PUSH_RETURN_CAR = 14;//后台还车成功

    public static final int CODE_CLOSE_WX_CLIENT_TIP = 15;//提示没安装微信客户端时，关闭加载对话框
    public static final int CODE_FINISH_LOAD_ACTIVITY = 16;//关闭loadActivity

    public static final int CODE_SEARCH_PARK = 17;//搜索网点
    public static final int CODE_CHOOSE_CITY = 18;//选择城市

    //绑定优惠券成功
    public static final int CODE_BIND_COUPON_SUCCESS = 19;//绑定优惠券成功
    public static final int CODE_TO_ROUTE = 20;//导航
    //登录成功，没有跳转到恢复页面，预约之前的页面要更新一下，从而知道是企业用户还是普通用户
    public static final int CODE_UPDATE_PRE_ORDER_CAR = 21;

    public static final int CODE_ORDER_CAR_ERROR = 22;//预约车辆错误
    public static final int CODE_RECOVER_LONG_RENT = 23;//登录后复活短租

    //退出后在主页重新请求一下灰度
    public static final int CODE_REQUEST_HUI_DU = 24;//退出登录后，重新请求一下灰度
    public static final int CODE_SHARE_CALL_BACK = 25;//分享回调

    public static final int CODE_WX_NICKNAME = 26;//获取微信昵称
    public static final int CODE_CLOSE_PARKING_FEE = 27;//关闭停车费填写的两个页面
    public static final int CODE_REPEAT_INVOICE_NO = 28;//存在重复的发票号码

    public static final int CODE_CANCEL_REFUND_PLEDGE_SUCCESS = 29;//取消押金退款成功

    public static final int CODE_LOCATE_STATUS = 30;//定位状态
    public static final int CODE_SEARCH_PARK_FROM_PRE_ORDER_HOUR_RENT = 31;//搜索网点，从预约时租进入
    public static final int CODE_SEARCH_PARK_FROM_USE_CAR = 32;//搜索网点，从用车页面进入

    public static final int CODE_IS_TIP_CAN_RETURN = 33;//是否显示“已进入还车范围”提示
    public static final int CODE_REMOVE_GEO_FENCE = 34;//移除地理围栏

    public static final int CODE_UPDATE_COUPON_COUNT = 35;//更新优惠券的数量显示
    public static final int CODE_CANCEL_LONG_RENT_ORDER = 36;//取消短租订单

    public static final int CODE_RED_PACKET_DISMISS_CALLBACK = 37;//红包弹出消失后的回调
    public static final int CODE_LONG_RENT_BEFORE_FINISH_TIP = 38;//短租套餐到期之前提示
    public static final int CODE_DRAWLAYOUT_STATUS = 39;//drawlayout状态
}
