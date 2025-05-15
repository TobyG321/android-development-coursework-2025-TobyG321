package uk.ac.hope.mcse.android.coursework.model.deals;

import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;

public class Roulette extends Deal{
    public BasketItem item;
    public boolean spicy;

    public Roulette(MenuItems randomDog, boolean sp) {
        this.spicy = sp;

        // Build deal name and description
        this.deal_name = sp
                ? "**Spicy** Roulette"
                : "Roulette";

        this.deal_description = sp
                ? "Get a random **spicy** dog for half price!"
                : "Get a random dog for half price!";

        // Create discounted BasketItem
        this.item = new BasketItem(randomDog);
        this.deal_price = randomDog.price / 2;
    }
}
