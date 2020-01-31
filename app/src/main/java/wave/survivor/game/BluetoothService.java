package wave.survivor.game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothService extends Activity 
{
	
	//Local Bluetooth Adapter
	BluetoothAdapter mBluetoothAdapter;
	
	//Constants
	private static final int REQUEST_ENABLE_BT = 3; 
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	
	//ArrayAdapters that contain discovered and paired devices
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
	
    private static final String NAME = "BluetoothChatSecure";
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_READ = 2;
    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    
    // Member fields
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    EditText toSend;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_menu);
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        
        // If adapter is null, Bluetooth isn't supported
        if (mBluetoothAdapter == null) 
        {
        	Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        else // this else not needed
        	Toast.makeText(this, "Bluetooth works on this device,  congrats", Toast.LENGTH_SHORT).show(); // test toast
        
        
        // If bluetooth is not enabled, this will request for it to be turned on
        if (!mBluetoothAdapter.isEnabled()) 
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        
    	// array adapters will hold the discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.bluetooth_device);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.bluetooth_device);
        
        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
        // this will search for already paired devices to see if desired device is already known
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
	     // If there are paired devices
	    if (pairedDevices.size() > 0) 
	    {
	        // Loop through paired devices
	        for (BluetoothDevice device : pairedDevices) 
	        {
				// Add the name and address to an array adapter to show in a ListView
	        	mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	        }
	    }
	    else 
	    {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    
    	//start(); // added to start listening for incoming connection

	    
    }
    /*
    @Override
    protected void onStart(){
    	
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	start(); // added to start listening for incoming connection
    }
    */
   
    @Override
    protected void onDestroy() 
    {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBluetoothAdapter != null) 
        {
        	mBluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
        
        stop();
    }
    
    public synchronized void stop()
	{
    	if (mConnectThread != null) 
		{
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) 
		{
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) 
		{
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
    }
    
    private void showToast(Context ctx, String str) 
    {
        Toast.makeText(ctx, str, Toast.LENGTH_SHORT).show();
    }
    
    // start listening for incoming connection from AcceptThread 
    public synchronized void start() 
	{
        
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    } 
    
    
    public synchronized void connect(BluetoothDevice device) 
	{
            // Cancel any thread attempting to make a connection
    	Log.d(CONNECTIVITY_SERVICE, device.getName());
    	Log.d(CONNECTIVITY_SERVICE, device.getAddress());
    	
        if (mConnectThread != null) 
        {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) 
        {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }
		   	
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        
    }
    
    
    @SuppressLint({ "NewApi", "NewApi" })
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        
    	if( socket.isConnected()) // test 
    		Log.i("connected()", "This socket is connected");
    	
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        
        
        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        
        
        if( !socket.isConnected())  // test 
    		Log.i("connected()", "This socket is not connected after mAcceptThread");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }
    
    
    private void connectDevice(Intent data) 
	{
        // Get the device MAC address
        String address = data.getExtras().getString(EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        connect(device);
    }
    
    
    public void findDevices(View v)
	{
		Toast.makeText(this, "its in the find devices method", Toast.LENGTH_LONG).show();	// test not needed
		
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
	            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
	            startActivity(discoverableIntent);
	    }
		
		doDiscovery();	//finds paired and new devices
		v.setVisibility(View.GONE);		// makes view invisible, wont take up space
		
	}
    
    public void something(View v)
	{
    	//String something = "in something method";
    	//Toast.makeText(this, something, Toast.LENGTH_LONG).show();
    	Log.e("Something", "In something()");
    	
    	String message = toSend.getText().toString();
    	byte[] send = message.getBytes();
    	write(send);
        //mConnectedThread.write(send);

        // Reset out string buffer to zero and clear the edit text field
        //mOutStringBuffer.setLength(0);
        //mOutEditText.setText(mOutStringBuffer);
    	//mConnectedThread.write("HELLO WORLD!?");
    }
    
    
    
    public void write(byte[] out) 
	{
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) 
		{
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    
    
    //doDiscover will search for paired and new devices
  	private void doDiscovery()
	{
  		
  		  // Indicate scanning in the title
          setProgressBarIndeterminateVisibility(true);
          setTitle(R.string.scanning);
          
          // Turn on sub-title for new devices
          findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
  		
          // If we're already discovering, stop it
          if (mBluetoothAdapter.isDiscovering()) 
		  {
          	mBluetoothAdapter.cancelDiscovery();
          }
          
          mBluetoothAdapter.startDiscovery();
  	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
	{
        getMenuInflater().inflate(R.menu.bluetooth_options, menu);
        return true;
    }
    
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() 
	{ 
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) 
		{
            // Cancel discovery because it's costly and we're about to connect
        	mBluetoothAdapter.cancelDiscovery();
        	
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            Log.d(CONNECTIVITY_SERVICE, info);
            String address = info.substring(info.length() - 17);
            Log.d(CONNECTIVITY_SERVICE, address);
            //Log.d(CONNECTIVITY_SERVICE, String.valueOf(info.length()));
            
            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                        
            connectDevice(intent);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            
        }
    };
    
    // The BroadcastReceiver that listens for discovered devices and
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() 
	{

        @Override
        public void onReceive(Context context, Intent intent) 
		{
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) 
			{
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) 
				{
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } 
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) 
			{
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) 
				{
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
    
    
    private class AcceptThread extends Thread 
	{
        private final BluetoothServerSocket mmServerSocket;
        Activity act; 
        public AcceptThread() 
		{
        	act = BluetoothService.this;
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
        	Log.i("AcceptThread", "Entered AcceptThread Constructor");
            BluetoothServerSocket tmp = null;
            try 
			{
            	Log.i("AcceptThread", "Entered AcceptThread tryblock");
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } 
            catch (IOException e) 
			{ 
            	Log.i("AcceptThread", "listen() failed");
            }
            mmServerSocket = tmp;
        }
     
        public void run() 
		{
        	Log.i("AcceptThread", "Entered run()");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) 
			{
            	Log.i("AcceptThread", "Entered listening block");
                try 
				{
                    socket = mmServerSocket.accept();
                } 
                catch (IOException e) 
				{
                	Log.i("AcceptThread", "accept() failed");
                    break;
                }
                // If a connection was accepted
                if (socket != null) 
				{
                	Log.i("AcceptThread", "Connection was accepted");
                	
                	
                	synchronized (BluetoothService.this) 
					{
                		//mAcceptThread = null;
                		connected(socket, socket.getRemoteDevice());
                		//mAcceptThread = null;
                    }
                	
                	final String connectedDevice = "Successfully connected to " + socket.getRemoteDevice().getName() + " : " + socket.getRemoteDevice().getAddress();
                    act.runOnUiThread(new Runnable() 
                    {
                        public void run() 
                        {
                            showToast(act, connectedDevice);
                        }
                    });
                	//connected(socket, socket.getRemoteDevice());
                    
                    break;
                }
            }
        }
     
        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() 
		{
            try 
			{
                mmServerSocket.close();
            } 
            catch (IOException e) 
			{
            	Log.i("AcceptThread", "close() of server failed");
            }
        }
    }
    
    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        Activity act;
     
        public ConnectThread(BluetoothDevice device)
        {
        	act = BluetoothService.this;
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.i("ConnectThread", "Entered ConnectThread Constructor");
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try 
			{
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.i("ConnectThread", "Successfully created RfcommSocket");
            } 
            catch (IOException e)
            {
            	Log.i("ConnectThread", "create() failed");
            }
            mmSocket = tmp;
           // Log.i("ConnectThread", "Socket:" + mmSocket.toString());
        }
     
        
		public void run() 
		{
        	
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();
            Log.i("ConnectThread", "Entered ConnectThread run() - attempting connect");
            try 
			{
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect(); 
                Log.i("ConnectThread", "Successfully connected to " + mmDevice.getName() + " : " + mmDevice.getAddress());
            } 
            catch (IOException connectException) 
			{
                // Unable to connect; close the socket and get out
                try 
				{
                    mmSocket.close();
                } 
                catch (IOException closeException) 
				{
                	Log.i("ConnectThread", "unable to close socket");
                }
                return;
            }           
            
            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) 
			{
                mConnectThread = null;
            }
     
            // Do work to manage the connection (in a separate thread)
            connected(mmSocket, mmSocket.getRemoteDevice());
            final String connectedDevice = "Successfully connected to " + mmDevice.getName() + " : " + mmDevice.getAddress();
            
            act.runOnUiThread(new Runnable() 
            {
                public void run() 
                {
                    showToast(act, connectedDevice);
                }
            });
            
        }
     
        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() 
		{
            try 
			{
                mmSocket.close();
            } 
            catch (IOException e) 
			{
            	Log.i("ConnectThread", "unable to close socket ");
            }
        }
    }
    
    private class ConnectedThread extends Thread 
	{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        
        Activity act;
        @SuppressLint("NewApi")
		public ConnectedThread(BluetoothSocket socket) 
		{
        	act = BluetoothService.this;         	
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            
            // Get the input and output streams, using temp objects because
            // member streams are final
            try 
			{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } 
            catch (IOException e) 
			{
            	Log.i("ConnectedThread", "temp sockets not created");
            }
     
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            
            if( mmSocket.isConnected())
        		Log.i("ConnectedThread", "This socket is connected");
        }
     
        
		public void run() 
		{
        	//Log.i("ConnectedThread", "incoming socket " + mmSocket.toString());
        	Log.i("ConnectedThread", "BEGIN mConnectedThread");
        	if(mmSocket != null)
        		Log.i("ConnectedThread", "run() the socket is connected");
        	       		
        	
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
     
            
            // Keep listening to the InputStream until an exception occurs
            while (true) 
			{
                try 
				{
                	Log.i("ConnectedThread", "Entered listening loop - attempting to read from buffer");
                    // Read from the InputStream
                	bytes = mmInStream.read(buffer);
                    
                    Log.i("ConnectedThread", "after reading bytes");
         
                    final String readMessage = new String(buffer, 0, bytes);
                    Log.i("ConnectedThread run()", readMessage);
                    
                    act.runOnUiThread(new Runnable() 
                    {
                        public void run() 
                        {
                            showToast(act, readMessage);
                        }
                    });
                    
                    
                    //Toast.makeText(this, readMessage, Toast.LENGTH_LONG).show();
                    //displayMessage(readMessage);
                    Log.i("ConnectedThread", "Continue - attempting to continue read loop");
                    
                    // Send the obtained bytes to the UI activity
                    //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } 
                catch (IOException e) 
				{
                	Log.i("ConnectedThread", "Exception thrown - Disconnected");
                	Log.i("ConnectedThread", "Error: " + e);
                	BluetoothService.this.start();
                    break;
                }
            }
        }
     
        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) 
		{
            try 
			{
            	Log.i("ConnectedThread", "Entered write -  write to buffer");
                mmOutStream.write(bytes);
            } 
            catch (IOException e) 
			{
            	Log.e("ConnectedThread", "Exception during write", e);
            }
        }
     
        /* Call this from the main activity to shutdown the connection */
        public void cancel() 
		{
            try 
			{
                mmSocket.close();
            } 
            catch (IOException e) 
			{ 
            	Log.e("ConnectedThread", "close() of connect socket failed", e);
            }
        }
    } // end ConnectedThread
        
	// test method to display incoming message
    void displayMessage(String message)
    {
    	Log.i("displayMessage", "Entered displayMessage - attempting to display message");
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    
}//MainActivity
