package com.zakol.walkie.event;

public class StopEvent
{
	// Sygna� zatrzymania
	private boolean stop = false;
	
	// Ustawienia pocz�tkowe
	public StopEvent()
	{
		// Wy��czenie sygna�u zatrzymania
		stop = false;
	}
	
	// W��czenie sygna�u zatrzymania
	public void stop()
	{
		stop = true;
	}
	
	// Zresetowanie sygnalizacji
	public void reset()
	{
		stop = false;
	}
	
	// Sprawdzenie sygnalizacji
	public boolean isStopped()
	{
		return stop;
	}
}
