package communication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


import java.util.ArrayList;

import observer.ICommunicationListener;
import observer.IConnectionListener;
import threadProcessing.AbstractThread;
import threadProcessing.AcceptThread;
import threadProcessing.ConnectThread;
import threadProcessing.ConnectedThread;

/**
 * Created by Bailantaotao on 2014/8/19.
 */
public class ManagerBt extends AbstractCommunication implements ICommunicationListener, IConnectionListener {

    private boolean SecureOption = false;

    private final BluetoothAdapter mAdapter;

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;

    public ArrayList<ConnectedThread> getmConnThreads() {
        return mConnThreads;
    }

    private ArrayList<ConnectedThread> mConnThreads;

    public Handler getmHandler() {
        return mHandler;
    }

    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    private Handler mHandler =  null;

    /**
     * constructor
     */
    public ManagerBt() {
        super(ManagerBt.class.getSimpleName());
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mConnThreads = new ArrayList<ConnectedThread>();

    }

    @Override
    public synchronized void shutDown() {
        Logd("stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        Logd("BTChatService stop, connection count is "+ String.valueOf(mConnThreads.size()));
        for(ConnectedThread cThread:mConnThreads)
        {
            if (cThread != null) {
                cThread.cancel();
                cThread = null;
            }
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setCommandState(CommandState.NONE);
    }

    public boolean isSecureOption() {
        return SecureOption;
    }

    public void setSecureOption(boolean secureOption) {
        SecureOption = secureOption;
    }


    @Override
    public synchronized void stateChange(byte channel) {
        switch(channel)
        {
            case AbstractThread.AcceptThread_CHANNEL:

                break;
            case AbstractThread.ConnectThread_CHANNEL:

                break;
            case AbstractThread.ConnectedThread_CHANNEL:

                break;
            default:
                break;
        }
    }

    @Override
    public synchronized void dataChange(byte channel, int MSG_WHAT, String deviceName, String data) {
        switch (channel)
        {
            case AbstractThread.AcceptThread_CHANNEL:

                break;
            case AbstractThread.ConnectThread_CHANNEL:

                break;
            case AbstractThread.ConnectedThread_CHANNEL:
                switch(MSG_WHAT)
                {
                    case ConnectedThread.MSG_READ:
                        if(getmHandler() != null)
                        {
                            Message msg = mHandler.obtainMessage(ConnectedThread.MSG_READ);
                            Bundle bundle = new Bundle();
                            bundle.putString(ConnectedThread.KEY_DEVICE_NAME, deviceName);
                            bundle.putString(ConnectedThread.KEY_DEVICE_DATA, data);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }
                        break;
                    case ConnectedThread.MSG_WRITE:
                        if(getmHandler() != null)
                        {
                            Message msg = mHandler.obtainMessage(ConnectedThread.MSG_WRITE);
                            Bundle bundle = new Bundle();
                            bundle.putString(ConnectedThread.KEY_DEVICE_NAME, deviceName);
                            bundle.putString(ConnectedThread.KEY_DEVICE_DATA, data);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }
                        break;
                }
                break;
            default:
                break;
        }

    }

    @Override
    public synchronized void errorCallback (byte channel, int MSG_WHAT, String deviceName, String msg) {
        switch (channel)
        {
            case AbstractThread.AcceptThread_CHANNEL:

                break;
            case AbstractThread.ConnectThread_CHANNEL:

                break;
            case AbstractThread.ConnectedThread_CHANNEL:
                switch(MSG_WHAT) {
                    case ConnectedThread.MSG_DEVICE_DISCONNECT:
                        ConnectedThread r = null;
                        for(ConnectedThread conn:mConnThreads)
                        {
                            if(conn.getDeviceName().equals(deviceName))
                            {
                                r = conn;
                                break;
                            }
                        }
                        mConnThreads.remove(r);
                        if (getmHandler() != null) {
                            mHandler.obtainMessage(ConnectedThread.MSG_DEVICE_DISCONNECT, -1, -1, deviceName).sendToTarget();
                        }
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Logd("start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        for(ConnectedThread cThread:mConnThreads)
        {
            if (cThread != null) {
                cThread.cancel();
                cThread = null;
            }
        }
        setCommandState(CommandState.LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread(isSecureOption(), mAdapter, this, this);
            mAcceptThread.AcceptClient = true;
            mAcceptThread.start();
            Logd("BTChat wait connection");
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * *****Server side doesn't use this method
     *
     * @param device
     *            The BluetoothDevice to connect
     * @param secure
     *            Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Logd("connect to: " + device);

        setSecureOption(secure);
        // Cancel any thread attempting to make a connection
        if (getSensorState() == CommandState.CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure, this, this, mAdapter, mConnectThread);
        mConnectThread.start();
        setCommandState(CommandState.CONNECTING);
    }

    /**
     * this is callback function, called by the ConnectThread class
     * */
    @Override
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, String socketType) {
        Logd("connected, Socket Type:" + socketType);

        // Start the thread to manage the connection and perform transmissions
        ConnectedThread mConnectedThread = new ConnectedThread(socket, socketType, device.getName(), device.getAddress(), this, this);
        mConnectedThread.start();
        // Add each connected thread to an array
        mConnThreads.add(mConnectedThread);


        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(ConnectedThread.MSG_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(ConnectedThread.KEY_DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setCommandState(CommandState.CONNECTED);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param message
     *            The bytes to write
     */
    public void write(String message, String TARGET) {
        // should we bind the mConnThreads to the ConnectedThread that can send message?

        for (ConnectedThread mConThread : mConnThreads) {
            if(!TARGET.equals(TARGET_ALL))
            {
                if(!TARGET.equals(mConThread.getDeviceName()))
                {
                    continue;
                }
            }
            // Create temporary object
            ConnectedThread r;
            // Synchronize a copy of the ConnectedThread
            synchronized (this) {
                if (getSensorState() != CommandState.CONNECTED)
                    return;
                r = mConThread;
            }
            // Perform the write unsynchronized
            r.write(message);
        }

    }
}
