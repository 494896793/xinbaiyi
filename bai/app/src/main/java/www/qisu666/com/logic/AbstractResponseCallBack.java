package www.qisu666.com.logic;


import org.xutils.common.util.LogUtil;

import java.util.Map;

/**
 * Created by Administrator on 2016/4/7.
 */
public abstract class AbstractResponseCallBack implements ResponseCallBack {

    @Override
    public abstract void onResponse(Map<String,Object> map, String tag);

    @Override
    public void onError(String tag) {
        LogUtil.e(tag+"请求失败");
    }
}
