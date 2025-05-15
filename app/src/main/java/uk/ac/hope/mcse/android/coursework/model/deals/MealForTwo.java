package uk.ac.hope.mcse.android.coursework.model.deals;

import java.util.ArrayList;
import java.util.List;

import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;

public class MealForTwo extends Deal{
    public List<BasketItem> dogs;
    public List<String> sides1;
    public List<String> sides2;
    public List<String> drinks;

    public MealForTwo(BasketItem item1, BasketItem item2) {
        dogs = new ArrayList<>();
        dogs.add(item1);
        dogs.add(item2);
        deal_name = "Meal For Two";
        deal_description = "Upgrade two meals for £4.00 and save money on the extras.";
        deal_price = dogs.get(0).baseItem.price + dogs.get(1).baseItem.price + 4.00;
    }
}
