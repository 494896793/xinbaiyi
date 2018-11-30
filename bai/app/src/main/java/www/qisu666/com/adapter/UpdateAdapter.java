package www.qisu666.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import www.qisu666.com.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by baby on 2016/5/9.
 */
public class UpdateAdapter extends BaseAdapter {
    private List<String> buglist;
    private Context context;

    public UpdateAdapter(Context context) {
        this.context = context;
    }

    public void setBuglist(List<String> buglist) {
        this.buglist = buglist;
        notifyDataSetChanged();
        notifyDataSetInvalidated();
    }


    @Override
    public int getCount() {
        if (buglist != null && buglist.size() > 0)
            return buglist.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return buglist;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_update_bug, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (buglist != null && buglist.size() > position) {
            holder.tvUpdateBug.setText(buglist.get(position));
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_update_bug)
        TextView tvUpdateBug;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}