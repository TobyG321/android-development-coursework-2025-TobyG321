package uk.ac.hope.mcse.android.coursework;

import static uk.ac.hope.mcse.android.coursework.MainActivity.db;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentSecondBinding;
import uk.ac.hope.mcse.android.coursework.model.UserDao;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private GestureDetector gestureDetector;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout stampCard = view.findViewById(R.id.stampCard);
        int stamps = MainActivity.currentUser.stamps;

        if (MainActivity.currentUser == null) {
            Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_FirstFragment); // Or your login fragment
            return;
        }

        for (int i = 0; i < 5; i++) {
            View stampView;
            if (i < stamps) {
                stampView = getLayoutInflater().inflate(R.layout.stamp_layout, stampCard, false);
            } else {
                stampView = getLayoutInflater().inflate(R.layout.empty_stamp_layout, stampCard, false);
            }
            stampCard.addView(stampView);
        }
        // Set up gesture detector
        gestureDetector = new GestureDetector(getContext(), new SwipeGestureListener());

        // Attach gesture detector to transparent overlay view
        view.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        // Set up button click listeners
        binding.menuButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_ThirdFragment);
        });

        binding.rewardsButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.to_rewards);
        });

        binding.dealsButton.setOnClickListener(v -> {
            UserDao userDao = db.userDao();
            userDao.updateStamps(
                    MainActivity.currentUser.getUsername(),
                    MainActivity.currentUser.getStamps()
            );

            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_FifthFragment);
        });

        binding.ordersButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_SeventhFragment);
        });
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            try {
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            Log.d("SWIPE", "Swipe Right");
                            onSwipeRight();
                        } else {
                            Log.d("SWIPE", "Swipe Left");
                            onSwipeLeft();
                        }
                        return true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            Log.d("SWIPE", "Swipe Down");
                            onSwipeDown();
                        } else {
                            Log.d("SWIPE", "Swipe Up - Ignored");
                            onSwipeUp();
                        }
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private void onSwipeLeft() {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_ThirdFragment);// Deals
        }

        private void onSwipeRight() {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_FifthFragment);// Menu
        }

        private void onSwipeUp() {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.to_rewards); // Rewards
        }

        private void onSwipeDown() {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_SeventhFragment); // Orders
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
