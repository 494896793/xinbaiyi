package www.qisu666.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.HashMap;
import java.util.Map;

import www.qisu666.com.R;
import www.qisu666.com.utils.ConstantCode;
import www.qisu666.com.utils.DialogHelper;
import www.qisu666.com.widget.AlertDialog;

//充电时的选项   类型等
public class ChargingFilterActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    private RadioButton rb_interface_all;
    private RadioButton rb_interface_1;
    private RadioButton rb_interface_2;
    private RadioButton rb_mode_all;
    private RadioButton rb_mode_1;
    private RadioButton rb_mode_2;
    private RadioButton rb_type_all;
    private RadioButton rb_type_1;
    private RadioButton rb_type_2;
    private RadioButton rb_carr_all;
    private RadioButton rb_carr_1;
    private RadioButton rb_carr_2;
//    private SwitchButton sb_is_void;
//    private SwitchButton sb_with_gun;
    private SwitchButton sb_free_parking;
    private SwitchButton sb_all_day;
    private Button btn_submit;

    private String freeParking;
    private String allDay;
    private boolean isFreeParkingChanged = false;
    private boolean isAllDayChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView(R.layout.activity_charging_filter);
        initView();
    }

    private void initView() {
        initTitleBar();

        rb_interface_all = (RadioButton) findViewById(R.id.rb_interface_all);
        rb_interface_1 = (RadioButton) findViewById(R.id.rb_interface_1);
        rb_interface_2 = (RadioButton) findViewById(R.id.rb_interface_2);
        rb_mode_all = (RadioButton) findViewById(R.id.rb_mode_all);
        rb_mode_1 = (RadioButton) findViewById(R.id.rb_mode_1);
        rb_mode_2 = (RadioButton) findViewById(R.id.rb_mode_2);
        rb_type_all = (RadioButton) findViewById(R.id.rb_type_all);
        rb_type_1 = (RadioButton) findViewById(R.id.rb_type_1);
        rb_type_2 = (RadioButton) findViewById(R.id.rb_type_2);
        rb_carr_all = (RadioButton) findViewById(R.id.rb_carr_all);
        rb_carr_1 = (RadioButton) findViewById(R.id.rb_carr_1);
        rb_carr_2 = (RadioButton) findViewById(R.id.rb_carr_2);
//        rb_interface_1.setOnCheckedChangeListener(this);
//        rb_interface_all.setOnClickListener(this);
//        rb_interface_1.setOnClickListener(this);
//        rb_interface_2.setOnClickListener(this);
//        rb_mode_all.setOnClickListener(this);
//        rb_mode_1.setOnClickListener(this);
//        rb_mode_2.setOnClickListener(this);
//        rb_type_all.setOnClickListener(this);
//        rb_type_1.setOnClickListener(this);
//        rb_type_2.setOnClickListener(this);
//        rb_carr_all.setOnClickListener(this);
//        rb_carr_1.setOnClickListener(this);
//        rb_carr_2.setOnClickListener(this);

//        sb_is_void = (SwitchButton) findViewById(R.id.sb_is_void);
//        sb_with_gun = (SwitchButton) findViewById(R.id.sb_with_gun);
        sb_free_parking = (SwitchButton) findViewById(R.id.sb_free_parking);
        sb_free_parking.setOnCheckedChangeListener(this);
        sb_all_day = (SwitchButton) findViewById(R.id.sb_all_day);
        btn_submit = (Button) findViewById(R.id.btn_submit);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        initFilter();
    }

    /**
     * 初始化筛选条件
     */
    private void initFilter() {
        reset();
        Map<String, String> filterMap = (Map<String, String>) getIntent().getSerializableExtra("filterMap");
        if("01".equals(filterMap.get("charge_interface"))){
            rb_interface_1.setChecked(true);
        } else if("02".equals(filterMap.get("charge_interface"))){
            rb_interface_2.setChecked(true);
        } else {
            rb_interface_all.setChecked(true);
        }

        if("0".equals(filterMap.get("charge_carr"))){
            rb_carr_1.setChecked(true);
        } else if("1".equals(filterMap.get("charge_carr"))){
            rb_carr_2.setChecked(true);
        } else {
            rb_carr_all.setChecked(true);
        }

        if("01".equals(filterMap.get("charge_method"))){
            rb_mode_1.setChecked(true);
        } else if("02".equals(filterMap.get("charge_method"))){
            rb_mode_2.setChecked(true);
        } else {
            rb_mode_all.setChecked(true);
        }

        if("01".equals(filterMap.get("charge_pile_bel"))){
            rb_type_1.setChecked(true);
        } else if("02".equals(filterMap.get("charge_pile_bel"))){
            rb_type_2.setChecked(true);
        } else {
            rb_type_all.setChecked(true);
        }

        /*if("02".equals(filterMap.get("pile_state"))){
            sb_is_void.setChecked(false);
        }

        if("0".equals(filterMap.get("charging_gun"))){
            sb_with_gun.setChecked(true);
        }*/

        if("0".equals(filterMap.get("parking_free"))){
            sb_free_parking.setChecked(true);
        } else {
            sb_free_parking.setChecked(false);
        }

        if("0".equals(filterMap.get("service_time"))){
            sb_all_day.setChecked(true);
        } else {
            sb_all_day.setChecked(false);
        }


    }

    /**
     * 提交参数
     */
    private void submit() {
        HashMap<String, String> map = new HashMap<>();
        map.put("charge_interface", (!rb_interface_1.isChecked() && !rb_interface_2.isChecked()) ? null:(rb_interface_1.isChecked()?"01":"02"));
        map.put("charge_carr", (!rb_carr_1.isChecked() && !rb_carr_2.isChecked()) ? null:(rb_carr_1.isChecked()?"0":"1"));
        map.put("charge_method", (!rb_mode_1.isChecked() && !rb_mode_2.isChecked()) ? null:(rb_mode_1.isChecked()?"01":"02"));
        map.put("charge_pile_bel", (!rb_type_1.isChecked() && !rb_type_2.isChecked()) ? null:(rb_type_1.isChecked()?"01":"02"));
//        map.put("pile_state", sb_is_void.isChecked() ? "01":null);
//        map.put("charging_gun", sb_with_gun.isChecked() ? "0":null);
        map.put("parking_free", sb_free_parking.isChecked() ? "0":null);
        map.put("service_time", sb_all_day.isChecked() ? "0":null);

        Intent intent = new Intent();
        intent.putExtra("filter", map);
        setResult(ConstantCode.RES_FILTER, intent);
        finish();
    }

    private void initTitleBar() {
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText("筛选");
        View left_btn = findViewById(R.id.img_title_left);
        left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ImageView right_btn = (ImageView) findViewById(R.id.img_title_right);
        right_btn.setImageResource(R.mipmap.ic_charging_refresh);
        right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.confirmDialog(ChargingFilterActivity.this, "确认重置选项？", new AlertDialog.OnDialogButtonClickListener() {

                    @Override
                    public void onConfirm() {
                        reset();
                    }
                    @Override
                    public void onCancel() {          }


                });
            }
        });
    }

    private void reset() {
        rb_interface_all.setChecked(true);
        rb_interface_1.setChecked(false);
        rb_interface_2.setChecked(false);
        rb_mode_all.setChecked(true);
        rb_mode_1.setChecked(false);
        rb_mode_2.setChecked(false);
        rb_type_all.setChecked(true);
        rb_type_1.setChecked(false);
        rb_type_2.setChecked(false);
        rb_carr_all.setChecked(true);
        rb_carr_1.setChecked(false);
        rb_carr_2.setChecked(false);

//        sb_is_void.setChecked(true);
//        sb_with_gun.setChecked(false);
        sb_free_parking.setChecked(false);
        sb_all_day.setChecked(false);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.sb_free_parking:
                isFreeParkingChanged = !isFreeParkingChanged;
                break;
            case R.id.sb_all_day:
                isAllDayChanged = !isAllDayChanged;
                break;
        }
    }

    @Override
    public void setView() {

    }

    @Override
    public void initDatas() {

    }

    @Override
    public void onComplete(String result, int type) {

    }

    @Override
    public void onFailure(String msg, int type) {

    }

    /*private void RadioButtonToggle(RadioButton[] RadioButtones, RadioButton RadioButton){
        if(!RadioButton.isChecked()){
            return;
        }
        for(RadioButton rb : RadioButtones){
            if(rb.isChecked()){
                rb.setChecked(false);
            }
        }
        RadioButton.setChecked(!RadioButton.isChecked());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rb_interface_1:
                RadioButtonToggle(new RadioButton[]{rb_interface_1, rb_interface_2}, rb_interface_1);
                break;
            case R.id.rb_interface_2:
                RadioButtonToggle(new RadioButton[]{rb_interface_1, rb_interface_2}, rb_interface_2);
                break;
            case R.id.rb_mode_1:
                RadioButtonToggle(new RadioButton[]{rb_mode_1, rb_mode_2}, rb_mode_1);
                break;
            case R.id.rb_mode_2:
                RadioButtonToggle(new RadioButton[]{rb_mode_1, rb_mode_2}, rb_mode_2);
                break;
            case R.id.rb_type_1:
                RadioButtonToggle(new RadioButton[]{rb_type_1, rb_type_2}, rb_type_1);
                break;
            case R.id.rb_type_2:
                RadioButtonToggle(new RadioButton[]{rb_type_1, rb_type_2}, rb_type_2);
                break;
            case R.id.rb_carr_1:
                RadioButtonToggle(new RadioButton[]{rb_carr_1, rb_carr_2}, rb_carr_1);
                break;
            case R.id.rb_carr_2:
                RadioButtonToggle(new RadioButton[]{rb_carr_1, rb_carr_2}, rb_carr_2);
                break;
        }
    }*/
}
