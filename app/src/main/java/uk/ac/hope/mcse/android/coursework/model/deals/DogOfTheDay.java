package uk.ac.hope.mcse.android.coursework.model.deals;

import android.util.Log;

import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;

public class DogOfTheDay extends Deal{
    public BasketItem item;

    public DogOfTheDay(MenuItems featuredDog) {
        deal_name = "Dog of the Day ("+featuredDog.item_name+")";
        deal_description = "Get the dog of the day for 30% off!";

        item = new BasketItem(featuredDog);
        deal_price = item.baseItem.price*0.7;
    }

    public DogOfTheDay(BasketItem featuredDog) {
        deal_name = "Dog of the Day ("+featuredDog.baseItem.item_name+")";
        deal_description = "Get the dog of the day for 30% off!";
        item = featuredDog;
        deal_price = item.baseItem.price*0.7;
    }
}
