package uk.ac.hope.mcse.android.coursework;

import static uk.ac.hope.mcse.android.coursework.MainActivity.db;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Random;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentSecondBinding;
import uk.ac.hope.mcse.android.coursework.model.UserDao;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayout, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;

            v.setPadding(
                    v.getPaddingLeft(),
                    topInset,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );

            return insets;
        });

        binding.dealsButton.setOnClickListener(v -> {
            MainActivity.currentUser.addStamp(); // increases local stamp count

            UserDao userDao = db.userDao();
            userDao.updateStamps(
                    MainActivity.currentUser.getUsername(),
                    MainActivity.currentUser.getStamps()
            );
        });

        binding.menuButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_ThirdFragment);
        });

        binding.rewardsButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.to_rewards);
        });

        binding.dealsButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_FifthFragment);
        });

        ImageView[] stampViews = {
                binding.stamp1,
                binding.stamp2,
                binding.stamp3,
                binding.stamp4,
                binding.stamp5
        };

        //int stamps = MainActivity.currentUser.getStamps();
        int stamps = 4;

        if (stamps < 5) {
            binding.stampContainer.setVisibility(View.VISIBLE);
            binding.voucherImage.setVisibility(View.GONE);

            binding.mainLayout.setPadding(binding.mainLayout.getPaddingLeft(),
                    50, // Top padding
                    binding.mainLayout.getPaddingRight(),
                    binding.mainLayout.getPaddingBottom());

            // Get the current layout parameters for the mainLayout (assuming it's a ViewGroup)
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.welcome.getLayoutParams();
            layoutParams.bottomMargin = 50; // For top margin
            binding.welcome.setLayoutParams(layoutParams);


            Random random = new Random();
            for (int i = 0; i < stampViews.length; i++) {
                int angle = random.nextInt(361);
                if (i < stamps) {
                    stampViews[i].setImageResource(R.drawable.stamp);
                    stampViews[i].setRotation(angle);
                } else if (i == stamps) {
                    stampViews[i].setImageResource(
                            new int[]{R.drawable.empty1, R.drawable.empty2, R.drawable.empty3, R.drawable.empty4, R.drawable.empty5}[i]
                    );
                } else {
                    stampViews[i].setImageResource(R.drawable.empty);
                }
            }
        } else {
            // User has 5 or more stamps, show voucher
            binding.stampContainer.setVisibility(View.GONE);
            binding.voucherImage.setVisibility(View.VISIBLE);
            binding.mainLayout.setPadding(binding.mainLayout.getPaddingLeft(),
                    80, // Top padding
                    binding.mainLayout.getPaddingRight(),
                    binding.mainLayout.getPaddingBottom());

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.welcome.getLayoutParams();
            layoutParams.bottomMargin = 24; // For top margin
            binding.welcome.setLayoutParams(layoutParams);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}