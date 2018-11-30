package www.qisu666.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import butterknife.BindView;
import butterknife.OnClick;
import rx.functions.Action1;
import www.qisu666.com.R;
import www.qisu666.com.app.OrderType;
import www.qisu666.com.app.PayMode;
import www.qisu666.com.callback.BillResp;
import www.qisu666.com.callback.CouponBean;
import www.qisu666.com.callback.CouponListChooseResp;
import www.qisu666.com.callback.PrePayResp;
import www.qisu666.com.callback.WeixinPayData;
import www.qisu666.com.constant.Config;
import www.qisu666.com.constant.RequestUrls;
import www.qisu666.com.pay.ali.AlipayManager;
import www.qisu666.com.pay.wx.WxPayManager;
import www.qisu666.com.request.CouponListChooseRequest;
import www.qisu666.com.request.UseBillRequest;
import www.qisu666.com.rx.RxBus;
import www.qisu666.com.rx.RxBusEvent;
import www.qisu666.com.rx.RxEventCodes;
import www.qisu666.com.utils.DateUtils;
import www.qisu666.com.utils.StringUtils;
import www.qisu666.com.utils.TVUtils;
import www.qisu666.com.utils.ToastUtil;
import www.qisu666.com.utils.UserUtils;
import www.qisu666.com.view.ChooseCouponView;
import www.qisu666.com.view.ChoosePayTypeView;
import www.qisu666.com.view.OrderDiscountView;

/**
 * Created by wujiancheng on 2017/7/31.
 * 还车成功账单
 */

public class ReturnCarSuccessBillActivity extends BaseActivity {

    @BindView(R.id.tvTitleName)
    TextView tvTitleName;
    @BindView(R.id.tvConfirmPay)
    TextView tvConfirmPay;
    @BindView(R.id.tvTotalCost)
    TextView tvTotalCost;
    @BindView(R.id.tv_order_mileage)
    TextView tvOrderMileage;
    @BindView(R.id.tv_calculate_mileage)
    TextView tvCalculateMileage;
    @BindView(R.id.tv_returncar_mileage_cost)
    TextView tvReturncarMileageCost;
    @BindView(R.id.rl_mileage_cost)
    LinearLayout rlMileageCost;
    @BindView(R.id.tv_returncar_order_cost_time)
    TextView tvReturncarOrderCostTime;
    @BindView(R.id.tv_calculate_time)
    TextView tvCalculateTime;
    @BindView(R.id.tv_returncar_order_time_cost)
    TextView tvReturncarOrderTimeCost;
    @BindView(R.id.rl_order_cost_time)
    LinearLayout rlOrderCostTime;
    @BindView(R.id.chooseCouponView)
    ChooseCouponView chooseCouponView;
    @BindView(R.id.choosePayTypeView)
    ChoosePayTypeView choosePayTypeView;//选择支付方式
    @BindView(R.id.orderDiscountView)
    OrderDiscountView orderDiscountView;
    @BindView(R.id.tv_returncar_transferMoney)
    TextView tv_returncar_transferMoney;
    @BindView(R.id.rl_transferMoney_cost)
    LinearLayout rl_transferMoney_cost;
    @BindView(R.id.rl_realElectricityMoney_time)
    LinearLayout rl_realElectricityMoney_time;
    @BindView(R.id.tv_realElectricityMoney_cost)
    TextView tv_realElectricityMoney_cost;
    @BindView(R.id.rl_minExpenditure_cost)
    LinearLayout rl_minExpenditure_cost;
    @BindView(R.id.tv_returncar_minExpenditure)
    TextView tv_returncar_minExpenditure;

    private static final int COUPON_LIST = 0;
    private static final int PAY_BALANCE = 1;

    private BillResp billResp;
    //总计费用，不包括不计免赔
    private double costTotal = 0;
    //用户余额
    private double useBalance;
    //选择的优惠券
    private CouponBean couponBean;
    private CouponListChooseResp couponListChooseResp;

    //支付类型
    private String payType;
    //是否选择优惠券
    private boolean isChooseCoupon = true;
    //不计免赔
    private double insuranceD = 0;

    @Override
    public void setView() {
        setContentView(R.layout.activity_return_car_success_bill);
    }

    @Override
    public void initDatas() {
        tvTitleName.setText("支付");
        billResp = (BillResp) getIntent().getSerializableExtra("billResp");
        if (null != billResp) {
            String costS = billResp.getCarSharingOrderMoney();
            String insuranceMoney = billResp.getInsuranceMoney();//不计免赔
            if (!TextUtils.isEmpty(costS)) {
                if (!TextUtils.isEmpty(insuranceMoney) && !"0".equals(insuranceMoney)) {
                    insuranceD = Integer.parseInt(insuranceMoney) / 100.00;
                }
                costTotal = Double.valueOf(costS) / 100.00;
            }
            tvTotalCost.setText("¥" + TVUtils.toString(costTotal));
            costTotal = costTotal - insuranceD;

            tvOrderMileage.setText("(" + billResp.getCarSharingOrderMileage() + "公里)");
            tvCalculateMileage.setText(billResp.getCarSharingOrderMileage() + "x0.99元");
            String mileageCostS = billResp.getCarSharingOrderMileageMoney();
            double mileageCost = 0;
            if (!TextUtils.isEmpty(mileageCostS)) {
                mileageCost = Double.valueOf(mileageCostS) / 100.00;
            }
            //折扣率
            int mileageDiscount = billResp.getMileageDiscount();
            if (mileageDiscount != 100) {
                //原价
                String originMileagePrice = TVUtils.toString(billResp.getMileageDiscountBeforeMoney()/100.0);
                tvReturncarMileageCost.setText(originMileagePrice + "x" + TVUtils.toString1(mileageDiscount / 10.0 + "") + "折=" + TVUtils.toString(mileageCost) + "元");
            }else {
                tvReturncarMileageCost.setText(TVUtils.toString(mileageCost) + "元");
            }
            String costTimeS = billResp.getCarSharingOrderCostTime();
            if (!TextUtils.isEmpty(costTimeS)) {
                int costTime = (int) (Double.parseDouble(costTimeS));
                tvReturncarOrderCostTime.setText("(" + DateUtils.minuteToDay(costTime) + ")");
            } else {
                tvReturncarOrderCostTime.setText("(" + costTimeS + "分钟)");
            }
            tvCalculateTime.setText(costTimeS + "x0.15元");
            String timeCostS = billResp.getCarSharingOrderTimeMoney();
            double timeCost = 0;
            if (!TextUtils.isEmpty(timeCostS)) {
                timeCost = Integer.valueOf(timeCostS) / 100.00;
            }   

            //折扣率
            int timeDiscount = billResp.getTimeDiscount();
            if (timeDiscount != 100) {
                //原价
                String originTimePrice = TVUtils.toString(billResp.getTimeDiscountBeforeMoney()/100.0);
                tvReturncarOrderTimeCost.setText(originTimePrice + "x" + TVUtils.toString1(timeDiscount / 10.0 + "") + "折=" + TVUtils.toString(timeCost) + "元");
            }else {
                tvReturncarOrderTimeCost.setText(TVUtils.toString(timeCost) + "元");
            }

            String minExpenditure = billResp.getMinExpenditure();
            if (!TextUtils.isEmpty(minExpenditure) && !TVUtils.toString(Integer.valueOf(minExpenditure) / 100).equals("0")) {
                rl_minExpenditure_cost.setVisibility(View.VISIBLE);
                tv_returncar_minExpenditure.setText(TVUtils.toString(Integer.valueOf(minExpenditure) / 100) + "元");
            }

            if (!TextUtils.isEmpty(billResp.getRealElectricityMoney())) {
                rl_realElectricityMoney_time.setVisibility(View.VISIBLE);
                tv_realElectricityMoney_cost.setText(TVUtils.toString(Integer.valueOf(billResp.getRealElectricityMoney())/100) + "元");
            } else {
                rl_realElectricityMoney_time.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(billResp.getTransferMoney())) {
                rl_transferMoney_cost.setVisibility(View.VISIBLE);
                tv_returncar_transferMoney.setText(TVUtils.toString(Integer.parseInt(billResp.getTransferMoney()) / 100.00) + "元");
            } else {
                rl_transferMoney_cost.setVisibility(View.GONE);
            }

            //夜间优惠
            orderDiscountView.setNightDiscountData(billResp.getNightReductionMoney());
            //网点折扣
            orderDiscountView.setParkDiscountData(billResp.getParkDiscountMoney(), billResp.getParkDiscountLimit());
            //企业折扣，隐藏
            orderDiscountView.setCompanyDiscountData("");
            //优惠券优惠，隐藏
            orderDiscountView.setCouponDiscountData("");
            orderDiscountView.isViewDiscountLineVisibility();
            //不计免赔
            orderDiscountView.setInsuranceMoneyData(insuranceMoney, "不能用优惠券抵扣");

            choosePayTypeView.setOnPayTypeChooseListener(new ChoosePayTypeView.OnPayTypeChooseListener() {
                @Override
                public void onPayTypeChoose(String payType) {
                    ReturnCarSuccessBillActivity.this.payType = payType;
                    //实付金额
                    double actualPayMoney = getActualPayMoney();
                    tvConfirmPay.setText("确认支付￥" + TVUtils.toString2((actualPayMoney) + ""));
                }
            });

            choosePayTypeView.setPayMoney(getActualPayMoney(), Config.ORDER_CATEGORY_TIME_RENT, false);
        }

        chooseCouponView.setOnCheckedChangeListener(new ChooseCouponView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChange(boolean isChecked) {
                isChooseCoupon = isChecked;
                choosePayTypeView.setPayMoney(getActualPayMoney(), Config.ORDER_CATEGORY_TIME_RENT, false);
            }
        });
        chooseCouponView.setOnCouponChooseListener(new ChooseCouponView.OnCouponChooseListener() {
            @Override
            public void onCouponChoose() {
                Intent intent1 = new Intent(mContext, CouponChoiceActivity.class);
                intent1.putExtra("couponData", couponListChooseResp);
                if (billResp != null) {
                    intent1.putExtra("orderId", billResp.getCarSharingOrderNumber());
                }
                startActivity(intent1);
            }
        });

        //获取优惠券列表
        getCouponsList();
        observeEvent();
    }

    private void observeEvent() {
        busSubscription = RxBus.getInstance()
                .toObservable(RxBusEvent.class)
                .subscribe(new Action1<RxBusEvent>() {
                    @Override
                    public void call(RxBusEvent rxBusEvent) {
                        switch (rxBusEvent.getEventCode()) {
                            case RxEventCodes.CHOOSE_COUPON:
                                couponBean = (CouponBean) rxBusEvent.getContent();
                                if (null == couponBean) {
                                    break;
                                }
                                chooseCouponView.setData("¥" + getCouponValue(), 0);
                                //重置余额是否可用
                                choosePayTypeView.setPayMoney(getActualPayMoney(), Config.ORDER_CATEGORY_TIME_RENT, false);
                                break;
                            case RxEventCodes.CODE_USE_CAR_PAY_SUCCESS:
                                closeDialog();
                                toComments();
                                break;
                            case RxEventCodes.CODE_CLOSE_WX_CLIENT_TIP:
                                closeDialog();
                                break;
                            default:
                                break;
                        }
                    }
                });
    }

    @Override
    public void onComplete(String result, int type) {
        if (isSuccess(result)) {
            if (type == COUPON_LIST) {
                couponListChooseResp = getBean(result, CouponListChooseResp.class);
                if (null != couponListChooseResp && couponListChooseResp.getCanUseList() != null && couponListChooseResp.getCanUseList().size() > 0) {
                    //默认选择的优惠券的位置
//                    int defaultChoosePosition = UserUtils.getIntCoupon(costTotal, data);
//                    if (defaultChoosePosition >= 0 && defaultChoosePosition < data.size()) {
//                        couponListResp = data.get(defaultChoosePosition);
//                    }
                    chooseCouponView.setData("可用(" + couponListChooseResp.getCanUseList().size() + ")", R.color.color_blue_02b2e4);
                } else {
                    setCouponUnenable();
                }
                choosePayTypeView.setPayMoney(getActualPayMoney(), Config.ORDER_CATEGORY_TIME_RENT, false);

            } else if (type == PAY_BALANCE) {
                PrePayResp msg = getBean(result, PrePayResp.class);
                if (PayMode.BALANCE_PAY_TYPE.equals(payType) || msg.getCarSharingPayMoney() == 0) {
                    ToastUtil.showImage(mContext, "支付成功");
                    //支付成功进入评价页面
                    toComments();
                } else if (PayMode.WEIXIN_PAY_TYPE.equals(payType)) {

                    WeixinPayData data = JSON.parseObject(msg.getCarSharingPayRequestData(), WeixinPayData.class);
                    WxPayManager wxPayManager = new WxPayManager();
                    wxPayManager.pay(this, application, data, msg.getCarSharingPayMoney(), RxEventCodes.CODE_USE_CAR_PAY_SUCCESS);
                } else if (PayMode.ALI_PAY_TYPE.equals(payType)) {
                    doCheck(Config.PAYING_STRING, true);
                    AlipayManager alipayManager = new AlipayManager();
                    alipayManager.pay(this, msg.getCarSharingPayRequestData(), RxEventCodes.CODE_USE_CAR_PAY_SUCCESS);
                }
            }
        } else {
            if (type == COUPON_LIST) {
                setCouponUnenable();
            }
        }
    }

    @Override
    public void onFailure(String msg, int type) {
        if (type == COUPON_LIST) {
            setCouponUnenable();
        }
    }

    private void toComments() {
        Bundle bundle = new Bundle();
        if (null != billResp) {
            bundle.putString("orderNo", billResp.getCarSharingOrderNumber());
        }
        bundle.putString("type", OrderType.CAR_ORDER);
        bundle.putInt("fromPage", 1);
        bundle.putInt("showTip", 1);//是否显示支付成功提示,1:显示

        bundle.putString("orderType", "");//订单类型，时租可不传，红包用到
        bundle.putBoolean("isPayComplete", true);//支付完成，红包用到

        //评价后列表页面刷新
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    @OnClick({R.id.ivTitleLeft, R.id.tvConfirmPay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivTitleLeft:
                activityUtil.jumpTo(ControlerActivity.class);
                finish();
                break;
            case R.id.tvConfirmPay://确认支付
                toPayOrder();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            activityUtil.jumpTo(ControlerActivity.class);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 实付金额
     *
     * @return 实付金额
     */
    private double getActualPayMoney() {
        int couponValue = getCouponValue();
        double temperaTotal = costTotal;
        if (couponValue > 0 && billResp != null && !TextUtils.isEmpty(billResp.getTransferMoney())) {
            temperaTotal = temperaTotal - Integer.valueOf(billResp.getTransferMoney()) / 100;
        }
        double payMoney = temperaTotal - couponValue;
        if (payMoney < 0) {
            payMoney = 0.00;
        }
        if (couponValue > 0 && billResp != null && !TextUtils.isEmpty(billResp.getTransferMoney())) {
            payMoney = payMoney + Integer.valueOf(billResp.getTransferMoney()) / 100;
        }
        return payMoney + insuranceD;
    }

    private void setCouponUnenable() {
        chooseCouponView.setData("无可用", R.color.color_gray_999999);
    }

    /**
     * 获取选中优惠券的值
     *
     * @return
     */
    private int getCouponValue() {
        int coupon = 0;
        if (couponBean != null) {
            //选择了优惠券
            String couponBalance = couponBean.getMoney();
            if (StringUtils.isIntOrFloat(couponBalance)) {
                coupon = (int) (Integer.parseInt(couponBalance) / 100.00);
            }
        }
        if (!isChooseCoupon) {//没选择优惠券
            coupon = 0;
        }
        return coupon;
    }

    /**
     * 获取优惠券
     */
    public void getCouponsList() {
        CouponListChooseRequest data = new CouponListChooseRequest();
        data.setCustomerId(UserUtils.getCustomerId());
        if (null != billResp) {
            data.setOrderId(billResp.getCarSharingOrderNumber());
        }
        data.setMethod(RequestUrls.QUERY_ORDER_CAN_USE_COUPON);
        doGet(data, COUPON_LIST, Config.LOADING_STRING, true);
    }

    /**
     * 支付订单
     */
    public void toPayOrder() {
        UseBillRequest data = new UseBillRequest();
        data.setCustomerId(UserUtils.getCustomerId());
        if (null != billResp) {
            data.setCarSharingOrderNumber(billResp.getCarSharingOrderNumber());
        }
        data.setCarSharingPayType(payType);
        if (null != couponBean && isChooseCoupon) {
            data.setCouponCode(couponBean.getCouponCode());
        }
        data.setMethod(RequestUrls.TO_PAY_TIME_SHARE_ORDER);
        doGet(data, PAY_BALANCE, Config.PAYING_STRING, true);
    }
}