package www.qisu666.com.callback;

import java.io.Serializable;

public class OrderCarResp implements Serializable {
    private String carSharingOrderNumber;//订单号 string
    private String systemTime;
    private String orderTime;

    public String getCarSharingOrderNumber() {
        return carSharingOrderNumber;
    }

    public void setCarSharingOrderNumber(String carSharingOrderNumber) {
        this.carSharingOrderNumber = carSharingOrderNumber;
    }

    public String getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(String systemTime) {
        this.systemTime = systemTime;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }
}
