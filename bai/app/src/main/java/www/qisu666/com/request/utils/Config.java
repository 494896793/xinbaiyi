package www.qisu666.com.request.utils;

/**
 * 717219917@qq.com ${DATA} 11:21.
 */

public class Config {
    /**
     * 谷歌转高德直接加    高德转谷歌减
     */
//    lat -0.002868    lng   +0.004929  //
    public static double OFF_LAT=-0.002868;
    public static double OFF_LNG=0.004929;


    public static double OFF_LAT_BAIDU=-0.0061651;
    public static double OFF_LNG_BAIDU=0.00646262;

    /**
     * 来源，App：1
     */
    public final static String REG_CHANL = "01";
    /**
     *   face id的key
     */
    public static final String FACE_KEY = "7fIb47kR1fNeoFnOZ1RvjTLZvDWgMIj7";
    /**
     *   face id的api_secret
     */
    public static final String FACE_SECRET = "zPtVSbB5515aDIdnsFKMQADNXnh-jHVF";

    //友盟分享相关key start
    /**
     * 微信
     */
//    public static final String WECHAT_APPID="wxf46055df3f9eafeb";
    /**
     * 微信
      */
//    public static final String WECHAT_APPSECRET="22e9f0e71ca95172befe5b7a9d4fa443";
    public static final String WECHAT_APPID="wx1f605acead7ddda8";
    public static final String WECHAT_APPSECRET="d54099cd05b6010ded23d697e6d48f47";

    /**
     * 新浪
     */
    public static final String SINA_APPKEY="2765245093";
    public static final String SINA_APPSECRET="45797c6ec3fb24aefed513e446fae93a";
    /**
     * QQ空间
     */
    public static final String QQZONE_APPKEY="101462113";
    public static final String QQZONE_APPSECRET="7b9af9c8d4b9e2d3ea1d087d76958149";
    // 友盟分享相关key end

    // 图片服务器环境配置
    /**
     * 图片正式服务器
     */
    public static final String IMAGE_HOST = "http://qsimages.qisu666.com";
    /**
     * 图片测试服务器
     */
//      public static final String IMAGE_HOST = "http://218.17.140.195:18080";

    //图片测试服务器
//    public static final String IMAGE_HOST = "http://192.168.1.203:18080";

    /*测试生产环境*/
//    public static final String BASE ="http://218.17.140.195:8000/qs-uas/";
//    public static final String IMAGE_HOST = "http://218.17.140.195:18080";

//    佰壹个人后台
//    public final static String BASE="http://192.168.2.118:8000";

    //常文兵
//        public final static String BASE="http://192.168.2.118:6005";
        //徐亚桥
//    public final static String BASE="http://192.168.2.16:6005";

    public final static String BASE="http://interface.baogny.com/chargingPile/";

    // 生产环境服务器配置
    /**
     * 正式环境 - mobi
     */
      public final static String REQUEST_URL = "http://qsapp.qisu666.com/mobi";
    /**
     *
     * 正式环境 - uas
     */
//      public static final String BASE ="http://qsuas.qisu666.com/qs-uas/";
    // 测试环境服务器配置
    /**
     * 测试环境 - 桩点
     */
//      public final static String REQUEST_URL = "http://192.168.1.210:8844/mobi";
    /**
     * 测试环境 - uas
      */
//      public static final String BASE = "http://192.168.1.210:8000/qs-uas/";
    // 灰度测试环境服务器配置
    /**
     * 灰度环境 - mobi
     */
    //public final static String REQUEST_URL = "http://hqsapp.qisu666.com/mobi";
    /**
     * 灰度环境 - uas
     */
    //public static final String        BASE = "http://hqsuas.qisu666.com/qs-uas/";

//    public static final String REQUEST_URL = BuildConfig.REQUEST_URL;
//    public static final String BASE = BuildConfig.BASE;















}
