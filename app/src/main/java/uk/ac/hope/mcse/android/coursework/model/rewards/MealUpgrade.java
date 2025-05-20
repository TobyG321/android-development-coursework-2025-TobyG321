package uk.ac.hope.mcse.android.coursework.model.rewards;

import java.util.List;

import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;
import uk.ac.hope.mcse.android.coursework.model.deals.MealForOne;

public class MealUpgrade extends Reward{
    public MealForOne meal;

    public MealUpgrade(BasketItem item, List<String> sides, String drink){
        meal = new MealForOne(item, sides, drink);
        reward_name = "Meal Upgrade";
        points_cost = 400;
    }

    public MealUpgrade(){
        reward_name = "Meal Upgrade";
        points_cost = 400;
    }
}
