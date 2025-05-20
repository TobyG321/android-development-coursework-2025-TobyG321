package uk.ac.hope.mcse.android.coursework.model.rewards;

import java.util.List;

import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.deals.MealForOne;

public class FreeMeal extends Reward {
    public MealForOne meal;

    public FreeMeal(BasketItem item, List<String> sides, String drink) {
        this.reward_name = "Free Meal";
        this.points_cost = 1000;

        // Build the MealForOne and override its price
        this.meal = new MealForOne(item, sides, drink);
        this.meal.deal_price = 0.0; // Make it free
    }

    public FreeMeal() {
        this.reward_name = "Free Meal";
        this.points_cost = 1000;
    }

    public FreeMeal(MealForOne meal) {
        this.reward_name = "Free Meal";
        this.points_cost = 1000;
        this.meal = meal;
        this.meal.deal_price = 0.0;
    }
}
