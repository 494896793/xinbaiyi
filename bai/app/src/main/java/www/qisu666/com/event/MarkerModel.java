package www.qisu666.com.event;


import com.amap.api.maps.model.LatLng;

/**
 * Created by admin on 2016/6/13.
 */
public class MarkerModel {

    public MarkerModel(){}

    public MarkerModel(String id, String title, String count_fast, String count_slow, String elec_type, String address, String distance, LatLng position, String is_favor, String pile_num, String charge_method, String charging_fee, String service_fee, String station_parking_fee){
        this.id = id;
        this.title = title;
        this.count_fast = count_fast;
        this.count_slow = count_slow;
        this.elec_type = elec_type;
        this.address = address;
        this.distance = distance;
        this.position = position;
        this.is_favor = is_favor;
        this.pile_num = pile_num;
        this.charge_method = charge_method;
        this.charging_fee = charging_fee;
        this.service_fee = service_fee;
        this.station_parking_fee = station_parking_fee;
    }

    private String id;
    private String title;
    private String count_fast;
    private String count_slow;
    private String elec_type;
    private String distance;
    private String address;
    private String is_favor;
    private LatLng position;
    private String pile_num;
    private String charge_method;
    private String charging_fee;
    private String service_fee;
    private String station_parking_fee;

    public String getPile_num() {
        return pile_num;
    }

    public void setPile_num(String pile_num) {
        this.pile_num = pile_num;
    }

    public String getCharge_method() {
        return charge_method;
    }

    public void setCharge_method(String charge_method) {
        this.charge_method = charge_method;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCount_fast() {
        return count_fast;
    }

    public void setCount_fast(String count_fast) {
        this.count_fast = count_fast;
    }

    public String getElec_type() {
        return elec_type;
    }

    public void setElec_type(String elec_type) {
        this.elec_type = elec_type;
    }

    public String getCount_slow() {
        return count_slow;
    }

    public void setCount_slow(String count_slow) {
        this.count_slow = count_slow;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getIs_favor() {
        return is_favor;
    }

    public void setIs_favor(String is_favor) {
        this.is_favor = is_favor;
    }

    public String getCharging_fee() {
        return charging_fee;
    }

    public void setCharging_fee(String charging_fee) {
        this.charging_fee = charging_fee;
    }

    public String getService_fee() {
        return service_fee;
    }

    public void setService_fee(String service_fee) {
        this.service_fee = service_fee;
    }

    public String getStation_parking_fee() {
        return station_parking_fee;
    }

    public void setStation_parking_fee(String station_parking_fee) {
        this.station_parking_fee = station_parking_fee;
    }
}
