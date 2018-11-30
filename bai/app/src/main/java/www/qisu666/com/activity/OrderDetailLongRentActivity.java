package www.qisu666.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.lankton.flowlayout.FlowLayout;
import rx.functions.Action1;
import www.qisu666.com.R;
import www.qisu666.com.app.CarOrderState;
import www.qisu666.com.app.PayMode;
import www.qisu666.com.callback.BillLongRentResp;
import www.qisu666.com.callback.CouponBean;
import www.qisu666.com.callback.CouponListChooseResp;
import www.qisu666.com.callback.EvaluateInfoResp;
import www.qisu666.com.callback.OrderListResp;
import www.qisu666.com.callback.ParkingFeeAuditStatusResp;
import www.qisu666.com.callback.PrePayResp;
import www.qisu666.com.callback.WeixinPayData;
import www.qisu666.com.constant.Config;
import www.qisu666.com.constant.RequestUrls;
import www.qisu666.com.pay.ali.AlipayManager;
import www.qisu666.com.pay.wx.WxPayManager;
import www.qisu666.com.request.CouponListChooseRequest;
import www.qisu666.com.request.LongRentExtraPayRequest;
import www.qisu666.com.request.ParkingFeeAuditStatusRequest;
import www.qisu666.com.rx.RxBus;
import www.qisu666.com.rx.RxBusEvent;
import www.qisu666.com.rx.RxEventCodes;
import www.qisu666.com.utils.DPUtil;
import www.qisu666.com.utils.DateUtils;
import www.qisu666.com.utils.StringUtils;
import www.qisu666.com.utils.TVUtils;
import www.qisu666.com.utils.ToastUtil;
import www.qisu666.com.utils.UserUtils;
import www.qisu666.com.view.ChooseCouponView;
import www.qisu666.com.view.ChoosePayTypeView;
import www.qisu666.com.view.LongRentBillView;
import www.qisu666.com.view.OrderDiscountView;

/**
 * 订单详情，短租
 */
public class OrderDetailLongRentActivity extends BaseActivity {
    private static final int REQUEST_CODE_COMMENT = 1;
    @BindView(R.id.ivTitleLeft)
    ImageView ivTitleLeft;
    @BindView(R.id.tvTitleName)
    TextView tvTitleName;
    @BindView(R.id.tvTitleRight)
    TextView tvTitleRight;
    @BindView(R.id.llytTitleRight)
    LinearLayout llytTitleRight;
    @BindView(R.id.ivCarPic)
    ImageView ivCarPic;
    @BindView(R.id.tvOrderDetailUnfold)
    TextView tvOrderDetailUnfold;
    @BindView(R.id.ivOrderDetailDown)
    ImageView ivOrderDetailDown;
    @BindView(R.id.llytOrderDetailUnfold)
    LinearLayout llytOrderDetailUnfold;
    @BindView(R.id.layoutBillDetail)
    LinearLayout layoutBillDetail;
    @BindView(R.id.tvCarSeatNum)
    TextView tvCarSeatNum;
    @BindView(R.id.tvCarNumber)
    TextView tvCarNumber;
    @BindView(R.id.tvCarTypeName)
    TextView tvCarTypeName;
    @BindView(R.id.llyt_car_number)
    LinearLayout llytCarNumber;
    @BindView(R.id.tvCarOrderNumber)
    TextView tvCarOrderNumber;
    @BindView(R.id.tvUseCarTime)
    TextView tvUseCarTime;
    @BindView(R.id.tvGetCarAddr)
    TextView tvGetCarAddr;
    @BindView(R.id.tvReturnCarAddr)
    TextView tvReturnCarAddr;
    @BindView(R.id.tvTotalPay)
    TextView tvTotalPay;
    @BindView(R.id.longRentBillView)
    LongRentBillView longRentBillView;
    @BindView(R.id.orderDiscountView)
    OrderDiscountView orderDiscountView;
    @BindView(R.id.tvDiscountCompany)
    TextView tvDiscountCompany;//标签
    @BindView(R.id.llytPayContainer)
    LinearLayout llytPayContainer;
    @BindView(R.id.chooseCouponView)
    ChooseCouponView chooseCouponView;//选择优惠券
    @BindView(R.id.choosePayTypeView)
    ChoosePayTypeView choosePayTypeView;//选择支付方式
    @BindView(R.id.tvCarColorName)
    TextView tvCarColorName;
    @BindView(R.id.tv_carorder_evaluate)
    TextView tvCarorderEvaluate;
    @BindView(R.id.rating_comment_order)
    RatingBar ratingCommentOrder;
    @BindView(R.id.tv_car_order_star)
    TextView tvCarOrderStar;
    @BindView(R.id.returnCarAddress)
    TextView returnCarAddress;
    @BindView(R.id.getCarAddress)
    TextView getCarAddress;
    @BindView(R.id.orderId)
    TextView orderId;
    @BindView(R.id.tv_evaluate_content)
    TextView tvEvaluateContent;
    @BindView(R.id.rl_car_order_detail_info)
    RelativeLayout rlCarOrderDetailInfo;
    @BindView(R.id.llytEvaluateContainer)
    LinearLayout llytEvaluateContainer;
    @BindView(R.id.tvToEvaluate)
    TextView tvToEvaluate;
    @BindView(R.id.tvToPay)
    TextView tvToPay;
    @BindView(R.id.flowLayout)
    FlowLayout flowLayout;
    @BindView(R.id.tvStatus)
    TextView tvStatus;

    private static final int COUPON_LIST = 0;
    private static final int PAY_BALANCE = 1;
    private static final int QUERY_AUDIT_STATUS = 2;//查询停车费报销审核进度
    private String orderNo;//orderId
    private OrderListResp mData;
    //总计费用，不包括不计免赔
    private double costTotal = 0;
    //选择的优惠券
    private CouponBean couponBean;
    //支付类型
    private String payType;
    //是否选择优惠券
    private boolean isChooseCoupon = true;
    private CouponListChooseResp couponListChooseResp;

    //1:后台还车后跳转到订单列表后，按返回键跳到主页
    private int toMain = 0;
    //不计免赔
    private double insuranceD = 0;
    private ParkingFeeAuditStatusResp parkingFeeAuditStatusResp;

    @Override
    public void setView() {
        setContentView(R.layout.activity_order_detail_long_rent);
    }

    @Override
    public void initDatas() {
        tvTitleName.setText("订单详情");

        mData = (OrderListResp) getIntent().getExtras().getSerializable("orderItem");
        orderNo = mData.getOrderId();
        setTextData();

        toMain = getIntent().getExtras().getInt("toMain");

        chooseCouponView.setOnCheckedChangeListener(new ChooseCouponView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChange(boolean isChecked) {
                isChooseCoupon = isChecked;
                choosePayTypeView.setPayMoney(getActualPayMoney(), Config.ORDER_CATEGORY_LONG_RENT, false);
            }
        });
        chooseCouponView.setOnCouponChooseListener(new ChooseCouponView.OnCouponChooseListener() {
            @Override
            public void onCouponChoose() {
                Intent intent1 = new Intent(mContext, CouponChoiceActivity.class);
                intent1.putExtra("couponData", couponListChooseResp);
                intent1.putExtra("orderId", orderNo);
                startActivity(intent1);
            }
        });

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
                                choosePayTypeView.setPayMoney(getActualPayMoney(), Config.ORDER_CATEGORY_LONG_RENT, false);
                                break;
                            case RxEventCodes.CODE_ORDER_PAY_SUCCESS:
                                closeDialog();
                                toComments();
                                break;
                            case RxEventCodes.CODE_CLOSE_WX_CLIENT_TIP:
                                closeDialog();
                                break;
//                            case RxEventCodes.CODE_CLOSE_PARKING_FEE://更改当前停车费审核状态
//                                tvStatus.setText("已受理");
//                                mData.setParkingFeeId(rxBusEvent.getContent().toString());
//                                break;
                            default:
                                break;
                        }
                    }
                });
    }

    private void setTextData() {
        if (null != mData) {
            Glide.with(mContext).load(mData.getCarImgUrl()).into(ivCarPic);
            tvCarNumber.setText(mData.getCarNumber());
            tvCarSeatNum.setText(mData.getSeat() + "座");
            tvCarTypeName.setText(mData.getBrand() + mData.getModels());
            tvCarColorName.setText(mData.getColor());
//            tvCarOrderNumber.setText("订单编号:" + mData.getOrderId());
            orderId.setText(mData.getOrderId());
            long timestampStartL = 0, timestampEndL = 0;
            String timestampStart = mData.getBeginTime();
            if (!TextUtils.isEmpty(timestampStart)) {
                timestampStartL = Long.valueOf(timestampStart);

            }
            String timestampEnd = mData.getEndTime();
            if (!TextUtils.isEmpty(timestampEnd)) {
                timestampEndL = Long.valueOf(timestampEnd);

            }
            //用车时间
            tvUseCarTime.setText(DateUtils.getMonthTime(timestampStartL) + "至" + DateUtils.getMonthTime(timestampEndL));
            //取车地点
            getCarAddress.setText(mData.getStartParkName());
            //还车地点
            returnCarAddress.setText(mData.getEndParkName());

            String costS = mData.getOrderMoney() + "";
            String payMoney = mData.getPayMoney() + "";
            String insuranceMoney = mData.getOutTimeInsuranceMoney();//不计免赔
            if (!StringUtils.isEmpty(costS)) {
                if (!TextUtils.isEmpty(insuranceMoney) && !"0".equals(insuranceMoney)) {
                    insuranceD = Integer.parseInt(insuranceMoney) / 100.00;
                }
                costTotal = Double.valueOf(payMoney) / 100.00;
                double costD = Double.valueOf(costS) / 100.00;
                tvTotalPay.setText("¥" + TVUtils.toString(costD) + "元");
            }

            costTotal = costTotal - insuranceD;

//            tvDiscountCompany.setVisibility(View.VISIBLE);
//            tvDiscountCompany.setText("个人");

            //构造数据
            BillLongRentResp billLongRentResp = new BillLongRentResp(mData.getInsuranceMoney(), mData.getOutTimeTotalMileage(),
                    mData.getOutTimeMileageMoney(), mData.getOutTimeTotalMinute(), mData.getOutTimeTotalMinuteCost(),
                    mData.getPackageMoney(), mData.getPackageName(), mData.getRentOrderCouponMoney(), mData.getTransferMoney(),
                    mData.getRealElectricityMoney(), mData.getTimeDiscount(), mData.getMileageDiscount(),
                    mData.getTimeDiscountBeforeMoney(),mData.getMileageDiscountBeforeMoney());
            longRentBillView.setData(billLongRentResp);

            //夜间优惠
            orderDiscountView.setNightDiscountData(mData.getNightDiscountsMoney());
            //网点折扣
            orderDiscountView.setParkDiscountData(mData.getParkDiscountMoney(), mData.getParkDiscountLimit());
            //企业折扣
            orderDiscountView.setCompanyDiscountData("");
            //不计免赔
            orderDiscountView.setInsuranceMoneyData(insuranceMoney, "不能用优惠券抵扣");

            //支付状态
            String payStatus = mData.getPayStatus();
            if (CarOrderState.Car_Order_Status_nopay.equals(payStatus)) {//未支付
                //优惠券优惠，隐藏
                orderDiscountView.setCouponDiscountData("");

                //收起还车详情
//                layoutBillDetail.setVisibility(View.GONE);
//                layoutBillDetail.setVisibility(View.VISIBLE);

                choosePayTypeView.setOnPayTypeChooseListener(new ChoosePayTypeView.OnPayTypeChooseListener() {
                    @Override
                    public void onPayTypeChoose(String payType) {
                        OrderDetailLongRentActivity.this.payType = payType;
                        //实付金额
                        double actualPayMoney = getActualPayMoney();
                        tvToPay.setText("确认支付￥" + TVUtils.toString2((actualPayMoney) + ""));
                    }
                });

                choosePayTypeView.setPayMoney(getActualPayMoney(), Config.ORDER_CATEGORY_LONG_RENT, false);

                //隐藏评价
                llytEvaluateContainer.setVisibility(View.GONE);
                tvToEvaluate.setVisibility(View.GONE);

                //获取优惠券列表
                getCouponsList();

            } else {//已完成

                //优惠券抵用
                String coupon = mData.getCoupon();
                orderDiscountView.setCouponDiscountData(coupon);

                //显示还车详情
//                layoutBillDetail.setVisibility(View.VISIBLE);
//                layoutBillDetail.setVisibility(View.GONE);

                //隐藏支付部分
                llytPayContainer.setVisibility(View.GONE);
                tvToPay.setVisibility(View.GONE);
                //是否已评价
                if (mData.getOrderEvaluate() == null) {//还没评价
                    llytEvaluateContainer.setVisibility(View.GONE);
                    tvToEvaluate.setVisibility(View.VISIBLE);
                } else {
                    //已评价，展示评价内容
                    llytEvaluateContainer.setVisibility(View.VISIBLE);
                    tvToEvaluate.setVisibility(View.GONE);

                    setEvaluateInfoData(mData.getOrderEvaluate());
                }
            }
            orderDiscountView.isViewDiscountLineVisibility();

            String status = mData.getParkingFeeStatusName();
            if (!StringUtils.isEmpty(status)) {
                tvStatus.setText(status);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getParkingFeeAuditStatus();
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
                choosePayTypeView.setPayMoney(getActualPayMoney(), Config.ORDER_CATEGORY_LONG_RENT, false);

            } else if (type == PAY_BALANCE) {
                PrePayResp msg = getBean(result, PrePayResp.class);
                if (PayMode.BALANCE_PAY_TYPE.equals(payType) || msg.getCarSharingPayMoney() == 0) {
                    ToastUtil.showImage(mContext, "支付成功");
                    //支付成功进入评价页面
                    toComments();
                } else if (PayMode.WEIXIN_PAY_TYPE.equals(payType)) {
                    WeixinPayData data = JSON.parseObject(msg.getCarSharingPayRequestData(), WeixinPayData.class);
                    WxPayManager wxPayManager = new WxPayManager();
                    wxPayManager.pay(OrderDetailLongRentActivity.this, application, data, msg.getCarSharingPayMoney(), RxEventCodes.CODE_ORDER_PAY_SUCCESS);
                } else if (PayMode.ALI_PAY_TYPE.equals(payType)) {
                    doCheck(Config.PAYING_STRING, true);
                    AlipayManager alipayManager = new AlipayManager();
                    alipayManager.pay(this, msg.getCarSharingPayRequestData(), RxEventCodes.CODE_ORDER_PAY_SUCCESS);
                }
            } else if (type == QUERY_AUDIT_STATUS) {//查询停车费报销审核进度
                parkingFeeAuditStatusResp = getBean(result, ParkingFeeAuditStatusResp.class);
                if (null != parkingFeeAuditStatusResp) {
                    String status = parkingFeeAuditStatusResp.getParkingFeeStatus();
                    List<ParkingFeeAuditStatusResp.ParkingFeeAuditList> list = parkingFeeAuditStatusResp.getList();
                    if (null != list) {
                        for (ParkingFeeAuditStatusResp.ParkingFeeAuditList parkingFeeAuditList : list) {
                            if (!StringUtils.isEmpty(status) && status.equals(parkingFeeAuditList.getStatus())) {
                                tvStatus.setText(parkingFeeAuditList.getStatusName());
                                break;
                            }
                        }
                    }
                }

//                if (StringUtils.isEmpty(result)) {
//                    //填写报销单
//                    startToParkingFeeReturnActivity();
//                } else {
//                    //查看进度条
//                    Intent intent = new Intent(mContext, ParkingFeeStatusActivity.class);
//                    intent.putExtra("auditStatus", resp);
//                    intent.putExtra("carNumber", mData.getCarNumber());
//                    intent.putExtra("orderId", orderNo);
//                    intent.putExtra("orderCategory", Config.ORDER_CATEFORY_LONG_RENT);
//                    startActivity(intent);
//                }
            }
        } else {
            if (type == COUPON_LIST) {
                setCouponUnenable();
            } else if (type == QUERY_AUDIT_STATUS) {//查询停车费报销审核进度
                //填写报销单
//                startToParkingFeeReturnActivity();
            }
        }
    }

    @Override
    public void onFailure(String msg, int type) {
        if (type == COUPON_LIST) {
            setCouponUnenable();
        }
    }

    /**
     * 支付成功到评价
     */
    private void toComments() {
        Bundle bundle = new Bundle();
        bundle.putString("orderNo", orderNo);
        bundle.putString("type", Config.ORDER_CATEGORY_LONG_RENT);
        bundle.putInt("fromPage", 0);
        bundle.putInt("toMain", toMain);

        bundle.putString("orderType", Config.ORDER_CATEGORY_LONG_RENT);//订单类型，红包用到
        bundle.putBoolean("isPayComplete", true);//支付完成，红包用到

        //评价后列表页面刷新
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void setCouponUnenable() {
        chooseCouponView.setData("无可用", R.color.color_gray_999999);
    }

    /**
     * 设置评价
     *
     * @param result
     */
    private void setEvaluateInfoData(EvaluateInfoResp result) {
        if (null != result) {
            /**
             * 星级
             */
            ratingCommentOrder.setRating(result.getStarLevel());
            tvCarOrderStar.setText(result.getStarLevel() + "");
            //"evaluateLabel":"车子性能差,路上抛锚,不好用",
            tvCarorderEvaluate.setText(TVUtils.evaluateStar(result.getStarLevel()));

            /**
             * 标签
             */
            if (!TextUtils.isEmpty(result.getLabel())) {
                String[] label = result.getLabel().split(",");
                if (label.length > 0) {
                    for (int i = 0, length = label.length; i < length; i++) {
                        String labels = label[i];
                        if (null != labels && !"".equals(labels) && !"null".equals(labels)) {
                            addCommentLabel(labels);
                        }
                    }
                    if (flowLayout.getChildCount() == 0) {
                        flowLayout.setVisibility(View.GONE);
                    }
                } else {
                    flowLayout.setVisibility(View.GONE);
                }
            }

            /**
             * 评价内容
             */
            if (!TextUtils.isEmpty(result.getContent()) && !"null".equals(result.getContent())) {
                tvEvaluateContent.setText(result.getContent());
            } else {
                tvEvaluateContent.setText("");
            }
        }
    }

    private void addCommentLabel(String label) {

        int ranHeight = DPUtil.dip2px(this, 30);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ranHeight);

        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        ViewGroup.LayoutParams lpLinearLayout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ranHeight);
        linearLayout.setPadding(0, 0, DPUtil.dip2px(this, 10), 0);

        TextView tv = new TextView(getApplicationContext());
        tv.setPadding(DPUtil.dip2px(this, 15), 0, DPUtil.dip2px(this, 15), 0);
        tv.setTextColor(ContextCompat.getColor(mContext, R.color.color_blue_02b2e4));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tv.setText(label);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setLines(1);
        tv.setBackgroundResource(R.drawable.bg_blue_stroke_radius);

        linearLayout.addView(tv, lpLinearLayout);
        flowLayout.addView(linearLayout, lp);
    }

    @OnClick({R.id.ivTitleLeft, R.id.tvToEvaluate, R.id.llytOrderDetailUnfold, R.id.tvToPay, R.id.llytToRefundParkFee})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivTitleLeft:
//                activityUtil.jumpBackTo(OrderListActivity.class);
                finish();
                break;
            case R.id.tvToEvaluate://评价
                Bundle bundle = new Bundle();
                bundle.putString("orderNo", orderNo);
                bundle.putString("type", Config.ORDER_CATEGORY_LONG_RENT);
                bundle.putInt("fromPage", 0);
                bundle.putInt("toMain", toMain);

                bundle.putString("orderType", Config.ORDER_CATEGORY_LONG_RENT);//订单类型，红包用到

                //评价后列表页面刷新
                Intent intent = new Intent(this, CommentsActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_CODE_COMMENT);

                break;
            case R.id.llytOrderDetailUnfold:
                if (layoutBillDetail.getVisibility() == View.VISIBLE) {
                    layoutBillDetail.setVisibility(View.GONE);
                    tvOrderDetailUnfold.setText("展开");
                    ivOrderDetailDown.setImageResource(R.mipmap.order_detail_arrow_down);
                } else {
                    layoutBillDetail.setVisibility(View.VISIBLE);
                    tvOrderDetailUnfold.setText("收起");
                    ivOrderDetailDown.setImageResource(R.mipmap.order_detail_arrow_up);
                }
                break;
            case R.id.tvToPay://确认支付
                toPayOrder();
                break;
            case R.id.llytToRefundParkFee://停车费报销
//                getParkingFeeAuditStatus();
                if (null == parkingFeeAuditStatusResp) {
                    //填写报销单
                    startToParkingFeeReturnActivity();
                } else {
                    //查看进度条
                    Intent intent2 = new Intent(mContext, ParkingFeeStatusActivity.class);
                    intent2.putExtra("auditStatus", parkingFeeAuditStatusResp);
                    intent2.putExtra("carNumber", mData.getCarNumber());
                    intent2.putExtra("orderId", orderNo);
                    intent2.putExtra("orderCategory", Config.ORDER_CATEGORY_LONG_RENT);
                    startActivity(intent2);
//                }
                    break;
                }
        }
    }

    /**
     * 实付金额
     *
     * @return 实付金额
     */

    private double getActualPayMoney() {
        int couponValue = getCouponValue();
        double temperaTotal = costTotal;
        if (couponValue > 0 && mData != null && !TextUtils.isEmpty(mData.getTransferMoney())) {
            temperaTotal = temperaTotal - Integer.valueOf(mData.getTransferMoney()) / 100;
        }
        double payMoney = temperaTotal - couponValue;
        if (payMoney < 0) {
            payMoney = 0.00;
        }
        if (couponValue > 0 && mData != null && !TextUtils.isEmpty(mData.getTransferMoney())) {
            payMoney = payMoney + Integer.valueOf(mData.getTransferMoney()) / 100;
        }
        return payMoney + insuranceD;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == REQUEST_CODE_COMMENT) {
                    setResult(RESULT_OK);
                    //评价完成关闭页面
                    finish();
                }
                break;
        }
    }

    /**
     * 获取优惠券
     */
    public void getCouponsList() {
        CouponListChooseRequest data = new CouponListChooseRequest();
        data.setCustomerId(UserUtils.getCustomerId());
        data.setOrderId(orderNo);
        data.setMethod(RequestUrls.QUERY_ORDER_CAN_USE_COUPON);
        doGet(data, COUPON_LIST, Config.LOADING_STRING, true);
    }

    /**
     * 支付订单
     */
    public void toPayOrder() {
        LongRentExtraPayRequest data = new LongRentExtraPayRequest();
        data.setCustomerId(UserUtils.getCustomerId());
        data.setCarRentOrderNumber(orderNo);
        data.setCarRentPayType(payType);
        if (null != couponBean && isChooseCoupon) {
            data.setCouponCode(couponBean.getCouponCode());
        }
        data.setMethod(RequestUrls.QUERY_LONG_RENT_PAY_OUT_TIME);
        doGet(data, PAY_BALANCE, Config.PAYING_STRING, true);
    }

    /**
     * 查询停车费报销审核进度
     */
    public void getParkingFeeAuditStatus() {
        ParkingFeeAuditStatusRequest request = new ParkingFeeAuditStatusRequest();
//        request.setParkingFeeId(mData.getParkingFeeId());
        request.setOrderId(mData.getOrderId());
        request.setOrderCategory(mData.getOrderCategory());
        request.setMethod(RequestUrls.QUERY_PARKING_FEE_AUDIT_STATUS);
        doGet(request, QUERY_AUDIT_STATUS, Config.LOADING_STRING, true);
    }

    /**
     * 填写报销单
     */
    private void startToParkingFeeReturnActivity() {
        Intent intent2 = new Intent(this, ParkingFeeReturnActivity.class);
        intent2.putExtra("carNumber", mData.getCarNumber() + "");
        intent2.putExtra("orderId", orderNo);
        intent2.putExtra("orderCategory", Config.ORDER_CATEGORY_LONG_RENT);
        startActivity(intent2);
    }
}
