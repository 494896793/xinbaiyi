package www.qisu666.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import www.qisu666.com.R;
import www.qisu666.com.app.PayMode;
import www.qisu666.com.app.UserState;
import www.qisu666.com.callback.CarParkResp;
import www.qisu666.com.callback.CarResp;
import www.qisu666.com.callback.ParkResp;
import www.qisu666.com.callback.PreRechargeResp;
import www.qisu666.com.callback.UserInfoResp;
import www.qisu666.com.callback.WeixinPayData;
import www.qisu666.com.constant.Config;
import www.qisu666.com.constant.RequestUrls;
import www.qisu666.com.fragment.UseCarPreOrderHourRentFragment;
import www.qisu666.com.fragment.UseCarPreOrderLongRentFragment;
import www.qisu666.com.pay.ali.AlipayManager;
import www.qisu666.com.pay.wx.WxPayManager;
import www.qisu666.com.request.CancelRefundRequest;
import www.qisu666.com.request.ChargeRequest;
import www.qisu666.com.rx.RxBus;
import www.qisu666.com.rx.RxBusEvent;
import www.qisu666.com.rx.RxEventCodes;
import www.qisu666.com.utils.CacheUtils;
import www.qisu666.com.utils.Logger;
import www.qisu666.com.utils.StringUtils;
import www.qisu666.com.utils.ToastUtil;
import www.qisu666.com.utils.UserUtils;
import www.qisu666.com.view.CarInfoOrderView;
import www.qisu666.com.view.CustomAlertDialog;
import www.qisu666.com.view.CustomAlertDialog2;
import www.qisu666.com.view.CustomAlertDialogPay;
import www.qisu666.com.view.ViewPagerWithTitleView;
import www.qisu666.com.view.ViewPagerWithTitleViews;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.functions.Action1;

/**
 * Created by wujiancheng on 2017/7/28.
 * 预约车辆前的确认用车
 */

public class UseCarPreOrderingActivity extends BaseActivity {
    private static final String TAG = UseCarPreOrderingActivity.class.getSimpleName();

    //带背景的dialog
    private static final int DIALOG_NO_CAR = 1;
    private static final int DIALOG_COMFIRN_USE_CAR = 2;
    private static final int DIALOG_NO_DEPOSIT = 3;//押金不足
    private static final int DIALOG_NO_BALANCE = 4;//余额不足
    private static final int DIALOG_TO_PAY = 5;//未支付，去支付

    //不带背景的dialog
    private static final int DIALOG_INTERTNET_TIP = 1;//网络请求的提示
    private static final int DIALOG_NO_COMPLETE_VERIFY = 2;//没有完成资质认证
    private static final int DIALOG_ADD_PLEDGE = 3;//增加押金额度

    private static final int REQUEST_CANCEL_REFUND = 1;//取消押金退款
    private static final int QUERY_PAY_PLEDGE = 2;//支付押金

    @BindView(R.id.tvTitleName)
    TextView tvTitleName;
    @BindView(R.id.carInfoOrderView)
    CarInfoOrderView carInfoOrderView;
    @BindView(R.id.viewPagerWithTitleView)
    ViewPagerWithTitleViews viewPagerWithTitleView;
    @BindView(R.id.title_bar)
    RelativeLayout title_bar;

    private CarResp carResp;
    private ParkResp parkResp;

    private static final int HOUR_RENT = 0;
    private static final int LONG_RENT = 1;
    private UseCarPreOrderHourRentFragment hourRentFragment;
    private UseCarPreOrderLongRentFragment longRentFragment;

    private CarParkResp carParkRespRecover;
    private boolean isRecover;

    private String payPledgeCategory;//补交押金支付类型

    @Override
    public void setView() {
        setContentView(R.layout.activity_use_car_pre_ordering);
    }

    @Override
    public void initDatas() {
        tvTitleName.setText("确认用车");
        carResp = (CarResp) getIntent().getSerializableExtra("carInfo");
        parkResp = (ParkResp) getIntent().getSerializableExtra("parkInfo");
        title_bar.setBackgroundColor(getResources().getColor(R.color.content_bg));


        //判断复活
        recoverData();

        if (null != carResp) {
            //电池剩余量
            int batteryPercent = 0;
            String battery = carResp.getBatteryResidual() + "";
            if (!StringUtils.isEmpty(battery)) {
                double batteryD = Double.valueOf(battery);
                if (batteryD > 100) {
                    batteryD = 100;
                } else if (batteryD < 0) {
                    batteryD = 0;
                }
                batteryPercent = (int) batteryD;
            }
            carInfoOrderView.setArrowRightVisibility(View.GONE);
            //车辆信息
            carInfoOrderView.setData(carResp.getCarColor(),carResp.getCarSetsNums(),carResp.getModels(),carResp.getCarImgUri(), carResp.getCarNumber(), carResp.getCarBrand(),
                    carResp.getBatteryResidual(), carResp.getCanUseMileage(), carResp.getIsRedPkCar() == 1, carResp.getIsRedPkCar() == 1);
        }

        setViewPager();

        observeEvent();
    }

    private void recoverData() {
        UserInfoResp userInfoResp = CacheUtils.getIn().getUserInfo();
        if (null != userInfoResp && UserState.isOrdering(userInfoResp.getStatus())) {
            carParkRespRecover = (CarParkResp) getIntent().getSerializableExtra("recoverData");
            if (null != carParkRespRecover) {
                carResp = carParkRespRecover.getCarBaseInfo();
                parkResp = carParkRespRecover.getParkBaseinfo();
                isRecover = true;
            }
        }
    }

    private void setViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        Bundle bundle = new Bundle();
        bundle.putSerializable("carResp", carResp);
        bundle.putSerializable("parkResp", parkResp);

        //标题
        List<String> titles = new ArrayList<String>() {{
            add("分时租赁");
            add("短租");
        }};

        hourRentFragment = new UseCarPreOrderHourRentFragment();
        hourRentFragment.setArguments(bundle);

        if (isRecover && null != carParkRespRecover) {//说明是复活
            bundle.putSerializable("packageInfo", carParkRespRecover.getPackageInfo());
            bundle.putSerializable("userResurgenceRentOrderInfo", carParkRespRecover.getUserResurgenceRentOrderInfo());
        }
        longRentFragment = new UseCarPreOrderLongRentFragment();
        longRentFragment.setArguments(bundle);

        fragments.add(hourRentFragment);
        fragments.add(longRentFragment);

        viewPagerWithTitleView.setData(this, titles, fragments);

        if (isRecover) {//复活跳到短租页面
            viewPagerWithTitleView.setCurrentItem(LONG_RENT);
        }
    }

    private void observeEvent() {
        busSubscription = RxBus.getInstance()
                .toObservable(RxBusEvent.class)
                .subscribe(new Action1<RxBusEvent>() {
                    @Override
                    public void call(RxBusEvent rxBusEvent) {
                        switch (rxBusEvent.getEventCode()) {
                            case RxEventCodes.CODE_ORDER_CAR_ERROR://预约车辆错误
                                Map<String, String> map = (Map<String, String>) rxBusEvent.getContent();
                                //                车辆下线，不可预约-- 原来：300302  ，现在：20
                                //                未审核或者处于用车状态，暂时不能进行约车-- 原来：300303，现在：600202
                                //                您的驾驶证即将到期，请重新上传驾驶证  -- 原来： 300304，现在：600203
                                //                会员资金被冻结，不可约车 -- 原来： 300305，现在：600204
                                //                您有未支付订单，请先支付后在约车-- 原来： 300306，现在：600205
                                //                车辆已经他人预约  -- 原来 ：300307  ，现在：600210
                                //                会员的退款申请正在审核当中，不可约车  -- 原来： 300308，现在：600206
                                //                押金不足，不能预订车辆，请充值！  -- 原来： 300309，现在：600207
                                //                余额不足，不能预订车辆，请充值！ -- 原来： 300310，现在：600208
                                //                还车失败订单不存在  -- 原来： 400001，现在：600209
                                //                单号不存在 --  原来： 300302，现在：600209
                                //                预约订单不成功其他原因-- 原来： 300302，现在：600201

                                String code = map.get("code");
                                String result = map.get("result");
                                if ("600203".equals(code)) {//您的驾驶证即将到期，请重新上传驾驶证
                                    Intent intent = new Intent(mContext, IdVerifyStatusActivity.class);
                                    startActivity(intent);
                                    ToastUtil.show(mContext, getMsg(result));
                                } else if ("600205".equals(code)) {//您有未支付订单，请先支付后在进行约车
                                    showFailureDialog(DIALOG_TO_PAY, "有未支付订单", getMsg(result), "取消", "去支付");
                                } else if ("600210".equals(code)) {
                                    showFailureDialog(DIALOG_NO_CAR, "手慢了", getMsg(result), "取消", "更多车辆");
                                } else if ("600206".equals(code)) {//取消退押金，确认用车
                                    showFailureDialog(DIALOG_COMFIRN_USE_CAR, "确认用车吗？", getMsg(result), "取消用车", "确认用车");
                                } else if ("600207".equals(code)) {
                                    showFailureDialog(DIALOG_NO_DEPOSIT, "未交纳用车保证金", getMsg(result), "一会再说", "立即交纳");
                                } else if ("600208".equals(code)) {
                                    showFailureDialog(DIALOG_NO_BALANCE, "余额不足", getMsg(result), "取消", "充值");
                                } else if ("600216".equals(code)) {//未完成资质认证
                                    showDialog(getMsg(result), DIALOG_NO_COMPLETE_VERIFY, 0);
                                } else if ("600218".equals(code)) {//用车需要提升押金额度
                                    showDialog(getMsg(result), DIALOG_ADD_PLEDGE, getBean(result, Integer.class));
                                } else {//约车时20或其他错误码
                                    showDialog(getMsg(result), DIALOG_INTERTNET_TIP, 0);
                                }
                                break;
                            //登录成功，恢复页面，则先把该页面关掉
                            case RxEventCodes.CODE_RECOVER_LONG_RENT:
                                finish();
                                break;
                            case RxEventCodes.CODE_CLOSE_WX_CLIENT_TIP://微信或支付宝支付取消，关闭加载框
                                Logger.i(TAG, "关闭加载框");
                                closeDialog();
                                break;
                            case RxEventCodes.CODE_PLEDGE_PAY_SUCCESS://押金补交充值成功
                                closeDialog();
                                break;
                            default:
                                break;
                        }
                    }
                });
    }

    /**
     * 设置剩余电量的背景
     */
//    private void setLeftBatteryBg(final double battery) {
//        ViewTreeObserver vto2 = rlytEnduranceBg.getViewTreeObserver();
//        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                rlytEnduranceBg.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                Logger.i(TAG, rlytEnduranceBg.getHeight() + "," + rlytEnduranceBg.getWidth());
//
//                RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) viewLeftEndurance.getLayoutParams(); //取控件textView当前的布局参数 linearParams.height = 20;// 控件的高强制设成20
//                linearParams.width = (int) (battery / 100.00 * rlytEnduranceBg.getWidth());// 强制设置控件的宽
//                viewLeftEndurance.setLayoutParams(linearParams); //使设置好的布局参数应用到控件
//            }
//        });
//    }
    @Override
    public void onComplete(String result, int type) {
        if (isSuccess(result)) {
            if (type == REQUEST_CANCEL_REFUND) {//取消退款成功
                //预约车辆
                if (viewPagerWithTitleView.getCurrentPosition() == HOUR_RENT) {//预约时租
                    hourRentFragment.orderCarRequest();
                } else if (viewPagerWithTitleView.getCurrentPosition() == LONG_RENT) {//预约短租
                    longRentFragment.requestOrderLongRentCombo();
                }
            } else if (type == QUERY_PAY_PLEDGE) {//支付押金
                PreRechargeResp resp = getBean(result, PreRechargeResp.class);
                if (PayMode.ALI_PAY_TYPE.equals(payPledgeCategory)) {
                    doCheck("请稍后...", true);
                    AlipayManager alipayManager = new AlipayManager();
                    alipayManager.pay(this, resp.getCustomerRechargeRequestData(), RxEventCodes.CODE_PLEDGE_PAY_SUCCESS);
                } else if (PayMode.WEIXIN_PAY_TYPE.equals(payPledgeCategory)) {
                    WeixinPayData data = JSON.parseObject(resp.getCustomerRechargeRequestData(), WeixinPayData.class);
                    WxPayManager wxPayManager = new WxPayManager();
                    wxPayManager.pay(this, application, data, resp.getCustomerRechargeMoney(), RxEventCodes.CODE_PLEDGE_PAY_SUCCESS);
                }
            }
        } else {
            if (type == REQUEST_CANCEL_REFUND) {//取消退款失败
                if (Config.REQUEST_ORDER_NOT_EXIST.equals(getCode(result))) {
                    showDialog(getMsg(result), DIALOG_INTERTNET_TIP, 0);
                }
            } else if (type == QUERY_PAY_PLEDGE) {//支付押金失败
                ToastUtil.show(mContext, getMsg(result));
            }
        }
    }

    @Override
    public void onFailure(String msg, int type) {

    }

    @OnClick({R.id.ivTitleLeft})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivTitleLeft://返回
                finish();
                break;
        }
    }

    /**
     * 显示失败对话框
     */
    private void showFailureDialog(final int type, String title, String message, String negativeText, String positiveText) {
        final CustomAlertDialog2 dialog2 = CustomAlertDialog2.getAlertDialog(this, true, true);

        if (type == DIALOG_NO_CAR || type == DIALOG_NO_BALANCE) {//车辆被人预约了或余额不足
            dialog2.setTitle(title);
            dialog2.setBackgroundImg(R.mipmap.dialog_failure_bg);
        } else if (type == DIALOG_COMFIRN_USE_CAR) {//取消退押金，确认用车
            dialog2.setTitle(title);
            dialog2.setBackgroundImg(R.mipmap.dialog_prompt_bg);
        } else if (type == DIALOG_NO_DEPOSIT) {//押金不足
            dialog2.setTitle(title);
            dialog2.setBackgroundImg(R.mipmap.dialog_failure_bg);
        } else if (type == DIALOG_TO_PAY) {//去支付
            dialog2.setTitle(title);
            dialog2.setBackgroundImg(R.mipmap.dialog_failure_bg);
        }

        dialog2.setMessage(message)
                .setOnPositiveClickListener(positiveText, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type == DIALOG_NO_CAR) {//车辆被人预约了
                            Intent intent = new Intent(mContext, ControlerActivity.class);
                            startActivity(intent);
                        } else if (type == DIALOG_COMFIRN_USE_CAR) {//取消退押金，确认用车
                            CancelRefundRequest request = new CancelRefundRequest();
                            request.setCustomerId(UserUtils.getCustomerId());
                            request.setMethod(RequestUrls.CANCEL_REFUND);
                            doGet(request, REQUEST_CANCEL_REFUND, "", false);
                        } else if (type == DIALOG_NO_DEPOSIT) {//没交押金
                            Intent intent = new Intent(mContext, PledgeActivity.class);
                            intent.putExtra("noDeposit", true);
                            startActivity(intent);
                        } else if (type == DIALOG_NO_BALANCE) {//余额不足
                            Intent intent = new Intent(mContext, RechargeBalanceActivity.class);
                            startActivity(intent);
                        } else if (type == DIALOG_TO_PAY) {//去支付
                            Intent intent = new Intent(mContext, AllOrderActivity.class);
                            startActivity(intent);
                        }
                        dialog2.dismiss();
                    }
                });
        if (type == DIALOG_COMFIRN_USE_CAR || type == DIALOG_NO_DEPOSIT || type == DIALOG_NO_BALANCE
                || type == DIALOG_TO_PAY || type == DIALOG_NO_CAR) {
            dialog2.setOnNegativeClickListener(negativeText, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog2.dismiss();
                }
            });
        }
        dialog2.show();
    }

    private void showDialog(String msg, int type, final Integer pledge) {
        final CustomAlertDialog alertDialog = CustomAlertDialog.getAlertDialog(mContext, true, true);
        alertDialog.setMessage(msg);
        if (type == DIALOG_INTERTNET_TIP) {//网络请求错误提示
            alertDialog.setBtnConfirmColor(R.color.new_primary)
                    .setOnPositiveClickListener("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
        } else if (type == DIALOG_NO_COMPLETE_VERIFY) {//资质认证
            alertDialog.setBtnConfirmColor(R.color.new_primary)
                    .setOnPositiveClickListener("去认证", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(mContext, IdVerifyStatusActivity.class));
                            alertDialog.dismiss();
                        }
                    })
                    .setBtnCancelColor(R.color.main_background)
                    .setOnNegativeClickListener("稍后再说", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
        } else if (type == DIALOG_ADD_PLEDGE) {//用车提升押金额度
            alertDialog.setBtnConfirmColor(R.color.new_primary)
                    .setOnPositiveClickListener("去交纳", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showPayPledgeDialog(pledge);
                            alertDialog.dismiss();
                        }
                    })
                    .setBtnCancelColor(R.color.main_background)
                    .setOnNegativeClickListener("稍后再说", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
        }

        alertDialog.show();
    }

    /**
     * 补交押金
     */
    private void showPayPledgeDialog(final Integer pledge) {
        int toPayPledge = 0;
        if (pledge != null) {
            toPayPledge = pledge / 100;
        }
        final CustomAlertDialogPay dialogPay = CustomAlertDialogPay.getAlertDialog(mContext, true, true);
        dialogPay
                .setOnPositiveClickListener("交纳", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //微信或支付宝充值
                        ChargeRequest data = new ChargeRequest();
                        data.setBankType(payPledgeCategory);
                        data.setRechargeType(Config.DEPOSIT_TYPE);
                        data.setRechargeMoney(pledge + "");//用车补交押金
                        //                data.setRechargeMoney("" + 1);//500块押金
                        data.setCustomerId(UserUtils.getCustomerId());
                        data.setMethod(RequestUrls.PRE_RECHARGE);
                        doGet(data, QUERY_PAY_PLEDGE, "请稍后...", true);
                        dialogPay.dismiss();
                    }
                })
                .setWxDesc("微信(" + toPayPledge + "元)").setAliDesc("支付宝(" + toPayPledge + "元)")
                .setOnPayTypeClickListener(new CustomAlertDialogPay.OnPayTypeClickListener() {
                    @Override
                    public void onPayTypeClick(String payType) {
                        payPledgeCategory = payType;
                    }
                })
                .show();
    }
}
