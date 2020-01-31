package wave.survivor.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends Activity
{
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Button cont = (Button) findViewById(R.id.Continue);
		Button newGame = (Button) findViewById(R.id.New_Game);
		Button upgrades = (Button) findViewById(R.id.Upgrades);
		Button achieve = (Button) findViewById(R.id.Achievements);
		Button about = (Button) findViewById(R.id.About);
		
		cont.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View opt) 
			{
				// TODO Auto-generated method stub
			}
		});
		newGame.setOnClickListener(new View.OnClickListener() 
		{
			//@SuppressWarnings("deprecation")
			public void onClick(View opt) 
			{
				/*
				final AlertDialog dialog = new AlertDialog.Builder(MainMenu.this).create();
				dialog.setTitle("Game Type");
				dialog.setButton("Single", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialg, int which)
					{
						dialog.dismiss();
					}
				});
				dialog.setButton2("Multi", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialg, int which)
					{
											
						//Intent myIntent = new Intent(MainMenu.this, BluetoothService.class);
						//MainMenu.this.startActivity(myIntent);
						startActivity(new Intent("wave.survivor.game.GamePanel"));
						dialog.dismiss();
					}
				});
				dialog.show(); */
				
				startActivity(new Intent("wave.survivor.game.GamePanel"));
				
			}
		});
		upgrades.setOnClickListener(new View.OnClickListener() 
		{
			
			public void onClick(View opt) {
				// TODO Auto-generated method stub
				startActivity(new Intent("wave.survivor.game.Upgrades"));
			}
		});
		achieve.setOnClickListener(new View.OnClickListener() 
		{
			
			public void onClick(View opt) {
				// TODO Auto-generated method stub
				startActivity(new Intent("wave.survivor.game.Achieve"));
			}
		});
		about.setOnClickListener(new View.OnClickListener() 
		{
			
			public void onClick(View opt) {
				// TODO Auto-generated method stub
				startActivity(new Intent("wave.survivor.game.About"));
			}
		});
	}
}
