package www.qisu666.com.activity

import android.view.WindowManager
import www.qisu666.com.R
import kotlinx.android.synthetic.main.activity_cannot_return_sample.*

/**
 * Created by wujiancheng on 2017/11/9.
 * 无法还车示例
 */
class CannotReturnSampleActivity : BaseActivity() {
    override fun setView() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_cannot_return_sample)
    }

    override fun initDatas() {
        ivSample.setOnClickListener { finish() }
    }

    override fun onComplete(result: String?, type: Int) {

    }

    override fun onFailure(msg: String?, type: Int) {

    }

}