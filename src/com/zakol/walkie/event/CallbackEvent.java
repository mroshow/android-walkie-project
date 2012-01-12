package com.zakol.walkie.event;

public interface CallbackEvent
{
	// Zdarzenie przes³ania komunikatu zwrotnego
	abstract void onCallback(Object arg);
}