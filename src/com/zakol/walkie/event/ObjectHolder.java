package com.zakol.walkie.event;

public class ObjectHolder<Type>
{
	// Pole wartoœci
    private Type value = null;
    
    // Ustawienie nowej wartoœci
    public void setObject(Type newObject)
    {
    	value = newObject;
    }
    
    // Pobranie aktualnej wartoœci
    public Type getObject()
    {
    	return value;
    }
}