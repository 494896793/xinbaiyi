package www.qisu666.com.utils;

import com.linfaxin.recyclerview.PullRefreshLoadRecyclerView;
import com.linfaxin.recyclerview.headfoot.LoadMoreView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/4.
 */
public class PageLoadUtils {

    /**
     * 设置分页加载的刷新和加载控件的状态
     * @param currentPage
     * @param PAGE_NUM
     * @param pull_refresh_load_recycler_view
     * @param loadMoreView
     * @param adapter
     * @param list
     * @param listString
     */
    public static void pageLoad(int currentPage, int PAGE_NUM, PullRefreshLoadRecyclerView pull_refresh_load_recycler_view,
                                LoadMoreView loadMoreView, PullRefreshLoadRecyclerView.LoadRefreshAdapter adapter, List<Map<String,Object>> list, String listString){
        if(currentPage==1){
            adapter.notifyItemRangeRemoved(0, adapter.getItemCount());
            list.clear();
        }

        if (listString != null && !"".equals(listString.toString()) && !"[]".equals(listString.toString())) {
            try {
                JSONArray array = new JSONArray(listString.toString());

                int count = array.length();
//                if (currentPage > 1) {
//                    currentPage--;
//                }
                pull_refresh_load_recycler_view.emptyViewTuggle(false);
                if (count < PAGE_NUM) {
//                    if(currentPage>1){
//                        currentPage--;
//                    }
                    if (pull_refresh_load_recycler_view.getLoadMoreView() != null) {
                        pull_refresh_load_recycler_view.getLoadMoreView().setState(LoadMoreView.STATE_NO_MORE);
                    }
                }

                for (int i = 0; i < count; i++) {
                    JSONObject object = array.getJSONObject(i);
                    list.add(JsonUtils.jsonToMap(object.toString()));
                }

                adapter.notifyItemRangeInserted((currentPage - 1) * PAGE_NUM, list.size() - (currentPage - 1) * PAGE_NUM);
                if (currentPage == 1) {
                    if (count == PAGE_NUM) {
                        loadMoreView.setState(LoadMoreView.STATE_NORMAL);
                        pull_refresh_load_recycler_view.setLoadMoreView(loadMoreView);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if(currentPage == 1){
                pull_refresh_load_recycler_view.emptyViewTuggle(true);
            }
            if(pull_refresh_load_recycler_view.getLoadMoreView()!=null){
                pull_refresh_load_recycler_view.getLoadMoreView().setState(LoadMoreView.STATE_NO_MORE);
            }
        }
    }
}
