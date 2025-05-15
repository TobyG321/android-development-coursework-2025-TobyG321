package uk.ac.hope.mcse.android.coursework;

import static uk.ac.hope.mcse.android.coursework.MainActivity.db;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFifthBinding;
import uk.ac.hope.mcse.android.coursework.databinding.FragmentSecondBinding;
import uk.ac.hope.mcse.android.coursework.databinding.FragmentSixthBinding;
import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.UserDao;
import uk.ac.hope.mcse.android.coursework.model.deals.Deal;
import uk.ac.hope.mcse.android.coursework.model.deals.DogOfTheDay;
import uk.ac.hope.mcse.android.coursework.model.deals.MealForOne;
import uk.ac.hope.mcse.android.coursework.model.deals.MealForTwo;
import uk.ac.hope.mcse.android.coursework.model.deals.Roulette;

public class SixthFragment extends Fragment {

    private FragmentSixthBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSixthBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout basketContainer = view.findViewById(R.id.basket_items_container);
        List<BasketItem> basketItems = MainActivity.basket;

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (BasketItem item : basketItems) {
            View itemView = inflater.inflate(R.layout.basket_item, basketContainer, false);

            TextView nameView = itemView.findViewById(R.id.item_name);
            TextView priceView = itemView.findViewById(R.id.item_price);
            TextView quantityView = itemView.findViewById(R.id.text_quantity);
            Button buttonIncrease = itemView.findViewById(R.id.button_increase);
            Button buttonDecrease = itemView.findViewById(R.id.button_decrease);
            ImageView imageView = itemView.findViewById(R.id.item_image);
            TextView customizationsView = itemView.findViewById(R.id.item_customizations);

            nameView.setText(item.baseItem.item_name);

            // Set image
            String imageName = item.baseItem.item_name.toLowerCase().replace(" ", "");
            int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
            imageView.setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);

            // Set customization summary
            StringBuilder customizations = new StringBuilder();
            if (!item.removedBread.isEmpty()) customizations.append("No ").append(String.join(", ", item.removedBread)).append(". ");
            if (!item.removedDog.isEmpty()) customizations.append("No ").append(String.join(", ", item.removedDog)).append(". ");
            if (!item.removedCheese.isEmpty()) customizations.append("No ").append(String.join(", ", item.removedCheese)).append(". ");
            if (!item.removedSauces.isEmpty()) customizations.append("No ").append(String.join(", ", item.removedSauces)).append(". ");
            if (!item.removedToppings.isEmpty()) customizations.append("No ").append(String.join(", ", item.removedToppings)).append(". ");
            if (customizations.length() == 0) customizations.append("No customizations");
            customizationsView.setText(customizations.toString());

            // Set quantity and price
            item.quantity = Math.max(item.quantity, 1); // Default to at least 1
            quantityView.setText(String.valueOf(item.quantity));
            priceView.setText(String.format("$%.2f", item.price * item.quantity));

            // Listeners to update quantity and price
            buttonIncrease.setOnClickListener(v -> {
                item.quantity++;
                quantityView.setText(String.valueOf(item.quantity));
                priceView.setText(String.format("$%.2f", item.price * item.quantity));
                updateTotalPrice(basketItems, view);
            });

            buttonDecrease.setOnClickListener(v -> {
                if (item.quantity > 1) {
                    item.quantity--;
                    quantityView.setText(String.valueOf(item.quantity));
                    priceView.setText(String.format("$%.2f", item.price * item.quantity));
                    updateTotalPrice(basketItems, view);
                }
            });

            ImageButton deleteButton = itemView.findViewById(R.id.button_delete);

            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Remove Item")
                        .setMessage("Are you sure you want to remove this item from the basket?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Remove from basket list
                            basketItems.remove(item);

                            // Remove the view from the container
                            basketContainer.removeView(itemView);

                            // Update total
                            updateTotalPrice(basketItems, view);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });


            basketContainer.addView(itemView);
        }


        List<Deal> deals = MainActivity.deals;
        for (Deal deal : deals) {
            // Add a deal header
            TextView dealHeader = new TextView(getContext());
            dealHeader.setText(deal.deal_name + " - $" + String.format("%.2f", deal.deal_price));
            dealHeader.setTypeface(null, Typeface.BOLD);
            dealHeader.setTextSize(18f);
            dealHeader.setPadding(0, 32, 0, 8);
            basketContainer.addView(dealHeader);

            List<BasketItem> itemsInDeal = new ArrayList<>();

            if (deal instanceof MealForOne) {
                itemsInDeal.add(((MealForOne) deal).dog);
            } else if (deal instanceof MealForTwo) {
                itemsInDeal.addAll(((MealForTwo) deal).dogs);
            } else if (deal instanceof DogOfTheDay) {
                itemsInDeal.add(((DogOfTheDay) deal).item);
            } else if (deal instanceof Roulette) {
                itemsInDeal.add(((Roulette) deal).item);
            }


            for (int i = 0; i < itemsInDeal.size(); i++) {
                BasketItem item = itemsInDeal.get(i);
                View itemView = inflater.inflate(R.layout.basket_item, basketContainer, false);

                TextView nameView = itemView.findViewById(R.id.item_name);
                TextView priceView = itemView.findViewById(R.id.item_price);
                TextView quantityView = itemView.findViewById(R.id.text_quantity);
                Button buttonIncrease = itemView.findViewById(R.id.button_increase);
                Button buttonDecrease = itemView.findViewById(R.id.button_decrease);
                ImageView imageView = itemView.findViewById(R.id.item_image);
                TextView customizationsView = itemView.findViewById(R.id.item_customizations);
                ImageButton deleteButton = itemView.findViewById(R.id.button_delete);

                // Name and image

                if (deal instanceof Roulette) {
                    imageView.setImageResource(R.drawable.roulette_checkout_icon);
                    nameView.setText("Roulette");
                } else {
                    nameView.setText(item.baseItem.item_name);
                    String imageName = item.baseItem.item_name.toLowerCase().replace(" ", "");
                    int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
                    imageView.setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);
                }

                // Customizations
                StringBuilder customizations = new StringBuilder();
                if (item.removedBread != null && !item.removedBread.isEmpty()) {
                    customizations.append("No ").append(String.join(", ", item.removedBread)).append(". ");
                }
                if (item.removedDog != null && !item.removedDog.isEmpty()) {
                    customizations.append("No ").append(String.join(", ", item.removedDog)).append(". ");
                }
                if (item.removedCheese != null && !item.removedCheese.isEmpty()) {
                    customizations.append("No ").append(String.join(", ", item.removedCheese)).append(". ");
                }
                if (item.removedSauces != null && !item.removedSauces.isEmpty()) {
                    customizations.append("No ").append(String.join(", ", item.removedSauces)).append(". ");
                }
                if (item.removedToppings != null && !item.removedToppings.isEmpty()) {
                    customizations.append("No ").append(String.join(", ", item.removedToppings)).append(". ");
                }
                if (customizations.length() == 0) {
                    customizations.append("No customizations");
                }

                // Include sides and drink if present (only on MealForOne and MealForTwo)
                if (deal instanceof MealForOne) {
                    MealForOne m1 = (MealForOne) deal;
                    customizations.append("\nSides: ").append(String.join(", ", m1.sides));
                    customizations.append("\nDrink: ").append(m1.drink);
                }

                if (deal instanceof MealForTwo) {
                    MealForTwo m2 = (MealForTwo) deal;
                    if (i == 0) {
                        customizations.append("\nSides: ").append(String.join(", ", m2.sides1));
                        customizations.append("\nDrink: ").append(m2.drinks.get(0));
                    } else if (i == 1 && m2.drinks.size() > 1 && m2.sides2 != null) {
                        customizations.append("\nSides: ").append(String.join(", ", m2.sides2));
                        customizations.append("\nDrink: ").append(m2.drinks.get(1));
                    }
                }

                customizationsView.setText(customizations.toString());

                // No quantity editing for deal items
                priceView.setVisibility(View.GONE);
                quantityView.setVisibility(View.GONE);
                buttonIncrease.setVisibility(View.GONE);
                buttonDecrease.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);

                basketContainer.addView(itemView);
            }
        }

        // Update total price
        updateTotalPrice(basketItems, view);
    }

    private void updateTotalPrice(List<BasketItem> basketItems, View view) {
        double total = 0;
        for (BasketItem item : MainActivity.basket) {
            total += item.price * Math.max(item.quantity, 1);
        }
        for (Deal deal : MainActivity.deals) {
            total += deal.deal_price;
        }
        TextView totalText = view.findViewById(R.id.total_text);
        totalText.setText(String.format("Total: $%.2f", total));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}