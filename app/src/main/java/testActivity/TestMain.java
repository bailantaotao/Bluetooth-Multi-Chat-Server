package testActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import arrayAdapter.deviceAdapter;
import communication.AbstractCommunication;
import communication.ManagerBt;
import services.ServiceBt;
import threadProcessing.AbstractThread;
import threadProcessing.ConnectedThread;

/**
 * Created by Bailantaotao on 2014/8/19.
 */
public class TestMain extends Activity {
    /** Log debug information */
    private static final String TAG = ServiceBt.class.getSimpleName();

    /** determine whether or not enable debug message */
    public static final boolean D = true;
    public void Logd(String msg)
    {
        if(D) Log.d(TAG, "-----" + msg + "-----");
    }

    private ManagerBt mManagerBT = null;

    private boolean mBTBound = false;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton, mSecureButton, mInsecureButton, mScanButton;
    private Spinner mSp;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Name of the local device mac address
    private String mLocalDeviceMacAddress = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    private List<String> mConnections = null;
    private deviceAdapter<ConnectedThread> mSpAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get local Bluetooth MAC address
        mLocalDeviceMacAddress = mBluetoothAdapter.getAddress();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        ((Button) findViewById(R.id.btnSecure)).setText(mBluetoothAdapter.getName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (D)
            Log.e(TAG, "++ ON START ++");
        setupChat();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            mBTBound = bindService(new Intent(this, ServiceBt.class), btConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBTBound) {
            sendMessage("OPERATION/DISCONNECT/" + mLocalDeviceMacAddress, AbstractCommunication.TARGET_ALL);
            unbindService(btConnection);
//            ((ConnectedThread)mSp.getSelectedItem()).
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    mBTBound = bindService(new Intent(this, ServiceBt.class), btConnection, BIND_AUTO_CREATE);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Logd("BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private final ServiceConnection btConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mManagerBT = ((ServiceBt.LocalBinder) service).getManager();
            mManagerBT.setmHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mManagerBT = null;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
//                case MESSAGE_STATE_CHANGE:
//                    if (D)
//                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
//                    switch (msg.arg1) {
//                        case BluetoothChatService.STATE_CONNECTED:
//                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
//                            mConversationArrayAdapter.clear();
//                            break;
//                        case BluetoothChatService.STATE_CONNECTING:
//                            setStatus(R.string.title_connecting);
//                            break;
//                        case BluetoothChatService.STATE_LISTEN:
//                        case BluetoothChatService.STATE_NONE:
//                            setStatus(R.string.title_not_connected);
//                            break;
//                    }
//                    break;
                case ConnectedThread.MSG_READ:
                    mConversationArrayAdapter.add(msg.getData().getString(ConnectedThread.KEY_DEVICE_NAME) +": "+ msg.getData().getString(ConnectedThread.KEY_DEVICE_DATA));
                    break;
                case ConnectedThread.MSG_WRITE:
                    mConversationArrayAdapter.add(msg.getData().getString(ConnectedThread.KEY_DEVICE_NAME) +": "+ msg.getData().getString(ConnectedThread.KEY_DEVICE_DATA));
                    break;
                case ConnectedThread.MSG_DEVICE_NAME:
                    Toast.makeText(getApplicationContext(), "Connected to " + msg.getData().getString(ConnectedThread.KEY_DEVICE_NAME), Toast.LENGTH_SHORT).show();
                    updateConnectionDevice();
                    break;
                case AbstractThread.MSG_DEVICE_DISCONNECT:
                    Toast.makeText(getApplicationContext(), "Device " + ((String)msg.obj) + " is disconnect", Toast.LENGTH_SHORT).show();
                    updateConnectionDevice();
                    break;
            }
        }
    };

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mManagerBT == null)
                    return;

                if(((ArrayList<ConnectedThread>) mManagerBT.getmConnThreads()).size() == 0)
                    return;

                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                Logd("select id = " + ((ConnectedThread)mSp.getSelectedItem()).getDeviceName());
                sendMessage(message, ((ConnectedThread)mSp.getSelectedItem()).getDeviceName());
            }
        });

        mSecureButton = (Button) findViewById(R.id.btnSecure);
        mSecureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent serverIntent = null;
                // Send a message using content of the edit text widget
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(TestMain.this, deviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        });

        mInsecureButton = (Button) findViewById(R.id.btnInsecuce);
        mInsecureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent serverIntent = null;
                // Send a message using content of the edit text widget
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(TestMain.this, deviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            }
        });

        mScanButton = (Button) findViewById(R.id.btnScan);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                // Ensure this device is discoverable by others
                Log.d(TAG, "aaaa");
                ensureDiscoverable();
            }
        });

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        mSp = (Spinner)findViewById(R.id.SpUser);
        mConnections = new ArrayList<String>();
        updateConnectionDevice();
    }
    private void updateConnectionDevice()
    {
        mConnections.clear();
        mConnections.add(AbstractCommunication.TARGET_ALL);
        if(mManagerBT == null)
            return;
        ArrayList<ConnectedThread> tmp = (ArrayList<ConnectedThread>) mManagerBT.getmConnThreads().clone();
        mSpAdapter = new deviceAdapter(this, android.R.layout.simple_spinner_item, tmp);
        mSpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSp.setAdapter(mSpAdapter);
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message, String TARGET) {
        // Check that we're actually connected before trying anything
        if (mManagerBT.getSensorState() != AbstractCommunication.CommandState.CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            mManagerBT.write(message, TARGET);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(deviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mManagerBT.connect(device, secure);
    }

    private void ensureDiscoverable() {
        if (D)
            Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the
            // message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message, ((ConnectedThread)mSp.getSelectedItem()).getDeviceName());
                Logd("select id = " +((ConnectedThread)mSp.getSelectedItem()).getDeviceName());
            }
            if (D)
                Log.i(TAG, "END onEditorAction");
            return true;
        }
    };
}
