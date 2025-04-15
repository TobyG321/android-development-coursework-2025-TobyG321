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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

        // Display USA items by default
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
            itemParams.setMargins(16, 16, 16, 16); // spacing between elements
            itemLayout.setLayoutParams(itemParams);

            TextView title = new TextView(getContext());
            title.setText(item.item_name);
            title.setTypeface(null, Typeface.BOLD);
            title.setTextSize(18f);

            TextView price = new TextView(getContext());
            price.setText("Price: £" + item.price);

            ImageView image = new ImageView(getContext());
            String imageName = item.item_name.toLowerCase().replace(" ", "");
            int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());

            if (imageResId != 0) {
                image.setImageResource(imageResId);
            } else {
                Log.w("IMAGE_NOT_FOUND", "Missing image for: " + item.item_name + " -> fallback used.");
                image.setImageResource(R.drawable.newyork);
            }

            image.setAdjustViewBounds(true);
            image.setLayoutParams(new LinearLayout.LayoutParams(300, 300));

            itemLayout.addView(title);
            itemLayout.addView(price);
            itemLayout.addView(image);

            itemLayout.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(item.item_name);

                StringBuilder message = new StringBuilder();
                message.append("Price: £").append(item.price).append("\n\n");
                if (item.bread != null && !item.bread.isEmpty())
                    message.append("Bread: ").append(item.bread).append("\n");
                if (item.cheese != null && !item.cheese.isEmpty())
                    message.append("Cheese: ").append(item.cheese).append("\n");
                if (item.sauces != null && !item.sauces.isEmpty())
                    message.append("Sauces: ").append(item.sauces).append("\n");
                if (item.toppings != null && !item.toppings.isEmpty())
                    message.append("Toppings: ").append(item.toppings).append("\n");

                builder.setMessage(message.toString());
                builder.setPositiveButton("ADD TO BASKET", (dialog, which) -> {
                    MainActivity.menuItems.add(item);
                    Toast.makeText(getContext(), item.item_name + " added to basket!", Toast.LENGTH_SHORT).show();
                });

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}