package uk.ac.hope.mcse.android.coursework;

import static uk.ac.hope.mcse.android.coursework.MainActivity.db;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFifthBinding;
import uk.ac.hope.mcse.android.coursework.databinding.FragmentSecondBinding;
import uk.ac.hope.mcse.android.coursework.databinding.FragmentSixthBinding;
import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;
import uk.ac.hope.mcse.android.coursework.model.UserDao;
import uk.ac.hope.mcse.android.coursework.model.deals.Deal;
import uk.ac.hope.mcse.android.coursework.model.deals.DogOfTheDay;
import uk.ac.hope.mcse.android.coursework.model.deals.MealForOne;
import uk.ac.hope.mcse.android.coursework.model.deals.MealForTwo;
import uk.ac.hope.mcse.android.coursework.model.deals.Roulette;
import uk.ac.hope.mcse.android.coursework.model.rewards.FreeDog;
import uk.ac.hope.mcse.android.coursework.model.rewards.FreeMeal;
import uk.ac.hope.mcse.android.coursework.model.rewards.FreeSide;
import uk.ac.hope.mcse.android.coursework.model.rewards.MealUpgrade;
import uk.ac.hope.mcse.android.coursework.model.rewards.Reward;

public class SixthFragment extends Fragment {

    private FragmentSixthBinding binding;

    private double finalPrice;

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
        List<BasketItem> basketItems = MainActivity.basket;
        updateTotalPrice(basketItems, view);

        binding.placeOrderButton.setOnClickListener(v -> {
            // Check if order contains anything
            if (MainActivity.basket.isEmpty() && MainActivity.deals.isEmpty() && MainActivity.rewards.isEmpty()) {
                Toast.makeText(requireContext(), "Your order is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show confirmation dialog
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Order")
                    .setMessage("Are you sure you've got everything?")
                    .setPositiveButton("Yes", (dialog, which) -> placeOrder())
                    .setNegativeButton("No", null)
                    .show();
        });

        TextView priceText = view.findViewById(R.id.total_text);

        LinearLayout basketContainer = view.findViewById(R.id.basket_items_container);
        basketItems = MainActivity.basket;

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

            if (item.baseItem.item_name.equalsIgnoreCase("Custom Dog")) {
                customizations.append("Selections:\n");

                addSelections(customizations, "Bread", item.baseItem.bread, item.removedBread);
                addSelections(customizations, "Dog", item.baseItem.dog, item.removedDog);
                addSelections(customizations, "Cheese", item.baseItem.cheese, item.removedCheese);
                addSelections(customizations, "Sauces", item.baseItem.sauces, item.removedSauces);
                addSelections(customizations, "Toppings", item.baseItem.toppings, item.removedToppings);
            } else {
                if (!item.removedBread.isEmpty())
                    customizations.append("No ").append(String.join(", ", item.removedBread)).append(". ");
                if (!item.removedDog.isEmpty())
                    customizations.append("No ").append(String.join(", ", item.removedDog)).append(". ");
                if (!item.removedCheese.isEmpty())
                    customizations.append("No ").append(String.join(", ", item.removedCheese)).append(". ");
                if (!item.removedSauces.isEmpty())
                    customizations.append("No ").append(String.join(", ", item.removedSauces)).append(". ");
                if (!item.removedToppings.isEmpty())
                    customizations.append("No ").append(String.join(", ", item.removedToppings)).append(". ");
            }

            if (customizations.length() == 0) customizations.append("No customizations");

            customizationsView.setText(customizations.toString());


            // Set quantity and price
            item.quantity = Math.max(item.quantity, 1); // Default to at least 1
            quantityView.setText(String.valueOf(item.quantity));
            priceView.setText(String.format("$%.2f", item.price * item.quantity));

            // Listeners to update quantity and price
            List<BasketItem> finalBasketItems = basketItems;
            buttonIncrease.setOnClickListener(v -> {
                item.quantity++;
                quantityView.setText(String.valueOf(item.quantity));
                priceView.setText(String.format("$%.2f", item.price * item.quantity));
                updateTotalPrice(finalBasketItems, view);
            });

            List<BasketItem> finalBasketItems1 = basketItems;
            buttonDecrease.setOnClickListener(v -> {
                if (item.quantity > 1) {
                    item.quantity--;
                    quantityView.setText(String.valueOf(item.quantity));
                    priceView.setText(String.format("$%.2f", item.price * item.quantity));
                    updateTotalPrice(finalBasketItems1, view);
                }
            });

            ImageButton deleteButton = itemView.findViewById(R.id.button_delete);

            List<BasketItem> finalBasketItems2 = basketItems;
            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Remove Item")
                        .setMessage("Are you sure you want to remove this item from the basket?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Remove from basket list
                            finalBasketItems2.remove(item);

                            // Remove the view from the container
                            basketContainer.removeView(itemView);

                            // Update total
                            updateTotalPrice(finalBasketItems2, view);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });


            basketContainer.addView(itemView);
        }


        List<Deal> deals = MainActivity.deals;
        for (Deal deal : deals) {
            // Track views associated with this deal
            List<View> dealViews = new ArrayList<>();

            // Add a deal header
            TextView dealHeader = new TextView(getContext());
            dealHeader.setText(deal.deal_name + " - $" + String.format("%.2f", deal.deal_price));
            dealHeader.setTypeface(null, Typeface.BOLD);
            dealHeader.setTextSize(18f);
            dealHeader.setPadding(0, 32, 0, 8);
            basketContainer.addView(dealHeader);
            dealViews.add(dealHeader);  // Track this header

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
                View itemView = inflater.inflate(R.layout.basket_deal, basketContainer, false);
                dealViews.add(itemView);  // Track this view

                TextView nameView = itemView.findViewById(R.id.item_name);
                ImageView imageView = itemView.findViewById(R.id.item_image);
                TextView customizationsView = itemView.findViewById(R.id.item_customizations);
                ImageButton deleteButton = itemView.findViewById(R.id.button_delete);

                if (deal instanceof Roulette) {
                    imageView.setImageResource(R.drawable.roulette_checkout_icon);
                    nameView.setText("Roulette");
                } else {
                    nameView.setText(item.baseItem.item_name);
                    String imageName = item.baseItem.item_name.toLowerCase().replace(" ", "");
                    int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
                    imageView.setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);
                }

                StringBuilder customizations = new StringBuilder();

                if (item.baseItem.item_name.equalsIgnoreCase("Custom Dog")) {
                    customizations.append("Selections:\n");

                    addSelections(customizations, "Bread", item.baseItem.bread, item.removedBread);
                    addSelections(customizations, "Dog", item.baseItem.dog, item.removedDog);
                    addSelections(customizations, "Cheese", item.baseItem.cheese, item.removedCheese);
                    addSelections(customizations, "Sauces", item.baseItem.sauces, item.removedSauces);
                    addSelections(customizations, "Toppings", item.baseItem.toppings, item.removedToppings);
                } else {
                    if (item.removedBread != null && !item.removedBread.isEmpty())
                        customizations.append("No ").append(String.join(", ", item.removedBread)).append(". ");
                    if (item.removedDog != null && !item.removedDog.isEmpty())
                        customizations.append("No ").append(String.join(", ", item.removedDog)).append(". ");
                    if (item.removedCheese != null && !item.removedCheese.isEmpty())
                        customizations.append("No ").append(String.join(", ", item.removedCheese)).append(". ");
                    if (item.removedSauces != null && !item.removedSauces.isEmpty())
                        customizations.append("No ").append(String.join(", ", item.removedSauces)).append(". ");
                    if (item.removedToppings != null && !item.removedToppings.isEmpty())
                        customizations.append("No ").append(String.join(", ", item.removedToppings)).append(". ");
                    if (customizations.length() == 0)
                        customizations.append("No customizations");
                }


                if (deal instanceof MealForOne) {
                    MealForOne m1 = (MealForOne) deal;
                    customizations.append("\nSides: ").append(String.join(", ", m1.sides));
                    customizations.append("\nDrink: ").append(m1.drink);
                } else if (deal instanceof MealForTwo) {
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

                // Enable and handle delete
                deleteButton.setVisibility(View.VISIBLE);
                List<BasketItem> finalBasketItems3 = basketItems;
                deleteButton.setOnClickListener(v -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Remove Deal")
                            .setMessage("Are you sure you want to remove this deal?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                deals.remove(deal); // Remove from deal list
                                for (View dv : dealViews) {
                                    basketContainer.removeView(dv); // Remove all deal views
                                }
                                updateTotalPrice(finalBasketItems3, view);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                basketContainer.addView(itemView);
            }
        }

        List<Reward> rewards = MainActivity.rewards;
        for (Reward reward : rewards) {

            Log.d("Rewards", "Reward: " + reward.reward_name);
            // Header Text
            TextView rewardHeader = new TextView(getContext());
            rewardHeader.setText(reward.reward_name + " - FREE");
            rewardHeader.setTypeface(null, Typeface.BOLD);
            rewardHeader.setTextSize(18f);
            rewardHeader.setPadding(0, 32, 0, 8);
            basketContainer.addView(rewardHeader);

            View itemView = inflater.inflate(R.layout.basket_deal, basketContainer, false);

            TextView nameView = itemView.findViewById(R.id.item_name);
            TextView customizationsView = itemView.findViewById(R.id.item_customizations);
            ImageView imageView = itemView.findViewById(R.id.item_image);
            ImageButton deleteButton = itemView.findViewById(R.id.button_delete);

            // Fill in content based on reward type
            if (reward instanceof FreeDog) {
                FreeDog freeDog = (FreeDog) reward;
                BasketItem dogItem = freeDog.dog;

                nameView.setText(dogItem.baseItem.item_name);

                // Image
                String imageName = dogItem.baseItem.item_name.toLowerCase().replace(" ", "");
                int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
                imageView.setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);

                // Customisation summary
                StringBuilder custom = new StringBuilder();

                if (dogItem.baseItem.item_name.equalsIgnoreCase("Custom Dog")) {
                    custom.append("Selections:\n");
                    addSelections(custom, "Bread", dogItem.baseItem.bread, dogItem.removedBread);
                    addSelections(custom, "Dog", dogItem.baseItem.dog, dogItem.removedDog);
                    addSelections(custom, "Cheese", dogItem.baseItem.cheese, dogItem.removedCheese);
                    addSelections(custom, "Sauces", dogItem.baseItem.sauces, dogItem.removedSauces);
                    addSelections(custom, "Toppings", dogItem.baseItem.toppings, dogItem.removedToppings);
                } else {
                    boolean hasCustom = false;
                    if (dogItem.removedBread != null && !dogItem.removedBread.isEmpty()) {
                        custom.append("No ").append(String.join(", ", dogItem.removedBread)).append(". ");
                        hasCustom = true;
                    }
                    if (dogItem.removedDog != null && !dogItem.removedDog.isEmpty()) {
                        custom.append("No ").append(String.join(", ", dogItem.removedDog)).append(". ");
                        hasCustom = true;
                    }
                    if (dogItem.removedCheese != null && !dogItem.removedCheese.isEmpty()) {
                        custom.append("No ").append(String.join(", ", dogItem.removedCheese)).append(". ");
                        hasCustom = true;
                    }
                    if (dogItem.removedSauces != null && !dogItem.removedSauces.isEmpty()) {
                        custom.append("No ").append(String.join(", ", dogItem.removedSauces)).append(". ");
                        hasCustom = true;
                    }
                    if (dogItem.removedToppings != null && !dogItem.removedToppings.isEmpty()) {
                        custom.append("No ").append(String.join(", ", dogItem.removedToppings)).append(". ");
                        hasCustom = true;
                    }
                    if (!hasCustom) {
                        custom.append("No customizations");
                    }
                }

                customizationsView.setText(custom.toString());
            }
            else if (reward instanceof FreeSide) {
                nameView.setText("Free Side");
                customizationsView.setText("Get a free side of " + ((FreeSide) reward).side + "!");
                imageView.setImageResource(R.drawable.fries); // Example icon
            } else if (reward instanceof FreeMeal) {
                FreeMeal freeMeal = (FreeMeal) reward;

                if (freeMeal.meal != null && freeMeal.meal.dog != null && freeMeal.meal.dog.baseItem != null) {
                    String dogName = freeMeal.meal.dog.baseItem.item_name;
                    nameView.setText(dogName);

                    String imageName = dogName.toLowerCase().replace(" ", "");
                    int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
                    imageView.setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);

                    StringBuilder custom = new StringBuilder();

                    BasketItem dogItem = freeMeal.meal.dog;

                    if (dogItem.baseItem.item_name.equalsIgnoreCase("Custom Dog")) {
                        custom.append("Selections:\n");
                        addSelections(custom, "Bread", dogItem.baseItem.bread, dogItem.removedBread);
                        addSelections(custom, "Dog", dogItem.baseItem.dog, dogItem.removedDog);
                        addSelections(custom, "Cheese", dogItem.baseItem.cheese, dogItem.removedCheese);
                        addSelections(custom, "Sauces", dogItem.baseItem.sauces, dogItem.removedSauces);
                        addSelections(custom, "Toppings", dogItem.baseItem.toppings, dogItem.removedToppings);
                    } else {
                        boolean hasCustom = false;
                        if (dogItem.removedBread != null && !dogItem.removedBread.isEmpty()) {
                            custom.append("No ").append(String.join(", ", dogItem.removedBread)).append(". ");
                            hasCustom = true;
                        }
                        if (dogItem.removedDog != null && !dogItem.removedDog.isEmpty()) {
                            custom.append("No ").append(String.join(", ", dogItem.removedDog)).append(". ");
                            hasCustom = true;
                        }
                        if (dogItem.removedCheese != null && !dogItem.removedCheese.isEmpty()) {
                            custom.append("No ").append(String.join(", ", dogItem.removedCheese)).append(". ");
                            hasCustom = true;
                        }
                        if (dogItem.removedSauces != null && !dogItem.removedSauces.isEmpty()) {
                            custom.append("No ").append(String.join(", ", dogItem.removedSauces)).append(". ");
                            hasCustom = true;
                        }
                        if (dogItem.removedToppings != null && !dogItem.removedToppings.isEmpty()) {
                            custom.append("No ").append(String.join(", ", dogItem.removedToppings)).append(". ");
                            hasCustom = true;
                        }
                        if (!hasCustom) {
                            custom.append("No customizations");
                        }
                    }

                    if (freeMeal.meal.sides != null && !freeMeal.meal.sides.isEmpty()) {
                        custom.append("\nSides: ").append(String.join(", ", freeMeal.meal.sides));
                    }

                    if (freeMeal.meal.drink != null && !freeMeal.meal.drink.isEmpty()) {
                        custom.append("\nDrink: ").append(freeMeal.meal.drink);
                    }

                    customizationsView.setText(custom.toString());

                } else {
                    // Fallback display if meal is missing or incomplete
                    nameView.setText("Free Meal");
                    imageView.setImageResource(R.drawable.newyork);
                    customizationsView.setText("Enjoy a free meal!");
                }
            }
            else if (reward instanceof MealUpgrade) {MealUpgrade upgrade = (MealUpgrade) reward;
                MealForOne m1 = upgrade.meal;

                if (m1 != null && m1.dog != null && m1.dog.baseItem != null) {
                    String dogName = m1.dog.baseItem.item_name;
                    double price = m1.dog.baseItem.price;

                    rewardHeader.setText(String.format("Free Meal Upgrade (%s) - $%.2f", dogName, price));

                    String imageName = dogName.toLowerCase().replace(" ", "");
                    int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
                    imageView.setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);

                    StringBuilder custom = new StringBuilder();

                    // === Removed ingredients display ===
                    BasketItem dogItem = m1.dog;

                    boolean hasCustom = false;

                    if (dogItem.removedBread != null && !dogItem.removedBread.isEmpty()) {
                        custom.append("No ").append(String.join(", ", dogItem.removedBread)).append(". ");
                        hasCustom = true;
                    }
                    if (dogItem.removedDog != null && !dogItem.removedDog.isEmpty()) {
                        custom.append("No ").append(String.join(", ", dogItem.removedDog)).append(". ");
                        hasCustom = true;
                    }
                    if (dogItem.removedCheese != null && !dogItem.removedCheese.isEmpty()) {
                        custom.append("No ").append(String.join(", ", dogItem.removedCheese)).append(". ");
                        hasCustom = true;
                    }
                    if (dogItem.removedSauces != null && !dogItem.removedSauces.isEmpty()) {
                        custom.append("No ").append(String.join(", ", dogItem.removedSauces)).append(". ");
                        hasCustom = true;
                    }
                    if (dogItem.removedToppings != null && !dogItem.removedToppings.isEmpty()) {
                        custom.append("No ").append(String.join(", ", dogItem.removedToppings)).append(". ");
                        hasCustom = true;
                    }

                    if (!hasCustom) {
                        custom.append("No customizations");
                    }

                    // Append sides + drink
                    if (m1.sides != null && !m1.sides.isEmpty()) {
                        custom.append("\nSides: ").append(String.join(", ", m1.sides));
                    }
                    if (m1.drink != null && !m1.drink.isEmpty()) {
                        custom.append("\nDrink: ").append(m1.drink);
                    }

                    customizationsView.setText(custom.toString());
                    nameView.setText(dogName);
                }
                else {
                    rewardHeader.setText("Free Meal Upgrade - Pending");
                    nameView.setText("Pending");

                    // Set the customizationsView as clickable text
                    customizationsView.setText("Tap here to add a dog");
                    customizationsView.setPaintFlags(customizationsView.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
                    customizationsView.setClickable(true);

                    // Navigate to menu fragment on click
                    customizationsView.setOnClickListener(v -> {
                        NavHostFragment.findNavController(SixthFragment.this)
                                .navigate(R.id.action_menu);  // Make sure this ID is correct in nav_graph.xml
                    });

                    imageView.setImageResource(R.drawable.newyork);
                }

            }

        // Delete logic
            List<BasketItem> finalBasketItems4 = basketItems;
            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Remove Reward")
                        .setMessage("Are you sure you want to remove this reward?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            rewards.remove(reward);
                            basketContainer.removeView(rewardHeader);
                            basketContainer.removeView(itemView);
                            updateTotalPrice(finalBasketItems4, view); // Rewards are free, but good to refresh UI
                            MainActivity.currentUser.points += (int) reward.points_cost;
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            basketContainer.addView(itemView);
        }

        // Update total price
        updateTotalPrice(basketItems, view);
    }

    private void placeOrder() {
        Dialog loadingDialog = new Dialog(requireContext());
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView statusText = loadingDialog.findViewById(R.id.status_info);
        if (statusText != null) {
            statusText.setText("Placing order!");
        }

        loadingDialog.show();

        new Thread(() -> {
            try {
                // Date and time
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date now = new Date();
                String date = dateFormat.format(now);
                String time = timeFormat.format(now);

                // Build order string
                StringBuilder itemQuantityBuilder = new StringBuilder();
                if (!MainActivity.basket.isEmpty()) {
                    itemQuantityBuilder.append("Items:\n");
                    boolean hasItems = false;
                    for (BasketItem item : MainActivity.basket) {
                        if (hasItems) itemQuantityBuilder.append(", ");
                        itemQuantityBuilder.append(item.baseItem.item_name).append(" x").append(item.quantity);
                        hasItems = true;
                    }
                    itemQuantityBuilder.append("\n");
                }

                if (!MainActivity.deals.isEmpty()) {
                    itemQuantityBuilder.append("Deals:\n");
                    for (Deal deal : MainActivity.deals) {
                        itemQuantityBuilder.append(deal.deal_name);

                        if (deal instanceof DogOfTheDay) {
                            DogOfTheDay d = (DogOfTheDay) deal;
                            itemQuantityBuilder.append("\n    Dog: ").append(d.item.baseItem.item_name +"\n");
                            appendCustomizations(itemQuantityBuilder, d.item, "    ");
                        } else if (deal instanceof Roulette) {
                            Roulette r = (Roulette) deal;
                            itemQuantityBuilder.append("\n    Dog: ").append(r.item.baseItem.item_name +"\n");
                            if (r.spicy) itemQuantityBuilder.append(" (Spicy)");
                            appendCustomizations(itemQuantityBuilder, r.item, "    ");
                        } else if (deal instanceof MealForOne) {
                            MealForOne m1 = (MealForOne) deal;
                            itemQuantityBuilder.append("\n    Dog: ").append(m1.dog.baseItem.item_name +"\n");
                            appendCustomizations(itemQuantityBuilder, m1.dog, "    ");
                            itemQuantityBuilder.append("    Sides: ").append(String.join(", ", m1.sides)).append("\n");
                            itemQuantityBuilder.append("    Drink: ").append(m1.drink);
                        } else if (deal instanceof MealForTwo) {
                            MealForTwo m2 = (MealForTwo) deal;
                            itemQuantityBuilder.append("\n    Dog 1: ").append(m2.dogs.get(0).baseItem.item_name +"\n");
                            appendCustomizations(itemQuantityBuilder, m2.dogs.get(0), "    ");
                            itemQuantityBuilder.append("    Sides: ").append(String.join(", ", m2.sides1)).append("\n");
                            itemQuantityBuilder.append("    Drink: ").append(m2.drinks.get(0)).append("\n");

                            if (m2.dogs.size() > 1) {
                                itemQuantityBuilder.append("    Dog 2: ").append(m2.dogs.get(1).baseItem.item_name).append("\n");
                                appendCustomizations(itemQuantityBuilder, m2.dogs.get(1), "    ");
                                if (m2.sides2 != null)
                                    itemQuantityBuilder.append("    Sides: ").append(String.join(", ", m2.sides2)).append("\n");
                                if (m2.drinks.size() > 1)
                                    itemQuantityBuilder.append("    Drink: ").append(m2.drinks.get(1)).append("\n");
                            }
                        }
                        itemQuantityBuilder.append("\n");
                    }
                }

                if (!MainActivity.rewards.isEmpty()) {
                    itemQuantityBuilder.append("Rewards:\n");

                    for (Reward reward : MainActivity.rewards) {
                        if (reward instanceof FreeSide) {
                            itemQuantityBuilder.append("  • Free Side: ")
                                    .append(((FreeSide) reward).side)
                                    .append("\n");

                        } else if (reward instanceof FreeDog) {
                            FreeDog freeDog = (FreeDog) reward;
                            BasketItem dog = freeDog.dog;

                            itemQuantityBuilder.append("  • Free Dogg\n");
                            itemQuantityBuilder.append("    Dog: ").append(dog.baseItem.item_name).append("\n");
                            appendCustomizations(itemQuantityBuilder, dog, "    ");

                        } else if (reward instanceof MealUpgrade) {
                            MealUpgrade upgrade = (MealUpgrade) reward;
                            itemQuantityBuilder.append("  • Meal Upgrade\n");

                            if (upgrade.meal != null && upgrade.meal.dog != null) {
                                BasketItem dog = upgrade.meal.dog;

                                itemQuantityBuilder.append("    Dog: ").append(dog.baseItem.item_name).append("\n");
                                appendCustomizations(itemQuantityBuilder, dog, "    ");

                                if (upgrade.meal.sides != null && !upgrade.meal.sides.isEmpty()) {
                                    itemQuantityBuilder.append("    Sides: ").append(String.join(", ", upgrade.meal.sides)).append("\n");
                                }
                                if (upgrade.meal.drink != null && !upgrade.meal.drink.isEmpty()) {
                                    itemQuantityBuilder.append("    Drink: ").append(upgrade.meal.drink).append("\n");
                                }
                            } else {
                                itemQuantityBuilder.append("    (Pending selection)\n");
                            }

                        } else if (reward instanceof FreeMeal) {
                            FreeMeal freeMeal = (FreeMeal) reward;
                            itemQuantityBuilder.append("  • Free Meal\n");

                            if (freeMeal.meal != null && freeMeal.meal.dog != null) {
                                BasketItem dog = freeMeal.meal.dog;

                                itemQuantityBuilder.append("    Dog: ").append(dog.baseItem.item_name).append("\n");
                                appendCustomizations(itemQuantityBuilder, dog, "    ");

                                if (freeMeal.meal.sides != null && !freeMeal.meal.sides.isEmpty()) {
                                    itemQuantityBuilder.append("    Sides: ").append(String.join(", ", freeMeal.meal.sides)).append("\n");
                                }
                                if (freeMeal.meal.drink != null && !freeMeal.meal.drink.isEmpty()) {
                                    itemQuantityBuilder.append("    Drink: ").append(freeMeal.meal.drink).append("\n");
                                }
                            } else {
                                itemQuantityBuilder.append("    (Pending selection)\n");
                            }
                        }
                    }
                }


                String itemQuantity = itemQuantityBuilder.toString();

                // Calculate cost
                double total = 0;
                for (BasketItem item : MainActivity.basket) {
                    total += item.price * Math.max(item.quantity, 1);
                }
                for (Deal deal : MainActivity.deals) {
                    total += deal.deal_price;
                }
                for (Reward reward : MainActivity.rewards) {
                    if (reward instanceof MealUpgrade) {
                        MealUpgrade upgrade = (MealUpgrade) reward;
                        if (upgrade.meal != null && upgrade.meal.dog != null && upgrade.meal.dog.baseItem != null) {
                            total += upgrade.meal.deal_price;
                        }
                    }
                }

                double finalPrice = (MainActivity.currentUser.stamps == 5) ? total * 0.8 : total;

                // Send to Google Sheets
                URL url = new URL("https://script.google.com/macros/s/AKfycbyb9TK2THnGlqFAEh5phsGwyt6vmGP0ZWDYmvH70yNTdikIIxBtRGO4O4QdAenleV3N/exec");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("route", "order");
                json.put("date", date);
                json.put("time", time);
                json.put("itemQuantity", itemQuantity);
                json.put("cost", String.format("£%.2f", finalPrice));
                json.put("email", MainActivity.currentUser.email);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();

                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();

                    if (responseCode == 200) {
                        // Update user stats
                        if (MainActivity.currentUser.stamps == 5) {
                            MainActivity.currentUser.stamps = 1;
                        } else {
                            MainActivity.currentUser.stamps += 1;
                        }

                        for (Reward reward : MainActivity.rewards) {
                            MainActivity.currentUser.points -= (int) reward.points_cost;
                        }

                        MainActivity.currentUser.points += ((int) finalPrice) * 10;

                        Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_LONG).show();

                        // Clear UI
                        MainActivity.basket.clear();
                        MainActivity.deals.clear();
                        MainActivity.rewards.clear();

                        LinearLayout basketContainer = requireView().findViewById(R.id.basket_items_container);
                        basketContainer.removeAllViews();
                        updateTotalPrice(MainActivity.basket, requireView());

                        // Update user to sheet
                        new Thread(() -> {
                            try {
                                URL updateUrl = new URL("https://script.google.com/macros/s/AKfycbxEC6tlQwVkjiq0UV5Gk7BFVzTiOGeOfk3XE93mnkgaBuygDLrzFmR8rL-vr6D05i1IvQ/exec?route=updateUser");
                                HttpURLConnection updateConn = (HttpURLConnection) updateUrl.openConnection();
                                updateConn.setRequestMethod("POST");
                                updateConn.setRequestProperty("Content-Type", "application/json");
                                updateConn.setDoOutput(true);

                                JSONObject updateJson = new JSONObject();
                                updateJson.put("email", MainActivity.currentUser.email);
                                updateJson.put("stamps", MainActivity.currentUser.stamps);
                                updateJson.put("points", MainActivity.currentUser.points);

                                try (OutputStream os = updateConn.getOutputStream()) {
                                    byte[] input = updateJson.toString().getBytes("utf-8");
                                    os.write(input, 0, input.length);
                                }

                                int updateResponse = updateConn.getResponseCode();
                                Log.d("UpdateUser", "Update response code: " + updateResponse);

                            } catch (Exception ex) {
                                Log.e("UpdateUser", "Error: " + ex.getMessage());
                            }
                        }).start();

                    } else {
                        Toast.makeText(requireContext(), "Failed to place order. Try again.", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Error placing order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void addSelections(StringBuilder builder, String category, String csvData, List<String> removed) {
        if (csvData == null || csvData.trim().isEmpty()) return;

        String[] all = csvData.split(",");
        List<String> selected = new ArrayList<>();
        for (String item : all) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty() && (removed == null || !removed.contains(trimmed))) {
                selected.add(trimmed);
            }
        }

        if (!selected.isEmpty()) {
            builder.append(category).append(": ").append(String.join(", ", selected));
            if (!category.equalsIgnoreCase("Toppings")) {
                builder.append("\n");
            }
        }
    }


    private void updateTotalPrice(List<BasketItem> basketItems, View view) {
        finalPrice = 0;

        // Calculate basket item total
        for (BasketItem item : MainActivity.basket) {
            finalPrice += item.price * Math.max(item.quantity, 1);
        }

        // Add deals
        for (Deal deal : MainActivity.deals) {
            finalPrice += deal.deal_price;
        }

        for (Reward reward : MainActivity.rewards) {
            if (reward instanceof MealUpgrade) {
                MealUpgrade upgrade = (MealUpgrade) reward;
                if (upgrade.meal != null && upgrade.meal.dog != null && upgrade.meal.dog.baseItem != null) {
                    finalPrice += upgrade.meal.deal_price;
                }
            }
        }

        TextView totalText = view.findViewById(R.id.total_text);

        // Apply stamp discount if stamps == 5
        if (MainActivity.currentUser.stamps == 5) {
            double discountedTotal = finalPrice * 0.8;

            // Format strings
            String original = String.format("$%.2f", finalPrice);
            String discount = String.format("$%.2f", discountedTotal);

            SpannableString styledText = new SpannableString(original + " " + discount);

            // Strikethrough original price
            styledText.setSpan(new StrikethroughSpan(), 0, original.length(), 0);
            styledText.setSpan(new ForegroundColorSpan(Color.RED), 0, original.length(), 0);

            // Discounted price in black
            styledText.setSpan(new ForegroundColorSpan(Color.BLACK), original.length() + 1, styledText.length(), 0);

            totalText.setText(styledText);
        } else {
            // No discount, just show total normally
            totalText.setText(String.format("Total: $%.2f", finalPrice));
        }
    }

    private void appendCustomizations(StringBuilder builder, BasketItem dog, String indent) {
        if (dog.baseItem.item_name.equalsIgnoreCase("Custom Dog")) {
            builder.append(indent).append("Selections:\n");

            appendSelectedInline(builder, indent + "  ", "Bread", dog.baseItem.bread, dog.removedBread);
            appendSelectedInline(builder, indent + "  ", "Dog", dog.baseItem.dog, dog.removedDog);
            appendSelectedInline(builder, indent + "  ", "Cheese", dog.baseItem.cheese, dog.removedCheese);
            appendSelectedInline(builder, indent + "  ", "Sauces", dog.baseItem.sauces, dog.removedSauces);
            appendSelectedInline(builder, indent + "  ", "Toppings", dog.baseItem.toppings, dog.removedToppings);
        } else {
            List<String> removed = new ArrayList<>();
            if (dog.removedBread != null) removed.addAll(dog.removedBread);
            if (dog.removedDog != null) removed.addAll(dog.removedDog);
            if (dog.removedCheese != null) removed.addAll(dog.removedCheese);
            if (dog.removedSauces != null) removed.addAll(dog.removedSauces);
            if (dog.removedToppings != null) removed.addAll(dog.removedToppings);

            if (!removed.isEmpty()) {
                builder.append(indent).append("No: ").append(String.join(", ", removed)).append("\n");
            }
        }

        // ✅ Ensure line break between customization and next deal part
        if (!builder.toString().endsWith("\n")) {
            builder.append("\n");
        }
    }

    private void appendSelectedInline(StringBuilder builder, String indent, String label, String csv, List<String> removed) {
        if (csv == null || csv.trim().isEmpty()) return;

        String[] all = csv.split(",");
        List<String> selected = new ArrayList<>();
        for (String item : all) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty() && (removed == null || !removed.contains(trimmed))) {
                selected.add(trimmed);
            }
        }

        if (!selected.isEmpty()) {
            builder.append(indent).append(label).append(": ").append(String.join(", ", selected)).append("\n");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}