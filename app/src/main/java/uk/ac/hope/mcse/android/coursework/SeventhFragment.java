package uk.ac.hope.mcse.android.coursework;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentSeventhBinding;
import uk.ac.hope.mcse.android.coursework.model.PastOrder;

public class SeventhFragment extends Fragment {

    private FragmentSeventhBinding binding;
    private GestureDetector gestureDetector;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSeventhBinding.inflate(inflater, container, false);
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

        // Check if orders cached
        if (!MainActivity.pastOrders.isEmpty()) {
            displayOrders(MainActivity.pastOrders);
            return;
        }

        // Show loading dialog
        Dialog loadingDialog = new Dialog(requireContext());
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView statusText = loadingDialog.findViewById(R.id.status_info);
        if (statusText != null) {
            statusText.setText("Loading past orders...");
        }

        loadingDialog.show();

        new Thread(() -> {
            try {
                URL url = new URL("https://script.google.com/macros/s/AKfycbyb9TK2THnGlqFAEh5phsGwyt6vmGP0ZWDYmvH70yNTdikIIxBtRGO4O4QdAenleV3N/exec");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("queryEmail", MainActivity.currentUser.email);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    Scanner scanner = new Scanner(conn.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                    }
                    scanner.close();

                    JSONArray array = new JSONArray(response.toString());
                    List<PastOrder> fetchedOrders = new ArrayList<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        String date = obj.optString("date").split("T")[0];
                        String time = obj.optString("time").split("T")[1].substring(0, 5);
                        String itemQuantityRaw = obj.optString("item/quantity");
                        String cost = String.format(Locale.UK, "£%.2f", obj.optDouble("cost"));
                        String orderNumber = obj.optString("orderNumber");

                        String items = "", deals = "", rewards = "";
                        String[] lines = itemQuantityRaw.split("\n");
                        String currentSection = "";

                        StringBuilder itemsBuilder = new StringBuilder();
                        StringBuilder dealsBuilder = new StringBuilder();
                        StringBuilder rewardsBuilder = new StringBuilder();

                        for (String line : lines) {
                            if (line.startsWith("Items:")) {
                                currentSection = "items";
                            } else if (line.startsWith("Deals:")) {
                                currentSection = "deals";
                            } else if (line.startsWith("Rewards:")) {
                                currentSection = "rewards";
                            } else {
                                switch (currentSection) {
                                    case "items":
                                        itemsBuilder.append(line).append(", ");
                                        break;
                                    case "deals":
                                        dealsBuilder.append(line).append("\n");
                                        break;
                                    case "rewards":
                                        rewardsBuilder.append(line).append("\n");
                                        break;
                                }
                            }
                        }

                        items = itemsBuilder.toString().replaceAll(", $", "").trim();
                        deals = dealsBuilder.toString().trim();
                        rewards = rewardsBuilder.toString().trim();

                        fetchedOrders.add(new PastOrder(date, time, items, deals, rewards, cost, orderNumber));
                    }

                    requireActivity().runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        MainActivity.pastOrders = fetchedOrders; // cache orders
                        displayOrders(fetchedOrders);
                    });
                } else {
                    requireActivity().runOnUiThread(loadingDialog::dismiss);
                }

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(loadingDialog::dismiss);
            }
        }).start();
    }

    private void displayOrders(List<PastOrder> pastOrders) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (PastOrder order : pastOrders) {
            View orderView = inflater.inflate(R.layout.past_order_layout, binding.ordersContainer, false);

            ((TextView) orderView.findViewById(R.id.order_number)).setText("Order No. " + order.orderNumber);
            ((TextView) orderView.findViewById(R.id.order_date)).setText(order.date);
            ((TextView) orderView.findViewById(R.id.order_time)).setText(order.time);

            LinearLayout contentLayout = orderView.findViewById(R.id.order_contents_layout);
            ImageView dropdown = orderView.findViewById(R.id.dropdown_button);

            addSection(contentLayout, "Items", order.items);
            addSection(contentLayout, "Deals", order.deals);
            addSection(contentLayout, "Rewards", order.rewards);

            View divider = new View(getContext());
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 2);
            dividerParams.setMargins(0, 16, 0, 16);
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(Color.parseColor("#5B3000"));
            contentLayout.addView(divider);

            TextView totalView = new TextView(getContext());
            totalView.setText("Total\n" + order.price);
            totalView.setTextColor(Color.parseColor("#5B3000"));
            totalView.setTextSize(16);
            totalView.setTypeface(null, android.graphics.Typeface.BOLD);
            contentLayout.addView(totalView);

            dropdown.setOnClickListener(v1 -> {
                boolean isExpanded = contentLayout.getVisibility() == View.VISIBLE;
                float fromRotation = isExpanded ? 180f : 0f;
                float toRotation = isExpanded ? 0f : 180f;

                RotateAnimation rotate = new RotateAnimation(
                        fromRotation, toRotation,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                rotate.setDuration(200);
                rotate.setFillAfter(true);
                dropdown.startAnimation(rotate);

                if (isExpanded) {
                    collapse(contentLayout);
                } else {
                    expand(contentLayout);
                }
            });

            binding.ordersContainer.addView(orderView);
        }
    }

    private void addSection(LinearLayout layout, String title, String rawContent) {
        if (rawContent.trim().isEmpty()) return;

        TextView titleView = new TextView(getContext());
        titleView.setText(title);
        titleView.setTextColor(Color.parseColor("#5B3000"));
        titleView.setTextSize(14);
        titleView.setTypeface(null, Typeface.BOLD);
        layout.addView(titleView);

        String[] lines = rawContent.split("\n");
        StringBuilder blockBuilder = new StringBuilder();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            // Detect section headers
            boolean isNewItem = title.equalsIgnoreCase("Items") && line.matches(".* x\\d+");
            boolean isNewDealOrReward = (title.equalsIgnoreCase("Deals") || title.equalsIgnoreCase("Rewards")) && isDealHeader(line);

            if (isNewItem || isNewDealOrReward) {
                if (blockBuilder.length() > 0) {
                    if (title.equalsIgnoreCase("Items")) {
                        addItemBlock(layout, blockBuilder.toString().trim());
                    } else if (title.equalsIgnoreCase("Deals")) {
                        addDealBlock(layout, blockBuilder.toString().trim());
                    } else if (title.equalsIgnoreCase("Rewards")) {
                        addRewardBlock(layout, blockBuilder.toString().trim());
                    }
                    blockBuilder.setLength(0);
                }
            }

            blockBuilder.append(line).append("\n");
        }

        // Add last block
        if (blockBuilder.length() > 0) {
            if (title.equalsIgnoreCase("Items")) {
                addItemBlock(layout, blockBuilder.toString().trim());
            } else if (title.equalsIgnoreCase("Deals")) {
                addDealBlock(layout, blockBuilder.toString().trim());
            } else if (title.equalsIgnoreCase("Rewards")) {
                addRewardBlock(layout, blockBuilder.toString().trim());
            }
        }

        // Space between sections
        View spacer = new View(getContext());
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 16);
        spacer.setLayoutParams(spacerParams);
        layout.addView(spacer);
    }
    private void addRewardBlock(LinearLayout layout, String rewardText) {
        String[] lines = rewardText.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            TextView lineView = new TextView(getContext());
            lineView.setTextColor(Color.parseColor("#5B3000"));
            lineView.setTextSize(14);

            SpannableString styledLine;

            if (i == 0) {
                // First line (e.g. • Free Meal) bold
                styledLine = new SpannableString(line);
                styledLine.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, line.length(), 0);
                lineView.setText(styledLine);
            } else if (line.contains(":")) {
                // Split at the colon for key:value pairs
                int colonIndex = line.indexOf(":");
                String label = line.substring(0, colonIndex + 1);
                String value = line.substring(colonIndex + 1).trim();

                styledLine = new SpannableString("    " + label + " " + value);
                styledLine.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 4, 4 + label.length(), 0);
                lineView.setText(styledLine);
            } else {
                // Fallback for lines without colon
                lineView.setText("    " + line);
            }

            layout.addView(lineView);
        }

        // Add spacing between reward blocks
        View spacer = new View(getContext());
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 16);
        spacer.setLayoutParams(spacerParams);
        layout.addView(spacer);
    }
    private void addDealBlock(LinearLayout layout, String dealText) {
        String[] lines = dealText.split("\n");
        boolean isFirstLine = true;
        boolean inSelectionsBlock = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("•")) {
                line = line.substring(1).trim();
            }

            line = line.replaceAll(",$", ""); // Clean trailing commas

            TextView lineView = new TextView(getContext());
            lineView.setTextColor(Color.parseColor("#5B3000"));
            SpannableString styledLine;

            if (isFirstLine) {
                styledLine = new SpannableString("• " + line);
                styledLine.setSpan(new StyleSpan(Typeface.BOLD), 0, styledLine.length(), 0);
                lineView.setTextSize(16);
                lineView.setText(styledLine);
                isFirstLine = false;

            } else if (line.equalsIgnoreCase("Selections") || line.equalsIgnoreCase("Selections:")) {
                inSelectionsBlock = true;
                styledLine = new SpannableString("    Selections:");
                styledLine.setSpan(new StyleSpan(Typeface.BOLD), 4, 4 + "Selections:".length(), 0);
                lineView.setTextSize(14);
                lineView.setText(styledLine);

            } else if (line.contains(":")) {
                int colonIndex = line.indexOf(":");
                String label = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();

                // Detect end of Selections block
                if (inSelectionsBlock && (label.equalsIgnoreCase("Sides") || label.equalsIgnoreCase("Drink"))) {
                    inSelectionsBlock = false;
                }

                if (!inSelectionsBlock && (label.equalsIgnoreCase("Dog") || label.matches("Dog \\d+"))) {
                    styledLine = new SpannableString("  " + label + ": " + value);
                    styledLine.setSpan(new StyleSpan(Typeface.BOLD), 2, 2 + label.length() + 1, 0);
                    lineView.setTextSize(15); // H2

                } else {
                    styledLine = new SpannableString("    " + label + ": " + value);
                    styledLine.setSpan(new StyleSpan(Typeface.BOLD), 4, 4 + label.length() + 1, 0);
                    lineView.setTextSize(14); // H3
                }

                lineView.setText(styledLine);

            } else {
                styledLine = new SpannableString("    " + line);
                styledLine.setSpan(new StyleSpan(Typeface.BOLD), 4, 4 + line.length(), 0);
                lineView.setTextSize(14);
                lineView.setText(styledLine);
            }

            layout.addView(lineView);
        }

        View spacer = new View(getContext());
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 16);
        spacer.setLayoutParams(spacerParams);
        layout.addView(spacer);
    }
    private void addItemBlock(LinearLayout layout, String itemText) {
        String[] tokens = itemText.split(",");
        boolean isFirstLine = true;
        String currentLabel = null;
        StringBuilder currentValue = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();

            TextView lineView = new TextView(getContext());
            lineView.setTextColor(Color.parseColor("#5B3000"));
            lineView.setTextSize(14);
            SpannableString styledLine;

            // First line is item name like "Custom Dog x1"
            if (isFirstLine) {
                styledLine = new SpannableString("• " + token);
                styledLine.setSpan(new StyleSpan(Typeface.BOLD), 0, styledLine.length(), 0);
                lineView.setTextSize(16);
                lineView.setText(styledLine);
                layout.addView(lineView);
                isFirstLine = false;
                continue;
            }

            // Handle section header like "Selections:"
            if (token.equalsIgnoreCase("Selections:")) {
                combineSelections(layout, currentLabel, currentValue); // write previous
                currentLabel = null;
                styledLine = new SpannableString("    Selections:");
                styledLine.setSpan(new StyleSpan(Typeface.BOLD), 4, 4 + "Selections:".length(), 0);
                lineView.setText(styledLine);
                layout.addView(lineView);
                continue;
            }

            // If it's a label:value (e.g. "Cheese: Cheddar")
            if (token.contains(":")) {
                combineSelections(layout, currentLabel, currentValue);
                currentLabel = token.substring(0, token.indexOf(":")).trim();
                currentValue = new StringBuilder(token.substring(token.indexOf(":") + 1).trim());
                continue;
            }

            // If it's a new item like "New York x1"
            if (token.matches(".* x\\d+")) {
                combineSelections(layout, currentLabel, currentValue);
                currentLabel = null;
                styledLine = new SpannableString("• " + token);
                styledLine.setSpan(new StyleSpan(Typeface.BOLD), 0, styledLine.length(), 0);
                lineView.setTextSize(16);
                lineView.setText(styledLine);
                layout.addView(lineView);
                continue;
            }

            // Otherwise, assume continuation from current label (e.g., more cheeses)
            if (currentLabel != null) {
                currentValue.append(", ").append(token);
            } else {
                // Fallback
                styledLine = new SpannableString("    " + token);
                styledLine.setSpan(new StyleSpan(Typeface.BOLD), 4, 4 + token.length(), 0);
                lineView.setText(styledLine);
                layout.addView(lineView);
            }
        }

        combineSelections(layout, currentLabel, currentValue);

        View spacer = new View(getContext());
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 16);
        spacer.setLayoutParams(spacerParams);
        layout.addView(spacer);
    }

    private void combineSelections(LinearLayout layout, String label, StringBuilder value) {
        if (label == null || value.length() == 0) return;

        TextView lineView = new TextView(layout.getContext());
        lineView.setTextColor(Color.parseColor("#5B3000"));
        lineView.setTextSize(14);

        String fullText = "        " + label + ": " + value.toString();
        SpannableString styledLine = new SpannableString(fullText);
        styledLine.setSpan(new StyleSpan(Typeface.BOLD), 8, 8 + label.length() + 1, 0);

        lineView.setText(styledLine);
        layout.addView(lineView);
    }

    public void expand(final View view) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 0;
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                view.requestLayout();
                view.setAlpha(interpolatedTime);
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration((int) (targetHeight / view.getContext().getResources().getDisplayMetrics().density * 2));
        view.startAnimation(animation);
    }

    public void collapse(final View view) {
        final int initialHeight = view.getMeasuredHeight();

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.setAlpha(1 - interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration((int) (initialHeight / view.getContext().getResources().getDisplayMetrics().density * 2));
        view.startAnimation(animation);
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();

            try {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY < 0) {
                        Log.d("SWIPE", "Swipe Up");
                        onSwipeUp();
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private void onSwipeUp() {
            NavHostFragment.findNavController(SeventhFragment.this)
                    .navigate(R.id.action_SeventhFragment_to_SecondFragment);
        }
    }

    private boolean isDealHeader(String line) {
        line = line.trim().toLowerCase();
        return line.startsWith("• ")
                || line.matches("^(meal for one|meal for two|roulette|\\*\\*spicy\\*\\* roulette|dog of the day.*)$");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
