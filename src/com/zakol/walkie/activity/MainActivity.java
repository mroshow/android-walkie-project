package com.zakol.walkie.activity;

import com.zakol.walkie.activity.task.StartNetworkTask;
import com.zakol.walkie.event.CallbackEvent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity
{
	// Pola w¹tku ³¹cz¹cego
	private CallbackEvent startNetworkCallback = null;
	private StartNetworkTask startNetworkTask = null;

	// Pole blokady wygaszania ekranu
	private WakeLock wakeLock = null;
	
	// Pola kontrolek interfejsu
	public static EditText StatusField = null;
	public static EditText NameField = null;
	public static Button TransmitButton = null;
	public static Button ConnectButton = null;
	public static Button ExitButton = null;
	
	// Uruchomienie widoku g³ównego
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	// Za³adowanie widoku z szablonu
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Blokada wygaszania ekranu
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        
        // Inicjalizacja pól tekstowych
        StatusField = (EditText)findViewById(R.id.editText1);
        NameField = (EditText)findViewById(R.id.editText2);
        
        // Inicjalizacja przycisku transmicji
        TransmitButton = (Button)findViewById(R.id.button2);
        TransmitButton.setTag(false);
        TransmitButton.setOnClickListener(new OnClickListener()
	    {
        	// Prze³¹czanie miêdzy transmisj¹ a nas³uchem po wciœniêciu przycisku
	    	public void onClick(View arg0)
			{
	    		// Prze³¹czenie stanu przycisku transmisji
	    		TransmitButton.setTag(!(Boolean)TransmitButton.getTag());
	    		
	    		// Modyfikacja przycisku
	    		if ((Boolean)TransmitButton.getTag())
	    			TransmitButton.setText("Stop sending voice");
	    		else
	    			TransmitButton.setText("Send voice");
			}
	    });
        
        // Inicjalizacja przycisku ³¹czenia
	    ConnectButton = (Button)findViewById(R.id.button1);
	    ConnectButton.setTag(false);
	    ConnectButton.setOnClickListener(new OnClickListener()
	    {
	    	// Rozpoczynanie ³¹czenia lub jego przerywanie po wciœniêciu przycisku
	    	public void onClick(View arg0)
			{
	    		// Przerywanie ³¹czenia
	    		if ((Boolean)ConnectButton.getTag())
	    		{
	    			// Wys³anie powiadomienia o przerwaniu
	    			startNetworkTask.stopNotifier.stop();
	    			
	    			// Modyfikacja przycisków
	    			ConnectButton.setText("Disconnecting...");
	    			ConnectButton.setEnabled(false);
	    			TransmitButton.setEnabled(false);
	    			
	    			// Wymuszenie powrotu przycisku transmisji do stanu zwyk³ego
	    			if ((Boolean)TransmitButton.getTag())
	    				TransmitButton.performClick();
	    		}
	    		else
	    		// Rozpoczynanie ³¹czenia
	    		{
	    			// Uruchomienie w¹tku ³¹cz¹cego
	    			startNetworkTask = new StartNetworkTask(MainActivity.this, startNetworkCallback);
	    			startNetworkTask.execute(null, null);
	    			
	    			// Modyfikacja przycisków
	    			ConnectButton.setText("Cancel");
	    			ConnectButton.setTag(true);
	    		}
			}
	    });
	    
	    // Inicjalizacja przycisku wy³¹czania plikacji
	    ExitButton = (Button)findViewById(R.id.button3);
	    ExitButton.setOnClickListener(new OnClickListener()
	    {
	    	// Wy³¹czenie aplikacji po wciœniêciu przycisku
	    	public void onClick(View arg0)
			{
	    		// Wy³¹czenie aplikacji
	    		System.exit(0);
			}
	    });
	    
	    // Komunikat zwrotny po zakoñczeniu wykonywania w¹tku 
        startNetworkCallback = new CallbackEvent()
		{
			public void onCallback(Object arg)
			{
				// Modyfikacja przycisków
				TransmitButton.setEnabled(false);
				ConnectButton.setTag(false);
				ConnectButton.setText("Connect");
    			ConnectButton.setEnabled(true);
			}
		};
    }

    // Zdarzenie wstrzymania aplikacji
    @Override
    protected void onPause()
    {
		super.onPause();
		wakeLock.release();
    }

    // Zdarzenie wznowienia aplikacji
    @Override
    protected void onResume()
    {
		super.onResume();
		wakeLock.acquire();
    }
    
    // Tworzenie menu programu
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
    {
    	// Dodanie do menu pozycji ustawieñ
    	menu.add(0, 0, 0, "Preferences");
    	return true;
    }
    
    // Zdarzenie wyboru pozycji z menu
    @Override
	public boolean onOptionsItemSelected(MenuItem item)
    {
    	// Zale¿noœæ od identyfikatora w argumencie
    	switch (item.getItemId())
    	{
    		// Po wybraniu pozycji ustawieñ
    		case 0:
    		{
    			// Uruchomienie widoku ustawieñ 
    			Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
    			startActivity(intent);
    			return true;
    		}
    	}
    	
    	return false;
    }
}