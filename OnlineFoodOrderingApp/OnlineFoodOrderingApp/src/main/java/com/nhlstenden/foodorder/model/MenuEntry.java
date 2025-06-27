package com.nhlstenden.foodorder.model;

public class MenuEntry
{
    private final String type;
    private final String key;
    private String name;
    private double price;

    public MenuEntry(String type, String key, String name, double price)
    {
        this.type  = type;
        this.key   = key;
        this.name  = name;
        this.price = price;
    }

    public String getType() { return type;  }
    public String getKey()    { return key;   }
    public String getName()   { return name;  }
    public double getPrice()  { return price; }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }
}
