package www.qisu666.com.request;

/**
 * Created by wujiancheng on 17-10-17.
 * 请求参数
 */
public class ParkingFeeRequest extends RequestBaseParams {
    /**
     * 停车费Id，订单号，订单类型(短租要传'rentOrder'，代表短租订单)
     */

    private String parkingFeeId;
    private String orderId;
    private String orderCategory;

    public String getParkingFeeId() {
        return parkingFeeId;
    }

    public void setParkingFeeId(String parkingFeeId) {
        this.parkingFeeId = parkingFeeId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderCategory() {
        return orderCategory;
    }

    public void setOrderCategory(String orderCategory) {
        this.orderCategory = orderCategory;
    }
}
