package www.qisu666.com.callback;

/**
 * Created by wujiancheng on 2017/11/21.
 * 预约还车网点
 */

public class AppointmentsReturnParkResp {
    private String parkAddress;//
    private String parkFreeCarNum;//
    private String parkFreeCarportNum;//
    private String latitude;//
    private String longitude;//
    private String parkName;//
    private String parkType;//
    private String id;//
    private String remark;//
    private String cityCode;//
    private String spaceNums;//
    private String systemTime;//
    private String orderTime;//
    private String orderSubsistNum;//: 15
    private Float discountLimit;//折扣

    public String getParkAddress() {
        return parkAddress;
    }

    public void setParkAddress(String parkAddress) {
        this.parkAddress = parkAddress;
    }

    public String getParkFreeCarNum() {
        return parkFreeCarNum;
    }

    public void setParkFreeCarNum(String parkFreeCarNum) {
        this.parkFreeCarNum = parkFreeCarNum;
    }

    public String getParkFreeCarportNum() {
        return parkFreeCarportNum;
    }

    public void setParkFreeCarportNum(String parkFreeCarportNum) {
        this.parkFreeCarportNum = parkFreeCarportNum;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getParkName() {
        return parkName;
    }

    public void setParkName(String parkName) {
        this.parkName = parkName;
    }

    public String getParkType() {
        return parkType;
    }

    public void setParkType(String parkType) {
        this.parkType = parkType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getSpaceNums() {
        return spaceNums;
    }

    public void setSpaceNums(String spaceNums) {
        this.spaceNums = spaceNums;
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

    public String getOrderSubsistNum() {
        return orderSubsistNum;
    }

    public void setOrderSubsistNum(String orderSubsistNum) {
        this.orderSubsistNum = orderSubsistNum;
    }

    public Float getDiscountLimit() {
        return discountLimit;
    }

    public void setDiscountLimit(Float discountLimit) {
        this.discountLimit = discountLimit;
    }
}
