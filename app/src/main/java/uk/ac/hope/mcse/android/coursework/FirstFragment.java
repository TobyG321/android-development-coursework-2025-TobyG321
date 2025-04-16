package uk.ac.hope.mcse.android.coursework;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFirstBinding;
import uk.ac.hope.mcse.android.coursework.model.AppDatabase;
import uk.ac.hope.mcse.android.coursework.model.User;
import uk.ac.hope.mcse.android.coursework.model.UserDao;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 *
 */
public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonLogin.setOnClickListener(v -> {
            String username = binding.edittextUsername.getText().toString().trim();
            String password = binding.edittextPassword.getText().toString().trim();

            AppDatabase db = Room.databaseBuilder(requireContext(),
                            AppDatabase.class, "UpDoggData")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();

            UserDao userDao = db.userDao();

            User user = userDao.findByUsername(username);

            if (user == null || !user.password.equals(password)) {
                Toast.makeText(getContext(), "Username or password is incorrect", Toast.LENGTH_SHORT).show();
            } else {

                MainActivity.currentUser = new User();
                MainActivity.currentUser.setUsername(username);
                MainActivity.currentUser.setStamps(user.getStamps());
                MainActivity.currentUser.setPoints(6000);

                // Inputs are valid – navigate to the next fragment
                Toast.makeText(getContext(), "Welcome back, " + user.username, Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);

            }
        });

        binding.buttonCreateAccount.setOnClickListener(v -> {
            // Inflate the layout
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.create_account_popup, null);

            EditText editUsername = dialogView.findViewById(R.id.edit_username);
            EditText editPassword = dialogView.findViewById(R.id.edit_password);
            EditText editConfirmPassword = dialogView.findViewById(R.id.edit_confirm_password);
            Button buttonSubmit = dialogView.findViewById(R.id.button_submit_account);

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .create();

            buttonSubmit.setOnClickListener(btn -> {
                String username = editUsername.getText().toString().trim();
                String password = editPassword.getText().toString();
                String confirmPassword = editConfirmPassword.getText().toString();

                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Create new user object
                    User newUser = new User();
                    newUser.username = username;
                    newUser.password = password; // assuming you have this captured from the dialog

                    // Insert into database
                    AppDatabase db = Room.databaseBuilder(requireContext(),
                                    AppDatabase.class, "UpDoggData")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();

                    UserDao userDao = db.userDao();
                    userDao.insert(newUser);

                    // Confirmation message
                    Toast.makeText(getContext(), "Account created for: " + username, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            dialog.show();
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}