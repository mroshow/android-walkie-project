package com.zakol.walkie.event;

public class StopEvent
{
	// Sygna³ zatrzymania
	private boolean stop = false;
	
	// Ustawienia pocz¹tkowe
	public StopEvent()
	{
		// Wy³¹czenie sygna³u zatrzymania
		stop = false;
	}
	
	// W³¹czenie sygna³u zatrzymania
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
