package wave.survivor.game;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

public class Upgrades extends Activity
{
	int shipSpeed, shipHealth, shipShield, shieldRechargeRate, weaponDamage, weaponTravel, pointsLeft;
	TextView t_pointsLeft;
	SharedPreferences getSetting;
	SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upgrades);
		
		
		SeekBar s_shipSpeed = (SeekBar)findViewById(R.id.ship_speed);
		SeekBar s_shipHealth = (SeekBar)findViewById(R.id.ship_health);
		SeekBar s_shipShield = (SeekBar)findViewById(R.id.ship_shield);
		SeekBar s_shieldRechargeRate = (SeekBar)findViewById(R.id.shield_recharge_rate);
		SeekBar s_weaponDamage = (SeekBar)findViewById(R.id.ship_weapon_damage);
		SeekBar s_weaponTravel = (SeekBar)findViewById(R.id.ship_weapon_travel_speed);
		
		t_pointsLeft = (TextView)findViewById(R.id.upgrade_points);
		final TextView t_shipSpeed = (TextView)findViewById(R.id.ship_speed_t);
		final TextView t_shipHealth = (TextView)findViewById(R.id.ship_health_t);
		final TextView t_shipShield = (TextView)findViewById(R.id.ship_shield_t);
		final TextView t_shieldRechargeRate = (TextView)findViewById(R.id.shield_recharge_rate_t);
		final TextView t_weaponDamage = (TextView)findViewById(R.id.ship_weapon_damage_t);
		final TextView t_weaponTravel = (TextView)findViewById(R.id.ship_weapon_travel_t);
				
		getSetting = getSharedPreferences("game_settings", 0);
		editor = getSetting.edit();
		
		pointsLeft = getSetting.getInt("upgrade_points", 7);
		shipSpeed = getSetting.getInt("ship_speed", 0);
		shipHealth = getSetting.getInt("ship_health", 0);
		shipShield = getSetting.getInt("ship_shield", 0);
		shieldRechargeRate = getSetting.getInt("ship_shield_recharge", 0);
		weaponDamage = getSetting.getInt("ship_weap_damange", 0);
		weaponTravel = getSetting.getInt("ship_weap_speed", 0);
		
		t_pointsLeft.setText(String.valueOf(pointsLeft));
		s_shipSpeed.setProgress(getSetting.getInt("ship_speed", 0));
		s_shipHealth.setProgress(getSetting.getInt("ship_health", 0));
		s_shipShield.setProgress(getSetting.getInt("ship_shield", 0));
		s_shieldRechargeRate.setProgress(getSetting.getInt("ship_shield_recharge", 0));
		s_weaponDamage.setProgress(getSetting.getInt("ship_weap_damange", 0));
		s_weaponTravel.setProgress(getSetting.getInt("ship_weap_speed", 0));
		
		s_shipSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
		{
			
			public void onStopTrackingTouch(SeekBar s_shipSpeed) { }
			
			public void onStartTrackingTouch(SeekBar s_shipSpeed) { }
			
			public void onProgressChanged(SeekBar s_shipSpeed, int progress, boolean arg2) 
			{
				shipSpeed = isAllowed(shipSpeed, s_shipSpeed);
				t_shipSpeed.setText(String.valueOf(shipSpeed));
				editor.putInt("ship_speed", shipSpeed);
				editor.apply();
			}
		});
		
		s_shipHealth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
		{
			
			public void onStopTrackingTouch(SeekBar s_shipHealth) { }
			
			public void onStartTrackingTouch(SeekBar s_shipHealth) { }
					
			public void onProgressChanged(SeekBar s_shipHealth, int progress, boolean arg2) 
			{
				shipHealth = isAllowed(shipHealth, s_shipHealth);
				t_shipHealth.setText(String.valueOf(shipHealth));
				editor.putInt("ship_health", shipHealth);
				editor.apply();
			}
		});
		
		s_shipShield.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
		{
			
			public void onStopTrackingTouch(SeekBar s_shipShield) { }
			
			public void onStartTrackingTouch(SeekBar s_shipShield) { }
					
			public void onProgressChanged(SeekBar s_shipShield, int progress, boolean arg2) 
			{
				shipShield = isAllowed(shipShield, s_shipShield);
				t_shipShield.setText(String.valueOf(shipShield));
				editor.putInt("ship_shield", shipShield);
				editor.apply();
			}
		});
		
		s_shieldRechargeRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
		{
			
			public void onStopTrackingTouch(SeekBar s_shieldRechargeRate) { }
			
			public void onStartTrackingTouch(SeekBar s_shieldRechargeRate) { }
					
			public void onProgressChanged(SeekBar s_shieldRechargeRate, int progress, boolean arg2) 
			{
				shieldRechargeRate = isAllowed(shieldRechargeRate, s_shieldRechargeRate);
				t_shieldRechargeRate.setText(String.valueOf(shieldRechargeRate));
				editor.putInt("ship_shield_recharge", shieldRechargeRate);
				editor.apply();
			}
		});
		
		s_weaponDamage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
		{
			
			public void onStopTrackingTouch(SeekBar s_weaponDamage) { }
			
			public void onStartTrackingTouch(SeekBar s_weaponDamage) { }
					
			public void onProgressChanged(SeekBar s_weaponDamage, int progress, boolean arg2) 
			{
				weaponDamage = isAllowed(weaponDamage, s_weaponDamage);
				t_weaponDamage.setText(String.valueOf(weaponDamage));
				editor.putInt("ship_weap_damange", weaponDamage);
				editor.apply();
			}
		});
		
		s_weaponTravel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
		{
			
			public void onStopTrackingTouch(SeekBar s_weaponTravel) { }
			
			public void onStartTrackingTouch(SeekBar s_weaponTravel) { }
				
			public void onProgressChanged(SeekBar s_weaponTravel, int progress, boolean arg2) 
			{
				weaponTravel = isAllowed(weaponTravel, s_weaponTravel);
				t_weaponTravel.setText(String.valueOf(weaponTravel));
				editor.putInt("ship_weap_speed", weaponTravel);
				editor.apply();
			}
		});
	}
	
	public int isAllowed(int previousValue, SeekBar whichBar)
	{
		//Log.i("previous value", String.valueOf(previousValue));
		final SharedPreferences.Editor editor = getSetting.edit();
		if(whichBar.getProgress() > previousValue)
		{
			if((whichBar.getProgress() - previousValue) >= pointsLeft)
			{
				whichBar.setProgress(previousValue + (pointsLeft));
				pointsLeft = 0;				
			}
			else if((whichBar.getProgress() - previousValue) < pointsLeft)
			{
				pointsLeft -= (whichBar.getProgress() - previousValue);
			}
		}
		else if(whichBar.getProgress() < previousValue)
		{
			pointsLeft += (previousValue - whichBar.getProgress());
		}
		t_pointsLeft.setText(String.valueOf(pointsLeft));
		editor.putInt("upgrade_points", pointsLeft);
		editor.apply();
	//	Log.i("points left", String.valueOf(pointsLeft));
		return whichBar.getProgress();
	}
}
