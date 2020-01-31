package wave.survivor.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainSplashScreen extends Activity 
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_splash_screen);
        Thread splashTimer = new Thread()
        {
        	public void run()
        	{
        		try
        		{
        			int timer = 0; //change to for-loop sometime
        			while(timer < 1500)
        			{
        				sleep(100);
        				timer+=100;
        			}
        			startActivity(new Intent("wave.survivor.game.MM"));
        		}
        		catch(InterruptedException e)
        		{
        			e.printStackTrace();
        		}
        		finally
        		{
        			finish();
        		}
        	}
        };
        splashTimer.start();
    }

	@Override
	protected void onStart() 
	{
		// TODO Auto-generated method stub
		super.onStart();
	}
    
	@Override
	protected void onResume() 
	{
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onPause() 
	{
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onStop() 
	{
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onRestart() 
	{
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}