package uk.ac.hope.mcse.android.coursework.model.rewards;

import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;

public class FreeDog extends Reward {
    public BasketItem dog;

    public FreeDog(BasketItem item, int points) {
        dog = item;
        dog.baseItem = item.baseItem;
        points_cost = points;

        reward_name = "Free Dogg ("+dog.baseItem.item_name+")";
        reward_description = "Get a free "+ dog.baseItem.item_name + " Doggo!";
    }
}
