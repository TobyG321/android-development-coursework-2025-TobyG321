package uk.ac.hope.mcse.android.coursework;

import static uk.ac.hope.mcse.android.coursework.MainActivity.db;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

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

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FifthFragment extends Fragment {

    private FragmentFifthBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFifthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
            Deal deal = new DogOfTheDay(featuredDog);

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


        binding.mealDeal.setOnClickListener(v -> showMealDialog("Meal Deal", new MenuSelectionCallback() {
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

            showMealDialog("Meal for Two - First Dog", new MenuSelectionCallback() {
                @Override
                public void onHotDogSelected(BasketItem selected, AlertDialog dialog) {}

                @Override
                public void onHotDogSelectedWithExtras(BasketItem selected, List<String> sides, String drink, AlertDialog dialog) {
                    first[0] = selected;
                    sides1[0] = sides;
                    drink1[0] = drink;

                    dialog.dismiss();

                    showMealDialog("Meal for Two - Second Dog", new MenuSelectionCallback() {
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

    private void showMealDialog(String title, MenuSelectionCallback callback) {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(popupView)
                .setNegativeButton("CLOSE", null);

        AlertDialog menuDialog = builder.create();

        populateSelectableMenu(popupView, usa, south, europe, menuDialog, callback);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                populateSelectableMenu(popupView, usa, south, europe, menuDialog, callback);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        menuDialog.show();
    }

    private void populateSelectableMenu(View rootView, List<MenuItems> usa, List<MenuItems> south, List<MenuItems> europe, AlertDialog dialog, MenuSelectionCallback callback) {
        LinearLayout menuContainer = rootView.findViewById(R.id.menu_container);
        menuContainer.removeAllViews();

        List<MenuItems> list = usa;
        TabLayout tabLayout = rootView.findViewById(R.id.region_tabs);
        String region = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
        if (region.equals("Latin America")) list = south;
        else if (region.equals("Europe")) list = europe;

        LinearLayout row = null;
        for (int i = 0; i < list.size(); i++) {
            MenuItems item = list.get(i);

            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(24, 24, 24, 24);
            itemLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            itemLayout.setBackgroundResource(R.drawable.item_card_background);

            itemLayout.setOnClickListener(v -> showSelectionDialog(item, callback, dialog));

            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(0, 0, 1f);
            itemParams.setMargins(16, 16, 16, 16);
            itemParams.height = getResources().getDisplayMetrics().widthPixels / 2 - 32;
            itemLayout.setLayoutParams(itemParams);

            TextView title = new TextView(getContext());
            title.setText(item.item_name);
            title.setTypeface(null, Typeface.BOLD);
            title.setTextSize(18f);

            TextView price = new TextView(getContext());
            price.setText("Price: £" + item.price);

            ImageView image = new ImageView(getContext());
            int imageResId = getResources().getIdentifier(item.item_name.toLowerCase().replace(" ", ""), "drawable", getContext().getPackageName());
            image.setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);
            image.setAdjustViewBounds(true);
            image.setLayoutParams(new LinearLayout.LayoutParams(300, 300));

            itemLayout.addView(title);
            itemLayout.addView(price);
            itemLayout.addView(image);

            if (i % 2 == 0) {
                row = new LinearLayout(getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                menuContainer.addView(row);
            }
            row.addView(itemLayout);
        }
    }

    private void showSelectionDialog(MenuItems item, MenuSelectionCallback callback, AlertDialog menuDialog) {
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

        populateGrid(dialogView.findViewById(R.id.grid_bread), item.bread, removedBread);
        populateGrid(dialogView.findViewById(R.id.grid_dog), item.dog, removedDog);
        populateGrid(dialogView.findViewById(R.id.grid_cheese), item.cheese, removedCheese);
        populateGrid(dialogView.findViewById(R.id.grid_sauces), item.sauces, removedSauces);
        populateGrid(dialogView.findViewById(R.id.grid_toppings), item.toppings, removedToppings);

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("ADD", (dialog, which) -> {
                    BasketItem built = new BasketItem(item, removedBread, removedDog, removedCheese, removedSauces, removedToppings);
                    showSideAndDrinkDialog(built, callback, menuDialog);
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void populateGrid(FlexboxLayout gridLayout, String csvData, List<String> removedItems) {
        gridLayout.removeAllViews();

        if (csvData != null && !csvData.trim().isEmpty() && !csvData.trim().equals("N/A")) {
            String[] items = csvData.split(",");
            for (String entry : items) {
                if (!entry.trim().isEmpty()) {
                    Button button = new Button(getContext());
                    button.setText(entry.trim());
                    button.setTextSize(12);
                    button.setAllCaps(true);
                    button.setBackgroundResource(R.drawable.rounded_container);
                    button.setTag("enabled");

                    button.setOnClickListener(v -> {
                        String state = (String) button.getTag();
                        String label = button.getText().toString();

                        if ("enabled".equals(state)) {
                            button.setBackgroundResource(R.drawable.rounded_container_disabled);
                            button.setTag("disabled");
                            removedItems.add(label);
                        } else {
                            button.setBackgroundResource(R.drawable.rounded_container);
                            button.setTag("enabled");
                            removedItems.remove(label);
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
        } else {
            Button placeholder = new Button(getContext());
            placeholder.setText("None");
            placeholder.setEnabled(false);
            placeholder.setTextSize(12);
            placeholder.setAllCaps(true);
            placeholder.setBackgroundResource(R.drawable.rounded_container);

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            gridLayout.addView(placeholder, params);
        }
    }

    private void showSideAndDrinkDialog(BasketItem hotdogItem, MenuSelectionCallback callback, AlertDialog menuDialog) {
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

interface MenuSelectionCallback {
    void onHotDogSelected(BasketItem selected, AlertDialog menuDialog);
    void onHotDogSelectedWithExtras(BasketItem selected, List<String> sides, String drink, AlertDialog menuDialog);
}
