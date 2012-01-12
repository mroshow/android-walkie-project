package com.zakol.walkie.event;

public interface ThreadingEvent
{
	// Uruchomienie operacji w nowym w�tku
	abstract void startThread() throws Exception;
	
	// Pobranie nazwy operacji
	abstract String getOperationName();
}