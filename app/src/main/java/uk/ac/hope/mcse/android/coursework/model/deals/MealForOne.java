package uk.ac.hope.mcse.android.coursework.model.deals;

import android.util.Log;

import java.util.List;

import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;

public class MealForOne extends Deal{
    public BasketItem dog;
    public List<String> sides;
    public String drink;

    public MealForOne(MenuItems item, List<String> removedBread, List<String> removedDog, List<String> removedCheese, List<String> removedSauces, List<String> removedToppings) {
        dog = new BasketItem(item, removedBread, removedDog, removedCheese, removedSauces, removedToppings);
        deal_name = "Meal For One";
        deal_description = "Upgrade to a meal deal for £2.50 and save money on extras.";
        deal_price = dog.price + 2.50;
    }
}
