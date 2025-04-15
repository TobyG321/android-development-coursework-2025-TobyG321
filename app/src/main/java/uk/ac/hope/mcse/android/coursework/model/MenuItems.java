package uk.ac.hope.mcse.android.coursework.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity
public class MenuItems {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "item_name")
    public String item_name;

    @ColumnInfo(name = "last_update")
    public String last_update;

    @ColumnInfo(name = "region")
    public String region;

    @ColumnInfo(name = "price")
    public double price;

    @ColumnInfo(name = "bread")
    public String bread;

    @ColumnInfo(name = "cheese")
    public String cheese;

    @ColumnInfo(name = "sauces")
    public String sauces; //e.g tomato, mustard

    @ColumnInfo(name = "toppings")
    public String toppings; //e.g chopped onions, tomatoes
}