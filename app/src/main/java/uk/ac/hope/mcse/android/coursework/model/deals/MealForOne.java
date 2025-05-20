package uk.ac.hope.mcse.android.coursework.model.deals;

import android.util.Log;

import java.util.Arrays;
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

    public MealForOne(BasketItem item, List<String> sides, String drink) {
        this.dog = item;
        this.sides = sides;
        this.drink = drink;
        this.deal_name = "Meal For One";
        this.deal_description = "Reward bonus meal upgrade";

        if (item != null) {
            this.deal_price = item.baseItem.price;
        } else {
            this.deal_price = 0.0; // fallback/default price
        }
    }

    public MealForOne(MenuItems baseItem, List<String> removedBread, List<String> removedDog, List<String> removedCheese, List<String> removedSauces, List<String> removedToppings, List<String> sides, String drink) {
        this.dog = new BasketItem(baseItem, removedBread, removedDog, removedCheese, removedSauces, removedToppings);
        this.sides = sides;
        this.drink = drink;
        this.deal_name = "Meal For One";
    }
}
