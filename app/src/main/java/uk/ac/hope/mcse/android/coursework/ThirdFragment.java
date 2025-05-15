package uk.ac.hope.mcse.android.coursework;

import static uk.ac.hope.mcse.android.coursework.MainActivity.db;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.util.Locale;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentThirdBinding;
import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;

public class ThirdFragment extends Fragment {

    private FragmentThirdBinding binding;
    private int currentRegionIndex = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
            switch (item.region) {
                case "USA": usa.add(item); break;
                case "South": south.add(item); break;
                case "Europe": europe.add(item); break;
            }
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int newIndex = tab.getPosition();
                boolean goingRight = newIndex > currentRegionIndex;
                currentRegionIndex = newIndex;

                String region = tab.getText().toString();
                animateMenuSwap(container, () -> {
                    populateMenu(region, view, usa, south, europe);
                    animateMenuEntry(container, goingRight);
                }, goingRight);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        tabLayout.getTabAt(0).select();
        populateMenu("USA", view, usa, south, europe);
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

    private void populateMenu(String region, View view, List<MenuItems> usa, List<MenuItems> south, List<MenuItems> europe) {
        LinearLayout menuContainer = view.findViewById(R.id.menu_container);
        menuContainer.removeAllViews();

        List<MenuItems> selectedList = region.equals("USA") ? usa
                : region.equals("Latin America") ? south : europe;

        LinearLayout currentRow = null;

        for (int i = 0; i < selectedList.size(); i++) {
            MenuItems item = selectedList.get(i);

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
            String formattedPrice = "£" + String.format(Locale.UK, "%.2f", item.price).replace('.', ',');
            price.setText(formattedPrice);
            price.setTextSize(16f);
            price.setTextColor(Color.parseColor("#5B3000"));
            price.setGravity(Gravity.CENTER_HORIZONTAL);
            card.addView(price);

            card.setOnClickListener(v -> {
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

                nameTextView.setText(item.item_name);
                int popupImageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
                imageView.setImageResource(popupImageResId != 0 ? popupImageResId : R.drawable.newyork);

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
                            .setTitle("Confirm Removals")
                            .setMessage(summary.toString())
                            .setPositiveButton("CONFIRM", (d2, w2) -> {
                                BasketItem newItem = new BasketItem(item, removedBread, removedDog, removedCheese, removedSauces, removedToppings);
                                for(BasketItem basketItem : MainActivity.basket){
                                    if(basketItem.isEqualTo(newItem)){
                                        basketItem.quantity++;
                                        return;
                                    }
                                }
                                MainActivity.basket.add(newItem);
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
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                menuContainer.addView(currentRow);
            }

            currentRow.addView(card);

            if (i == selectedList.size() - 1 && selectedList.size() % 2 != 0) {
                View spacer = new View(getContext());
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                spacerParams.setMargins(24, 24, 24, 24);
                spacer.setLayoutParams(spacerParams);
                currentRow.addView(spacer);
            }
        }
    }

    private void populateGrid(FlexboxLayout gridLayout, String csvData, List<String> removedItems) {
        gridLayout.removeAllViews();
        if (csvData != null && !csvData.trim().isEmpty() && !csvData.trim().equals("N/A")) {
            String[] items = csvData.split(",");
            for (String entry : items) {
                if (!entry.equals(" ")) {
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
