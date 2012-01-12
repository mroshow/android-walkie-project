package com.zakol.walkie.wifi;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.zakol.walkie.event.NotifierEvent;
import com.zakol.walkie.event.ObjectHolder;
import com.zakol.walkie.event.StopEvent;
import com.zakol.walkie.event.ThreadingEvent;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

public class TransmissionAdapter
{
	public final static int MaxPacketSize = 2 + AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

	final static int SendPacketTimeout = 5000;
	final static int ReceivePacketTimeout = 5000;
	
	final static int WaitTimeSpan = 250;
	
	static int ActionCounter = 0;
	
	static ServerSocket server = null;
	static Socket socket = null;
	
	private static boolean ConditionalThreading(int timeout, int timeSpan, StopEvent stopNotifier, ThreadingEvent event)
	{
		int ActionId = ActionCounter ++;
		
		if (timeSpan < 0)
			timeSpan = WaitTimeSpan;
		
		final ThreadingEvent eventHandler = event;
		
		final NotifierEvent threadNotifier = new NotifierEvent();
		final Thread receiverThread = new Thread(new Runnable()
		{
            public void run()
            {
            	try
            	{
            		eventHandler.startThread();
            		threadNotifier.setResultSuccess();
            	}
            	catch (Exception e)
            	{
            		threadNotifier.setResultFailure(e);
            	}
            }
		});
		
		try
		{
			Log.d("Network action", "Action # " + ActionId + ": " + event.getOperationName() + " [ start ]");
			
			receiverThread.start();
			
			boolean status = false;
			timeout /= timeSpan;
			
			Log.d("Network action", "Action # " + ActionId + ": " + event.getOperationName() + "[ loop ]");
			Log.v("Network action", "Action # " + ActionId + ": " + event.getOperationName() + "[ timeout " + (timeout * timeSpan) + " ]");
			
			while ((stopNotifier == null || !stopNotifier.isStopped()) && !(status = !receiverThread.isAlive()) && (timeout-- > 0))
			{
				try
				{
					Thread.sleep(timeSpan);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				Log.v("Network action", "Action # " + ActionId + ": " + event.getOperationName() + "[ timeout " + (timeout * timeSpan) + " ]");
			}
			
			if (stopNotifier != null && stopNotifier.isStopped())
			{
				Log.i("Network action", "Action # " + ActionId + ": " + event.getOperationName() + " [ cancelled ]");
				
				receiverThread.interrupt();
				CloseConnections();
				
				return false;
			}
			
			if (threadNotifier.getResult() == NotifierEvent.ThreadResult.Failure)
				throw threadNotifier.getException();
			
			if (status)
			{
				Log.i("Network action", "Action # " + ActionId + ": " + event.getOperationName() + " [ success ]");
				
				return true;
			}
			else
			{
				Log.w("Network action", "Action # " + ActionId + ": " + event.getOperationName() + " [ timeout ]");
				
				receiverThread.interrupt();
				CloseConnections();
				
				return false;
			}
		}
		catch (Exception e)
		{
			Log.e("Network action", "Action # " + ActionId + ": " + event.getOperationName() + " [ exception ]");
			
			receiverThread.interrupt();
			CloseConnections();
			
			e.printStackTrace();
			return false;
		}
	}
	
	private static void CloseConnections()
	{
    	try
    	{
    		if (socket != null)
    			socket.close();
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}

    	try
    	{
    		if (server != null)
    			server.close();
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
	}
	
	public static boolean SendPackets(byte[] packet, StopEvent stopNotifier, int timeout, int timeSpan)
	{
    	final byte[] packetHandler = packet;
		final StopEvent stopNotifierHandler = stopNotifier;
    	
	    return ConditionalThreading(((timeout >= 0) ? timeout : SendPacketTimeout), timeSpan, stopNotifier, new ThreadingEvent()
	    {
	    	public void startThread() throws Exception
			{
	    		socket = null;
	            
				try
	    		{
					while (true)
					{
						try
						{
							socket = new Socket(NetworkAdapter.GetOtherClientIP(), 2012);
							break;
						}
						catch (Exception e)
						{
		            		// Temporary workaround
		            		//e.printStackTrace();
						}
					}
					
					if (stopNotifierHandler != null && stopNotifierHandler.isStopped())
	                	return;
					
	    			OutputStream outputStream = socket.getOutputStream();
	            	outputStream.write(packetHandler);
	            }
	            catch (Exception e)
	            {
	            	throw e;
	            }
	            finally
	            {
	            	CloseConnections();
	            }
			}
			
			public String getOperationName()
			{
				return "Sending data...";
			}
	    });
	}

	public static boolean ReceivePackets(byte[] packet, ObjectHolder<String> senderIPHolder, StopEvent stopNotifier, int timeout, int timeSpan)
	{
		final ObjectHolder<String> senderIPHolderHandler = senderIPHolder;
		final ObjectHolder<byte[]> packetHolder = new ObjectHolder<byte[]>();
		final StopEvent stopNotifierHandler = stopNotifier;
		
	    boolean result = ConditionalThreading(((timeout >= 0) ? timeout : ReceivePacketTimeout), timeSpan, stopNotifier, new ThreadingEvent()
	    {
	    	public void startThread() throws Exception
			{
                server = null;
                socket = null;

                try
                {
                	while (true)
                	{
                		try
                		{
                			server = new ServerSocket(2012);
                			break;
                		}
                		catch (Exception e)
                		{
		            		// Temporary workaround
		            		//e.printStackTrace();
                		}
                	}
                	
                	if (stopNotifierHandler != null && stopNotifierHandler.isStopped())
	                	return;
                	
                	socket = server.accept();
                	
                    InputStream inputStream = socket.getInputStream();
                    
                    byte[] inputBuffer = new byte[MaxPacketSize];
                    
                    inputStream.read(inputBuffer);
                    
                    if (senderIPHolderHandler != null)
                    	senderIPHolderHandler.setObject(socket.getInetAddress().getHostAddress());
                    
                    packetHolder.setObject(inputBuffer);
                }
                catch (Exception e)
                {
                	throw e;
                }
                finally
                {
                	CloseConnections();
                }
			}
			
			public String getOperationName()
			{
				return "Receiving data...";
			}
	    });
	    
	    if (result)
	    {
	    	try
	    	{
	    		for (int i = 0; i < packet.length; i ++)
	    			packet[i] = packetHolder.getObject()[i];
	    	}
	    	catch (Exception e)
	    	{
	    		e.printStackTrace();
	    	}
	    }
	    
	    return result;
	}
}
