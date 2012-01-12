package com.zakol.walkie.activity.task;

import com.zakol.walkie.activity.MainActivity;
import com.zakol.walkie.event.CallbackEvent;
import com.zakol.walkie.event.ObjectHolder;
import com.zakol.walkie.event.StopEvent;
import com.zakol.walkie.wifi.NetworkAdapter;
import com.zakol.walkie.wifi.SessionAdapter;
import com.zakol.walkie.wifi.SessionAdapter.SessionMessage;
import com.zakol.walkie.wifi.TransmissionAdapter;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;

public class StartNetworkTask extends AsyncTask<Object, Object, Object>
{
	// Limity czasu dla operacji
	private final static int SendPacketTimeout = 5000;
	private final static int ReceivePacketTimeout = 500;
	private final static int TransactionTimeout = 10000;
	
	// Okres czasu szybkiego oczekiwania pasywnego
	private final static int FastTimeSpan = 100;
	
	// Sygnalizator zatrzymania w¹tków
	public StopEvent stopNotifier = null;
	
	// Kontekst aplikacji
	private Context context = null;
	
	// Komunikat zwrotny dla aplikacji
	private CallbackEvent callback = null;
	
	// Komunikaty zwrotne dla w¹tku
	private CallbackEvent statusCallback = null;
	private CallbackEvent runtimeCallback = null;
	private CallbackEvent interfaceCallback = null;
	private CallbackEvent timeoutCallback = null;
	
	// Mened¿er odtwarzania dŸwiêku
	private AudioTrack voice = null;
	
	// Ustawienia pocz¹tkowe
	public StartNetworkTask(Context context, CallbackEvent callback)
	{
		super();
		
		// Ustawienie kontekstu i komunikatu zwrotnego dla aplikacji
		this.context = context;
		this.callback = callback;
		
		// Utworzenie sygnalizatora
		stopNotifier = new StopEvent();

		// Utworzenie komunikatu zwrotnego o zmianie aktualne wykonywanej operacji
    	statusCallback = new CallbackEvent()
    	{
			public void onCallback(Object arg)
			{
				// Przekazanie komunikatu do w¹tku g³ównego
				publishProgress(new Object[] { 0, arg });
			}
    	};

		// Utworzenie komunikatu zwrotnego o zmianie stanu po³¹czenia
    	runtimeCallback = new CallbackEvent()
    	{
			public void onCallback(Object arg)
			{
				// Przekazanie komunikatu do w¹tku g³ównego
				publishProgress(new Object[] { 1, arg });
			}
    	};

		// Utworzenie komunikatu zwrotnego o zmianie blokowania interfejsu
    	interfaceCallback = new CallbackEvent()
    	{
			public void onCallback(Object arg)
			{
				// Przekazanie komunikatu do w¹tku g³ównego
				publishProgress(new Object[] { 2, arg });
			}
    	};
    	
		// Utworzenie komunikatu zwrotnego o przekroczeniu czasu oczekiwania na dane dŸwiêkowe
    	timeoutCallback = new CallbackEvent()
    	{
			public void onCallback(Object arg)
			{
				// Przekazanie komunikatu do w¹tku g³ównego
				publishProgress(new Object[] { 3, arg });
			}
    	};
    	
    	// Próba utworzenia, konfiguracji i uruchomienia mened¿era odtwarzania dŸwiêku
    	try
    	{
	    	// Utworzenie mened¿era z ustawieniami do odtwarzania dŸwiêku
			voice = new AudioTrack(AudioManager.STREAM_MUSIC,
					8000,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
	                AudioFormat.ENCODING_PCM_16BIT,
	                TransmissionAdapter.MaxPacketSize - 2,
	                AudioTrack.MODE_STREAM);
			
			// Ustawienie czêstotliwoœci próbkowania odtwarzanego dŸwiêku
			voice.setPlaybackRate(8000);
			
			// Rozpoczêcie odtwarzania dŸwiêku
			voice.play();
    	}
    	// Niepowodzenie przy tworzeniu mened¿era
    	catch (Exception e)
    	{
    		// Zapis wyj¹tku w logu
    		e.printStackTrace();
    	}
	}
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
	}

	private String ByteArrayToString(byte[] bytes)
	{
		String result = "";
		
		for (int i = 0; ((i < bytes.length) && (bytes[i] != 0)); i ++)
			result += (char)bytes[i];
		
		return result;
	}
	
	private void OnConnected()
	{
		byte[] inputBuffer = new byte[TransmissionAdapter.MaxPacketSize];
		byte[] payloadBuffer = new byte[TransmissionAdapter.MaxPacketSize - 2];
		
		statusCallback.onCallback("Waiting...");
		int timeout = -1;
		
		while (!stopNotifier.isStopped())
		{
			if ((Boolean)MainActivity.TransmitButton.getTag())
			{
				if (TransmissionAdapter.SendPackets(
						SessionAdapter.PacketGenerator(
								SessionMessage.MSG_TRANSMISSION_BEGIN,
								null),
						stopNotifier, SendPacketTimeout, FastTimeSpan)
					)
				{
					statusCallback.onCallback("Talking...");
					
					AudioRecord record = null;
					
					try
					{
				        record = new AudioRecord(AudioSource.MIC,
			        		8000,
			        		AudioFormat.CHANNEL_CONFIGURATION_MONO,
			        		AudioFormat.ENCODING_PCM_16BIT,
			        		TransmissionAdapter.MaxPacketSize - 2);
				        
				        record.startRecording();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
			        timeout = TransactionTimeout;
			        
					while (!stopNotifier.isStopped() && (Boolean)MainActivity.TransmitButton.getTag())
					{
						if (record != null)
							record.read(payloadBuffer, 0, TransmissionAdapter.MaxPacketSize - 2);
		            	
		            	if (TransmissionAdapter.SendPackets(
								SessionAdapter.PacketGenerator(
										SessionMessage.MSG_TRANSMISSION_PAYLOAD,
										payloadBuffer),
								stopNotifier, SendPacketTimeout, FastTimeSpan))
		            		timeout = TransactionTimeout;
		            	else
		            	{
		    				timeout -= SendPacketTimeout;
		    					
	    					if (timeout < 0)
	    					{
	    						timeout = -1;
	    						timeoutCallback.onCallback(null);
	    						break;
	    					}
		            	}
					}
					
					record.stop();

					statusCallback.onCallback("Waiting...");
					
					TransmissionAdapter.SendPackets(
							SessionAdapter.PacketGenerator(
									SessionMessage.MSG_TRANSMISSION_END,
									null),
							null, SendPacketTimeout, FastTimeSpan);
				}
			}
			else
			{
				if (TransmissionAdapter.ReceivePackets(inputBuffer, null, stopNotifier, ReceivePacketTimeout, FastTimeSpan))
				{
					switch (SessionAdapter.PacketDispatcher(inputBuffer, payloadBuffer))
					{
						case MSG_TRANSMISSION_BEGIN:
						{
							timeout = TransactionTimeout;
							
							statusCallback.onCallback("Listening...");
							interfaceCallback.onCallback(false);
							
							break;
						}
						case MSG_TRANSMISSION_PAYLOAD:
						{
							timeout = TransactionTimeout;
							
	            			voice.write(payloadBuffer, 0, payloadBuffer.length);
	            			
							break;
						}
						case MSG_TRANSMISSION_END:
						{
							timeout = -1;
							
							statusCallback.onCallback("Waiting...");
							interfaceCallback.onCallback(true);
							
							break;
						}
						case MSG_DISCONNECT:
						{
							return;
						}
					}
				}
				
				if (timeout != -1)
				{
					if (timeout >= 0)
						timeout -= ReceivePacketTimeout;
					
					if (timeout < 0)
					{
						timeout = -1;
						
						statusCallback.onCallback("Waiting...");
						interfaceCallback.onCallback(true);
					}
				}
			}
		}
		
		timeout = TransactionTimeout;
		
		while (!TransmissionAdapter.SendPackets(
				SessionAdapter.PacketGenerator(
						SessionMessage.MSG_DISCONNECT,
						null),
				null, SendPacketTimeout, FastTimeSpan))
		{
			timeout -= SendPacketTimeout;
			
			if (timeout < 0)
				break;
		}
	}
	
	@Override
	protected Object doInBackground(Object... params)
	{
		byte[] inputBuffer = new byte[TransmissionAdapter.MaxPacketSize];
		byte[] payloadBuffer = new byte[TransmissionAdapter.MaxPacketSize - 2];
		
		if (NetworkAdapter.BeginNetworkScan(context, statusCallback, stopNotifier))
		{
			if (NetworkAdapter.BeginNetworkConnection(context, statusCallback, stopNotifier))
			{
				NetworkAdapter.SetOtherClientIP(NetworkAdapter.GetServerIP(context));
				
				statusCallback.onCallback("Sending authorization request...");
				
				if (TransmissionAdapter.SendPackets(
						SessionAdapter.PacketGenerator(
								SessionMessage.MSG_CONNECTION_REQUEST,
								NetworkAdapter.GetUserName(context).getBytes()),
						stopNotifier, -1, -1)
					)
				{
					statusCallback.onCallback("Receiving autorization response...");
					
					if (TransmissionAdapter.ReceivePackets(inputBuffer, null, stopNotifier, -1, -1))
					{
						switch (SessionAdapter.PacketDispatcher(inputBuffer, payloadBuffer))
						{
							case MSG_CONNECTION_SUCCESS:
							{
								NetworkAdapter.SetOtherName(ByteArrayToString(payloadBuffer));
								
								statusCallback.onCallback("Outcoming connection established.");
								runtimeCallback.onCallback(true);
								
								OnConnected();
								
								runtimeCallback.onCallback(false);
								
								NetworkAdapter.StopWifi(context, statusCallback, null);
								NetworkAdapter.SetOtherClientIP("0.0.0.0");
								NetworkAdapter.SetOtherName("---");
								
								statusCallback.onCallback("Conection closed.");
								
								break;
							}
							case MSG_CONNECTION_FAILURE:
							{
								NetworkAdapter.StopWifi(context, statusCallback, null);
								NetworkAdapter.SetOtherClientIP("0.0.0.0");

								statusCallback.onCallback("Authorization rejected by server.");
								
								break;
							}
							default:
							{
								NetworkAdapter.StopWifi(context, statusCallback, null);
								NetworkAdapter.SetOtherClientIP("0.0.0.0");
	
								statusCallback.onCallback("Authorization failed.");
								
								break;
							}
						}
					}
					else
					{
						NetworkAdapter.StopWifi(context, statusCallback, null);
						NetworkAdapter.SetOtherClientIP("0.0.0.0");

						statusCallback.onCallback("Server is not responding.");
					}
				}
				else
				{
					NetworkAdapter.StopWifi(context, statusCallback, null);
					NetworkAdapter.SetOtherClientIP("0.0.0.0");
					
					statusCallback.onCallback("Network unreachable.");
				}
			}
			else
			{
				statusCallback.onCallback("Connection failed.");
			}
		}
		else if (!stopNotifier.isStopped())
		{
			if (NetworkAdapter.StartAccessPoint(context, statusCallback, stopNotifier))
			{
				boolean isUserConnected = false;
				ObjectHolder<String> senderIPHolder = new ObjectHolder<String>();
				
				while (!stopNotifier.isStopped() && !isUserConnected)
				{
					statusCallback.onCallback("Waiting for connections...");
					
					if (TransmissionAdapter.ReceivePackets(inputBuffer, senderIPHolder, stopNotifier, -1, -1))
					{
						switch (SessionAdapter.PacketDispatcher(inputBuffer, payloadBuffer))
						{
							case MSG_CONNECTION_REQUEST:
							{
								NetworkAdapter.SetOtherClientIP(senderIPHolder.getObject());
								
								statusCallback.onCallback("Sending authorization response...");
								
								if (TransmissionAdapter.SendPackets(
										SessionAdapter.PacketGenerator(
												SessionMessage.MSG_CONNECTION_SUCCESS,
												NetworkAdapter.GetUserName(context).getBytes()),
										stopNotifier, -1, -1)
									)
								{
									isUserConnected = true;
								}
								
								break;
							}
						}
					}
				}
				
				if (isUserConnected)
				{
					NetworkAdapter.SetOtherName(ByteArrayToString(payloadBuffer));
					
					statusCallback.onCallback("Incoming connection established.");
					runtimeCallback.onCallback(true);
					
					OnConnected();
					
					runtimeCallback.onCallback(false);
					
					NetworkAdapter.SetOtherClientIP("0.0.0.0");
					NetworkAdapter.SetOtherName("---");
				}
				
				NetworkAdapter.StopAccessPoint(context, statusCallback, null);
				statusCallback.onCallback("Conection closed.");
			}
			else
			{
				statusCallback.onCallback("Access Point not created.");
			}
		}
		
		 if (stopNotifier.isStopped())
			 statusCallback.onCallback("Idle.");
		
		return 0;
	}

	@Override
	protected void onProgressUpdate(Object... progress)
    {
		super.onProgressUpdate(progress);
		
		switch ((Integer)progress[0])
		{
			case 0:
			{
				MainActivity.StatusField.setText((String)progress[1]);
				break;
			}
			case 1:
			{
				if ((Boolean)progress[1])
				{
					MainActivity.NameField.setText("Connected with: " + NetworkAdapter.GetOtherName());
					MainActivity.TransmitButton.setEnabled(true);
				}
				else
				{
					MainActivity.NameField.setText("Connected with: ---");
					MainActivity.TransmitButton.setEnabled(false);
				}
				
				break;
			}
			case 2:
			{
				MainActivity.TransmitButton.setEnabled((Boolean)progress[1]);
				MainActivity.ConnectButton.setEnabled((Boolean)progress[1]);
				break;
			}
			case 3:
			{
				MainActivity.TransmitButton.performClick();
				break;
			}
		}
    }
	
	@Override
	protected void onPostExecute(Object result)
    {
		super.onPostExecute(result);
		
		try
		{
			if (voice != null)
				voice.stop();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		callback.onCallback(false);
    }
}