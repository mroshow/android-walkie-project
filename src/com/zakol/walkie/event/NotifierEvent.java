package com.zakol.walkie.event;

public class NotifierEvent
{
	// Typ stanu w�tku
	public enum ThreadResult { Pending, Success, Failure }
	
	// Stan w�tku
	private ThreadResult result = null;
	
	// Wyj�tek zaistnia�y podczas pracy w�tku
	private Exception exception = null;
	
	// Ustawienia pocz�tkowe
	public NotifierEvent()
	{
		// Ustawienie stanu w�tku na pracuj�cy
		result = ThreadResult.Pending;
	}
	
	// Ustawienie stanu w�tku na uko�czony powodzeniem
	public void setResultSuccess()
	{
		// Je�eli w�tek jest w stanie pracy
		if (result == ThreadResult.Pending)
			//Ustawienie stanu
			result = ThreadResult.Success;
	}
	
	// Ustawienie stanu w�tku na uko�czony niepowodzeniem
	public void setResultFailure(Exception e)
	{
		// Je�eli w�tek jest w stanie pracy
		if (result == ThreadResult.Pending)
		{
			// Ustawienie stanu i zapami�tanie zaistnia�ego wyj�tku
			result = ThreadResult.Failure;
			exception = e;
		}
	}
	
	// Pobranie stanu w�tku
	public ThreadResult getResult()
	{
		return result;
	}
	
	// Pobranie zaistnia�ego wyj�ku
	public Exception getException()
	{
		return exception;
	}
}
