package services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import communication.ManagerBt;

/**
 * Created by Bailantaotao on 2014/8/19.
 */
public class ServiceBt extends Service {
    /** Log debug information */
    private static final String TAG = ServiceBt.class.getSimpleName();

    /** determine whether or not enable debug message */
    public static final boolean D = true;

    private final LocalBinder mBinder = new LocalBinder();

    public void Logd(String msg)
    {
        if(D) Log.d(TAG, "-----" + msg + "-----");
    }

    private ManagerBt mManager;

    public class LocalBinder extends Binder {
        public ManagerBt getManager() {
            return mManager;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logd("First Client bound.");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Logd("Client rebound");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logd("All clients unbound.");
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Logd("Service created.");
        super.onCreate();
        mManager = new ManagerBt();
        mManager.start();
    }

    @Override
    public void onDestroy() {
        mManager.shutDown();
        mManager = null;
        super.onDestroy();
        Logd("Service destroyed.");
    }

}
