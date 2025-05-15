package uk.ac.hope.mcse.android.coursework.model.deals;

import android.util.Log;

import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;

public class DogOfTheDay extends Deal{
    public BasketItem item;

    public DogOfTheDay(MenuItems featuredDog) {
        deal_name = "Dog of the Day";
        deal_description = "Get the dog of the day for 30% off!";

        item = new BasketItem(featuredDog);
        deal_price = item.baseItem.price/2;
    }
}
