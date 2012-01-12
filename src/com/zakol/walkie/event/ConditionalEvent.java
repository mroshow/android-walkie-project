package com.zakol.walkie.event;

public interface ConditionalEvent
{
	// Rozpoczêcie zdarzenia
	abstract boolean startEvent() throws Exception;
	
	// Sprawdzenie warunków
	abstract boolean checkCondition() throws Exception;
	
	// Zdarzenie przekroczenia czasu
	abstract void onTimeout() throws Exception;
	
	// Pobranie nazwy operacji
	abstract String getOperationName();
}