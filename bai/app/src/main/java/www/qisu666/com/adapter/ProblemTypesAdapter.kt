package www.qisu666.com.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import www.qisu666.com.R
import www.qisu666.com.callback.ProblemTypesResp

/**
 * Created by wujiancheng on 2017/12/9.
 * 故障类型
 */
class ProblemTypesAdapter(val context: Context, private val problemTypes: List<ProblemTypesResp>) : RecyclerView.Adapter<ProblemTypesAdapter.ProblemTypesViewHolder>() {
    private var onProblemTypesSelectListener: OnProblemTypesSelectListener? = null
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ProblemTypesViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.listitem_problem_types, parent, false)
        return ProblemTypesViewHolder(view)
    }

    override fun getItemCount(): Int = problemTypes.size

    override fun onBindViewHolder(holder: ProblemTypesViewHolder?, position: Int) {

        val problemType = problemTypes[position]
        holder?.tvProblemType?.text = problemType.typeName

        if (problemType.selectedStatus) {
            holder?.tvProblemType?.setBackgroundResource(R.drawable.corners_3_bg_blue)
            holder?.tvProblemType?.setTextColor(ContextCompat.getColor(context, R.color.new_primary))
        } else {
            holder?.tvProblemType?.setBackgroundResource(R.drawable.bg_white_corners_overstroke)
            holder?.tvProblemType?.setTextColor(ContextCompat.getColor(context, R.color.main_info_color))
        }

        holder?.tvProblemType?.setOnClickListener {
            val type = problemTypes[holder.adapterPosition]
            type.selectedStatus = !type.selectedStatus

            notifyDataSetChanged()
            onProblemTypesSelectListener?.onProblemTypesSelect(problemTypes)
        }
    }

    class ProblemTypesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProblemType: TextView = itemView.findViewById(R.id.tvProblemType)
    }

    interface OnProblemTypesSelectListener {
        fun onProblemTypesSelect(problemTypes: List<ProblemTypesResp>)
    }

    fun setOnProblemTypesSelectListener(onProblemTypesSelectListener: OnProblemTypesSelectListener) {
        this.onProblemTypesSelectListener = onProblemTypesSelectListener
    }
}