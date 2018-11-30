package www.qisu666.com.logic;

import com.linfaxin.recyclerview.PullRefreshLoadRecyclerView;
import com.linfaxin.recyclerview.headfoot.LoadMoreView;
import com.linfaxin.recyclerview.headfoot.RefreshView;

import java.util.List;
import java.util.Map;

import www.qisu666.com.utils.PageLoadUtils;


/**
 * Created by Administrator on 2016/8/4.
 */
public class PageLoadResponseCallBack extends AbstractResponseCallBack {

    //当前数据分页
    private int currentPage;
    //每页数据条数
    private int pageNum;

    private PullRefreshLoadRecyclerView pull_refresh_load_recycler_view;
    private LoadMoreView loadMoreView;
    private PullRefreshLoadRecyclerView.LoadRefreshAdapter adapter;

    private List<Map<String, Object>> list;
    private String listParam;

    public PageLoadResponseCallBack(int currentPage, int pageNum, PullRefreshLoadRecyclerView pull_refresh_load_recycler_view, LoadMoreView loadMoreView, PullRefreshLoadRecyclerView.LoadRefreshAdapter adapter, List<Map<String, Object>> list, String listParam) {
        this.currentPage = currentPage;
        this.pageNum = pageNum;
        this.pull_refresh_load_recycler_view = pull_refresh_load_recycler_view;
        this.loadMoreView = loadMoreView;
        this.adapter = adapter;
        this.list = list;
        this.listParam = listParam;
    }

    @Override
    public void onResponse(Map<String, Object> map, String tag) {
        setStateNormal();
        String listString = map.get(listParam).toString();
        PageLoadUtils.pageLoad(currentPage, pageNum, pull_refresh_load_recycler_view, loadMoreView, adapter, list, listString);
    }

    @Override
    public void onError(String tag) {
        setStateNormal();
        super.onError(tag);
    }

    /**
     * 已获取到响应报文或者错误时，将 PullRefreshLoadRecyclerView 置为正常状态
     */
    private void setStateNormal() {
        if(pull_refresh_load_recycler_view!=null){
            if(pull_refresh_load_recycler_view.getRefreshView()!=null)
                pull_refresh_load_recycler_view.getRefreshView().setState(RefreshView.STATE_NORMAL);
            if(pull_refresh_load_recycler_view.getLoadMoreView()!=null)
                pull_refresh_load_recycler_view.getLoadMoreView().setState(LoadMoreView.STATE_NORMAL);
        }
    }
}
