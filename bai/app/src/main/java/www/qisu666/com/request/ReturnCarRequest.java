package www.qisu666.com.request;

/**
 * 时租还车
 */
public class ReturnCarRequest extends RequestBaseParams {
    private String customerId;
    private String carSharingOrderNumber;//时租订单号
    private String carRentOrderNumber;//短租订单号
    private String longitude;
    private String latitude;
    private String depotId;
    private String isCheck;
    private String enjoyDiscounts;//该订单是否享受折扣优惠,"true"：享受

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCarSharingOrderNumber() {
        return carSharingOrderNumber;
    }

    public void setCarSharingOrderNumber(String carSharingOrderNumber) {
        this.carSharingOrderNumber = carSharingOrderNumber;
    }

    public String getCarRentOrderNumber() {
        return carRentOrderNumber;
    }

    public void setCarRentOrderNumber(String carRentOrderNumber) {
        this.carRentOrderNumber = carRentOrderNumber;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getDepotId() {
        return depotId;
    }

    public void setDepotId(String depotId) {
        this.depotId = depotId;
    }

    public String getIsCheck() {
        return isCheck;
    }

    public void setIsCheck(String isCheck) {
        this.isCheck = isCheck;
    }

    public String getEnjoyDiscounts() {
        return enjoyDiscounts;
    }

    public void setEnjoyDiscounts(String enjoyDiscounts) {
        this.enjoyDiscounts = enjoyDiscounts;
    }
}
