package threadProcessing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

import observer.ICommunicationListener;
import observer.IConnectionListener;
import utility.MetadataBt;

/**
 * Created by Bailantaotao on 2014/8/19.
 */
public class AcceptThread extends AbstractThread {

    private static final int MAX_BLUETOOTH_CONNECTIONS = 7;

    // The local server socket
    private final BluetoothServerSocket mmServerSocket;
    private String mSocketType;
    public boolean AcceptClient = false;

    public AcceptThread(boolean secure, BluetoothAdapter mAdapter, IConnectionListener callbacksBTFunction, ICommunicationListener callbacksData) {
        super(ConnectedThread.class.getSimpleName(), callbacksBTFunction, callbacksData);
        BluetoothServerSocket tmp = null;
        mSocketType = secure ? "Secure" : "Insecure";

        // Create a new listening server socket
        try {
            if (secure) {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(MetadataBt.NAME_SECURE, MetadataBt.MY_UUID_SECURE);
            }
            else {
                tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(MetadataBt.NAME_INSECURE, MetadataBt.MY_UUID_INSECURE);
            }
        } catch (IOException e) {
            Loge("Socket Type: " + mSocketType + "listen() failed" + e);
            errorCallback(AcceptThread_CHANNEL, MSG_SOCKET_LISTEN_ERROR, null, "Socket Type: " + mSocketType + "listen() failed" + e.toString());
        }
        mmServerSocket = tmp;
    }

    public void run() {
        Logd("Socket Type: " + mSocketType + "BEGIN mAcceptThread" + this);

        setName("AcceptThread" + mSocketType);

        BluetoothSocket socket = null;

        // Listen to the server socket if we're not connected
        while (AcceptClient) {
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                if(currentConnections < MAX_BLUETOOTH_CONNECTIONS) {
                    socket = mmServerSocket.accept();
                    if(socket != null)
                    {
                        String address = socket.getRemoteDevice().getAddress();
                        if(callbacksBTFunction!=null)
                            callbacksBTFunction.connected(socket, socket.getRemoteDevice(), mSocketType);

                    }
                }
                else
                {
                    Logd("Current connections = " + currentConnections + ", is greater than MAX bluetooth connections ( "+MAX_BLUETOOTH_CONNECTIONS+" )");
                }
            } catch (IOException e) {
                Loge("Socket Type: " + mSocketType + "accept() failed" + e);
                errorCallback(AcceptThread_CHANNEL, MSG_SOCKET_ACCEPT_ERROR, null, "Socket Type: " + mSocketType + "accept() failed" + e);
//                    break;
            }
            Logd("END mAcceptThread, socket Type: " + mSocketType);
        }

    }

    public void cancel() {
        Logd("Socket Type" + mSocketType + "cancel " + this);
        AcceptClient = false;
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Logd("Socket Type" + mSocketType + "close() of server failed" + e);
            errorCallback(AcceptThread_CHANNEL, MSG_SOCKET_CLOSE_ERROR, null, "Socket Type" + mSocketType + "close() of server failed" + e);
        }
    }
}
