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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentThirdBinding;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;

public class ThirdFragment extends Fragment {

    private FragmentThirdBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentThirdBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.region_tabs);
        LinearLayout container = view.findViewById(R.id.menu_container);

        tabLayout.addTab(tabLayout.newTab().setText("USA"));
        tabLayout.addTab(tabLayout.newTab().setText("Latin America"));
        tabLayout.addTab(tabLayout.newTab().setText("Europe"));

        List<MenuItems> usa = new ArrayList<>();
        List<MenuItems> south = new ArrayList<>();
        List<MenuItems> europe = new ArrayList<>();

        for (MenuItems item : MainActivity.menuItems) {
            if (item.region.equals("USA")) {
                usa.add(item);
            } else if (item.region.equals("South")) {
                south.add(item);
            } else if (item.region.equals("Europe")) {
                europe.add(item);
            }
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String region = tab.getText().toString();
                populateMenu(region, view, usa, south, europe);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        tabLayout.getTabAt(0).select();
        populateMenu("USA", view, usa, south, europe);
    }

    private void populateMenu(String region, View view, List<MenuItems> usa, List<MenuItems> south, List<MenuItems> europe) {
        LinearLayout menuContainer = view.findViewById(R.id.menu_container);
        menuContainer.removeAllViews();

        List<MenuItems> selectedList = region.equals("USA") ? usa
                : region.equals("Latin America") ? south : europe;

        LinearLayout currentRow = null;

        for (int i = 0; i < selectedList.size(); i++) {
            MenuItems item = selectedList.get(i);

            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(24, 24, 24, 24);
            itemLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            itemLayout.setBackgroundResource(R.drawable.item_card_background);

            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            itemParams.setMargins(16, 16, 16, 16);
            itemLayout.setLayoutParams(itemParams);

            TextView title = new TextView(getContext());
            title.setText(item.item_name);
            title.setTypeface(null, Typeface.BOLD);
            title.setTextSize(18f);

            TextView price = new TextView(getContext());
            price.setText("Price: £" + item.price);

            ImageView image = new ImageView(getContext());
            String imageName = item.item_name.toLowerCase().replace(" ", "");
            int imageResIdInitial = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());

            image.setImageResource(imageResIdInitial != 0 ? imageResIdInitial : R.drawable.newyork);
            image.setAdjustViewBounds(true);
            image.setLayoutParams(new LinearLayout.LayoutParams(300, 300));

            itemLayout.addView(title);
            itemLayout.addView(price);
            itemLayout.addView(image);

            itemLayout.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialogView = inflater.inflate(R.layout.selection_popup, null);

                // Removed items per category
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

                nameTextView.setText(item.item_name);

                int imageResId = getResources().getIdentifier(
                        item.item_name.toLowerCase().replace(" ", ""),
                        "drawable", getContext().getPackageName());

                imageView.setImageResource(imageResId != 0 ? imageResId : R.drawable.newyork);

                populateGrid(gridBread, item.bread, removedBread);
                populateGrid(gridDog, item.dog, removedDog);
                populateGrid(gridCheese, item.cheese, removedCheese);
                populateGrid(gridSauces, item.sauces, removedSauces);
                populateGrid(gridToppings, item.toppings, removedToppings);

                builder.setView(dialogView);
                builder.setPositiveButton("ADD TO BASKET", (dialog, which) -> {
                    StringBuilder summary = new StringBuilder("You removed:\n\n");
                    if (!removedBread.isEmpty()) summary.append("Bread: ").append(removedBread).append("\n");
                    if (!removedDog.isEmpty()) summary.append("Dog: ").append(removedDog).append("\n");
                    if (!removedCheese.isEmpty()) summary.append("Cheese: ").append(removedCheese).append("\n");
                    if (!removedSauces.isEmpty()) summary.append("Sauces: ").append(removedSauces).append("\n");
                    if (!removedToppings.isEmpty()) summary.append("Toppings: ").append(removedToppings).append("\n");

                    if (summary.toString().equals("You removed:\n\n")) {
                        summary = new StringBuilder("No customisations made. Add to basket?");
                    }

                    new AlertDialog.Builder(getContext())
                            .setTitle("Confirm Selection")
                            .setMessage(summary.toString())
                            .setPositiveButton("CONFIRM", (d2, w2) -> {
                                MainActivity.menuItems.add(item);
                                Toast.makeText(getContext(), item.item_name + " added to basket!", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("CANCEL", null)
                            .show();
                });

                builder.setNegativeButton("CANCEL", null);
                builder.show();
            });

            if (i % 2 == 0) {
                currentRow = new LinearLayout(getContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                menuContainer.addView(currentRow);
            }

            currentRow.addView(itemLayout);
        }
    }

    private void populateGrid(FlexboxLayout gridLayout, String csvData, List<String> removedItems) {
        gridLayout.removeAllViews();

        if (csvData != null && !csvData.trim().isEmpty() && !csvData.trim().equals("N/A")) {
            String[] items = csvData.split(",");
            for (String entry : items) {
                if (!entry.equals(" ")){
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
