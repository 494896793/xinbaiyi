package www.qisu666.com.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import www.qisu666.com.R
import www.qisu666.com.activity.WebActivity
import www.qisu666.com.adapter.ImgTextMsgAdapter
import www.qisu666.com.callback.ImgTextMsgResp
import www.qisu666.com.constant.Config
import www.qisu666.com.constant.RequestUrls
import www.qisu666.com.request.RequestBaseParams
import www.qisu666.com.utils.StringUtils
import www.qisu666.com.utils.logI
import kotlinx.android.synthetic.main.fragment_img_text_message.*

/**
 * Created by wujiancheng on 2017/12/18.
 * 图文消息
 */
class ImgTextMessageFragment : BaseFragment() {
    private var QUERY_IMG_TEXT_MSG = 1
    private var imgTextMsgs: MutableList<ImgTextMsgResp> = mutableListOf()
    private var imgTextMsgAdapter: ImgTextMsgAdapter? = null

    override fun setLayoutResId(): Int {
        return R.layout.fragment_img_text_message
    }

    override fun initDatas(view: View?) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imgTextMsgAdapter = ImgTextMsgAdapter(activity, imgTextMsgs)
        lvMessage.adapter = imgTextMsgAdapter
        lvMessage.setOnItemClickListener { parent, view, position, id ->
            if (imgTextMsgs.isNotEmpty() && imgTextMsgs.size > position) {
                val msgData = imgTextMsgs[position]
                if (!StringUtils.isEmpty(msgData.messageUrl)) {
                    val intent = Intent(activity, WebActivity::class.java)
                    intent.putExtra("url", msgData.messageUrl)
                    if ("80" == msgData.id) {//国庆中秋活动
                        intent.putExtra("type", WebActivity.TYPE_PRIZE)
                    }
                    startActivity(intent)
                }
            }
        }
        queryImgTextMsg()
    }

    override fun onComplete(result: String?, type: Int) {
        if (isSuccess(result)) {
            when (type) {
                QUERY_IMG_TEXT_MSG -> {
                    val data = getList(result, ImgTextMsgResp::class.java)
                    if (data != null && data.size > 0) {
                        imgTextMsgs.addAll(data)
                    }
                    if (imgTextMsgs.size > 2) {
                        imgTextMsgs.sortByDescending { it.createTime }
                    }
                    imgTextMsgAdapter?.setData(imgTextMsgs)
                    if (imgTextMsgs.isEmpty()) {
                        rlytNoMessage.visibility = View.VISIBLE
                    } else {
                        rlytNoMessage.visibility = View.GONE
                    }
                }
            }
        } else {
            when (type) {
                QUERY_IMG_TEXT_MSG -> {
                    rlytNoMessage.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onFailure(msg: String?, type: Int) {
        when (type) {
            QUERY_IMG_TEXT_MSG -> {
                rlytNoMessage.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 图文消息
     */
    private fun queryImgTextMsg() {
        val data = RequestBaseParams()
        data.method = RequestUrls.QUERY_IMAGE_TEXT_MESSAGE
        doGet(data, QUERY_IMG_TEXT_MSG, Config.LOADING_STRING, true)
    }
}