package uk.ac.hope.mcse.android.coursework.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasketItem {
    public MenuItems baseItem;
    public List<String> removedBread;
    public List<String> removedDog;
    public List<String> removedCheese;
    public List<String> removedSauces;
    public List<String> removedToppings;
    public int quantity;
    public double price;

    public BasketItem(MenuItems item, List<String> removedBread, List<String> removedDog, List<String> removedCheese, List<String> removedSauces, List<String> removedToppings) {
        this.baseItem = item;
        this.removedBread = removedBread;
        this.removedDog = removedDog;
        this.removedCheese = removedCheese;
        this.removedSauces = removedSauces;
        this.removedToppings = removedToppings;
        this.quantity = 1;
        this.price = item.price;
    }

    public BasketItem(MenuItems item) {
        this.baseItem = item;
    }

    public boolean isEqualTo(BasketItem other) {
        if (other == null) return false;

        // Compare base item name
        if (!this.baseItem.item_name.equals(other.baseItem.item_name)) {
            return false;
        }

        // Compare all removed lists (order-insensitive)
        return listEquals(this.removedBread, other.removedBread) &&
                listEquals(this.removedDog, other.removedDog) &&
                listEquals(this.removedCheese, other.removedCheese) &&
                listEquals(this.removedSauces, other.removedSauces) &&
                listEquals(this.removedToppings, other.removedToppings);
    }

    private boolean listEquals(List<String> a, List<String> b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;

        List<String> copyA = new ArrayList<>(a);
        List<String> copyB = new ArrayList<>(b);
        Collections.sort(copyA);
        Collections.sort(copyB);
        return copyA.equals(copyB);
    }

}
