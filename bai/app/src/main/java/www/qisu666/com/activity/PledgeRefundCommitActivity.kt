package www.qisu666.com.activity

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import android.widget.LinearLayout
import www.qisu666.com.R
import www.qisu666.com.adapter.PledgeRefundCommitAdapter
import www.qisu666.com.app.PayMode
import www.qisu666.com.callback.CheckRefundOriginalResp
import www.qisu666.com.callback.CustomerPayInfoResp
import www.qisu666.com.callback.PledgeRefundCommitResp
import www.qisu666.com.constant.Config
import www.qisu666.com.constant.RequestUrls
import www.qisu666.com.request.CustomerPayInfoRequest
import www.qisu666.com.request.PledgeRefundCommitRequest
import www.qisu666.com.rx.RxBus
import www.qisu666.com.rx.RxBusEvent
import www.qisu666.com.rx.RxEventCodes
import www.qisu666.com.utils.*
import kotlinx.android.synthetic.main.activity_pledge_refund_commit.*
import kotlinx.android.synthetic.main.title_back.*


/**
 * Created by wujiancheng on 2017/10/9.
 * 停车费报销提交页面
 */
class PledgeRefundCommitActivity : BaseActivity() {
    private val REFUND_ORIGINAL = "R"//原路退还
    private val REFUND_ALI = PayMode.ALI_PAY_TYPE//退还到支付宝
    private val REFUND_WX = PayMode.WEIXIN_PAY_TYPE//退还到微信
    private var returnType = REFUND_ALI//退款类型
    private var refundAliOrWx = REFUND_ALI//记录上一次选择支付宝或微信支付

    private val QUERY_CUSTOMER_PAY_INFO = 1//获取用户提交过的支付信息
    private val QUERY_REFUND_COMMIT = 2//退款申请提交

    private var wxNickName: String = ""
    private var openId: String = ""
    private var refundAmount: String = ""//退款金额

    private var hasSwitch = false//是否点击切换收款账号
    private var isRefundOriginal: Boolean = false//是否可以原路退回

    private var adapter: PledgeRefundCommitAdapter? = null
    private var data = mutableListOf<CheckRefundOriginalResp>()

    override fun setView() {
        setContentView(R.layout.activity_pledge_refund_commit)
    }

    override fun initDatas() {
        tvTitleName.text = "退还保证金"

        //返回
        ivTitleLeft.setOnClickListener {
            finish()
        }

        val respResult = SharedPreferencesUtils.getString(mContext, "CheckRefundOriginalResp", "")
        data = getList(respResult, CheckRefundOriginalResp::class.java)
        data.sortByDescending { it.depositRest }

        rvPledgeRefund.isNestedScrollingEnabled = false
        rvPledgeRefund.layoutManager = LinearLayoutManager(mContext, LinearLayout.VERTICAL, false)
        adapter = PledgeRefundCommitAdapter(mContext, data)
        rvPledgeRefund.adapter = adapter
        adapter?.setOnItemClickListener(object : PledgeRefundCommitAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                logI("点击第${position}个")
                if (position in data.indices) {
                    setRefundType(data[position])
                }
            }
        })
        //默认选择第一个
        if (data.isNotEmpty()) {
            setRefundType(data[0])
        }

        queryCustomerPayInfo()

        //提交
        tvCommit.setOnClickListener {

            if (!isRefundOriginal) {//不能原路退回
                if (PayMode.ALI_PAY_TYPE == returnType) {//支付宝
                    if (getMoneyAccountView.aliAccountName.isEmpty()) {
                        ToastUtil.show(mContext, "请输入真实姓名")
                        return@setOnClickListener
                    }
                    if (getMoneyAccountView.aliAccountNumber.isEmpty()) {
                        ToastUtil.show(mContext, "请输入支付宝账号")
                        return@setOnClickListener
                    }
                    if (!StringUtils.isLetterOrChinese(getMoneyAccountView.aliAccountName)) {
                        ToastUtil.show(mContext, "姓名只能为中文或字母")
                        return@setOnClickListener
                    }
                    if (!StringUtils.isEMail(getMoneyAccountView.aliAccountNumber)
                            && !StringUtils.isTel(getMoneyAccountView.aliAccountNumber)) {
                        ToastUtil.show(mContext, "请输入正确的支付宝账号信息(邮箱或手机号)")
                        return@setOnClickListener
                    }

                } else if (PayMode.WEIXIN_PAY_TYPE == returnType) {//微信
                    if (StringUtils.isEmpty(openId) || StringUtils.isEmpty(wxNickName)) {
                        ToastUtil.show(mContext, "请获取微信账号信息")
                        return@setOnClickListener
                    }
                }
            }
            //提交审核
            commitRefund()
        }

        observeRxEventCode()

    }

    private fun observeRxEventCode() {
        busSubscription = RxBus.getInstance().toObservable(RxBusEvent::class.java)
                .subscribe { rxBusEvent ->
                    val eventCode = rxBusEvent.eventCode
                    when (eventCode) {
                        RxEventCodes.CODE_WX_NICKNAME
                        -> {
                            val map = rxBusEvent.content as HashMap<String, String>
                            wxNickName = map["nickname"] ?: ""
                            getMoneyAccountView.setWxNickName(wxNickName)
                            openId = map["openId"] ?: ""
                        }
                    }
                }
    }

    override fun onComplete(result: String?, type: Int) {
        when {
            isSuccess(result) -> {
                when (type) {
                    QUERY_REFUND_COMMIT -> {//提交申请退款
                        val resp = getBean(result, PledgeRefundCommitResp::class.java)
                        if ("true" == resp.status) {
                            //提交申请成功
                            val intent = Intent(mContext, PledgeRefundStatusActivity::class.java)
                            intent.putExtra("refundLogId", resp.refundLogId)
                            startActivity(intent)
                            finish()
                        }
                        ToastUtil.show(mContext, getMsg(result))
                    }
                    QUERY_CUSTOMER_PAY_INFO -> {
                        val resps: List<CustomerPayInfoResp> = getList(result, CustomerPayInfoResp::class.java)
                        for (resp in resps) {
                            if (resp.type == PayMode.ALI_PAY_TYPE) {
                                getMoneyAccountView.saveZhiFuBaoAccount(resp.payName, resp.payAccount)
                                getMoneyAccountView.setZhiFuBaoAccount(resp.payName, resp.payAccount)
                                break
                            }
                        }
                    }
                }
            }
            else -> {
                when (type) {
                    QUERY_REFUND_COMMIT -> {//提交申请退款
                        ToastUtil.show(mContext, getMsg(result))
                    }
                }
            }
        }
    }

    override fun onFailure(msg: String?, type: Int) {

    }

    /**
     * 退款申请提交
     */
    fun commitRefund() {
        var openIdP = ""
        var wxNickNameP = ""
        var aliAccountNumber = ""
        var aliAccountName = ""
        if (!isRefundOriginal) {//不能原路退还
            if (PayMode.ALI_PAY_TYPE == returnType) {//支付宝
                aliAccountNumber = getMoneyAccountView.aliAccountNumber
                aliAccountName = getMoneyAccountView.aliAccountName
            } else if (PayMode.WEIXIN_PAY_TYPE == returnType) {//微信
                openIdP = openId
                wxNickNameP = wxNickName
            }
        }

        val request = PledgeRefundCommitRequest(UserUtils.getCustomerId(), openIdP, wxNickNameP, aliAccountNumber, aliAccountName, returnType, refundAmount, "APP")
        request.method = RequestUrls.COMMIT_REFUND_PLEDGE
        doGet(request, QUERY_REFUND_COMMIT, Config.LOADING_STRING, true)
    }

    /**
     * 退款方式
     */
    private fun setRefundType(resp: CheckRefundOriginalResp) {
        isRefundOriginal = resp.checkResult == "true"
        logI("原路退还：$isRefundOriginal")
        if (isRefundOriginal) {//可以原路退回
            llytRefundThird.visibility = View.GONE
            tvRefundOriginal.visibility = View.VISIBLE
            var accountName = "支付宝"//默认
            if (PayMode.WEIXIN_PAY_TYPE == resp.repayAccount) {
                accountName = "微信"
            }
            tvRefundOriginal.text = Html.fromHtml(HighlightUtil.convertHightlightText(String.format(Config.PLEDGE_REFUND_ORIGINAL, accountName), accountName, "#02b2e4"))
            returnType = REFUND_ORIGINAL
        } else {//不可以原路退回
            llytRefundThird.visibility = View.VISIBLE
            tvRefundOriginal.visibility = View.GONE

            getMoneyAccountView.setBackground(R.color.white)
            returnType = refundAliOrWx
            //切换收款账号监听
            getMoneyAccountView.setOnSwitchAccountTypeListener { accountType ->
                refundAliOrWx = accountType
                returnType = refundAliOrWx
            }
        }
        logI("退还方式：$returnType")
        //退款金额
        refundAmount = resp.deposit.toString()
        logI("退款金额：" + refundAmount)
    }

    /**
     * 获取用户提交过的支付信息
     */
    private fun queryCustomerPayInfo() {
        val request = CustomerPayInfoRequest(UserUtils.getCustomerId())
        request.method = RequestUrls.QUERY_CUSTOMER_PAY_INFO
        doGet(request, QUERY_CUSTOMER_PAY_INFO, Config.LOADING_STRING, true)
    }
}