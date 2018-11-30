package www.qisu666.com.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import www.qisu666.com.R;
import www.qisu666.com.app.OrderType;
import www.qisu666.com.callback.MoneyLogsResp;
import www.qisu666.com.utils.DateUtils;
import www.qisu666.com.utils.Logger;
import www.qisu666.com.utils.TVUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoneyLogsAdapter extends BaseAdapter<MoneyLogsResp> {

    public MoneyLogsAdapter(Context context, List<MoneyLogsResp> datas) {
        super(context, datas);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = getV(R.layout.item_money_detail);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MoneyLogsResp moneyLogsResp = getItem(position);
        String type = moneyLogsResp.getType();

        if (OrderType.BALANCE.equals(type)) {
            holder.tvMoneyDetailName.setText(OrderType.BALANCE_STR);
        } else if (OrderType.DEPOSIT.equals(type)) {
            holder.tvMoneyDetailName.setText(OrderType.DEPOSIT_STR);
        } else if (OrderType.REFUND.equals(type)) {
            holder.tvMoneyDetailName.setText(OrderType.REFUND_STR);
        } else if (OrderType.GIFT_MONEY.equals(type)) {
            holder.tvMoneyDetailName.setText(OrderType.GIFT_MONEY_STR);
        } else if (OrderType.TIMESHARING.equals(type)) {
            holder.tvMoneyDetailName.setText(OrderType.TIMESHARING_STR);
        } else {
            holder.tvMoneyDetailName.setText(OrderType.TIMESHARING_STR);
        }

//            holder.tvMoneyDetailName.setText(moneyLogsResp.getCategory());
        if (!TextUtils.isEmpty(moneyLogsResp.getCreateTime())) {
            try {
                String time = DateUtils.timestampToString(moneyLogsResp.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                if (!TextUtils.isEmpty(time)) {
                    holder.tvMoneyDetailTime.setText(time);
                } else {
                    holder.tvMoneyDetailTime.setText("");
                }
            } catch (Exception e) {
                holder.tvMoneyDetailTime.setText("");
            }
        }
        int money = moneyLogsResp.getMoney();
        Logger.i("资金明细==" + money);
        String moneyS = TVUtils.toString2((money / 100.00) + "");
        Logger.i("资金明细111==" + moneyS);
        if ("+".equals(moneyLogsResp.getTurnoverType()) || "".equals(moneyLogsResp.getTurnoverType()) || " ".equals(moneyLogsResp.getTurnoverType())) {
            holder.tvMoneyDetailNum.setText("+" + moneyS);
        } else if ("-".equals(moneyLogsResp.getTurnoverType())) {
            holder.tvMoneyDetailNum.setText("-" + moneyS);
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.view)
        View view;
        @BindView(R.id.tv_money_detail_name)
        TextView tvMoneyDetailName;
        @BindView(R.id.tv_money_detail_time)
        TextView tvMoneyDetailTime;
        @BindView(R.id.tv_money_detail_num)
        TextView tvMoneyDetailNum;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}