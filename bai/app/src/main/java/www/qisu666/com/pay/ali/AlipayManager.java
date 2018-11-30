package www.qisu666.com.pay.ali;

import android.app.Activity;

import com.alipay.sdk.app.PayTask;
import www.qisu666.com.rx.RxBus;
import www.qisu666.com.rx.RxBusEvent;
import www.qisu666.com.rx.RxEventCodes;
import www.qisu666.com.rx.RxSchedulersHelper;
import www.qisu666.com.utils.Logger;
import www.qisu666.com.utils.ToastUtil;

import java.lang.ref.WeakReference;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Administrator on 2017/3/27.
 * 支付宝支付的管理类
 */

public class AlipayManager {
    private static final String TAG = AlipayManager.class.getSimpleName();
    /**
     * 支付宝支付业务：入参app_id
     */
//    public static final String APPID = "2017041106640030";
//    public static final String APPID = "2016080300157952";

    /** 商户私钥，pkcs8格式 */
    /** 如下私钥，RSA2_PRIVATE 或者 RSA_PRIVATE 只需要填入一个 */
    /** 如果商户两个都设置了，优先使用 RSA2_PRIVATE */
    /** RSA2_PRIVATE 可以保证商户交易在更加安全的环境下进行，建议使用 RSA2_PRIVATE */
    /** 获取 RSA2_PRIVATE，建议使用支付宝提供的公私钥生成工具生成， */
    /**
     * 工具地址：https://doc.open.alipay.com/docs/doc.htm?treeId=291&articleId=106097&docType=1
     */
//    public static final String RSA2_PRIVATE = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDPZcrYGA+frYPBpwurWJ2ROvrrLf5N23wAim5WFzthZ7crW71r+6DmL9qVqlPtNcdEIlwSpzSp0L3d46p3Se5cpg73zMd3uoiylvKBQO/jObhymNRLLp7ys7umholoeIoI/xRElya6N+v7VvmQ4YgiZoD/bW9IGk2DLo/3DnVf7bHMYblpLINIpLEjqg14K1IfKM+IibtTpLTPVFFO0OaiXPFQz73qcpVmdv6h0+i/kyBSA4K0HExiXAckKSnrHd/Lpgi/9/B673zHS+8f8ee5mu8HnpDqOAjKcLYtDHl3ogxFnUOiYxmBfL5hPy+sqJmpN+rWZY/QEaZOZqH0cmSTAgMBAAECggEBAK+dnEObLC+jt7uU/Xab1OCTNsFJ8tpujfPPRFq11VxHfHzEdAMp0TbZMpgk4Ggub4ssJRbOavOYhA2NqFzWR3ZATtBZoFWrp/DBjJsSJ48a4yp2at02nvUYaD88EMJbv1aa18i0zOBGVhY/sZeSwbmPXo/rVJ2TWCtKlDE5Fv6gOvyRSAwgp7NcpRL1gTeWEiFdBP8NUq3E4aJzI4+fbO26NfiKE/AJbTBTZAl1I3/srI3SInIYBHLjMt35d+jfLlY4UZqQW5bJZtB9d+LIaMrOGnfhofTCBMwuWtnAKTer94LcmYK82MQM4cMMpWejhay83zosjsF/r/orCzSg6kECgYEA6LcRcaXZolYEwylxqa+XbNJHan7HM1FOf4ea67kCetw7doGifgyjgjv0wRYIG9ymULV6r6GGQDXoY9YkETrRYJnlP6mAN2WB1P9zCZbG3DGqmx3zxBAxM8rzlCBBAZeCVr/5hb16dP17XQ8muUTwBqP8w9DzXiqk3Bul/uGf3AMCgYEA5CY5Wa7bU3tkhRrStO6tE0uvWZjnPARU5pfCFCHwWj0icyqgeTwMZ7dfPmG6c1wHE2mmD6j/xLGkoVpFUhoU5z3A3DDydW6H11xcRruf/6RcUvZoe5zr6XX50hX4YIZRz6yvOgk7Em7HCM37v5hwvfn9slwuysYHRFOQwXwTGDECgYEA1F1rf8iXqHrS8b46SV1B1D64iUiURBJojhyTlZVJ3BBp+yUlwd2KwOgx3JpcYACljXVvNiMyvhnzrlfWphqEw6DtAYGOzf7F0jgjJJcBdiOkytU0hLE6FUeZrI0ahXdbrAqYjCFzRJulHjEbZ2qvl3/CxHdD/6TuuSZJ/ySqmz0CgYAoSANqLTA2LhwSUCkRnu5Z8Mv14sQ42X7v861lhpR8Kj0R3l3U6eA606pyRij4QzBQoh+atjjqTn8Fqf2BQG+eYE3T0qSMTUaYpIr6Rhg6t5dBYpYR9SPF5XmFOTHQQQbixtdtRMH0QDDOAihHrK+DK0c6nYH5dTtFOgfd0X1BkQKBgGBy+LS48eSPtd6dDh2MG52N1XytkDsIw9Zzea4Eku31TdQ9INaG4Hc4OOzPYQfITw6VRXbyPzX79fAdhoIOjPmtPLcXmbUYbi7UbylZBZZcncQv1Kk//5KuJxy2YZSvYdZ9FTg24qUd+MRUrgReF1vLNLvwJM4aa/w3W4I0Xx8K";

    private Subscription mSubscriptionResult;

    /**
     * 支付
     *
     * @param type
     */
    public void pay(final Activity activity, final String orderInfo, final int type) {
        final WeakReference<Activity> weakReference = new WeakReference<>(activity);

        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo的获取必须来自服务端；
         */
//        boolean rsa2 = (RSA2_PRIVATE.length() > 0);

//        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID, rsa2, totalAmount, "subject测试", "body测试");
//        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);
//
//        String privateKey = RSA2_PRIVATE;
//        String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);
//        final String orderInfo = orderParam + "&" + sign;
//        Logger.i(TAG,"orderInfo == "+orderInfo);

        Observable<Map<String, String>> observable = Observable.create(new Observable.OnSubscribe<Map<String, String>>() {
            @Override
            public void call(Subscriber<? super Map<String, String>> subscriber) {
                if (weakReference.get() == null) {
                    return;
                }
                //向服务器发起支付
                PayTask alipay = new PayTask(activity);
                Map<String, String> result = alipay.payV2(orderInfo, false);
                Logger.i("支付结果", "支付结果 == " + result.toString());
                Logger.i("支付结果", "支付结果 == " + result.get("resultStatus"));
                subscriber.onNext(result);
            }
        });

        Subscriber<Map<String, String>> subscriber = new Subscriber<Map<String, String>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Map<String, String> stringStringMap) {
                RxBus rxBus = RxBus.getInstance();
                RxBusEvent rxBusEvent = new RxBusEvent();
                if (stringStringMap.get("resultStatus").equals("9000")) {

                    if (type == RxEventCodes.CODE_RECHARGE_PAY_SUCCESS) {//余额
                        ToastUtil.showImage(activity, "充值成功");
                        //通知改变余额
                        rxBusEvent.setEventCode(RxEventCodes.CODE_RECHARGE_PAY_SUCCESS);
                        activity.finish();
                    } else if (type == RxEventCodes.CODE_PLEDGE_PAY_SUCCESS) {//押金
                        ToastUtil.showImage(activity, "交纳成功");
                        //通知改变押金状态，成功
                        rxBusEvent.setEventCode(RxEventCodes.CODE_PLEDGE_PAY_SUCCESS);
                        activity.finish();
                    } else if (type == RxEventCodes.CODE_ORDER_PAY_SUCCESS) {//订单支付
                        ToastUtil.showImage(activity, "支付成功");
                        rxBusEvent.setEventCode(RxEventCodes.CODE_ORDER_PAY_SUCCESS);
//                        activity.finish();
                    } else if (type == RxEventCodes.CODE_USE_CAR_PAY_SUCCESS) {//用车支付
                        ToastUtil.showImage(activity, "支付成功");
                        rxBusEvent.setEventCode(RxEventCodes.CODE_USE_CAR_PAY_SUCCESS);
//                        activity.finish();
                    } else if (type == RxEventCodes.CODE_LONG_USE_CAR_PAY_SUCCESS) {//短租用车支付
                        ToastUtil.showImage(activity, "支付成功");
                        rxBusEvent.setEventCode(RxEventCodes.CODE_LONG_USE_CAR_PAY_SUCCESS);
//                        activity.finish();
                    }
                } else {
                    rxBusEvent.setEventCode(RxEventCodes.CODE_CLOSE_WX_CLIENT_TIP);
                }
                rxBus.post(rxBusEvent);
            }
        };

        mSubscriptionResult = observable.compose(RxSchedulersHelper.<Map<String, String>>ioMain())
                .subscribe(subscriber);

    }

    public void unregister() {
        //将支付结果返回的Subscription注销
        if (null != mSubscriptionResult) {
            if (!mSubscriptionResult.isUnsubscribed()) {
                mSubscriptionResult.unsubscribe();
            }
        }
    }
}
