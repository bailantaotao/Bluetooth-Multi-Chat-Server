package threadProcessing;

import android.util.Log;

import observer.ICommunicationListener;
import observer.IConnectionListener;

/**
 * Created by Bailantaotao on 2014/8/19.
 */
public abstract class AbstractThread extends Thread{
    // Debugging
    private final String TAG;
    private static final boolean D = true;
    public void Logd(String Msg) {
        if (D)
            Log.d(TAG, "-----" + Msg + "-----");
    }
    public void Loge(String Msg) {
        if (D)
            Log.e(TAG, "*****" + Msg + "*****");
    }

    static int currentConnections = 0;

    public static final byte AcceptThread_CHANNEL = (byte) 0;
    public static final byte ConnectThread_CHANNEL = (byte) 1;
    public static final byte ConnectedThread_CHANNEL = (byte) 2;

    public static final int MSG_READ = 0;
    public static final int MSG_WRITE = 1;
    public static final int MSG_DEVICE_NAME = 2;
    public static final int MSG_DEVICE_DISCONNECT = 3;
    public static final int MSG_WRITE_ERROR = 4;
    public static final int MSG_SOCKET_CLOSE_ERROR = 5;
    public static final int MSG_SOCKET_CREATE_ERROR = 6;
    public static final int MSG_SOCKET_LISTEN_ERROR = 7;
    public static final int MSG_SOCKET_ACCEPT_ERROR = 8;
    public static final int MSG_SOCKET_UNABLE_CONNECT = 9;

    public static final String KEY_DEVICE_NAME = "DEVICE_NAME";
    public static final String KEY_DEVICE_DATA = "DEVICE_DATA";


    IConnectionListener callbacksBTFunction = null;
    ICommunicationListener callbacksData = null;

    public AbstractThread(String TAG, IConnectionListener callbacksBTFunction, ICommunicationListener callbacksData)
    {
        this.TAG = TAG;
        this.callbacksBTFunction = callbacksBTFunction;
        this.callbacksData = callbacksData;
    }

    public void errorCallback(byte channel, int MSG_WHAT, String deviceName, String msg) {
        if(callbacksData!=null)
            callbacksData.errorCallback(channel, MSG_WHAT, deviceName, msg);
    }

}
