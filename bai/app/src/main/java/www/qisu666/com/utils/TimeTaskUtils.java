package www.qisu666.com.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

/**
 * Created by admin on 2017/3/8.
 */
public class TimeTaskUtils extends Service {
    public static TimerTaskListener timerTasklistener;
    private static MyThread myThread;
    private static int time = 0;
    private static boolean isLive;

    private static void startThread() {
        if (myThread == null) {
            myThread = new MyThread();
            new Thread(myThread).start();
        }
    }

    public static void cancelThread() {
        if (myThread != null) {
            isLive = false;
            handler.removeCallbacks(myThread);
            myThread = null;
        }
    }

    public static void setTime(int carTime) {
        if (myThread != null) {
            cancelThread();
        }
        time = carTime;
        startThread();
        isLive = true;
    }

    static Handler handler = new Handler() {   // handle
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ++time;
                    if (timerTasklistener != null) {
                        timerTasklistener.goTime(time);
                    }
            }
            super.handleMessage(msg);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static class MyThread implements Runnable {
        @Override
        public void run() {
            while (isLive) {
                try {
                    Thread.sleep(1000);  // sleep 1000ms
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                } catch (Exception e) {
                }
            }
        }
    }

    public interface TimerTaskListener {
        void goTime(int time);
    }
}
