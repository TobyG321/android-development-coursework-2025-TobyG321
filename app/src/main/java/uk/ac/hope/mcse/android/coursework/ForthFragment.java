package uk.ac.hope.mcse.android.coursework;

import static uk.ac.hope.mcse.android.coursework.MainActivity.db;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Random;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentForthBinding;
import uk.ac.hope.mcse.android.coursework.databinding.FragmentSecondBinding;
import uk.ac.hope.mcse.android.coursework.model.UserDao;

public class ForthFragment extends Fragment {

    private FragmentForthBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentForthBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int userPoints = MainActivity.currentUser.getPoints(); // e.g., 1800

        binding.rewardbox500.setOnClickListener(v ->
                confirmAndRedeemReward(view, 500, "Small"));

        binding.rewardbox1500.setOnClickListener(v ->
                confirmAndRedeemReward(view, 1500, "Medium"));

        binding.rewardbox3000.setOnClickListener(v ->
                confirmAndRedeemReward(view, 3000, "Large"));

        binding.rewardbox5000.setOnClickListener(v ->
                confirmAndRedeemReward(view, 5000, "Mega"));

        updatePointsDisplay(userPoints, view);
    }

    public void updatePointsDisplay(int userPoints, View view) {
        View[] rewardBoxes = {
                binding.rewardbox500,
                binding.rewardbox1500,
                binding.rewardbox3000,
                binding.rewardbox5000
        };

        int[] thresholds = {500, 1500, 3000, 5000};

        for (int i = 0; i < rewardBoxes.length; i++) {
            if (userPoints >= thresholds[i]) {
                rewardBoxes[i].setAlpha(1f);
                rewardBoxes[i].setEnabled(true);
            } else {
                rewardBoxes[i].setAlpha(0.3f);
                rewardBoxes[i].setEnabled(false);
            }
        }

        // Get the full height of the bar
        View backgroundBar = view.findViewById(R.id.background_bar);
        View progressFill = view.findViewById(R.id.progress_fill);

        backgroundBar.post(() -> {
            int barHeight = backgroundBar.getHeight();

            // Calculate fill height
            float fillPercent = Math.min(userPoints / (float) 5000, 1.0f);
            int targetHeight = (int) (barHeight * fillPercent);

            // Get current height of the fill view
            int currentHeight = progressFill.getHeight();

            // Animate the height change
            ValueAnimator animator = ValueAnimator.ofInt(currentHeight, targetHeight);
            animator.setDuration(500); // Duration in milliseconds
            animator.addUpdateListener(animation -> {
                int animatedHeight = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = progressFill.getLayoutParams();
                params.height = animatedHeight;
                progressFill.setLayoutParams(params);
            });
            animator.start();
        });

    }

    private void confirmAndRedeemReward(View view, int cost, String rewardLabel) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Redeem Reward")
                .setMessage("Are you sure you want to redeem the " + rewardLabel + " reward for " + cost + " points?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    int newPoints = MainActivity.currentUser.getPoints() - cost;
                    MainActivity.currentUser.setPoints(newPoints);
                    updatePointsDisplay(newPoints, view);
                    UserDao userDao = MainActivity.db.userDao();
                    userDao.updatePoints(
                            MainActivity.currentUser.getUsername(),
                            newPoints
                    );
                    MainActivity.rewards.add(String.valueOf(cost));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}