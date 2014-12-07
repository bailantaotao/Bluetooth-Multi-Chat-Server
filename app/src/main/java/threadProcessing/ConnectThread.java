package threadProcessing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

import observer.ICommunicationListener;
import observer.IConnectionListener;
import utility.MetadataBt;

/**
 * Created by Bailantaotao on 2014/8/19.
 */
public class ConnectThread extends AbstractThread {

    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private String mSocketType;
    private BluetoothAdapter mAdapter = null;
    private ConnectThread mConnectThread = null;


    public ConnectThread(BluetoothDevice device, boolean secure, IConnectionListener callbacksBTFunction, ICommunicationListener callbacksData,
                         BluetoothAdapter mAdapter, ConnectThread mConnectThread) {
        super(ConnectThread.class.getSimpleName(), callbacksBTFunction, callbacksData);
        mmDevice = device;
        BluetoothSocket tmp = null;
        mSocketType = secure ? "Secure" : "Insecure";
        this.mAdapter = mAdapter;
        this.mConnectThread = mConnectThread;
        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            if (secure) {
                // Parse the UUIDs and get the one you are interested in
                tmp = device.createRfcommSocketToServiceRecord(MetadataBt.MY_UUID_SECURE);
            } else {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MetadataBt.MY_UUID_INSECURE);
            }

        } catch (IOException e) {
            errorCallback(ConnectThread_CHANNEL, MSG_SOCKET_CREATE_ERROR, null, "Socket Type: " + mSocketType + "create() failed" + e.toString());
            Loge("Socket Type: " + mSocketType + "create() failed" + e);
        }

        mmSocket = tmp;
    }

    public void run() {
        Logd("BEGIN mConnectThread SocketType:" + mSocketType);
        setName("ConnectThread" + mSocketType);

        // Always cancel discovery because it will slow down a connection
        mAdapter.cancelDiscovery();

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mmSocket.connect();
        } catch (IOException e) {
            // Close the socket
            try {
                mmSocket.close();
            } catch (IOException e2) {
                errorCallback(ConnectThread_CHANNEL, MSG_SOCKET_CLOSE_ERROR, null, "unable to close() " + mSocketType + " socket during connection failure" + e2.toString());
                Loge("unable to close() " + mSocketType + " socket during connection failure" + e2);
            }
            errorCallback(ConnectThread_CHANNEL, MSG_SOCKET_UNABLE_CONNECT, null, "unable to connect, because " + e.toString());
            Loge("unable to connect, because " + e.toString());
//            connectionFailed();
            return;
        }

        // Reset the ConnectThread because we're done
        synchronized (ConnectThread.this) {
            mConnectThread = null;
        }
        Logd("mConnectThread = " + (mConnectThread==null));
        // Start the connected thread
        if(callbacksBTFunction!=null)
            callbacksBTFunction.connected(mmSocket, mmDevice, mSocketType);
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            errorCallback(ConnectThread_CHANNEL, MSG_SOCKET_CLOSE_ERROR, null, "close() of connect " + mSocketType + " socket failed" + e);
            Loge("close() of connect " + mSocketType + " socket failed" + e);
        }
    }

}
