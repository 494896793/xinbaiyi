package www.qisu666.com.activity

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import www.qisu666.com.R
import www.qisu666.com.adapter.PledgeRefundStatusAdapter
import www.qisu666.com.callback.PledgeRefundStatusResp
import www.qisu666.com.callback.UserInfoResp
import www.qisu666.com.constant.Config
import www.qisu666.com.constant.RequestUrls
import www.qisu666.com.request.CancelRefundRequest
import www.qisu666.com.request.PledgeRefundStatusRequest
import www.qisu666.com.request.UserInfoRequest
import www.qisu666.com.rx.RxBus
import www.qisu666.com.rx.RxBusEvent
import www.qisu666.com.rx.RxEventCodes
import www.qisu666.com.utils.CacheUtils
import www.qisu666.com.utils.ToastUtil
import www.qisu666.com.utils.UserUtils
import kotlinx.android.synthetic.main.activity_parking_fee_status.*
import kotlinx.android.synthetic.main.title_back.*

/**
 * Created by wujiancheng on 17-10-17.
 * 押金退还审核进度
 */
class PledgeRefundStatusActivity : BaseActivity() {
    private val QUERY_PLEDGE_REFUND_STATUS = 1
    private val QUERY_CANCEL_REFUND = 2
    private val QUERY_USER_INFO = 3
    private var refundStatus = ""
    override fun setView() {
        //用停车场页面的布局
        setContentView(R.layout.activity_parking_fee_status)
    }

    override fun initDatas() {
        ivTitleLeft.setOnClickListener {
            onBackPressed()
        }
        tvTitleName.text = "押金退还状态"

        //查询审核进度
        queryPledgeRefundStatus()
    }

    override fun onComplete(result: String?, type: Int) {
        if (isSuccess(result)) {
            when (type) {
                QUERY_PLEDGE_REFUND_STATUS -> {//押金退还状态
                    val resp = getBean(result, PledgeRefundStatusResp::class.java)
                    setRespData(resp)
                }
                QUERY_CANCEL_REFUND -> {//取消退款
                    val userInfoResp = CacheUtils.getIn().userInfo
                    if (userInfoResp != null) {
                        val data = UserInfoRequest()
                        data.customerPhone = userInfoResp.phone
                        data.method = RequestUrls.QUERY_USER_INFO
                        doGet(data, QUERY_USER_INFO, "", false)
                    }
                }
                QUERY_USER_INFO -> {//获取用户信息
                    val userInfoResp = getBean(result, UserInfoResp::class.java)
                    CacheUtils.getIn().save(userInfoResp)
                    ToastUtil.show(mContext, "取消退款成功")
                    val event = RxBusEvent<String>()
                    event.eventCode = RxEventCodes.CODE_CANCEL_REFUND_PLEDGE_SUCCESS
                    RxBus.getInstance().post(event)
                    finish()
                }
            }
        } else {
            when (type) {
                QUERY_CANCEL_REFUND -> {//取消退款
                    ToastUtil.show(mContext, "取消退款失败")
                }
                QUERY_USER_INFO -> {//获取用户信息
                    ToastUtil.show(mContext, "取消退款成功")
                    val event = RxBusEvent<String>()
                    event.eventCode = RxEventCodes.CODE_CANCEL_REFUND_PLEDGE_SUCCESS
                    RxBus.getInstance().post(event)
                    finish()
                }
            }
        }
    }

    override fun onFailure(msg: String?, type: Int) {
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    /**
     * 查询审核进度
     */
    private fun queryPledgeRefundStatus() {
        val refundLogId = intent.getStringExtra("refundLogId")
        val request = PledgeRefundStatusRequest(UserUtils.getCustomerId(), refundLogId)
        request.method = RequestUrls.QUERY_REFUND_TIME_LINE
        doGet(request, QUERY_PLEDGE_REFUND_STATUS, Config.LOADING_STRING, true)
    }

    private fun setRespData(resp: PledgeRefundStatusResp) {
        //支付方式
        val payType = resp.payType
        receiptAccountView.setData(payType, resp.payAccount, resp.payAccount, resp.payAmount.toString())

        //审核状态列表
        val data = resp.timeline
        data.sortedBy { it.sequence }
        rvAuditStatus.layoutManager = LinearLayoutManager(mContext, LinearLayout.VERTICAL, false)
        rvAuditStatus.isNestedScrollingEnabled = false
        val adapter = PledgeRefundStatusAdapter(mContext, data)
        rvAuditStatus.adapter = adapter

        //状态，1申请退款中，2退款驳回，3退款完成,4取消退款，5退款中(客服审核完后的状态) 6退款失败(财务打款失败)
        refundStatus = resp.refundStatus
        if ("2" == refundStatus || "6" == refundStatus) {
            llytReCommit.visibility = View.VISIBLE
            //取消退款
            tvReCommit.visibility = View.GONE
            //联系客服
            tvTel.setOnClickListener {
                val info = CacheUtils.getIn().system_Info
                val uri = Uri.parse("tel:" + info?.kfphone)
                val intent = Intent(Intent.ACTION_DIAL, uri)
                startActivity(intent)
            }
        } else if ("1" == refundStatus) {
            //取消退款
            tvReCommit.setOnClickListener {
                val request = CancelRefundRequest()
                request.customerId = UserUtils.getCustomerId()
                request.method = RequestUrls.CANCEL_REFUND
                doGet(request, QUERY_CANCEL_REFUND, "取消中", true)
            }
            tvReCommit.text = "取消退款"
            tvTel.visibility = View.GONE
            llytReCommit.visibility = View.VISIBLE

        } else {
            llytReCommit.visibility = View.GONE
        }
    }
}