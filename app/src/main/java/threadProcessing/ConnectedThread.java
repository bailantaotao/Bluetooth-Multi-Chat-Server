package threadProcessing;

import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import observer.ICommunicationListener;
import observer.IConnectionListener;

/**
 * Created by Bailantaotao on 2014/8/19.
 * This thread runs during a connection with a remote device. It handles all
 * incoming and outgoing transmissions.
 */
public class ConnectedThread extends AbstractThread {

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    private String deviceName = "";
    private String macAddress = "";
    private boolean AcceptMessage = false;

    public ConnectedThread(String TAG, IConnectionListener callbacksBTFunction, ICommunicationListener callbacksData, BluetoothSocket mmSocket, String name) {
        super(TAG, callbacksBTFunction, callbacksData);
        this.mmSocket = null;
        this.mmInStream = null;
        this.mmOutStream = null;
        setDeviceName(name);
    }

    public ConnectedThread(BluetoothSocket socket, String socketType, String deviceName, String macAddress, ICommunicationListener callbacksData, IConnectionListener callbacksBTFunction) {
        super(ConnectedThread.class.getSimpleName(), callbacksBTFunction, callbacksData);
        Logd("create ConnectedThread: " + socketType);
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.deviceName = deviceName;
        this.macAddress = macAddress;
        this.AcceptMessage = true;
        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            errorCallback(ConnectedThread_CHANNEL, MSG_SOCKET_CREATE_ERROR ,getDeviceName() ,"temp sockets not created" + e.toString());
            Loge("temp sockets not created" + e);
        }
        currentConnections++;
        Logd("BTChat accept connections is " + String.valueOf(currentConnections));
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        Logd("BEGIN mConnectedThread");
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (AcceptMessage) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);

                String msg = new String(buffer, 0, bytes);
                Logd("Received from "+getDeviceName() + ", msg = " + new String(buffer, 0, bytes));
//                if (!msg.contains("/")) {
////                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
//
//                    continue;
//                }
//                String[] verifyMsg = msg.split("/");
//                if (verifyMsg.length > 0) {
//                    if (!verifyMsg[0].equals("OPERATION"))
//                        continue;
//                    if (!verifyMsg[1].equals("DISCONNECT"))
//                        continue;
////                    BluetoothChatService.this.connectionInterrupt(verifyMsg[2]);
//                }
                callbacksData.dataChange(ConnectedThread_CHANNEL, MSG_READ, getDeviceName(), msg);
                Logd("Received string: " + new String(buffer, 0, bytes));
            } catch (IOException e) {
                Loge("disconnected " + e);
                errorCallback(ConnectedThread_CHANNEL, MSG_DEVICE_DISCONNECT, getDeviceName(),
                        "disconnected, device name = "+getDeviceName() +
                        ", MAC address = " + getMacAddress() + ", reason = "+ e.toString());
                if(currentConnections!=0)
                    currentConnections--;
                Logd("connections is " + currentConnections);
//                connectionLost();
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param message The string to write
     */
    public void write(String message) {
        byte[] buffer = message.getBytes();
        try {
            mmOutStream.write(buffer);
            callbacksData.dataChange(ConnectedThread_CHANNEL, MSG_WRITE, "Local", message);
            Logd("write to "+getDeviceName() + ", msg = " + message);
            // Share the sent message back to the UI Activity
//            mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
        } catch (IOException e) {
            errorCallback(ConnectedThread_CHANNEL, MSG_WRITE_ERROR, "Local", "Exception during write" + e.toString());
            Logd("Exception during write" + e);
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            errorCallback(ConnectedThread_CHANNEL, MSG_SOCKET_CLOSE_ERROR, getDeviceName(), "close() of connect socket failed" + e.toString());
            Logd("close() of connect socket failed" + e);
        }
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public String getDeviceName() {
        return deviceName;
    }
    public String getMacAddress() {
        return macAddress;
    }

}
