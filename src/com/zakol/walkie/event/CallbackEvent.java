package com.zakol.walkie.event;

public interface CallbackEvent
{
	// Zdarzenie przesłania komunikatu zwrotnego
	abstract void onCallback(Object arg);
}