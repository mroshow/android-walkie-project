package com.zakol.walkie.event;

public interface ConditionalEvent
{
	// Rozpocz�cie zdarzenia
	abstract boolean startEvent() throws Exception;
	
	// Sprawdzenie warunk�w
	abstract boolean checkCondition() throws Exception;
	
	// Zdarzenie przekroczenia czasu
	abstract void onTimeout() throws Exception;
	
	// Pobranie nazwy operacji
	abstract String getOperationName();
}