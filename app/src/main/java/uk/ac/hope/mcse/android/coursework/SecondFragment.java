package uk.ac.hope.mcse.android.coursework;

import static uk.ac.hope.mcse.android.coursework.MainActivity.db;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
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

        binding.dealsButton.setOnClickListener(v -> {
            MainActivity.currentUser.addStamp(); // increases local stamp count

            UserDao userDao = MainActivity.db.userDao();
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

        if (MainActivity.currentUser != null) {
            String name = MainActivity.currentUser.getUsername();
            binding.welcomeText.setText("Welcome back, " + name + "!");
        }

        ImageView[] stampViews = {
                binding.stamp1,
                binding.stamp2,
                binding.stamp3,
                binding.stamp4,
                binding.stamp5
        };

        int stamps = MainActivity.currentUser.getStamps();

        if (stamps < 5) {
            binding.stampLayout.setVisibility(View.VISIBLE);
            binding.voucherImage.setVisibility(View.GONE);

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
            binding.stampLayout.setVisibility(View.GONE);
            binding.voucherImage.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}