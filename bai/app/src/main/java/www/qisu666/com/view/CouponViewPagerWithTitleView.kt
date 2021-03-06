package www.qisu666.com.view

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import www.qisu666.com.R
import www.qisu666.com.adapter.MyFragmentStatePagerAdapter
import www.qisu666.com.utils.logE
import kotlinx.android.synthetic.main.view_viewpager_with_title.view.*

/**
 * Created by wujiancheng on 2017/11/11.
 * 带标题可滑动
 */
class CouponViewPagerWithTitleView : LinearLayout {
    lateinit var layoutInflater: LayoutInflater
    lateinit var fragmentAdapter: MyFragmentStatePagerAdapter

    private var onPageSelectedListener: OnPageSelectedListener? = null
    var currentPosition = 0

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        layoutInflater = LayoutInflater.from(context)
        layoutInflater.inflate(R.layout.view_coupon_viewpager_with_title, this)
    }

    fun setData(activity: FragmentActivity, titles: List<String>, fragments: List<Fragment>) {
        if (titles.size != fragments.size) {
            logE("标题和页面数量不一致，请检查")
            return
        }
        fragmentAdapter = MyFragmentStatePagerAdapter(activity.supportFragmentManager, fragments)
        viewPager.adapter = fragmentAdapter
        viewPager.offscreenPageLimit = 2

        for (index in titles.indices) {
            val viewTitleItem = layoutInflater.inflate(R.layout.view_coupon_viewpager_title_second_item, llytTitleBarName, false)
            val params = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F)
            viewTitleItem.layoutParams = params
            //标题名称
            val tvTitleBarName: TextView = viewTitleItem.findViewById(R.id.tvTitleBarName)
            tvTitleBarName.text = titles[index]
//            setTitleItemColor(index)
            tvTitleBarName.setOnClickListener {
                viewPager.currentItem = index
            }
            //添加标题
            llytTitleBarName.addView(viewTitleItem)
        }
        setTitleItemColor(0)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                setTitleItemColor(position)
                onPageSelectedListener?.onPageSelected(position)
                currentPosition = position
            }
        })
        //默认选择第一个
        viewPager.currentItem = currentPosition
    }

    /**
     * 跳转到第position页
     */
    fun setCurrentItem(position: Int) {
        viewPager.currentItem = position
    }

    /**
     * 设置标题颜色
     */
    private fun setTitleItemColor(index: Int) {
        val titleCount = llytTitleBarName.childCount
        if (titleCount <= 0) {
            return
        }
        for (childIndex in 0..(titleCount - 1)) {
            val child = llytTitleBarName.getChildAt(childIndex)
            //标题名称
            val tvTitleBarName: TextView = child.findViewById(R.id.tvTitleBarName)
            //标题下划线
            val viewTitleBarNameLine = child.findViewById<View>(R.id.viewTitleBarNameLine)
            if (index == childIndex) {
                tvTitleBarName.setTextColor(ContextCompat.getColor(context, R.color.new_primary))
                viewTitleBarNameLine.setBackgroundColor(ContextCompat.getColor(context, R.color.new_primary))
                viewTitleBarNameLine.visibility = View.VISIBLE
            } else {
                tvTitleBarName.setTextColor(ContextCompat.getColor(context, R.color.main_info_color))
                viewTitleBarNameLine.visibility = View.GONE
            }
        }
    }

    /**
     * 设置标题颜色
     */
    private fun secondSetTitleItemColor(index: Int) {
        val titleCount = llytTitleBarName.childCount
        if (titleCount <= 0) {
            return
        }
        for (childIndex in 0..(titleCount - 1)) {
            val child = llytTitleBarName.getChildAt(childIndex)
            //标题名称
            val tvTitleBarName: TextView = child.findViewById(R.id.tvTitleBarName)
            //标题下划线
            val viewTitleBarNameLine = child.findViewById<View>(R.id.viewTitleBarNameLine)
            if (index == childIndex) {
                tvTitleBarName.setTextColor(ContextCompat.getColor(context, R.color.main_background))
                viewTitleBarNameLine.setBackgroundColor(ContextCompat.getColor(context, R.color.main_background))
                viewTitleBarNameLine.visibility = View.VISIBLE
            } else {
                tvTitleBarName.setTextColor(ContextCompat.getColor(context, R.color.notice_color))
                viewTitleBarNameLine.visibility = View.GONE
            }
        }
    }

    /**
     * 更新优惠券的数量
     */
    fun updateTitle(titles: List<String>) {
        for (index in 0..titles.size - 1) {
            val child: View? = llytTitleBarName.getChildAt(index)
            //标题名称
            val tvTitleBarName: TextView? = child?.findViewById<TextView>(R.id.tvTitleBarName) as TextView
            tvTitleBarName?.text = titles[index]
        }
    }

    /**
     * 更新未读消息红点
     */
    fun updateTitleRedPoint(status: List<Int>) {
        for (index in 0..status.size - 1) {
            val child: View? = llytTitleBarName.getChildAt(index)
            //标题名称
            val ivRedPoint: ImageView? = child?.findViewById(R.id.ivRedPoint)
            ivRedPoint?.visibility = if (status[index] > 0) View.VISIBLE else View.GONE
        }
    }

    interface OnPageSelectedListener {
        fun onPageSelected(position: Int)
    }

    fun setOnPageSelectedListener(onPageSelectedListener: OnPageSelectedListener) {
        this.onPageSelectedListener = onPageSelectedListener
    }
}