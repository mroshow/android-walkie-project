package com.zakol.walkie.event;

public class NotifierEvent
{
	// Typ stanu w¹tku
	public enum ThreadResult { Pending, Success, Failure }
	
	// Stan w¹tku
	private ThreadResult result = null;
	
	// Wyj¹tek zaistnia³y podczas pracy w¹tku
	private Exception exception = null;
	
	// Ustawienia pocz¹tkowe
	public NotifierEvent()
	{
		// Ustawienie stanu w¹tku na pracuj¹cy
		result = ThreadResult.Pending;
	}
	
	// Ustawienie stanu w¹tku na ukoñczony powodzeniem
	public void setResultSuccess()
	{
		// Je¿eli w¹tek jest w stanie pracy
		if (result == ThreadResult.Pending)
			//Ustawienie stanu
			result = ThreadResult.Success;
	}
	
	// Ustawienie stanu w¹tku na ukoñczony niepowodzeniem
	public void setResultFailure(Exception e)
	{
		// Je¿eli w¹tek jest w stanie pracy
		if (result == ThreadResult.Pending)
		{
			// Ustawienie stanu i zapamiêtanie zaistnia³ego wyj¹tku
			result = ThreadResult.Failure;
			exception = e;
		}
	}
	
	// Pobranie stanu w¹tku
	public ThreadResult getResult()
	{
		return result;
	}
	
	// Pobranie zaistnia³ego wyj¹ku
	public Exception getException()
	{
		return exception;
	}
}
