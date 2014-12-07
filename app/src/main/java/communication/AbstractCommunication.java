package communication;

import android.util.Log;

import observer.ICommunicationListener;

/**
 * Created by Bailantaotao on 2014/8/19.
 */
public abstract class AbstractCommunication {
    /** Log debug information */
    private final String TAG ;

    /** determine whether or not enable debug message */
    public static final boolean D = true;

    public void Logd(String msg)
    {
        if(D)
            Log.d(TAG, "-----" + msg + "-----");
    }


    /** Call back function */
    public ICommunicationListener Callbacks;

    /** for BT channel */
    public static final byte BT_CHANNEL = (byte) 0;

    /** currents device state */
    public static enum CommandState
    {
        /** The function or command ...etc is error */
        ERROR,

        /** The function or command ...etc is success */
        SUCCESS,

        /** none */
        NONE,

        /** listening for incoming clients */
        LISTEN,

        /** now initiating an outgoing connection */
        CONNECTING,

        /** now connected to a remote device */
        CONNECTED,

        /** not accept a new client, already to connect */
        ACCEPT

    }

    /** Description of device current state */
    private CommandState sensorState = CommandState.NONE;

    /** the message of target */
    public static final String TARGET_ALL = "ALL";

    /** constructor */
    public AbstractCommunication(String TAG)
    {
        this.TAG = TAG;
    }

    /** check status is no problem */
    public boolean IsERROR(CommandState deviceState)
    {
        if(deviceState == CommandState.SUCCESS)
            return false;
        return true;
    }

    /** shut down the specific sensor */
    public abstract void shutDown();

    /** set the sensor state */
    public void setCommandState(CommandState sensorState)
    {
        Logd("setCinnandState(): " + this.sensorState + " -> " + sensorState);
        this.sensorState = sensorState;
    }

    /** get the sensor state */
    public CommandState getSensorState()
    {
        return sensorState;
    }
}
