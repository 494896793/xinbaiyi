package www.qisu666.com.request;

/**
 * Created by wujiancheng on 17-10-17.
 * 提交报销费信息
 */

public class ParkingFeeCommitRequest extends RequestBaseParams {
    private String customerPhone;
    private String carNumber;//  车牌
    private String orderId;// 订单号
    private String applyParkMoney;// 申报金额
    private String receiptStatus;//  收款方式(A 支付宝，WX 微信，B 余额)
    private String invoiceNumber;//  多个发票号码 json字符串
    private String customerNumber;// 微信的openId/支付宝账号
    private String customerName;//  支付宝姓名
    private String orderCategory;//   订单类型('rentOrder'代表短租订单)
    private String isSecondTime;//  是否第二次提交(0.第一次提交 1.再次提交)
    private String clientCategory;
    private String imgUrlList;
    private String imgBatchNo;//批次号

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getApplyParkMoney() {
        return applyParkMoney;
    }

    public void setApplyParkMoney(String applyParkMoney) {
        this.applyParkMoney = applyParkMoney;
    }

    public String getReceiptStatus() {
        return receiptStatus;
    }

    public void setReceiptStatus(String receiptStatus) {
        this.receiptStatus = receiptStatus;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getOrderCategory() {
        return orderCategory;
    }

    public void setOrderCategory(String orderCategory) {
        this.orderCategory = orderCategory;
    }

    public String getIsSecondTime() {
        return isSecondTime;
    }

    public void setIsSecondTime(String isSecondTime) {
        this.isSecondTime = isSecondTime;
    }

    public String getClientCategory() {
        return clientCategory;
    }

    public void setClientCategory(String clientCategory) {
        this.clientCategory = clientCategory;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getImgUrlList() {
        return imgUrlList;
    }

    public void setImgUrlList(String imgUrlList) {
        this.imgUrlList = imgUrlList;
    }

    public String getImgBatchNo() {
        return imgBatchNo;
    }

    public void setImgBatchNo(String imgBatchNo) {
        this.imgBatchNo = imgBatchNo;
    }
}
