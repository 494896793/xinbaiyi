package www.qisu666.com.root;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import www.qisu666.com.app.MyApplication;
import www.qisu666.com.callback.UserInfoResp;
import www.qisu666.com.constant.Config;
import www.qisu666.com.constant.RequestUrls;
import www.qisu666.com.request.RequestBaseParams;
import www.qisu666.com.request.VerifyInfoRequest;
import www.qisu666.com.utils.CacheUtils;
import www.qisu666.com.utils.Logger;
import www.qisu666.com.utils.SharedPreferencesUtils;

import org.xutils.common.util.KeyValue;
import org.xutils.http.RequestParams;
import org.xutils.http.body.MultipartBody;
import org.xutils.http.body.RequestBody;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class RequestUtil {

    /**
     * 封装参数
     *
     * @param obj
     * @return
     */
    @NonNull
    @SuppressWarnings("all")
    public static RequestParams setRequest(RequestBaseParams obj) {
        String body = JSON.toJSONString(obj);
        HashMap<String, Object> hashMap = new HashMap<String, Object>();

        if(obj.getMethod().equals(RequestUrls.OPERATE_CAR)){
            putJsonIntoMaps(body, hashMap);
        }else{
            putJsonIntoMap(body, hashMap);
        }
        String method = getMethod(hashMap);
        String getParams = getParams(hashMap);

        if (!TextUtils.isEmpty(getParams)) {
            getParams = getParams.substring(0, getParams.length() - 1);
        }
        String token = SharedPreferencesUtils.getString(MyApplication.getApplication(), Config.LOGIN_TOKEN, "");
        if (!TextUtils.isEmpty(token)) {
            getParams += "&customerToken=" + token;
        }

        Logger.d("请求参数==" + getParams);
        String url = null;
        if (method.contains("http")) {                                          
            if (!TextUtils.isEmpty(getParams)) {
                url = method + "?" + getParams;
            } else {
                url = method;
            }
        } else {
            if (!TextUtils.isEmpty(getParams)) {
                url = RequestUrls.url + method + "?" + getParams;
            } else {
                url = RequestUrls.url + method;
            }
        }

        Logger.d("请求url == " + url);

        RequestParams params = new RequestParams(url);
        setToken(params);

//        params.setCacheMaxAge(1000);
        params.setConnectTimeout(15 * 1000);
        params.setAsJsonContent(true);
        return params;
    }

    private static void putJsonIntoMaps(String json, HashMap<String, Object> hashMap) {
        HashMap mapType = JSON.parseObject(json, HashMap.class);
        Set<String> keySet = mapType.keySet();
        for (String key : keySet) {
            if ("param".equals(key)) {
                Object object = mapType.get(key);
                String jsonTmp = object.toString();
                putJsonIntoMaps(jsonTmp, hashMap);
            } else {
                Object object = mapType.get(key);
                if (object != null && !"".equals(object)) {
                    if ("method".equals(key)) {
                        hashMap.put(key, object);
                    } else {
                        try {
                            String tempera=URLEncoder.encode(object.toString(), "UTF-8");
                            String paraName=URLEncoder.encode(tempera, "UTF-8");
                            hashMap.put(key,paraName);
//                            hashMap.put(key, object.toString());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            Logger.i("参数编码错误了--》" + object);
                        }
                    }
                }
            }
        }
    }

    private static void putJsonIntoMap(String json, HashMap<String, Object> hashMap) {
        HashMap mapType = JSON.parseObject(json, HashMap.class);
        Set<String> keySet = mapType.keySet();
        for (String key : keySet) {
            if ("param".equals(key)) {
                Object object = mapType.get(key);
                String jsonTmp = object.toString();
                putJsonIntoMap(jsonTmp, hashMap);
            } else {
                Object object = mapType.get(key);
                if (object != null && !"".equals(object)) {
                    if ("method".equals(key)) {
                        hashMap.put(key, object);
                    } else {
                        try {
                            hashMap.put(key,URLEncoder.encode(object.toString(), "UTF-8") );
//                            hashMap.put(key, object.toString());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            Logger.i("参数编码错误了--》" + object);
                        }
                    }
                }
            }
        }
    }


    private static String getMethod(HashMap<String, Object> hashMap) {
        return hashMap.get("method").toString();
    }

    private static String getParams(HashMap<String, Object> hashMap) {
        String params = "";
        Set<String> keySet = hashMap.keySet();
        for (String key : keySet) {
            if (!"method".equals(key)) {
                params += key;
                params += "=";
                params += hashMap.get(key);
                params += "&";
            }
        }
        return params;

    }

    /**
     * 封装参数 post
     *
     * @param obj
     * @return
     */

    public static RequestParams setRequestPost(Object obj, File file) {
        String url = RequestUrls.url;
        RequestParams params = new RequestParams(url);
        if (obj != null) {
            ArrayList<KeyValue> keyValues = parserClass(obj.getClass(), obj, new ArrayList<KeyValue>());
            if (file != null && file.exists()) {
                keyValues.add(new KeyValue("file", file));
            }
            RequestBody requestBody = new MultipartBody(keyValues, "UTF-8");
            params.setRequestBody(requestBody);
            setToken(params);
        }
        params.setConnectTimeout(120 * 1000);
        params.setMultipart(true);
        return params;
    }

    private static void setToken(RequestParams params) {
        UserInfoResp mUser = CacheUtils.getIn().getUserInfo();
        if (mUser != null && !TextUtils.isEmpty(mUser.getToken())) {
            params.setHeader("customerToken", mUser.getToken());
        }
    }

    private static ArrayList<KeyValue> parserClass(Class cls, Object data, ArrayList<KeyValue> params) {
        Field[] fields = cls.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                field.getName();
                field.setAccessible(true);
                Object object = null;
                try {
                    object = field.get(data);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                } finally {
                    if (!field.getName().contains("$")) {
                        if (object == null) {
                            params.add(new KeyValue(field.getName(), ""));
                        } else if (object instanceof VerifyInfoRequest) {
                            params.add(new KeyValue(field.getName(), JSON.toJSONString(object)));
//                            params.put(field.getName(), JSON.toJSONString(object));
                        } else {
                            params.add(new KeyValue(field.getName(), object.toString()));
                        }
                    }
                }
            }
        }
        if (cls.getSuperclass() != null) {
            parserClass(cls.getSuperclass(), data, params);
        }
        return params;
    }
}
