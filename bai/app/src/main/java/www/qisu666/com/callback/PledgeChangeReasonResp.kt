package www.qisu666.com.request

/**
 * Created by wujiancheng on 2017/10/23.
 * 押金额度更改原因
 * deposit true string 已交纳押金
depositAll true string 所有应该交纳的押金
desc true string 说明(如: 违章太频繁)
 */
data class PledgeChangeReasonResp(var deposit: Int = 0, var depositAll: Int = 0, var desc: String = "") : RequestBaseParams()