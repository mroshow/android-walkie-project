package com.zakol.walkie.wifi;

import android.util.Log;

public class SessionAdapter
{
	public enum SessionMessage
	{
		MSG_UNKNOWN(0),
		MSG_CONNECTION_REQUEST(1),
		MSG_CONNECTION_SUCCESS(2),
		MSG_CONNECTION_FAILURE(3),
		MSG_TRANSMISSION_BEGIN(4),
		MSG_TRANSMISSION_PAYLOAD(5),
		MSG_TRANSMISSION_END(6),
		MSG_DISCONNECT(7);
		
		 private int code = -1;

		 private SessionMessage(int c)
		 {
			 code = c;
		 }

		 public int getCode()
		 {
			 return code;
		 }
		 
		 public String getDescription()
		 {
			 switch (SessionMessage.class.getEnumConstants()[code])
			 {
			 	case MSG_CONNECTION_REQUEST: return "connection request";
			 	case MSG_CONNECTION_SUCCESS: return "connected to server";
			 	case MSG_CONNECTION_FAILURE: return "connection rejected";
			 	case MSG_TRANSMISSION_BEGIN: return "transmission start";
			 	case MSG_TRANSMISSION_PAYLOAD: return "transmission payload";
			 	case MSG_TRANSMISSION_END: return "transmission end";
			 	case MSG_DISCONNECT: return "disconnect";
			 	default: return "unknown message";
			 }
		 }
	}
	
	static int ActionCounter = 0;
	
	public static SessionMessage PacketDispatcher(byte[] packet, byte[] payload)
	{
		int ActionId = ActionCounter ++;

		String operationName = "Dispatching message (" + SessionMessage.MSG_UNKNOWN.getDescription() + ")";
		
		try
		{
			operationName = "Dispatching message (" + SessionMessage.class.getEnumConstants()[packet[0]].getDescription() + ")";
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		Log.d("Session action", "Action # " + ActionId + ": " + operationName + " [ validating ]");
		
		if (packet[1] != '#')
			return SessionMessage.MSG_UNKNOWN;
		
		try
		{
			for (int i = 0; i < packet.length - 2; i ++)
				payload[i] = packet[i + 2];
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		Log.d("Session action", "Action # " + ActionId + ": " + operationName + " [ casting ]");
		
		try
		{
			return SessionMessage.class.getEnumConstants()[packet[0]];
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return SessionMessage.MSG_UNKNOWN;
	}

	public static byte[] PacketGenerator(SessionMessage message, byte[] payload)
	{
		int ActionId = ActionCounter ++;
		
		byte[] packet = new byte[TransmissionAdapter.MaxPacketSize];
		
		String operationName = "Generating message (" + message.getDescription() + ")";
		Log.d("Session action", "Action # " + ActionId + ": " + operationName + " [ generating ]");
		
		packet[0] = (byte)message.getCode();
		packet[1] = (byte)'#';
		
		if (payload == null)
			return packet;
		
		Log.d("Session action", "Action # " + ActionId + ": " + operationName + " [ payloading ]");
		
		try
		{
			for (int i = 0; i < payload.length; i ++)
				packet[i + 2] = payload[i];
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return packet;
	}
}
