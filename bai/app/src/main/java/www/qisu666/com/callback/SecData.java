package www.qisu666.com.callback;

public class SecData {
    private String code;
    private String msg;
    private UserInfoResp result;
    private String method;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public UserInfoResp getResult() {
        return result;
    }

    public void setResult(UserInfoResp result) {
        this.result = result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}
