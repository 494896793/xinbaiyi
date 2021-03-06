package www.qisu666.com.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import www.qisu666.com.R;
import www.qisu666.com.utils.ImageLogic;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import uk.co.senab.photoview.PhotoViewAttacher;

//缩放image图
public class ZoomImageActivity extends BaseActivity {

    private ImageView ziv_pic;
    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_zoom_image);
        ziv_pic = (ImageView) findViewById(R.id.ziv_pic);
//        ziv_pic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//        initViews();
        String path = getIntent().getStringExtra("img_path");
        ImageLogic.sendRequest(path, ziv_pic, 0, 0);
        mAttacher = new PhotoViewAttacher(ziv_pic);
//        new BitmapTask().execute(path);
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

    //自定义异步任务
    class BitmapTask extends AsyncTask<String,Integer,Bitmap> {
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ziv_pic.setImageBitmap(bitmap);
            super.onPostExecute(bitmap);
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            try {  return getBitmap(params[0]);  } catch (IOException e) { return null;  }
        }
    }

    //从网络获取位图  写法有点落后
    public static Bitmap getBitmap(String path) throws IOException {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setRequestMethod("GET");
        if(conn.getResponseCode() == 200){
            InputStream inputStream = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        }
        return null;
    }

//    private void initTitleBar() {
//        TextView title = (TextView) findViewById(R.id.tv_title);
//        title.setText(R.string.build_pile_title);
//        View left_btn = findViewById(R.id.img_title_left);
//        left_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//    }
//
//    /**
//     * 初始化控件
//     */
//    private void initViews() {
//        initTitleBar();
//        findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(ZoomImageActivity.this, ChargingOwnerActivity.class));
//                finish();
//            }
//        });
//    }
}
