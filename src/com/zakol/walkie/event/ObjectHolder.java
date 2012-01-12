package com.zakol.walkie.event;

public class ObjectHolder<Type>
{
	// Pole warto�ci
    private Type value = null;
    
    // Ustawienie nowej warto�ci
    public void setObject(Type newObject)
    {
    	value = newObject;
    }
    
    // Pobranie aktualnej warto�ci
    public Type getObject()
    {
    	return value;
    }
}