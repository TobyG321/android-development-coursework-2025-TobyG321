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

            // Then navigate
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_FirstFragment);
        });

        binding.menuButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_ThirdFragment);
        });

        if (MainActivity.currentUser != null) {
            String name = MainActivity.currentUser.getUsername();
            binding.welcomeText.setText("Welcome back, " + name + "!");
        }

        int stamps = MainActivity.currentUser.getStamps();
        ImageView[] stampViews = {
                binding.stamp1,
                binding.stamp2,
                binding.stamp3,
                binding.stamp4,
                binding.stamp5
        };

        Random random = new Random();
        for (int i = 0; i < stampViews.length; i++) {
            int angle = random.nextInt(361); // random angle between 0 and 360 degrees
            if (i < stamps) {
                stampViews[i].setImageResource(R.drawable.stamp); // filled
                stampViews[i].setRotation(angle);
            } else if (i == stamps) {
                stampViews[i].setImageResource(
                        new int[]{R.drawable.empty1, R.drawable.empty2, R.drawable.empty3, R.drawable.empty4, R.drawable.empty5}[i]
                ); // highlighted next stamp
            } else {
                stampViews[i].setImageResource(R.drawable.empty); // unfilled
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}