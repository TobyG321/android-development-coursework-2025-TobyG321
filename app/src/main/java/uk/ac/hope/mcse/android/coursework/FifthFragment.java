package uk.ac.hope.mcse.android.coursework;

import static uk.ac.hope.mcse.android.coursework.MainActivity.db;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.tabs.TabLayout;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFifthBinding;
import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;
import uk.ac.hope.mcse.android.coursework.model.deals.Deal;
import uk.ac.hope.mcse.android.coursework.model.deals.DogOfTheDay;
import uk.ac.hope.mcse.android.coursework.model.deals.MealForOne;
import uk.ac.hope.mcse.android.coursework.model.deals.MealForTwo;
import uk.ac.hope.mcse.android.coursework.model.deals.Roulette;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FifthFragment extends Fragment {

    private FragmentFifthBinding binding;

    private GestureDetector gestureDetector;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFifthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gestureDetector = new GestureDetector(getContext(), new SwipeGestureListener());
        View root = binding.getRoot();
        root.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayout, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(v.getPaddingLeft(), topInset, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        MenuItems featuredDog = ((MainActivity) getActivity()).getDogOfTheDay();
        if (featuredDog != null) {
            String imageName = featuredDog.item_name.toLowerCase().replace(" ", "");
            int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
            ImageView dogImage = view.findViewById(R.id.featured_dog);
            dogImage.setImageResource(imageResId);
        }

        binding.dogOfTheDay.setOnClickListener(v -> {
            if (featuredDog == null) {
                Toast.makeText(getContext(), "No Dog of the Day found!", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.selection_popup, null);

            List<String> removedBread = new ArrayList<>();
            List<String> removedDog = new ArrayList<>();
            List<String> removedCheese = new ArrayList<>();
            List<String> removedSauces = new ArrayList<>();
            List<String> removedToppings = new ArrayList<>();

            TextView nameTextView = dialogView.findViewById(R.id.hotdog_name);
            ImageView imageView = dialogView.findViewById(R.id.hotdog_image);
            FlexboxLayout gridBread = dialogView.findViewById(R.id.grid_bread);
            FlexboxLayout gridDog = dialogView.findViewById(R.id.grid_dog);
            FlexboxLayout gridCheese = dialogView.findViewById(R.id.grid_cheese);
            FlexboxLayout gridSauces = dialogView.findViewById(R.id.grid_sauces);
            FlexboxLayout gridToppings = dialogView.findViewById(R.id.grid_toppings);

            nameTextView.setText(featuredDog.item_name);
            int imageResId = getResources().getIdentifier(
                    featuredDog.item_name.toLowerCase().replace(" ", ""),
                    "drawable",
                    getContext().getPackageName()
            );
            imageView.setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);

            populateGrid(gridBread, featuredDog.bread, removedBread, true, false);
            populateGrid(gridDog, featuredDog.dog, removedDog, true, true);
            populateGrid(gridCheese, featuredDog.cheese, removedCheese, false, true);
            populateGrid(gridSauces, featuredDog.sauces, removedSauces, false, false);
            populateGrid(gridToppings, featuredDog.toppings, removedToppings, false, true);

            builder.setView(dialogView);
            builder.setPositiveButton("CONFIRM", (popupDialog, which) -> {
                BasketItem customDog = new BasketItem(
                        featuredDog,
                        removedBread,
                        removedDog,
                        removedCheese,
                        removedSauces,
                        removedToppings
                );

                Deal deal = new DogOfTheDay(customDog); // ✅ Use constructor that accepts BasketItem
                MainActivity.deals.add(deal);

                Toast.makeText(getContext(), "Dog of the Day added with customisations!", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("CANCEL", null);
            builder.show();
        });



        binding.roulette.setOnClickListener(v -> {
            MenuItems randomDog = db.menuDao().getRandomDog();
            Deal deal = new Roulette(randomDog, false);

            new AlertDialog.Builder(requireContext())
                    .setTitle(deal.deal_name)
                    .setMessage(deal.deal_description + "\n\nPrice: £" + String.format("%.2f", deal.deal_price))
                    .setPositiveButton("CONFIRM", (dialog, which) -> {
                        MainActivity.deals.add(deal);
                        Log.d("Deal", "Deal added: " + deal.deal_name);
                        Toast.makeText(getContext(), "Deal added!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        });


        binding.spicyRoulette.setOnClickListener(v -> {
            MenuItems randomDog = db.menuDao().getRandomDog();
            Deal deal = new Roulette(randomDog, true);

            new AlertDialog.Builder(requireContext())
                    .setTitle(deal.deal_name)
                    .setMessage(deal.deal_description + "\n\nPrice: £" + String.format("%.2f", deal.deal_price))
                    .setPositiveButton("CONFIRM", (dialog, which) -> {
                        MainActivity.deals.add(deal);
                        Log.d("Deal", "Deal added: " + deal.deal_name);
                        Toast.makeText(getContext(), "Deal added!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        });


        binding.mealDeal.setOnClickListener(v -> showMealDialog("Meal Deal", new MenuSelectionCallback2() {
            @Override
            public void onHotDogSelected(BasketItem selected, AlertDialog dialog) {
                // Do nothing – we always use the version with extras
            }

            @Override
            public void onHotDogSelectedWithExtras(BasketItem selected, List<String> sides, String drink, AlertDialog dialog) {
                MealForOne deal = new MealForOne(
                        selected.baseItem,
                        selected.removedBread,
                        selected.removedDog,
                        selected.removedCheese,
                        selected.removedSauces,
                        selected.removedToppings
                );
                deal.sides = sides;
                deal.drink = drink;
                MainActivity.deals.add(deal);
                Toast.makeText(getContext(), "Meal for One added!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }));


        binding.mealForTwo.setOnClickListener(v -> {
            final BasketItem[] first = new BasketItem[1];
            final List<String>[] sides1 = new List[1];
            final String[] drink1 = new String[1];

            showMealDialog("Meal for Two - First Dog", new MenuSelectionCallback2() {
                @Override
                public void onHotDogSelected(BasketItem selected, AlertDialog dialog) {}

                @Override
                public void onHotDogSelectedWithExtras(BasketItem selected, List<String> sides, String drink, AlertDialog dialog) {
                    first[0] = selected;
                    sides1[0] = sides;
                    drink1[0] = drink;

                    dialog.dismiss();

                    showMealDialog("Meal for Two - Second Dog", new MenuSelectionCallback2() {
                        @Override
                        public void onHotDogSelected(BasketItem selected2, AlertDialog dialog2) {}

                        @Override
                        public void onHotDogSelectedWithExtras(BasketItem selected2, List<String> sides2, String drink2, AlertDialog dialog2) {
                            MealForTwo deal = new MealForTwo(first[0], selected2);
                            deal.sides1 = sides1[0];
                            deal.sides2 = sides2;
                            deal.drinks = new ArrayList<>();
                            deal.drinks.add(drink1[0]);
                            deal.drinks.add(drink2);

                            MainActivity.deals.add(deal);
                            Toast.makeText(getContext(), "Meal for Two added!", Toast.LENGTH_SHORT).show();
                            dialog2.dismiss();
                        }
                    });
                }
            });
        });


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

        // ✅ Add Custom Dog to USA list
        MenuItems customDog = new MenuItems();
        customDog.item_name = "Custom Dog";
        customDog.region = "USA";
        customDog.price = 5.00;
        customDog.bread = "";   // Leave blank to handle dynamically
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
                    showCustomDogPopup(callback, dialog); // ✅ pass callback
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

    private void showSelectionDialog(MenuItems item, MenuSelectionCallback2 callback, AlertDialog menuDialog) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.selection_popup, null);

        List<String> removedBread = new ArrayList<>();
        List<String> removedDog = new ArrayList<>();
        List<String> removedCheese = new ArrayList<>();
        List<String> removedSauces = new ArrayList<>();
        List<String> removedToppings = new ArrayList<>();

        ((TextView) dialogView.findViewById(R.id.hotdog_name)).setText(item.item_name);
        int imageResId = getResources().getIdentifier(item.item_name.toLowerCase().replace(" ", ""), "drawable", getContext().getPackageName());
        ((ImageView) dialogView.findViewById(R.id.hotdog_image)).setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);

        populateGrid(dialogView.findViewById(R.id.grid_bread), item.bread, removedBread, true, false);
        populateGrid(dialogView.findViewById(R.id.grid_dog), item.dog, removedDog, true, true);
        populateGrid(dialogView.findViewById(R.id.grid_cheese), item.cheese, removedCheese, false, true);
        populateGrid(dialogView.findViewById(R.id.grid_sauces), item.sauces, removedSauces, false, false);
        populateGrid(dialogView.findViewById(R.id.grid_toppings), item.toppings, removedToppings, false, true);

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("ADD", (dialog, which) -> {
                    BasketItem built = new BasketItem(item, removedBread, removedDog, removedCheese, removedSauces, removedToppings);
                    showSideAndDrinkDialog(built, callback, menuDialog);
                })
                .setNegativeButton("CANCEL", null)
                .show();
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

                if (showExtras) {
                    if (lower.contains("ft frank") || lower.contains("bratwurst")) {
                        displayLabel += " (+£0.50)";
                    } else if (lower.contains("chorizo")) {
                        displayLabel += " (+£1.00)";
                    } else if (lower.contains("short")) {
                        displayLabel += " (-£1.00)";
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

                boolean enabled = !removedItems.contains(label);
                button.setBackgroundResource(enabled ? R.drawable.rounded_container : R.drawable.rounded_container_disabled);
                button.setTag(enabled ? "enabled" : "disabled");

                button.setOnClickListener(v -> {
                    if (singleSelect) {
                        for (int i = 0; i < gridLayout.getChildCount(); i++) {
                            View child = gridLayout.getChildAt(i);
                            if (child instanceof Button) {
                                Button b = (Button) child;
                                b.setBackgroundResource(R.drawable.rounded_container_disabled);
                                b.setTag("disabled");
                                String raw = b.getText().toString().replaceAll(" \\(.*\\)$", "");
                                if (!removedItems.contains(raw)) {
                                    removedItems.add(raw);
                                }
                            }
                        }
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


    private Button createSelectableButton(String label) {
        Button button = new Button(getContext());
        button.setText(label);
        button.setTextSize(12);
        button.setAllCaps(true);
        button.setBackgroundResource(R.drawable.rounded_container);
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
        return button;
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

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX < 0) {
                    onSwipeLeft();
                    return true;
                }
            }

            return false;
        }

        private void onSwipeLeft() {
            NavHostFragment.findNavController(FifthFragment.this)
                    .navigate(R.id.action_FifthFragment_to_SecondFragment); // Use a global action if needed
        }
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
        imageView.setImageResource(R.drawable.newyork); // Default image

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

interface MenuSelectionCallback2 {
    void onHotDogSelected(BasketItem selected, AlertDialog menuDialog);
    void onHotDogSelectedWithExtras(BasketItem selected, List<String> sides, String drink, AlertDialog menuDialog);
}
