package uk.ac.hope.mcse.android.coursework;

import static uk.ac.hope.mcse.android.coursework.MainActivity.db;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentForthBinding;
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

        int userPoints = 6000;
        //MainActivity.currentUser.getPoints();

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

        View rewardContainer = binding.rewardContainer;
        View progressFill = view.findViewById(R.id.progress_fill);

        rewardContainer.post(() -> {
            int containerHeight = rewardContainer.getHeight();
            int fillHeight = getFillHeightFromPoints(userPoints, containerHeight);

            // Animate fill bar height
            ValueAnimator animator = ValueAnimator.ofInt(progressFill.getHeight(), fillHeight);
            animator.setDuration(500);
            animator.addUpdateListener(animation -> {
                int animatedHeight = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = progressFill.getLayoutParams();
                params.height = animatedHeight;
                progressFill.setLayoutParams(params);
            });
            animator.start();
        });
    }

    /**
     * Converts user's point progress into vertical height based on 2:4:8:10 visual weights.
     */
    private int getFillHeightFromPoints(int points, int containerHeight) {
        int[] thresholds = {0, 500, 1500, 3000, 5000};
        int[] cumulativeWeights = {0, 2, 6, 14, 24}; // 2+4+8+10

        float visualWeight = 0f;

        for (int i = 1; i < thresholds.length; i++) {
            if (points <= thresholds[i]) {
                float range = thresholds[i] - thresholds[i - 1];
                float progress = points - thresholds[i - 1];
                float segmentWeight = cumulativeWeights[i] - cumulativeWeights[i - 1];
                visualWeight = cumulativeWeights[i - 1] + (progress / range) * segmentWeight;
                break;
            }
        }

        // Handle cases where points exceed max threshold
        if (points > 5000) {
            visualWeight = 24f; // Max visual fill
        }

        return (int) ((visualWeight / 24f) * containerHeight);
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
