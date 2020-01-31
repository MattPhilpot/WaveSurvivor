package wave.survivor.game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class GamePanel extends Activity implements OnTouchListener, SensorEventListener, OnClickListener
{
	public static final int WEAPONTYPE_MISSILE = 0;
	public static final int MISSILE_DEFAULT_SPEED = 15;
	public static final int MISSILE_DEFAULT_DAMAGE = 20;
	public static final int MISSILE_DEFAULT_COST = 75;
	
	public static final int WEAPONTYPE_BIG_MISSILE = 1;
	public static final int BIG_MISSILE_DEFAULT_SPEED = 15;
	public static final int BIG_MISSILE_DEFAULT_DAMAGE = 40;
	public static final int BIG_MISSILE_DEFAULT_COST = 175;
	
	public static final int WEAPONTYPE_LASER = 2;
	public static final int LASER_DEFAULT_SPEED = 75;
	public static final int LASER_DEFAULT_DAMAGE = 5;
	public static final int LASER_DEFAULT_COST = 30;
	
	public static final int WEAPONTYPE_CHARGED_LASER = 3;
	public static final int CHARGED_LASER_DEFAULT_SPEED = 75;
	public static final int CHARGED_LASER_DEFAULT_DAMAGE = 10;
	public static final int CHARGED_LASER_DEFAULT_COST = 65;
	
	public static final int WEAPONTYPE_BOMB = 4;
	public static final int BOMB_DEFAULT_SPEED = 10;
	public static final int BOMB_DEFAULT_DAMAGE = 40;
	public static final int BOMB_DEFAULT_COST = 150;
	
	public static final int WEAPONTYPE_DISRUPTION_BOMB = 5;
	public static final int DISRUPTION_BOMB_DEFAULT_SPEED = 15;
	public static final int DISRUPTION_BOMB_DEFAULT_DAMAGE = 10;
	public static final int DISRUPTION_BOMB_DEFAULT_COST = 350;
	
	public static final int SPEED = 0;
	public static final int DAMAGE = 1;
	public static final int COST = 2;
	public static final int GAME_WON = 21;
	public static final int GAME_LOST = 20;
	
	public static final int SHAKE_UP = 1;
	public static final int SHAKE_LEFT = 0;
	public static final int SHAKE_RIGHT = 2;
	public static final int SHAKE_DOWN = 3;
	public static final int SHAKE_DURATION = 2500;
	public static final int SHAKE_DISTANCE = 150;
	
	public static final int VIBRATION_LENGTH = 300;
	public static final int BOOST_TIME = 2000;
	public static final int BOOST_COST = 125;
	public static final int SHIELD_RECHARGE_DELAY = 6000;


	private static final int DEFAULT_HEIGHT = 1920;
	private static final int DEFAULT_WIDTH = 1080;

	private double scaleX = 1.0;
	private double scaleY = 1.0;
	
	GameSurfaceView v;
	Bitmap shipBitmap;
	Bitmap backgroundBitmap;
	Activity act;
	Dialog gameCreate;
	Dialog addOptionMenu;
	
	double centerLeeway;
	double sensitivity;
	
	SharedPreferences getSetting;
	SharedPreferences.Editor editor;
	
	private Vibrator vibe;
	int VibeSetting;
	/*BEGIN BLUETOOTH DECLARATIONS*/
	
	//Local Bluetooth Adapter
	BluetoothAdapter mBluetoothAdapter;
	ListView pairedListView;
	int selectedItem;
	int[][] weaponArray = new int[][] {	{MISSILE_DEFAULT_SPEED, 		MISSILE_DEFAULT_DAMAGE, 		MISSILE_DEFAULT_COST		},
										{BIG_MISSILE_DEFAULT_SPEED,		BIG_MISSILE_DEFAULT_DAMAGE,		BIG_MISSILE_DEFAULT_COST	},
										{LASER_DEFAULT_SPEED, 			LASER_DEFAULT_DAMAGE, 			LASER_DEFAULT_COST			},
										{CHARGED_LASER_DEFAULT_SPEED, 	CHARGED_LASER_DEFAULT_DAMAGE,	CHARGED_LASER_DEFAULT_COST	},
										{BOMB_DEFAULT_SPEED, 			BOMB_DEFAULT_DAMAGE, 			BOMB_DEFAULT_COST			},
										{DISRUPTION_BOMB_DEFAULT_SPEED,	DISRUPTION_BOMB_DEFAULT_DAMAGE,	DISRUPTION_BOMB_DEFAULT_COST}};
	//Constants
	//private static final int REQUEST_ENABLE_BT = 3; 
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
	private boolean startRendering;
	private boolean foundOpponent, playerReady;
	ProgressDialog prog;
	
	/*END BLUETOOTH DECLARATIONS*/
	
	PlayerObject mainPlayer, otherPlayer;
	int globalWidth, globalHeight;
	float selectedX;
	float selectedY;
	int globalTemp;
	/*SENSOR VARIABLE DECLARATIONS*/
	
	//Sounds Variables
	MediaPlayer mediaPlayer;
	SoundPool sounds;
    int sExplosion;
	int sLaser;
	int sRocket;
	int sBomb;

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private float[] orientVals;
	float orientOffsetX, orientOffsetY;
	boolean shaking;
	int shakeDirection;
	Date shakeTime;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.blank);
       sensitivity = 2;
       centerLeeway = 0.5;
       selectedX = selectedY = 0;
       getSetting = getSharedPreferences("game_settings", 0);
       editor = getSetting.edit();
	   preGameStartup();    
    }
    
    @TargetApi(13)
	public void preGameStartup()
    {
    	foundOpponent = false;
    	playerReady = false; 
    	shakeDirection = SHAKE_LEFT;
    	shakeTime = new Date();
    	shaking = false;
    	shakeTime.setTime((new Date().getTime()));
    	
        mediaPlayer = MediaPlayer.create(this,R.raw.bm1);
        mediaPlayer.setLooping(true);             
        sounds = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
 	   	sExplosion = sounds.load(getBaseContext(), R.raw.explosion, 1);
 	   	sLaser = sounds.load(getBaseContext(), R.raw.laser, 1);
 	   	sRocket = sounds.load(getBaseContext(), R.raw.rocket, 1);
 	   	sBomb = sounds.load(getBaseContext(), R.raw.bomb, 1);
    	 	
    	orientVals = new float[] {(float)0.0, (float)0.0, (float)0.0};
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);		
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		//enableSensor();
    	vibe = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
    	final Point point = new Point();
    	getWindowManager().getDefaultDisplay().getSize(point);
    	gameCreate = new Dialog(GamePanel.this);
    	gameCreate.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	gameCreate.setContentView(R.layout.bluetooth_menu);
    	gameCreate.setCancelable(false);
    	orientOffsetX = orientOffsetY = 0;
    	final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    	lp.copyFrom(gameCreate.getWindow().getAttributes());
    	lp.height = (int) (((double)point.y) * .9);
    	lp.width = (int) (((double)point.x) * .9);
    	
    	globalWidth = point.x;
    	globalHeight = point.y;
    	scaleX = (double)globalWidth / (double)DEFAULT_WIDTH;
    	scaleY = (double)globalHeight / (double)DEFAULT_HEIGHT;

    	
    	//initialize players, load and apply main player upgrades
    	mainPlayer = new PlayerObject(globalWidth/2, 7*(globalHeight/8), true, BitmapFactory.decodeResource(getResources(), R.drawable.shipwhite1));
        otherPlayer = new PlayerObject(globalWidth/2, globalHeight/8, false, BitmapFactory.decodeResource(getResources(), R.drawable.shipwhite2));
        mainPlayer.shipSpeed = getSetting.getInt("ship_speed", 0);
        mainPlayer.shipHealth = getSetting.getInt("ship_health", 0);
        mainPlayer.shipShield = getSetting.getInt("ship_shield", 0);
        mainPlayer.shieldRechargeRate = getSetting.getInt("ship_shield_recharge", 0);
        mainPlayer.weaponDamage = getSetting.getInt("ship_weap_damange", 0);
        mainPlayer.weaponTravel = getSetting.getInt("ship_weap_speed", 0);
        mainPlayer.applyUpgrades();
        
    	//Button findDev = (Button)gameCreate.findViewById(R.id.findDevices);
    	Button goBack = (Button)gameCreate.findViewById(R.id.Go_back_from_newGame);
    	Button startGame = (Button)gameCreate.findViewById(R.id.Start_Game);
    	Button addOptions = (Button)gameCreate.findViewById(R.id.Add_Options);
    	startGame.setEnabled(foundOpponent);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                
    	// array adapters will hold the discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.bluetooth_device);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.bluetooth_device);
        
        // Find and set up the ListView for paired devices
        pairedListView  = (ListView) gameCreate.findViewById(R.id.paired_devices);    
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);      
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        
        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) gameCreate.findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        
        pairedListView.setOnItemLongClickListener(new OnItemLongClickListener()
    	{
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
			{
				registerForContextMenu(pairedListView);
				selectedItem = arg2;

				//openContextMenu(arg0);
				return false;
			}		   		
    	});
             
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
	    
	    findDevices();
	    
	    addOptions.setOnClickListener(new View.OnClickListener() 
	    {	
			public void onClick(View v) 
			{
				
				addOptionMenu = new Dialog(GamePanel.this);
		    	addOptionMenu.requestWindowFeature(Window.FEATURE_NO_TITLE);
		    	addOptionMenu.setContentView(R.layout.additional_options);
		    	addOptionMenu.setCancelable(true);	 
		    	
		    	final CheckBox vibrateOption = (CheckBox)addOptionMenu.findViewById(R.id.vibrate_option);
		    	final TextView vibrateText = (TextView)addOptionMenu.findViewById(R.id.vibrate_text);
		    	final TextView sense = (TextView)addOptionMenu.findViewById(R.id.sensitivity);
		    	final TextView leeway = (TextView)addOptionMenu.findViewById(R.id.center_leeway);
		    	final TextView xSet = (TextView)addOptionMenu.findViewById(R.id.Xset);
		    	final TextView ySet = (TextView)addOptionMenu.findViewById(R.id.Yset);
		    	final Button calibrateButton = (Button)addOptionMenu.findViewById(R.id.calibration);
		    	SeekBar senseBar = (SeekBar)addOptionMenu.findViewById(R.id.sense_slider);
		    	SeekBar leewayBar = (SeekBar)addOptionMenu.findViewById(R.id.leeway_slider);
		    	
		    	SharedPreferences getSetting = getSharedPreferences("game_settings", 0);
				final SharedPreferences.Editor editor = getSetting.edit();
			
				VibeSetting = getSetting.getInt("Vibration_Setting", 0);
				
				//loading saved settings from last time
				if(VibeSetting == 1)
				{
					vibrateText.setText("Device will vibrate");
					vibrateOption.setChecked(true);
				}
				else if(VibeSetting == 0)
				{
					vibrateText.setText("Device won't vibrate");
					vibrateOption.setChecked(false);
				}		    	
		    	
		    	vibrateOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				    {
						if (isChecked)
				        {
							vibrateText.setText("Device will vibrate");
							VibeSetting = 1;
							editor.putInt("Vibration_Setting", 1);
							editor.commit();
				        }
						else
						{
							vibrateText.setText("Device won't vibrate");
							VibeSetting = 0;
							editor.putInt("Vibration_Setting", 0);
							editor.commit();
						}
				    }
				});
		    	
		    	calibrateButton.setOnClickListener(new View.OnClickListener()
		    	{

					public void onClick(View v) 
					{				
						enableSensor();
						orientOffsetX = orientOffsetY = 0;
				    	for(int i = 0; i < 50; i++)
				    	{
				    		orientOffsetX += orientVals[0];
				    		orientOffsetY += orientVals[1];
				    	}
				    	orientOffsetX /= (float)50.0;
				    	orientOffsetY /= (float)50.0;
								
	      				xSet.setText(String.valueOf(orientOffsetX));
	      				ySet.setText(String.valueOf(orientOffsetY));
					}
		    		
		    	});
		    	
		    	senseBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
				{
					
					public void onStopTrackingTouch(SeekBar Difficulty) 
					{
						// TODO Auto-generated method stub
					}
					
					public void onStartTrackingTouch(SeekBar Difficulty) 
					{
						// TODO Auto-generated method stub	
					}
					
					public void onProgressChanged(SeekBar Difficulty, int progress, boolean arg2) 
					{
						sense.setText( ( Double.toString(((double)progress)/10.0)) );
						sensitivity = ((double)progress)/10.0;
					}
				});
		    	
		    	leewayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
				{
					
					public void onStopTrackingTouch(SeekBar Difficulty) 
					{
						// TODO Auto-generated method stub
					}
					
					public void onStartTrackingTouch(SeekBar Difficulty) 
					{
						// TODO Auto-generated method stub	
					}
					
					public void onProgressChanged(SeekBar Difficulty, int progress, boolean arg2) 
					{
						leeway.setText( ( Double.toString(((double)progress)/10.0)) );
						centerLeeway = ((double)progress)/10.0;
					}
				});	    	
		    	lp.width *= .8;
		    	lp.height *= .8;
				addOptionMenu.show();
				addOptionMenu.getWindow().setAttributes(lp);
			}
		});
	    
	    goBack.setOnClickListener(new View.OnClickListener() 
	    {
			public void onClick(View v) 
			{
				gameCreate.dismiss();
				startActivity(new Intent("wave.survivor.game.MM"));
			}
		});

	    startGame.setOnClickListener(new View.OnClickListener() 
	    {
			public void onClick(View view) 
			{
				sendPlayerReady(mainPlayer.shipSpeed, mainPlayer.shipHealth, 
								mainPlayer.shipShield, mainPlayer.shieldRechargeRate, 
								mainPlayer.weaponDamage, mainPlayer.weaponTravel);
				if(!playerReady)
				{
					prog = new ProgressDialog(GamePanel.this);
					prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					prog.setMessage("Waiting for other Player");
					prog.setIndeterminate(true);
					prog.setCancelable(false);	
					prog.show();
				}	
				else
				{
					gameCreate.dismiss();	
    				startRendering = true;
    				enableSensor();
    				startGame();
				}
			}			
		});
	    
	    gameCreate.show();
	    gameCreate.getWindow().setAttributes(lp);
	    start();
    }
        
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.int_context_menu, menu);	
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case R.id.rename: //TODO: Finish
    		break;
    	case R.id.remove: //TODO: yep also this
    		break;
    	}
		return true;  	
    }
    
    
    public void startGame()
    {
    	FrameLayout Game = new FrameLayout(this);
	   	v = new GameSurfaceView(this); //extends surfaceview
	   	RelativeLayout gameWidgets = new RelativeLayout(this);
	   	int division = globalWidth/7;
	   	ImageButton missileButton = new ImageButton(this);
	   	missileButton.setBackgroundResource(R.drawable.select_missile_button);
	   	missileButton.setId(111111);
	   	ImageButton bigMissileButton = new ImageButton(this);
	   	bigMissileButton.setBackgroundResource(R.drawable.select_big_missile_button);
	   	bigMissileButton.setId(222222);
	   	ImageButton laserButton = new ImageButton(this);
	   	laserButton.setBackgroundResource(R.drawable.select_laser_button);
	   	laserButton.setId(333333);
	   	ImageButton chargedLaserButton = new ImageButton(this);
	   	chargedLaserButton.setBackgroundResource(R.drawable.select_charged_laser_button);
	   	chargedLaserButton.setId(444444);
	   	ImageButton bombButton = new ImageButton(this);
	   	bombButton.setBackgroundResource(R.drawable.select_bomb_button);
	   	bombButton.setId(555555);
	   	ImageButton distortionBombButton = new ImageButton(this);
	   	distortionBombButton.setBackgroundResource(R.drawable.select_disruption_bomb_button);
	   	distortionBombButton.setId(666666);
	   	ImageButton calibrateButton = new ImageButton(this);
	   	calibrateButton.setBackgroundResource(R.drawable.select_calibrate_button);
	   	calibrateButton.setId(777777);
	   	ImageButton boostButton = new ImageButton(this);
	   	boostButton.setBackgroundResource(R.drawable.select_boost_button);
	   	boostButton.setId(888888);
	   		   	
	   	gameWidgets.setLayoutParams(new LayoutParams
	   			(
	   				RelativeLayout.LayoutParams.MATCH_PARENT,  
	   				RelativeLayout.LayoutParams.MATCH_PARENT
	   			));
	   	
	   	missileButton.setOnClickListener(this);
	   	bigMissileButton.setOnClickListener(this);
	   	laserButton.setOnClickListener(this);
	   	chargedLaserButton.setOnClickListener(this);
	   	bombButton.setOnClickListener(this);
	   	distortionBombButton.setOnClickListener(this);
	   	calibrateButton.setOnClickListener(this);
	   	boostButton.setOnClickListener(this);
	   	
	   	missileButton.setScaleX((float) .5);
	   	missileButton.setScaleY((float) .5);
	   	missileButton.setX(0);
	   	missileButton.setY((float) (globalHeight - 175));
	   	gameWidgets.addView(missileButton); 
	   	
	   	selectedX = missileButton.getX();
	   	selectedY = missileButton.getY();
	   	mainPlayer.selectedWeapon = WEAPONTYPE_MISSILE;
	   	
	   	bigMissileButton.setScaleX((float) .5);
	   	bigMissileButton.setScaleY((float) .5);
	   	bigMissileButton.setX(division);
	   	bigMissileButton.setY((float) (globalHeight - 175));
	   	gameWidgets.addView(bigMissileButton); 
	   	
	   	laserButton.setScaleX((float) .5);
	   	laserButton.setScaleY((float) .5);
	   	laserButton.setX((float) (2*division));
	   	laserButton.setY((float) (globalHeight - 175));
	   	gameWidgets.addView(laserButton); 
	   	
	   	chargedLaserButton.setScaleX((float) .5);
	   	chargedLaserButton.setScaleY((float) .5);
	   	chargedLaserButton.setX((float) (3*division));
	   	chargedLaserButton.setY((float) (globalHeight - 175));
	   	gameWidgets.addView(chargedLaserButton); 
	   	
	   	bombButton.setScaleX((float) .5);
	   	bombButton.setScaleY((float) .5);
	   	bombButton.setX((float) (4*division));
	   	bombButton.setY((float) (globalHeight - 175));
	   	gameWidgets.addView(bombButton); 
	   	
	   	distortionBombButton.setScaleX((float) .5);
	   	distortionBombButton.setScaleY((float) .5);
	   	distortionBombButton.setX((float) (5*division));
	   	distortionBombButton.setY((float) (globalHeight - 175));
	   	gameWidgets.addView(distortionBombButton); 
	   	
	   	calibrateButton.setScaleX((float) .5);
	   	calibrateButton.setScaleY((float) .5);
	   	calibrateButton.setX((float) (6.25*division));
	   	calibrateButton.setY((float) (globalHeight - 175));
	   	gameWidgets.addView(calibrateButton); 
	   	
	   	boostButton.setScaleX((float) .5);
	   	boostButton.setScaleY((float) .5);
	   	boostButton.setX((float) (-8));
	   	boostButton.setY((float) (globalHeight - 175));
	   	gameWidgets.addView(boostButton); 
	   	
	    v.setOnTouchListener(this);
	    Game.addView(v);
	    Game.addView(gameWidgets);
	    shipBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.shipwhite1); 
	    backgroundBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.spaceback);
	    setContentView(Game);
	    //addContentView(missileButton, missileParams);
	    if(mediaPlayer != null)
	    	mediaPlayer.start();

	    v.resume();
    }
    
    
    
    @Override
    protected void onPause()
    {
    	super.onPause();
    	//sounds.play(sFart, 1.0f, 1.0f, 0, 0, 1.5f);
    	if(mediaPlayer != null)
    		mediaPlayer.pause();
    	if(sensorManager != null)
    		sensorManager.unregisterListener(this, accelerometer);
    	//if(mConnectedThread != null)
    	//	mConnectedThread.closeConnection();
    	if(v != null)
    		v.pause();
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	
    	//mp.start();
    	
    	if(sensorManager != null)
    	{
    		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    	} 
    	
    	if(v != null)
    		v.resume();
    }
    
	public void onClick(View v) 
	{
		switch(v.getId())
		{
		case 111111:
			mainPlayer.selectedWeapon = WEAPONTYPE_MISSILE;
			selectedX = v.getX();
			selectedY = v.getY();
			break;
		case 222222:
			mainPlayer.selectedWeapon = WEAPONTYPE_BIG_MISSILE;
			selectedX = v.getX();
			selectedY = v.getY();
			break;
		case 333333:
			mainPlayer.selectedWeapon = WEAPONTYPE_LASER;
			selectedX = v.getX();
			selectedY = v.getY();
			break;
		case 444444:
			mainPlayer.selectedWeapon = WEAPONTYPE_CHARGED_LASER;
			selectedX = v.getX();
			selectedY = v.getY();
			break;
		case 555555:
			mainPlayer.selectedWeapon = WEAPONTYPE_BOMB;
			selectedX = v.getX();
			selectedY = v.getY();
			break;
		case 666666:
			mainPlayer.selectedWeapon = WEAPONTYPE_DISRUPTION_BOMB;
			selectedX = v.getX();
			selectedY = v.getY();
			break;
		case 777777:
			orientOffsetX = orientOffsetY = 0;
	    	for(int i = 0; i < 50; i++)
	    	{
	    		orientOffsetX += orientVals[0];
	    		orientOffsetY += orientVals[1];
	    	}
	    	orientOffsetX /= (float)50.0;
	    	orientOffsetY /= (float)50.0;
			break;
		case 888888:
			if(!mainPlayer.boostEnabled && mainPlayer.boost > BOOST_COST)
			{
				mainPlayer.boostEnabled = true;
				mainPlayer.boostTime.setTime((new Date().getTime()));
				mainPlayer.boost -= BOOST_COST;
			}
		}	
	}
    
	public void gameOver(int victoryCondition)
	{
		final Dialog dialog = new Dialog(GamePanel.this);
    	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.exit_screen);
    	dialog.setCancelable(false);
    	
    	/* 	// >.< this is so bad
    	if(mediaPlayer != null)
    	{
    		mediaPlayer.stop();
    		mediaPlayer.release();
        	sounds.release();
    	}*/
    		
    	TextView text = (TextView) dialog.findViewById(R.id.finish_game_text);
    	Button option1 = (Button) dialog.findViewById(R.id.exit);
    	Button option2 = (Button) dialog.findViewById(R.id.exitandsave);
    	Button option3 = (Button) dialog.findViewById(R.id.cancel);
    	option3.setVisibility(Button.GONE);
    	switch(victoryCondition)
    	{
    	case GAME_WON:
    		text.setText("You Won! :)");
    		break;
    	case GAME_LOST:
    		text.setText("You Lost :(");
    		break;
    	}
    	
    	option1.setOnClickListener(new View.OnClickListener() 
		{
  			public void onClick(View opt) 
			{
  				dialog.dismiss();
  				startActivity(new Intent("wave.survivor.game.MM"));
			}
		});
    	
    	option2.setOnClickListener(new View.OnClickListener() 
		{
  			public void onClick(View opt) 
			{
  				dialog.dismiss();
  				startActivity(new Intent("wave.survivor.game.MM"));
			}
		});
    	    	        	
    	stop();
    	if(sensorManager != null)
    		sensorManager.unregisterListener(this, accelerometer);
    	dialog.show();
	}
	
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {	
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) 
        {
        	final Dialog dialog = new Dialog(GamePanel.this);
        	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        	dialog.setContentView(R.layout.exit_screen);
        	dialog.setCancelable(true);
        	
        	Button option1 = (Button) dialog.findViewById(R.id.exit);
        	Button option2 = (Button) dialog.findViewById(R.id.exitandsave);
        	Button option3 = (Button) dialog.findViewById(R.id.cancel);
        	
        	option1.setOnClickListener(new View.OnClickListener() 
    		{
      			public void onClick(View opt) 
    			{
      				dialog.dismiss();
      				stop();
      				startActivity(new Intent("wave.survivor.game.MM"));
    			}
    		});
        	
        	option2.setOnClickListener(new View.OnClickListener() 
    		{
      			public void onClick(View opt) 
    			{
      				dialog.dismiss();
      				stop();
      				startActivity(new Intent("wave.survivor.game.MM"));
    			}
    		});
        	
        	option3.setOnClickListener(new View.OnClickListener() 
    		{
      			public void onClick(View opt) 
    			{
      				if(!mediaPlayer.isPlaying())
      					mediaPlayer.start();
      				dialog.dismiss();
      				if(sensorManager != null)
        	    		sensorManager.registerListener(GamePanel.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
      				startRendering = false;
    			}
    		});
        	        	
        	dialog.setOnDismissListener(new OnDismissListener() 
        	{ 
        	    public void onDismiss(DialogInterface dialog) 
        	    { 
        	    	if(!mediaPlayer.isPlaying())
        	    		mediaPlayer.start();
        	    	if(sensorManager != null)
        	    		sensorManager.registerListener(GamePanel.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        	    	startRendering = false;
        	    } 
        	}); 
        	
        	if(sensorManager != null)
        		sensorManager.unregisterListener(this, accelerometer);
        	if(mediaPlayer.isPlaying())
        		mediaPlayer.pause();
        	dialog.show();
        	startRendering = true;
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    
    
    public void isShaking(Canvas canvas)
    {
    	if(shaking)
    	{
    		switch(shakeDirection)
    		{
    		case SHAKE_LEFT:
    			canvas.translate((float) (-SHAKE_DISTANCE*Math.random()), (float) (-SHAKE_DISTANCE*Math.random()));
    			shakeDirection = SHAKE_RIGHT;
    			break;
    		case SHAKE_RIGHT:
    			canvas.translate((float) (SHAKE_DISTANCE*Math.random()), (float) (SHAKE_DISTANCE*Math.random()));
    			shakeDirection = SHAKE_UP;
    			break;
    		case SHAKE_UP:
    			canvas.translate((float) (-SHAKE_DISTANCE*Math.random()), (float) (SHAKE_DISTANCE*Math.random()));
    			shakeDirection = SHAKE_DOWN;
    			break;
    		case SHAKE_DOWN:
    			canvas.translate((float) (SHAKE_DISTANCE*Math.random()), (float) (-SHAKE_DISTANCE*Math.random()));
    			shakeDirection = SHAKE_LEFT;
    			break;
    		}
    		if(VibeSetting == 1)
				vibe.vibrate(50);
    	}
    	if((((new Date()).getTime() - shakeTime.getTime()) > SHAKE_DURATION))
    		shaking = false;
    }
    
    
    public class GameSurfaceView extends SurfaceView implements Runnable 
    {
	    Thread t = null;	
	    SurfaceHolder holder;
	    boolean tRunning = false;
	    boolean done;
	    int beforeTime;
	    int afterTime;
	    int diffTime;
	    
	    public GameSurfaceView(Context context) 
	    {
	    	super(context);
	    	holder = getHolder();
	    }
	
	    public void run() 
	    {
		    //Log.i("GameSurfaceView","inside run()");
		    diffTime = 0;
		    //float fps = 0;
		    Paint paint = new Paint();
		    paint.setColor(Color.WHITE);
		    paint.setTextSize(30);
		      
		    if(!startRendering)
		    	pause();
		    //repeat the drawing loop until the thread is stopped.
		    while (tRunning && playerReady) 
		    {
				computeOrientation();
				mainPlayer.regenerateAmmo();
		    	if(!holder.getSurface().isValid())
		    	{
		    		  continue;
		    	}
		      	beforeTime = (int)System.currentTimeMillis();
		  	    // Lock the surface and return the canvas to draw onto.
		  	    Canvas canvas = holder.lockCanvas();
		  	    // TODO: Draw on the canvas!
		  	    // Log.i("GameSurfaceView","just drew new canvas!" );
		  	    // canvas.drawText(String.valueOf(fps), 30, 30, paint);  	    
		  	    canvas.drawBitmap(backgroundBitmap, 0,0,null);
		  	    paint.setColor(Color.YELLOW);
		  	    paint.setStrokeWidth(0);
		  	    canvas.drawRect(selectedX+54, selectedY+54, selectedX+159, selectedY+159, paint);
		  	    isShaking(canvas);
		  	    //canvas.drawBitmap(mainPlayer.aircraft, mainPlayer.x - (shipBitmap.getWidth()/2), mainPlayer.y - (shipBitmap.getHeight()/2), null);
		  	    otherPlayer.drawProjectiles(canvas);
		  	    mainPlayer.drawProjectiles(canvas);
		  	    otherPlayer.drawShip(canvas);
		  	    mainPlayer.drawShip(canvas);
		  	    
		  	    // drawOrientations(canvas, paint);  
		  	    // Unlock the canvas and render the current image.
		  	    	  	    
		  	    holder.unlockCanvasAndPost(canvas);
		  	    afterTime = (int)System.currentTimeMillis();
		  	    diffTime = afterTime - beforeTime;
		  	    //fps = 1/((float)diffTime/1000);
		  	    writeXandY((int)mainPlayer.x, (int)mainPlayer.y);
		  	    mainPlayer.computeCollision();
		    }
	    }
	
	    public void pause() 
	    {
	    	tRunning = false;
	    	while(true)
	    	{
	    		try
	    		{
	    			t.join();
	    		}
	    		catch ( InterruptedException e)
	    		{
	    			e.printStackTrace();
	    		}
	    		break;
	    	}
	    	t = null;
	    }
	
	    public void resume() 
	    {
	    	tRunning = true;
	    	t = new Thread(this);
	    	t.start();
	    }
    }
    
    public boolean onTouch(View v, MotionEvent me)
    {
    	switch(me.getAction())
    	{
    	case MotionEvent.ACTION_DOWN:
    		mainPlayer.fireProjectile((int)me.getX(), (int)me.getY());
    		switch(mainPlayer.selectedWeapon)
    		{
    		case WEAPONTYPE_MISSILE:
    			sounds.play(sRocket, 1.0f, 1.0f, 0, 0, 1.5f);
    			break;
    		case WEAPONTYPE_BIG_MISSILE:
    			sounds.play(sRocket, 1.0f, 1.0f, 0, 0, 1.5f);
    			break;
    		case WEAPONTYPE_LASER:
    			sounds.play(sLaser, 1.0f, 1.0f, 0, 0, 1.5f);
    			break;
    		case WEAPONTYPE_CHARGED_LASER:
    			sounds.play(sLaser, 1.0f, 1.0f, 0, 0, 1.5f);
    			break;
    		case WEAPONTYPE_BOMB:
    			sounds.play(sBomb, 1.0f, 1.0f, 0, 0, 1.5f);
    			break;
    		case WEAPONTYPE_DISRUPTION_BOMB:
    			sounds.play(sBomb, 1.0f, 1.0f, 0, 0, 1.5f);
    			break;
    		default:
    			break;
    		}
    		break;
    	case MotionEvent.ACTION_UP:
    		
    		break;
    	case MotionEvent.ACTION_MOVE:
    		break;
    	}
    	return true;
    }
     
    
    /*************************************************************************************************************/
    /********BLUETOOTH CODE********************/
    /*************************************************************************************************************/
    
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

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) 
        {
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

        /* original code
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) 
        {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }
		   	
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        */
        // below is test
        if(mConnectedThread == null)
        {
        	mConnectThread = new ConnectThread(device);
        	mConnectThread.start();
        }     
    }
    
    
    @SuppressLint({ "NewApi", "NewApi" })
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) 
    {    
    	if( socket.isConnected()) // test 
    		Log.i("connected()", "This socket is connected");
    	
        // Cancel the thread that completed the connection
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
        
        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) 
        {
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
    
    
    public void findDevices()
    {
	//	Toast.makeText(this, "its in the find devices method", Toast.LENGTH_LONG).show();	// test not needed	
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) 
		{
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
	        startActivity(discoverableIntent);
	    }	
		doDiscovery();	//finds paired and new devices
	//	v.setVisibility(View.GONE);		// makes view invisible, wont take up space	
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
          gameCreate.setTitle(R.string.scanning);
          
          // Turn on sub-title for new devices
          gameCreate.findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
  		
          // If we're already discovering, stop it
          if (mBluetoothAdapter.isDiscovering()) {
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
            //Log.d(CONNECTIVITY_SERVICE, info);
            String address = info.substring(info.length() - 17);
            //Log.d(CONNECTIVITY_SERVICE, address);
           
            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            connectDevice(intent);
            //Log.i("Item Clicked", info);
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

            if (BluetoothDevice.ACTION_FOUND.equals(action)) // When discovery finds a device
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) 
                {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } 
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) // When discovery is finished, change the Activity title
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
        	act = GamePanel.this;
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
        	//Log.i("AcceptThread", "Entered AcceptThread Constructor");
            BluetoothServerSocket tmp = null;
            try 
            {
            	//Log.i("AcceptThread", "Entered AcceptThread tryblock");
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
					synchronized (GamePanel.this) 
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
        	act = GamePanel.this;
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
            catch (IOException e) {
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
            synchronized (GamePanel.this) 
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
        
        
        @SuppressLint("NewApi")
		public ConnectedThread(BluetoothSocket socket) 
        {
        	act = GamePanel.this;         	
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
            
            act.runOnUiThread(new Runnable() 
            {
                public void run() 
                {              
                    foundOpponent = true;
                	Button startGame = (Button)gameCreate.findViewById(R.id.Start_Game);
                	startGame.setEnabled(foundOpponent);
                }
            });       
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
                	//Log.i("ConnectedThread", "Entered listening loop - attempting to read from buffer");
                    // Read from the InputStream
                	bytes = mmInStream.read(buffer);
                    
                    //Log.i("ConnectedThread", "after reading bytes");
         
                    final String readMessage = new String(buffer, 0, bytes);
                    //Log.i("ConnectedThread run()", readMessage);
                    
                    switch(buffer[0])
                    {
                    case (byte)0x76:
                    	getXandY(buffer);
                    	break;
                    	
                    case (byte)0x78:
                    	readNewProjectile(buffer);
                    	break;
                    	
                    case (byte)0x82:
                    	playerReady = true;
                    	readNewPlayer(buffer);
	                	if(prog != null)
	                	{
	                		act.runOnUiThread(new Runnable() 
	                        {
	                            public void run() 
	                            {
	                            	prog.dismiss();
	                            	gameCreate.dismiss();	
	                				startRendering = true;
	                				enableSensor();
	                				startGame();
	                            }
	                        });
	                	} 	
                    	break;
                    case (byte)0x48:
                    	readProjectileHit(buffer);
                    	break;
                    case (byte)0x4F:
                    	act.runOnUiThread(new Runnable() 
                        {
                            public void run() 
                            {
                            	playerReady = false;
                            	gameOver(GAME_WON);
                            	v.t.interrupt();
                            }
                        });
                    	break;
                    case (byte)0x53:
                    	readShield(buffer);
                    	break;
                    }
                } 
                catch (IOException e) 
                {
                	Log.i("ConnectedThread", "Exception thrown - Disconnected");
                	Log.i("ConnectedThread", "Error: " + e);
                	GamePanel.this.start();
                    break;
                }
            }
        }
     
		public void closeConnection()
		{
			if(mmInStream != null)
			{
				try 
	    		{
	    			mmInStream.close();
				} 
	    		catch (IOException e) 
	    		{
					e.printStackTrace();
				}
			}
			
			if(mmOutStream != null)
			{
				try 
	    		{
	    			mmOutStream.close();
				} 
	    		catch (IOException e) 
	    		{
					e.printStackTrace();
				}
			}
			
			if(mmSocket != null)
			{
				try 
	    		{
					mmSocket.close();
				} 
	    		catch (IOException e) 
	    		{
					e.printStackTrace();
				}
			}		
		}
		
		
        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) 
        {
            try 
            {
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
        
    public void sendPlayerReady(int a, int b, int c, int d, int e, int f)
    {
    	if(mConnectedThread != null)
	    	write (new byte[]
	    			{ 
	    				(byte)0x82, //ascii code for 'R' - I'm ready to rolllllllllll
	    				(byte) ((a >> 24) & 0xFF),
	    		        (byte) ((a >> 16) & 0xFF),   
	    		        (byte) ((a >> 8) & 0xFF),   
	    		        (byte) (a & 0xFF),
	    		        (byte) ((b >> 24) & 0xFF),
	    		        (byte) ((b >> 16) & 0xFF),   
	    		        (byte) ((b >> 8) & 0xFF),   
	    		        (byte) (b & 0xFF),
	    		        (byte) ((c >> 24) & 0xFF),
	    		        (byte) ((c >> 16) & 0xFF),   
	    		        (byte) ((c >> 8) & 0xFF),   
	    		        (byte) (c & 0xFF),
	    		        (byte) ((d >> 24) & 0xFF),
	    		        (byte) ((d >> 16) & 0xFF),   
	    		        (byte) ((d >> 8) & 0xFF),   
	    		        (byte) (d & 0xFF),
	    		        (byte) ((e >> 24) & 0xFF),
	    		        (byte) ((e >> 16) & 0xFF),   
	    		        (byte) ((e >> 8) & 0xFF),   
	    		        (byte) (e & 0xFF),
	    		        (byte) ((f >> 24) & 0xFF),
	    		        (byte) ((f >> 16) & 0xFF),   
	    		        (byte) ((f >> 8) & 0xFF),   
	    		        (byte) (f & 0xFF)
	    		        
	    			}); 
    }
    
    public void readNewPlayer(byte[] b)
    {
    	otherPlayer.shipSpeed = (b[4] & 0xFF | (b[3] & 0xFF) << 8 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 24);
    	otherPlayer.shipHealth = (b[8] & 0xFF | (b[7] & 0xFF) << 8 | (b[6] & 0xFF) << 16 | (b[5] & 0xFF) << 24);
    	otherPlayer.shipShield = (b[12] & 0xFF | (b[11] & 0xFF) << 8 | (b[10] & 0xFF) << 16 | (b[9] & 0xFF) << 24);
    	otherPlayer.shieldRechargeRate = (b[16] & 0xFF | (b[15] & 0xFF) << 8 | (b[14] & 0xFF) << 16 | (b[13] & 0xFF) << 24);
    	otherPlayer.weaponDamage = (b[20] & 0xFF | (b[19] & 0xFF) << 8 | (b[18] & 0xFF) << 16 | (b[17] & 0xFF) << 24);
    	otherPlayer.weaponTravel = (b[24] & 0xFF | (b[23] & 0xFF) << 8 | (b[22] & 0xFF) << 16 | (b[21] & 0xFF) << 24);
    	otherPlayer.applyUpgrades();
    }
    
    public void getXandY(byte[] b)
    {
    	otherPlayer.x = (int)((double)(b[4] & 0xFF | (b[3] & 0xFF) << 8 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 24) / scaleX);
    	otherPlayer.y = (int)((double)(b[8] & 0xFF | (b[7] & 0xFF) << 8 | (b[6] & 0xFF) << 16 | (b[5] & 0xFF) << 24) / scaleY);
    	otherPlayer.x = globalWidth - otherPlayer.x;
    	otherPlayer.y = globalHeight - otherPlayer.y;
    }
   
    public void writeShield(int shield)
    {
    	if(mConnectedThread != null)
    		write (new byte[]
    				{
    					(byte)0x53,
    					(byte) ((shield >> 24) & 0xFF),
	    		        (byte) ((shield >> 16) & 0xFF),   
	    		        (byte) ((shield >> 8) & 0xFF),   
	    		        (byte) (shield & 0xFF)
    				});
    }
    
    public void readShield(byte[] b)
    {
    	otherPlayer.shield = (int)(b[4] & 0xFF | (b[3] & 0xFF) << 8 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 24);
    }
    
    public void writeXandY(int x, int y)
    {
    	x = (int)((double)x * scaleX);
		y = (int)((double)x * scaleY);
    	if(mConnectedThread != null)
	    	write (new byte[]
	    			{
	    				(byte)0x76, //ascii code for 'L'
	    				(byte) ((x >> 24) & 0xFF),
	    		        (byte) ((x >> 16) & 0xFF),   
	    		        (byte) ((x >> 8) & 0xFF),   
	    		        (byte) (x & 0xFF),
	    		        (byte) ((y >> 24) & 0xFF),
	    		        (byte) ((y >> 16) & 0xFF),   
	    		        (byte) ((y >> 8) & 0xFF),   
	    		        (byte) (y & 0xFF)
	    			});
    }
    
    public void readNewProjectile(byte[] b)
    {
    	otherPlayer.fireProjectile
    	(
    			globalWidth - (b[4] & 0xFF | (b[3] & 0xFF) << 8 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 24),
    			globalHeight - (b[8] & 0xFF | (b[7] & 0xFF) << 8 | (b[6] & 0xFF) << 16 | (b[5] & 0xFF) << 24),
    			globalWidth - (b[12] & 0xFF | (b[11] & 0xFF) << 8 | (b[10] & 0xFF) << 16 | (b[9] & 0xFF) << 24),
    			globalHeight - (b[16] & 0xFF | (b[15] & 0xFF) << 8 | (b[14] & 0xFF) << 16 | (b[13] & 0xFF) << 24),
    							(b[20] & 0xFF | (b[19] & 0xFF) << 8 | (b[18] & 0xFF) << 16 | (b[17] & 0xFF) << 24)
    	);
    }
    
    
    public void writeNewProjectile(int x, int y, int xStart, int yStart, int type)
    {
    	if(mConnectedThread != null)
	    	write (new byte[]
	    			{
	    				(byte)0x78, //ascii code for 'N' - new projectile
	    				(byte) ((x >> 24) & 0xFF),
	    		        (byte) ((x >> 16) & 0xFF),   
	    		        (byte) ((x >> 8) & 0xFF),   
	    		        (byte) (x & 0xFF),
	    		        (byte) ((y >> 24) & 0xFF),
	    		        (byte) ((y >> 16) & 0xFF),   
	    		        (byte) ((y >> 8) & 0xFF),   
	    		        (byte) (y & 0xFF),
	    		        (byte) ((xStart >> 24) & 0xFF),
	    		        (byte) ((xStart >> 16) & 0xFF),   
	    		        (byte) ((xStart >> 8) & 0xFF),   
	    		        (byte) (xStart & 0xFF),
	    		        (byte) ((yStart >> 24) & 0xFF),
	    		        (byte) ((yStart >> 16) & 0xFF),   
	    		        (byte) ((yStart >> 8) & 0xFF),   
	    		        (byte) (yStart & 0xFF),
	    		        (byte) ((type >> 24) & 0xFF),
	    		        (byte) ((type >> 16) & 0xFF),   
	    		        (byte) ((type >> 8) & 0xFF),   
	    		        (byte) (type & 0xFF)
	    			});
    }
    
    public void sendProjectileHit(int x, int health, int shield)
    {
    	if(mConnectedThread != null){
	    	write (new byte[]
	    			{
	    				(byte)0x48, //ascii code for 'H' - Projectile Hit
	    				(byte) ((x >> 24) & 0xFF),
	    		        (byte) ((x >> 16) & 0xFF),   
	    		        (byte) ((x >> 8) & 0xFF),   
	    		        (byte) (x & 0xFF),
	    		        (byte) ((health >> 24) & 0xFF),
	    		        (byte) ((health >> 16) & 0xFF),   
	    		        (byte) ((health >> 8) & 0xFF),   
	    		        (byte) (health & 0xFF),
	    		        (byte) ((shield >> 24) & 0xFF),
	    		        (byte) ((shield >> 16) & 0xFF),   
	    		        (byte) ((shield >> 8) & 0xFF),   
	    		        (byte) (shield & 0xFF)
	    			});
	    sounds.play(sExplosion, 1.0f, 1.0f, 0, 0, 1.5f);
    	}
    }
    
    public void readProjectileHit(byte[] b)
    {
    	mainPlayer.hitRegistered((int)(b[4] & 0xFF | (b[3] & 0xFF) << 8 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 24)); 
    	otherPlayer.health = (b[8] & 0xFF | (b[7] & 0xFF) << 8 | (b[6] & 0xFF) << 16 | (b[5] & 0xFF) << 24);
    	otherPlayer.shield = (b[12] & 0xFF | (b[11] & 0xFF) << 8 | (b[10] & 0xFF) << 16 | (b[9] & 0xFF) << 24);
    }
    
    public void sendGameOver()
    {
    	if(mConnectedThread != null)
	    	write (new byte[]
	    			{
	    				(byte)0x4F //ascii for game over fool - I just lost
	    			});
    }
    
	// test method to display incoming message
    /*
    void displayMessage(String message)
    {
    	Log.i("displayMessage", "Entered displayMessage - attempting to display message");
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    */
    
    /******************************************/
    /********SENSOR CODE***********************/
    /******************************************/
    
	public void computeOrientation()
	{	 
		if(mainPlayer.boostEnabled)
		{
			if((orientVals[0]+orientOffsetX) > centerLeeway || (orientVals[0]+orientOffsetX) < -centerLeeway)
				mainPlayer.x -= (5+(.25*mainPlayer.shipSpeed))*(orientVals[0]-orientOffsetX);
			if((orientVals[1]+orientOffsetY) > centerLeeway || (orientVals[1]+orientOffsetY) < -centerLeeway)
				mainPlayer.y += (5+(.25*mainPlayer.shipSpeed))*(orientVals[1]-orientOffsetY);
		}
		else
		{
			if((orientVals[0]+orientOffsetX) > centerLeeway || (orientVals[0]+orientOffsetX) < -centerLeeway)
				mainPlayer.x -= (2+(.25*mainPlayer.shipSpeed))*(orientVals[0]-orientOffsetX);
			if((orientVals[1]+orientOffsetY) > centerLeeway || (orientVals[1]+orientOffsetY) < -centerLeeway)
				mainPlayer.y += (2+(.25*mainPlayer.shipSpeed))*(orientVals[1]-orientOffsetY);
		}	
		if(mainPlayer.x > globalWidth)
			mainPlayer.x = globalWidth;
		if(mainPlayer.x < 0)
			mainPlayer.x = 0;
		if(mainPlayer.y > globalHeight-150)
			mainPlayer.y = globalHeight-150;
		if(mainPlayer.y < 0)
			mainPlayer.y = 0;	 
	}
		 
	public void drawOrientations(Canvas canvas, Paint paint)
	{
		canvas.drawText(String.valueOf(orientVals[0]), 30, 60, paint);
		canvas.drawText(String.valueOf(orientVals[1]), 30, 90, paint);
		canvas.drawText(String.valueOf(orientOffsetX), 30, 120, paint);
		canvas.drawText(String.valueOf(orientOffsetY), 30, 150, paint);
	}
		 
		 
	public void enableSensor()
	{
		if(sensorManager != null)
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME); 
	}
		 
		
	public void disableSensor()
	{
		if(sensorManager != null)
			sensorManager.unregisterListener(this);
	}
		 
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
			 
	}
		 	
	public void onSensorChanged(SensorEvent event) 
	{
		synchronized(orientVals)
		{
			orientVals[0] = event.values[0];
			orientVals[1] = event.values[1];
			orientVals[2] = event.values[2];			
		}			 
	}
		  
		     
    /******************************************/
    /********PLAYER SHIP***********************/
    /******************************************/
    
    
    class PlayerObject implements Serializable
    {
    	private 	Bitmap aircraft, healthBar, ammoBar, shieldBar, boostBar;
    	private int x, y, xOffset, yOffset, selectedWeapon,
    				shipSpeed, shipHealth, 
    				shipShield, shieldRechargeRate, 
    				weaponDamage, weaponTravel;
    	double 		health, maxHealth, 
    				ammo, maxAmmo,
    				shield, maxShield,
    				boost, maxBoost;
    	boolean 	mainPlayer, boostEnabled;
    	Date		timeLastHit;
    	Date		boostTime;
    	//boolean modifyProjList;
    	
    	private ArrayList<Projectile> _projectile = new ArrayList<Projectile>();
    	
    	public PlayerObject(int x, int y, boolean mainPlayer, Bitmap bitmap)
    	{
    		this.mainPlayer = mainPlayer;
    		if(mainPlayer)
    		{
    			healthBar = BitmapFactory.decodeResource(getResources(), R.drawable.health100);
    	    	ammoBar = BitmapFactory.decodeResource(getResources(), R.drawable.ammo100);
    	    	shieldBar = BitmapFactory.decodeResource(getResources(), R.drawable.ammo100);
    	    	boostBar = BitmapFactory.decodeResource(getResources(), R.drawable.boost100);
    		}
    		else
    		{
    			healthBar = BitmapFactory.decodeResource(getResources(), R.drawable.health_horz);
    	    	ammoBar = BitmapFactory.decodeResource(getResources(), R.drawable.ammo_horz);
    	    	shieldBar = BitmapFactory.decodeResource(getResources(), R.drawable.ammo_horz);
    		}
    		boostEnabled = false;
    		aircraft = bitmap;
    		this.x = x;
    		this.y = y;
    		xOffset = aircraft.getWidth()/2;
    		yOffset = aircraft.getHeight()/2;
    		health = maxHealth = 200;
    		boost = maxBoost = 200;
    		ammo = maxAmmo = 1000;
    		shield = maxShield = 200;
    		timeLastHit = new Date();
    		boostTime = new Date();
    		timeLastHit.setTime((new Date().getTime()));
    		boostTime.setTime((new Date().getTime()));
    		shipSpeed = shipHealth = shipShield = shieldRechargeRate = weaponDamage = weaponTravel = selectedWeapon = 0;
    		//modifyProjList = true;
    	}
    	
    	public void drawShip(Canvas canvas)
    	{
    		 canvas.drawBitmap(aircraft, x - xOffset, y - yOffset, null);
    		 if(mainPlayer)
    		 {
    			// canvas.drawBitmap(Bitmap.createScaledBitmap(healthBar, healthBar.getWidth(),(int)( (double)globalHeight * (health/maxHealth)), true), 0, 0, null);
    			 if(health > 0)
    				 canvas.drawBitmap(Bitmap.createScaledBitmap(healthBar, healthBar.getWidth(),(int)( (double)(globalHeight/2) * (health/maxHealth)), true), 0, globalHeight - (int)( (double)(globalHeight/2) * (health/maxHealth)), null);
    			 if(shield > 0)
    				 canvas.drawBitmap(Bitmap.createScaledBitmap(shieldBar, shieldBar.getWidth(),(int)( (double)(globalHeight/2) * (shield/maxShield)), true), 0, (globalHeight/2) - (int)( (double)(globalHeight/2) * (shield/maxShield)), null);		 
        		 canvas.drawBitmap(Bitmap.createScaledBitmap(ammoBar, ammoBar.getWidth(),(int)( (double)globalHeight * (ammo/maxAmmo)), true), globalWidth-ammoBar.getWidth(), globalHeight - (int)( (double)globalHeight * (ammo/maxAmmo)), null);
        		 if(boost > 0)
        			 canvas.drawBitmap(Bitmap.createScaledBitmap(boostBar, (int)((double)globalWidth * (boost/maxBoost)),boostBar.getHeight(), true), (float) ((globalWidth/2) - ((globalWidth/2) * (boost/maxBoost))),(float) (globalHeight - boostBar.getHeight()), null);
    		 }
    		 else
    		 {
    			 if(health > 0)
    				 canvas.drawBitmap(Bitmap.createScaledBitmap(healthBar, (int)((double)((double)aircraft.getWidth()/2.0) * (health/maxHealth))+1, healthBar.getHeight(), true), x - xOffset, (y - yOffset) - (2*healthBar.getHeight()), null);
    			 if(shield > 0)
    				 canvas.drawBitmap(Bitmap.createScaledBitmap(shieldBar, (int)((double)((double)aircraft.getWidth()/2.0) * (shield/maxShield))+1, shieldBar.getHeight(), true), x, (y - yOffset) - (2*shieldBar.getHeight()), null);
    		 }
    			

    	}
    	
    	public void drawProjectiles(Canvas canvas)
    	{
    		
    		//if(modifyProjList)
    		//{
    		//	modifyProjList = false;
    		synchronized(_projectile)
    		{
    			List<Projectile> toRemove = new ArrayList<Projectile>();
        		for(Projectile proj : _projectile)
        		{
    	    		proj.moveProjectile();
    	    		proj.drawProjectile(canvas);
    	    		if(proj.x < 0 || proj.x > globalWidth || proj.y < 0 || proj.y > globalHeight)
    	    			toRemove.add(proj);
        		}
        		_projectile.removeAll(toRemove);
    		}
        		
        	//	modifyProjList = true;
    		//}
    		
    	}
    	
    	public void regenerateAmmo()
    	{
    		if(ammo < maxAmmo)
    			ammo+=2;
    		if((shield < maxShield) && (((new Date()).getTime() - timeLastHit.getTime()) > SHIELD_RECHARGE_DELAY))
    		{		
    			shield += 1+shieldRechargeRate;
    			writeShield((int)shield);
    		}	
    		if(shield > maxShield)
    			shield = maxShield;
    		if(boost < maxBoost)
    			boost += .1;
    		if(boostEnabled && (((new Date()).getTime() - boostTime.getTime()) > BOOST_TIME))
    			boostEnabled = false;
    	}
    	
    	public void fireProjectile(int x, int y)
    	{
    		double distance = Math.sqrt(Math.pow(Math.abs(x-this.x), 2) + Math.pow(Math.abs(y-this.y), 2));
    		if(ammo >= weaponArray[selectedWeapon][COST]+1 && (distance > ((double)aircraft.getHeight()*.75)))
    		{	
    		//	modifyProjList = false;
    			synchronized(_projectile)
    			{
    				_projectile.add(new Projectile(this.x, this.y - yOffset, x, y, selectedWeapon, weaponTravel, weaponDamage));
        			writeNewProjectile(x, y, this.x, this.y, selectedWeapon);
        			ammo -= weaponArray[selectedWeapon][COST];
    			}			
    		//	modifyProjList = true;
    		}
    	}
    	
    	public void fireProjectile(int x, int y, int xStart, int yStart, int selected)
    	{
    		//if(modifyProjList)
    		//{	
    		//	modifyProjList = false;
    		
    		synchronized(_projectile)
        	{
        		_projectile.add(new Projectile(xStart, yStart, x, y, selected, weaponTravel, weaponDamage));
        	}			
    		//	modifyProjList = true;
    		//}
    	}
    	
    	public void computeCollision()
    	{
    		synchronized(otherPlayer._projectile)
    		{
    			double distance;
    			List<Projectile> toRemove = new ArrayList<Projectile>();
    			for(Projectile proj : otherPlayer._projectile)
        		{
    				switch(proj.type)
    				{
    				case WEAPONTYPE_MISSILE:
    				case WEAPONTYPE_BIG_MISSILE:
    				case WEAPONTYPE_LASER:
    				case WEAPONTYPE_CHARGED_LASER:
            			if(	proj.x >= (x - (int)(xOffset*.75)) &&
        					proj.x <= (x + (int)(xOffset*.75)) &&
        					proj.y >= (y - (int)(yOffset*.75)) &&
        					proj.y <= (y + (int)(yOffset*.75)))
            			{
	        				if(shield > 0)
	        				{
	        					shield -= proj.damage;
	        					if(shield < 0)
	        					{
	        						health -= Math.abs(shield);
	        						shield = 0;
	        					}
	        				}
	        				else
	        					health -= proj.damage;
	        				timeLastHit.setTime((new Date().getTime()));
	        				toRemove.add(proj);
	        				sendProjectileHit(otherPlayer._projectile.indexOf(proj), (int)health, (int)shield);
	        				if(VibeSetting == 1)
	        					vibe.vibrate(VIBRATION_LENGTH);
            			} 
    					break;
    				case WEAPONTYPE_BOMB:
    					distance = Math.sqrt((Math.pow(Math.abs((double)(x - proj.x)), 2) + Math.pow(Math.abs((double)(y - proj.y)), 2)));
    					if(distance < 250.0)
            			{
	        				if(shield > 0)
	        				{
	        					shield -= proj.damage;
	        					if(shield < 0)
	        					{
	        						health -= Math.abs(shield);
	        						shield = 0;
	        					}
	        				}
	        				else
	        					health -= proj.damage;
	        				timeLastHit.setTime((new Date().getTime()));
	        				toRemove.add(proj);
	        				sendProjectileHit(otherPlayer._projectile.indexOf(proj), (int)health, (int)shield);
	        				if(VibeSetting == 1)
	        					vibe.vibrate(VIBRATION_LENGTH);
            			} 
    					break;
    				case WEAPONTYPE_DISRUPTION_BOMB:
    					distance = Math.sqrt((Math.pow(Math.abs((double)(x - proj.x)), 2) + Math.pow(Math.abs((double)(y - proj.y)), 2)));
    					if(distance < 250.0)
            			{
	        				if(shield > 0)
	        				{
	        					shield -= proj.damage;
	        					if(shield < 0)
	        					{
	        						health -= Math.abs(shield);
	        						shield = 0;
	        					}
	        				}
	        				else
	        					health -= proj.damage;
	        				timeLastHit.setTime((new Date().getTime()));
	        				toRemove.add(proj);
	        				shaking = true;
	        				shakeTime.setTime(new Date().getTime());
	        				sendProjectileHit(otherPlayer._projectile.indexOf(proj), (int)health, (int)shield);
	        				if(VibeSetting == 1)
	        					vibe.vibrate(VIBRATION_LENGTH);
            			} 
    					break;
    				}
	        	
        		}
    			otherPlayer._projectile.removeAll(toRemove);
    		}	
    		if(health <= 0)
    		{
    			act.runOnUiThread(new Runnable() 
                {
                    public void run() 
                    {
                    	playerReady = false;
                    	sendGameOver();
            			gameOver(GAME_LOST);
            			v.t.interrupt();
                    }
                });			
    		}
    	}
    	
    	public void hitRegistered(int i)
    	{
    		try
    		{
//   			Projectile temp = _projectile.get(i);	//might be a better way to get things done if concurrentmodificationexception occurs
    				
        		synchronized(_projectile)
    			{
        			_projectile.remove(i);
    			}	
    		}
    		catch (IndexOutOfBoundsException e)
    		{
    			
    		}		
    	}
    	
		public void applyUpgrades() //this only applies a few of the upgrades. Most get applied at use
    	{
    		maxHealth += shipHealth*25;
            health = maxHealth;
            maxShield += shipShield*25;
            shield = maxShield;
    	}	
    	
    	class Projectile
    	{
    		private int speed, 
    					direction, 
    					damage, 
    					x, y, 
    					type, ammoCost,
    					xOffset, yOffset, 
    					xTravel, yTravel,
    					weaponTravel, weaponDamage;
    		Matrix matrix;
    		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.projectile_missile);
    		
    		public Projectile(int startX, int startY, int x, int y, int type, int weaponTravel, int weaponDamage)
    		{
    			this.weaponTravel = weaponTravel;
    			this.weaponDamage = weaponDamage;
    			this.x = startX;
    			this.y = startY;
    			xOffset = 0;
    			yOffset = 0;
    			this.type = type;
    			switch(type)
    			{
    			case WEAPONTYPE_MISSILE:
    				bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.projectile_missile);
    				break;
    			case WEAPONTYPE_BIG_MISSILE:
    				bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.projectile_big_missile);
    				break;
    			case WEAPONTYPE_LASER:
    				bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.projectile_laser_beam);
    				break;
    			case WEAPONTYPE_CHARGED_LASER:
    				bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.projectile_charged_laser_beam);
    				break;
    			case WEAPONTYPE_BOMB:
    				bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.projectile_bomb);
    				break;
    			case WEAPONTYPE_DISRUPTION_BOMB:
    				bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.projectile_distortion_bomb);
    				break;
    			}
    			speed = (int)(weaponArray[type][SPEED]*(1+(.25*weaponTravel)));
    			damage = (int)(weaponArray[type][DAMAGE]*(1+(.25*weaponDamage)));
    			ammoCost = weaponArray[type][COST];
    			findDegree(x, y); 	   			
    		}
    		
    		public void findDegree(int x, int y)
    		{
    			xTravel = (x - this.x) / speed;
    			yTravel = (y - this.y) / speed;
    			double distance = Math.sqrt(Math.pow(xTravel, 2) + Math.pow(yTravel, 2));
    			xTravel = (int)((double)xTravel*((double)speed/distance));
    			yTravel = (int)((double)yTravel*((double)speed/distance));	
    			direction = (int) Math.toDegrees(Math.atan2((double)(y - this.y), (double)(x - this.x)));
    			matrix = new Matrix();
    			//matrix.setRotate(direction+90, BitmapFactory.decodeResource(getResources(), R.drawable.projectile_single).getWidth()/2, BitmapFactory.decodeResource(getResources(), R.drawable.projectile_single).getHeight()/2);
    			matrix.setRotate(direction+90, bitmap.getWidth()/2, bitmap.getHeight()/2);
    			matrix.postTranslate(this.x - xOffset, this.y - yOffset);
    		}
    		 		
    		public void moveProjectile()
    		{
    			x += xTravel;
    			y += yTravel;
    			matrix = new Matrix();
    			//matrix.setRotate(direction+90, BitmapFactory.decodeResource(getResources(), R.drawable.projectile_single).getWidth()/2, BitmapFactory.decodeResource(getResources(), R.drawable.projectile_single).getHeight()/2);
    			matrix.setRotate(direction+90, bitmap.getWidth()/2, bitmap.getHeight()/2);
    			matrix.postTranslate(x - xOffset, y - yOffset);
    		}
    		
    		public void drawProjectile(Canvas canvas)
    		{	
    			canvas.drawBitmap(bitmap, matrix, null);
    		} 		
    	}  	
    }
}       

//blahblahblahblah the end of file and all that jazz





