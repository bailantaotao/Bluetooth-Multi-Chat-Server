package observer;

public interface ICommunicationListener {

    public void stateChange(byte channel);
    public void dataChange(byte channel, int MSG_WHAT, String deviceName, String data);
    public void errorCallback(byte channel, int MSG_WHAT, String deviceName, String msg);
}
