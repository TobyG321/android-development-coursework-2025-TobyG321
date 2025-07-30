package uk.ac.hope.mcse.android.coursework;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import uk.ac.hope.mcse.android.coursework.databinding.ActivityMainBinding;
import uk.ac.hope.mcse.android.coursework.databinding.FragmentForthBinding;
import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;
import uk.ac.hope.mcse.android.coursework.model.deals.MealForOne;
import uk.ac.hope.mcse.android.coursework.model.rewards.FreeDog;
import uk.ac.hope.mcse.android.coursework.model.rewards.FreeMeal;
import uk.ac.hope.mcse.android.coursework.model.rewards.FreeSide;
import uk.ac.hope.mcse.android.coursework.model.rewards.MealUpgrade;

public class ForthFragment extends Fragment {

    private FragmentForthBinding binding;
    private GestureDetector gestureDetector;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentForthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gestureDetector = new GestureDetector(getContext(), new SwipeGestureListener());
        view.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        TextView userPointsDisplay = view.findViewById(R.id.points_tally);
        userPointsDisplay.setText("Points: " + MainActivity.currentUser.points);

        // Free Meal Reward
        LinearLayout container = view.findViewById(R.id.free_meal_container);
        container.setOnClickListener(v -> {
            showMealDialog("Claim Free Meal", new MenuSelectionCallback2() {
                @Override
                public void onHotDogSelected(BasketItem selected, AlertDialog dialog) {

                }

                @Override
                public void onHotDogSelectedWithExtras(BasketItem selected, List<String> sides, String drink, AlertDialog dialog) {
                    MealForOne reward = new MealForOne(
                            selected.baseItem,
                            selected.removedBread,
                            selected.removedDog,
                            selected.removedCheese,
                            selected.removedSauces,
                            selected.removedToppings
                    );
                    reward.sides = sides;
                    reward.drink = drink;

                    int cost = 1000;
                    int currentPoints = MainActivity.currentUser.getPoints();

                    if (currentPoints < cost) {
                        Toast.makeText(getContext(), "Not enough points", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MainActivity.currentUser.points = currentPoints - cost;
                    animatePoints(userPointsDisplay, currentPoints, currentPoints - cost);

                    MainActivity.rewards.add(new FreeMeal(reward));
                    updateFab();
                    Toast.makeText(getContext(), "Free meal reward added!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                }
            });
        });

        // Free Dog Rewards
        container = view.findViewById(R.id.section2_container);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        List<MenuItems> items = MainActivity.menuItems;

        if (MainActivity.menuItems == null || MainActivity.menuItems.isEmpty()) {
            Toast.makeText(getContext(), "Menu not available. Try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            MenuItems item = items.get(i);
            View cardView = inflater.inflate(R.layout.reward_card, container, false);

            TextView nameText = cardView.findViewById(R.id.reward_title);
            AtomicReference<ImageView> imageView = new AtomicReference<>(cardView.findViewById(R.id.reward_image));
            TextView pointsText = cardView.findViewById(R.id.reward_points);

            nameText.setText(item.item_name);
            int imageResId = getResources().getIdentifier(item.item_name.toLowerCase().replace(" ", ""), "drawable", getContext().getPackageName());
            imageView.get().setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);

            int roundedPoints = (int) (Math.round((item.price * 100f) / 50) * 50);
            pointsText.setText("\uD83C\uDF81 " + roundedPoints + "pts");

            // Style margins
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
            if (params == null) {
                params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
            }

            if (i == items.size() - 1) {
                params.setMargins(params.leftMargin, params.topMargin, 0, 0);
            } else {
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, 0);
            }
            cardView.setLayoutParams(params);

            cardView.setOnClickListener(v -> {
                int currentPoints = MainActivity.currentUser.getPoints();

                if (currentPoints < roundedPoints) {
                    Toast.makeText(getContext(), "Not enough points", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show customisation popup
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.selection_popup, null);

                List<String> removedBread = new ArrayList<>();
                List<String> removedDog = new ArrayList<>();
                List<String> removedCheese = new ArrayList<>();
                List<String> removedSauces = new ArrayList<>();
                List<String> removedToppings = new ArrayList<>();

                TextView nameTextView = dialogView.findViewById(R.id.hotdog_name);
                imageView.set(dialogView.findViewById(R.id.hotdog_image));
                FlexboxLayout gridBread = dialogView.findViewById(R.id.grid_bread);
                FlexboxLayout gridDog = dialogView.findViewById(R.id.grid_dog);
                FlexboxLayout gridCheese = dialogView.findViewById(R.id.grid_cheese);
                FlexboxLayout gridSauces = dialogView.findViewById(R.id.grid_sauces);
                FlexboxLayout gridToppings = dialogView.findViewById(R.id.grid_toppings);

                nameTextView.setText(item.item_name);
                int popupImageResId = getResources().getIdentifier(
                        item.item_name.toLowerCase().replace(" ", ""),
                        "drawable",
                        getContext().getPackageName()
                );
                imageView.get().setImageResource(popupImageResId != 0 ? popupImageResId : R.drawable.newyork);

                populateGrid(gridBread, item.bread, removedBread, true, false);
                populateGrid(gridDog, item.dog, removedDog, true, false);
                populateGrid(gridCheese, item.cheese, removedCheese, false, false);
                populateGrid(gridSauces, item.sauces, removedSauces, false, false);
                populateGrid(gridToppings, item.toppings, removedToppings, false, false);

                builder.setView(dialogView);
                builder.setPositiveButton("CONFIRM", (popupDialog, which) -> {
                    BasketItem newItem = new BasketItem(item, removedBread, removedDog, removedCheese, removedSauces, removedToppings);

                    int newPoints = currentPoints - roundedPoints;
                    MainActivity.currentUser.points = newPoints;

                    MainActivity.rewards.add(new FreeDog(newItem, roundedPoints));
                    updateFab();
                    animatePoints(userPointsDisplay, currentPoints, newPoints);
                    Toast.makeText(getContext(), item.item_name + " reward added with customisations!", Toast.LENGTH_SHORT).show();
                });

                builder.setNegativeButton("CANCEL", null);
                builder.show();
            });


            container.addView(cardView);
        }

        // Meal Upgrade Reward
        container = view.findViewById(R.id.meal_upgrade_container);
        container.setOnClickListener(v -> {
            int cost = 400;
            int currentPoints = MainActivity.currentUser.getPoints();

            if (currentPoints < cost) {
                Toast.makeText(getContext(), "Not enough points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get and remove the first item from basket if it exists
            if (MainActivity.basket.isEmpty()) {
                Toast.makeText(getContext(), "You need an item in your basket for a Meal Upgrade.", Toast.LENGTH_SHORT).show();
                return;
            }

            BasketItem basketItem = MainActivity.basket.remove(0);

            // Show popup to select sides & drink
            LayoutInflater inflater1 = LayoutInflater.from(getContext());
            View dialogView = inflater1.inflate(R.layout.extras_popup, null);

            Spinner sideSpinner1 = dialogView.findViewById(R.id.side_spinner_1);
            Spinner sideSpinner2 = dialogView.findViewById(R.id.side_spinner_2);
            Spinner drinkSpinner = dialogView.findViewById(R.id.drink_spinner);

            List<String> sides = List.of("Chips", "3 Wings", "A Cookie", "Nachos");
            List<String> drinks = List.of("Coke", "Coke Zero", "Fanta", "Sprite", "Dr Pepper", "Mezzo Mix", "Water", "Milkshake Choc", "Vanilla", "Strawberry", "Coffee", "Tea");

            ArrayAdapter<String> sideAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, sides);
            ArrayAdapter<String> drinkAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, drinks);

            sideSpinner1.setAdapter(sideAdapter);
            sideSpinner2.setAdapter(sideAdapter);
            drinkSpinner.setAdapter(drinkAdapter);

            new AlertDialog.Builder(getContext())
                    .setTitle("Select Sides & Drink")
                    .setView(dialogView)
                    .setPositiveButton("CONFIRM", (dialog, which) -> {
                        String side1 = sideSpinner1.getSelectedItem().toString();
                        String side2 = sideSpinner2.getSelectedItem().toString();
                        String drink = drinkSpinner.getSelectedItem().toString();

                        if (side1.equals(side2)) {
                            Toast.makeText(getContext(), "Please select two different sides", Toast.LENGTH_SHORT).show();
                            // Add the item back to the basket something went wrong
                            MainActivity.basket.add(0, basketItem);
                            return;
                        }

                        List<String> selectedSides = new ArrayList<>();
                        selectedSides.add(side1);
                        selectedSides.add(side2);

                        MainActivity.currentUser.points = currentPoints - cost;
                        MainActivity.rewards.add(new MealUpgrade(basketItem, selectedSides, drink));
                        updateFab();
                        animatePoints(userPointsDisplay, currentPoints, currentPoints - cost);
                        Toast.makeText(getContext(), "Meal Upgrade applied!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("CANCEL", (dialog, which) -> {
                        // Add the item back if user cancels
                        MainActivity.basket.add(0, basketItem);
                    })
                    .show();
        });

        // Small rewards
        container = view.findViewById(R.id.section3_container);
        List<String> sideImageNames = Arrays.asList("Fries", "Nachos", "Wings", "Drink");

        for (int j = 0; j < sideImageNames.size(); j++) {
            String name = sideImageNames.get(j);
            String imageName = name.toLowerCase();
            View smallCard = inflater.inflate(R.layout.reward_card_small, container, false);

            TextView nameText = smallCard.findViewById(R.id.reward_title_small);
            nameText.setText(name);
            ImageView imageView = smallCard.findViewById(R.id.reward_image_small);

            int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
            imageView.setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);

            TextView pointsText = smallCard.findViewById(R.id.reward_points);
            pointsText.setText("\uD83C\uDF81 250pts");

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            );
            if (j < sideImageNames.size() - 1) {
                int marginPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
                params.setMargins(0, 0, marginPx, 0);
            }
            smallCard.setLayoutParams(params);

            smallCard.setOnClickListener(v -> {
                int cost = 250;
                int currentPoints = MainActivity.currentUser.getPoints();

                if (currentPoints < cost) {
                    Toast.makeText(getContext(), "Not enough points", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(getContext())
                        .setTitle("Claim Side Reward")
                        .setMessage("Add a free " + name + " to your basket?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            int newPoints = currentPoints - cost;
                            MainActivity.currentUser.points = newPoints;

                            if (name.equalsIgnoreCase("drink")) {
                                // Prompt for drink selection

                                String[] drinks = {"Coke", "Coke Zero", "Fanta", "Sprite", "Dr Pepper", "Mezzo Mix", "Water", "Milkshake Choc", "Vanilla", "Strawberry", "Coffee", "Tea"};

                                new AlertDialog.Builder(getContext())
                                        .setTitle("Choose your free drink")
                                        .setItems(drinks, (d, selected) -> {
                                            String selectedDrink = drinks[selected];
                                            MainActivity.rewards.add(new FreeSide(selectedDrink));
                                            updateFab();
                                            animatePoints(userPointsDisplay, currentPoints, newPoints);
                                            Toast.makeText(getContext(), selectedDrink + " added!", Toast.LENGTH_SHORT).show();
                                        })
                                        .show();

                            } else {
                                // Standard side
                                MainActivity.rewards.add(new FreeSide(name));
                                updateFab();
                                animatePoints(userPointsDisplay, currentPoints, newPoints);
                                Toast.makeText(getContext(), name + " added!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            container.addView(smallCard);
        }
    }

    private void animatePoints(TextView pointsView, int from, int to) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(600);
        animator.addUpdateListener(animation ->
                pointsView.setText("Points: " + animation.getAnimatedValue()));
        animator.start();
    }

    private void showSideAndDrinkDialog(BasketItem hotdogItem, MenuSelectionCallback2 callback) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.extras_popup, null);

        Spinner sideSpinner1 = dialogView.findViewById(R.id.side_spinner_1);
        Spinner sideSpinner2 = dialogView.findViewById(R.id.side_spinner_2);
        Spinner drinkSpinner = dialogView.findViewById(R.id.drink_spinner);

        List<String> sides = List.of("Chips", "3 Wings", "A Cookie", "Nachos");
        List<String> drinks = List.of("Coke", "Coke Zero", "Fanta", "Sprite", "Dr Pepper", "Mezzo Mix", "Water", "Milkshake Choc", "Vanilla", "Strawberry", "Coffee", "Tea");

        sideSpinner1.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, sides));
        sideSpinner2.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, sides));
        drinkSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, drinks));

        new AlertDialog.Builder(getContext())
                .setTitle("Choose Sides & Drink")
                .setView(dialogView)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String side1 = sideSpinner1.getSelectedItem().toString();
                    String side2 = sideSpinner2.getSelectedItem().toString();
                    String drink = drinkSpinner.getSelectedItem().toString();

                    if (side1.equals(side2)) {
                        Toast.makeText(getContext(), "Please select two different sides", Toast.LENGTH_SHORT).show();
                    } else {
                        List<String> selectedSides = new ArrayList<>();
                        selectedSides.add(side1);
                        selectedSides.add(side2);
                        callback.onHotDogSelectedWithExtras(hotdogItem, selectedSides, drink, null);
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffY) > Math.abs(diffX) &&
                    Math.abs(diffY) > SWIPE_THRESHOLD &&
                    Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) onSwipeDown();
                return true;
            }
            return false;
        }

        private void onSwipeDown() {
            NavHostFragment.findNavController(ForthFragment.this)
                    .navigate(R.id.action_ForthFragment_to_SecondFragment);
        }
    }

    private void populateGrid(FlexboxLayout gridLayout, String csvData, List<String> removedItems, boolean singleSelect, boolean showExtras) {
        gridLayout.removeAllViews();
        if (csvData == null || csvData.trim().isEmpty() || csvData.trim().equals("N/A")) return;

        String[] items = csvData.split(",");

        for (String entry : items) {
            if (!entry.trim().isEmpty()) {
                String label = entry.trim();
                String displayLabel = label;
                String lower = label.toLowerCase(Locale.ROOT);

                // Apply price suffixes for Custom Dog extras
                if (showExtras) {
                    if (lower.contains("ft frank") || lower.contains("bratwurst")) {
                        displayLabel += " (+£0.50)";
                    } else if (lower.contains("short")) {
                        displayLabel += " (-£1.00)";
                    } else if (lower.contains("chorizo")) {
                        displayLabel += " (+£1.00)";
                    } else if (lower.contains("chili")) {
                        displayLabel += " (+£0.50)";
                    } else if (lower.contains("avocado") || lower.contains("guac")) {
                        displayLabel += " (+£1.00)";
                    } else if (lower.contains("shrimp salad")) {
                        displayLabel += " (+£0.50)";
                    } else if (gridLayout.getId() == R.id.grid_cheese) {
                        displayLabel += " (+£0.50)";
                    }
                }

                Button button = new Button(getContext());
                button.setText(displayLabel);
                button.setTextSize(12);
                button.setAllCaps(true);
                button.setPadding(5, 0, 5, 0);

                boolean enabled = !removedItems.contains(label);
                button.setBackgroundResource(enabled ? R.drawable.rounded_container : R.drawable.rounded_container_disabled);
                button.setTag(enabled ? "enabled" : "disabled");

                button.setOnClickListener(v -> {
                    if (singleSelect) {
                        // Deselect all other buttons
                        for (int i = 0; i < gridLayout.getChildCount(); i++) {
                            View child = gridLayout.getChildAt(i);
                            if (child instanceof Button) {
                                Button b = (Button) child;
                                b.setBackgroundResource(R.drawable.rounded_container_disabled);
                                b.setTag("disabled");
                                if (!removedItems.contains(b.getText().toString().replaceAll(" \\(.*\\)$", ""))) {
                                    removedItems.add(b.getText().toString().replaceAll(" \\(.*\\)$", ""));
                                }
                            }
                        }
                        // Enable this one
                        button.setBackgroundResource(R.drawable.rounded_container);
                        button.setTag("enabled");
                        removedItems.remove(label);
                    } else {
                        String state = (String) button.getTag();
                        if ("enabled".equals(state)) {
                            button.setBackgroundResource(R.drawable.rounded_container_disabled);
                            button.setTag("disabled");
                            removedItems.add(label);
                        } else {
                            button.setBackgroundResource(R.drawable.rounded_container);
                            button.setTag("enabled");
                            removedItems.remove(label);
                        }
                    }
                });

                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(8, 8, 8, 8);
                gridLayout.addView(button, params);
            }
        }
    }

    private void showMealDialog(String title, MenuSelectionCallback2 callback) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View popupView = inflater.inflate(R.layout.fragment_third, null);

        TabLayout tabLayout = popupView.findViewById(R.id.region_tabs);
        LinearLayout container = popupView.findViewById(R.id.menu_container);

        tabLayout.addTab(tabLayout.newTab().setText("USA"));
        tabLayout.addTab(tabLayout.newTab().setText("Latin America"));
        tabLayout.addTab(tabLayout.newTab().setText("Europe"));

        List<MenuItems> usa = new ArrayList<>();
        List<MenuItems> south = new ArrayList<>();
        List<MenuItems> europe = new ArrayList<>();

        for (MenuItems item : MainActivity.menuItems) {
            switch (item.region) {
                case "USA": usa.add(item); break;
                case "South": south.add(item); break;
                case "Europe": europe.add(item); break;
            }
        }

        // Add Custom Dog to USA list
        MenuItems customDog = new MenuItems();
        customDog.item_name = "Custom Dog";
        customDog.region = "USA";
        customDog.price = 5.00;
        customDog.bread = "";
        customDog.dog = "";
        customDog.cheese = "";
        customDog.sauces = "";
        customDog.toppings = "";
        usa.add(0, customDog);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(popupView)
                .setNegativeButton("CLOSE", null);

        AlertDialog menuDialog = builder.create();

        final int[] previousTabIndex = {0};

        populateSelectableMenu(popupView, usa, south, europe, menuDialog, callback);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int newTabIndex = tab.getPosition();
                boolean toLeft = newTabIndex > previousTabIndex[0];
                previousTabIndex[0] = newTabIndex;

                LinearLayout menuContainer = popupView.findViewById(R.id.menu_container);

                animateMenuSwap(menuContainer, () -> {
                    populateSelectableMenu(popupView, usa, south, europe, menuDialog, callback);
                    animateMenuEntry(menuContainer, toLeft);
                }, toLeft);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        menuDialog.show();
    }

    private void populateSelectableMenu(View rootView, List<MenuItems> usa, List<MenuItems> south, List<MenuItems> europe, AlertDialog dialog, MenuSelectionCallback2 callback) {
        LinearLayout menuContainer = rootView.findViewById(R.id.menu_container);
        menuContainer.removeAllViews();

        List<MenuItems> list;
        TabLayout tabLayout = rootView.findViewById(R.id.region_tabs);
        String region = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
        list = region.equals("Latin America") ? south : region.equals("Europe") ? europe : usa;

        LinearLayout currentRow = null;

        for (int i = 0; i < list.size(); i++) {
            MenuItems item = list.get(i);

            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.rounded_yellow_card);
            card.setElevation(8f);
            card.setPadding(0, 32, 0, 32);
            card.setGravity(Gravity.CENTER_HORIZONTAL);
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            cardParams.setMargins(24, 24, 24, 24);
            card.setLayoutParams(cardParams);

            TextView name = new TextView(getContext());
            name.setText(item.item_name);
            name.setTypeface(null, Typeface.BOLD);
            name.setTextSize(18f);
            name.setMaxLines(1);
            name.setEllipsize(TextUtils.TruncateAt.END);
            name.setTextColor(Color.parseColor("#5B3000"));
            name.setGravity(Gravity.CENTER_HORIZONTAL);
            card.addView(name);

            LinearLayout innerLayout = new LinearLayout(getContext());
            innerLayout.setOrientation(LinearLayout.VERTICAL);
            innerLayout.setPadding(32, 32, 32, 32);
            innerLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            card.addView(innerLayout);

            ImageView image = new ImageView(getContext());
            image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            image.setClickable(false);
            image.setFocusable(false);
            image.setPadding(20, 0, 20, 0);

            String imageName = item.item_name.toLowerCase().replace(" ", "");
            int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
            image.setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);

            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(1, 1);
            imageParams.gravity = Gravity.CENTER_HORIZONTAL;
            image.setLayoutParams(imageParams);
            innerLayout.addView(image);

            ViewTreeObserver.OnGlobalLayoutListener[] listenerHolder = new ViewTreeObserver.OnGlobalLayoutListener[1];
            listenerHolder[0] = () -> {
                int cardWidth = card.getWidth();
                if (cardWidth > 0) {
                    int imageSize = (int) (cardWidth * 0.9);
                    LinearLayout.LayoutParams adjustedParams = new LinearLayout.LayoutParams(imageSize, imageSize);
                    adjustedParams.gravity = Gravity.CENTER_HORIZONTAL;
                    adjustedParams.topMargin = 16;
                    adjustedParams.bottomMargin = 16;
                    image.setLayoutParams(adjustedParams);
                    card.getViewTreeObserver().removeOnGlobalLayoutListener(listenerHolder[0]);
                }
            };
            card.getViewTreeObserver().addOnGlobalLayoutListener(listenerHolder[0]);

            TextView price = new TextView(getContext());
            String formattedPrice = "£" + String.format(Locale.UK, "%.2f", item.price);
            price.setText(formattedPrice);
            price.setTextSize(16f);
            price.setTextColor(Color.parseColor("#5B3000"));
            price.setGravity(Gravity.CENTER_HORIZONTAL);
            card.addView(price);

            card.setOnClickListener(v -> {
                if (item.item_name.equals("Custom Dog")) {
                    showCustomDogPopup(callback, dialog);
                } else {
                    showSelectionDialog(item, callback, dialog);
                }
            });

            if (i % 2 == 0) {
                currentRow = new LinearLayout(getContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                menuContainer.addView(currentRow);
            }

            currentRow.addView(card);

            if (i == list.size() - 1 && list.size() % 2 != 0) {
                View spacer = new View(getContext());
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                spacerParams.setMargins(24, 24, 24, 24);
                spacer.setLayoutParams(spacerParams);
                currentRow.addView(spacer);
            }
        }
    }

    private void animateMenuSwap(View view, Runnable onComplete, boolean toLeft) {
        int anim = toLeft ? R.anim.slide_out_left : R.anim.slide_out_right;
        Animation slideOut = AnimationUtils.loadAnimation(getContext(), anim);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {
                onComplete.run();
            }
        });
        view.startAnimation(slideOut);
    }

    private void animateMenuEntry(View view, boolean fromRight) {
        int anim = fromRight ? R.anim.slide_in_right : R.anim.slide_in_left;
        Animation slideIn = AnimationUtils.loadAnimation(getContext(), anim);
        view.startAnimation(slideIn);
    }

    private void showCustomDogPopup(MenuSelectionCallback2 callback, AlertDialog menuDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.selection_popup, null);

        TextView nameTextView = dialogView.findViewById(R.id.hotdog_name);
        ImageView imageView = dialogView.findViewById(R.id.hotdog_image);
        FlexboxLayout gridBread = dialogView.findViewById(R.id.grid_bread);
        FlexboxLayout gridDog = dialogView.findViewById(R.id.grid_dog);
        FlexboxLayout gridCheese = dialogView.findViewById(R.id.grid_cheese);
        FlexboxLayout gridSauces = dialogView.findViewById(R.id.grid_sauces);
        FlexboxLayout gridToppings = dialogView.findViewById(R.id.grid_toppings);

        nameTextView.setText("Custom Dog");
        imageView.setImageResource(R.drawable.newyork);

        // Gather all options from existing menu items
        List<String> allBread = new ArrayList<>();
        List<String> allDog = new ArrayList<>();
        List<String> allCheese = new ArrayList<>();
        List<String> allSauces = new ArrayList<>();
        List<String> allToppings = new ArrayList<>();

        for (MenuItems item : MainActivity.menuItems) {
            addUnique(allBread, item.bread);
            addUnique(allDog, item.dog);
            addUnique(allCheese, item.cheese);
            addUnique(allSauces, item.sauces);
            addUnique(allToppings, item.toppings);
        }

        List<String> removedBread = new ArrayList<>(allBread);
        List<String> removedDog = new ArrayList<>(allDog);
        List<String> removedCheese = new ArrayList<>(allCheese);
        List<String> removedSauces = new ArrayList<>(allSauces);
        List<String> removedToppings = new ArrayList<>(allToppings);

        removedBread.removeIf(b -> b.equalsIgnoreCase("Brioche"));
        removedDog.removeIf(d -> d.equalsIgnoreCase("Frankfurter"));

        populateGrid(gridBread, String.join(",", allBread), removedBread, true, false);
        populateGrid(gridDog, String.join(",", allDog), removedDog, true, true);
        populateGrid(gridCheese, String.join(",", allCheese), removedCheese, false, true);
        populateGrid(gridSauces, String.join(",", allSauces), removedSauces, false, false);
        populateGrid(gridToppings, String.join(",", allToppings), removedToppings, false, true);

        builder.setView(dialogView);
        builder.setPositiveButton("NEXT", (dialog, which) -> {
            if (allBread.size() - removedBread.size() != 1) {
                Toast.makeText(getContext(), "Please select exactly ONE bread option.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (allDog.size() - removedDog.size() != 1) {
                Toast.makeText(getContext(), "Please select exactly ONE dog option.", Toast.LENGTH_SHORT).show();
                return;
            }

            MenuItems custom = new MenuItems();
            custom.item_name = "Custom Dog";
            custom.region = "USA";
            custom.bread = String.join(",", allBread);
            custom.dog = String.join(",", allDog);
            custom.cheese = String.join(",", allCheese);
            custom.sauces = String.join(",", allSauces);
            custom.toppings = String.join(",", allToppings);

            double price = 5.00;

            List<String> selectedDog = new ArrayList<>(allDog);
            selectedDog.removeAll(removedDog);
            List<String> selectedCheese = new ArrayList<>(allCheese);
            selectedCheese.removeAll(removedCheese);
            List<String> selectedToppings = new ArrayList<>(allToppings);
            selectedToppings.removeAll(removedToppings);

            for (String dog : selectedDog) {
                String d = dog.toLowerCase(Locale.ROOT);
                if (d.contains("ft frank") || d.contains("bratwurst")) price += 0.5;
                if (d.contains("short")) price -= 1.0;
                if (d.contains("chorizo")) price += 1.0;
            }

            price += selectedCheese.size() * 0.5;

            for (String topping : selectedToppings) {
                String t = topping.toLowerCase(Locale.ROOT);
                if (t.contains("chili")) price += 0.5;
                if (t.contains("avocado") || t.contains("guac")) price += 1.0;
                if (t.contains("shrimp salad")) price += 0.5;
            }

            custom.price = Math.round(price * 100.0) / 100.0;

            BasketItem item = new BasketItem(custom, removedBread, removedDog, removedCheese, removedSauces, removedToppings);

            // ✅ Use meal callback instead of adding to basket directly
            showSideAndDrinkDialog(item, callback, menuDialog);
        });

        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }
    private void addUnique(List<String> list, String csv) {
        if (csv == null || csv.equals("N/A")) return;
        for (String item : csv.split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty() && !list.contains(trimmed)) {
                list.add(trimmed);
            }
        }
    }
    private void showSideAndDrinkDialog(BasketItem hotdogItem, MenuSelectionCallback2 callback, AlertDialog menuDialog) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.extras_popup, null);

        Spinner sideSpinner1 = dialogView.findViewById(R.id.side_spinner_1);
        Spinner sideSpinner2 = dialogView.findViewById(R.id.side_spinner_2);
        Spinner drinkSpinner = dialogView.findViewById(R.id.drink_spinner);

        List<String> sides = List.of("Chips", "3 Wings", "A Cookie", "Nachos");
        List<String> drinks = List.of("Coke", "Coke Zero", "Fanta", "Sprite", "Dr Pepper", "Mezzo Mix", "Water", "Milkshake Choc", "Vanilla", "Strawberry", "Coffee", "Tea");

        ArrayAdapter<String> sideAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, sides);
        ArrayAdapter<String> drinkAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, drinks);

        sideSpinner1.setAdapter(sideAdapter);
        sideSpinner2.setAdapter(sideAdapter);
        drinkSpinner.setAdapter(drinkAdapter);

        new AlertDialog.Builder(getContext())
                .setTitle("Choose Sides & Drink")
                .setView(dialogView)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String side1 = sideSpinner1.getSelectedItem().toString();
                    String side2 = sideSpinner2.getSelectedItem().toString();
                    String drink = drinkSpinner.getSelectedItem().toString();

                    if (side1.equals(side2)) {
                        Toast.makeText(getContext(), "Please select two different sides", Toast.LENGTH_SHORT).show();
                    } else {
                        List<String> selectedSides = new ArrayList<>();
                        selectedSides.add(side1);
                        selectedSides.add(side2);
                        callback.onHotDogSelectedWithExtras(hotdogItem, selectedSides, drink, menuDialog);
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void showSelectionDialog(MenuItems item, MenuSelectionCallback2 callback, AlertDialog menuDialog) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.selection_popup, null);

        List<String> removedBread = new ArrayList<>();
        List<String> removedDog = new ArrayList<>();
        List<String> removedCheese = new ArrayList<>();
        List<String> removedSauces = new ArrayList<>();
        List<String> removedToppings = new ArrayList<>();

        boolean customDog = item.item_name.equals("Custom Dog");

        ((TextView) dialogView.findViewById(R.id.hotdog_name)).setText(item.item_name);
        int imageResId = getResources().getIdentifier(item.item_name.toLowerCase().replace(" ", ""), "drawable", getContext().getPackageName());
        ((ImageView) dialogView.findViewById(R.id.hotdog_image)).setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);

        populateGrid(dialogView.findViewById(R.id.grid_bread), item.bread, removedBread, true, false);
        populateGrid(dialogView.findViewById(R.id.grid_dog), item.dog, removedDog, true, customDog);
        populateGrid(dialogView.findViewById(R.id.grid_cheese), item.cheese, removedCheese, false, customDog);
        populateGrid(dialogView.findViewById(R.id.grid_sauces), item.sauces, removedSauces, false, false);
        populateGrid(dialogView.findViewById(R.id.grid_toppings), item.toppings, removedToppings, false, customDog);

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("ADD", (dialog, which) -> {
                    BasketItem built = new BasketItem(item, removedBread, removedDog, removedCheese, removedSauces, removedToppings);
                    showSideAndDrinkDialog(built, callback, menuDialog);
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void updateFab(){
        FloatingActionButton fab = MainActivity.binding.fab;
        fab.setImageResource(R.drawable.cart_fab);
        fab.setOnClickListener(v1 ->
                NavHostFragment.findNavController(ForthFragment.this)
                        .navigate(R.id.action_checkout)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
