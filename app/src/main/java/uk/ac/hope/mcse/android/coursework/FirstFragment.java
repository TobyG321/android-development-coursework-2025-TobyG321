package uk.ac.hope.mcse.android.coursework;

import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFirstBinding;
import uk.ac.hope.mcse.android.coursework.model.AppDatabase;
import uk.ac.hope.mcse.android.coursework.model.User;
import uk.ac.hope.mcse.android.coursework.model.UserDao;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*binding.buttonLogin.setOnClickListener(v -> {
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
                MainActivity.currentUser.setPoints(6000); // test value

                Toast.makeText(getContext(), "Welcome back, " + user.username, Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });*/

        binding.buttonLogin.setOnClickListener(v -> {
            String username = binding.edittextUsername.getText().toString().trim();
            String password = binding.edittextPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("username", username);
                jsonBody.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestQueue queue = Volley.newRequestQueue(requireContext());
            String url = "https://script.google.com/macros/s/AKfycbyh9T7br3EvMc5QUUIBIEt4cqAI672IF9fq3YDnIIdwasjgPBCG-A84pRd5pdSBnOop6w/exec?route=login";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        Log.d("LOGIN_RESPONSE", response.toString());  // 🔍 Log entire JSON response

                        if (response.has("error")) {
                            try {
                                String errorMessage = response.getString("error");
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Unexpected response format", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        try {
                            User user = new User();
                            user.username = response.getString("username");
                            user.stamps = response.getInt("stamps");
                            user.points = response.getInt("points");
                            user.mobile = response.getString("mobile");
                            user.email = response.getString("email");
                            user.address = response.getString("address");

                            MainActivity.currentUser = user;

                            Toast.makeText(getContext(), "Welcome, " + user.username, Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(FirstFragment.this)
                                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Login failed. Invalid data", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String body = new String(error.networkResponse.data);
                            Log.e("LOGIN_ERROR_BODY", body);  // 🔍 Log raw error body
                        }
                        error.printStackTrace();
                        Toast.makeText(getContext(), "Error connecting to server", Toast.LENGTH_SHORT).show();
                    }
            );


            queue.add(request);
        });



        // ✅ Mutable reference
        final String[] selectedAddress = {""};

        binding.buttonCreateAccount.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.create_account_popup, null);

            EditText editUsername = dialogView.findViewById(R.id.edit_username);
            EditText editPassword = dialogView.findViewById(R.id.edit_password);
            EditText editConfirmPassword = dialogView.findViewById(R.id.edit_confirm_password);
            EditText editMobile = dialogView.findViewById(R.id.edit_mobile);
            EditText editEmail = dialogView.findViewById(R.id.edit_email);
            EditText editPostcode = dialogView.findViewById(R.id.edit_address_postcode);
            EditText editNumber = dialogView.findViewById(R.id.edit_address_number);
            EditText editStreet = dialogView.findViewById(R.id.edit_address_street);
            Button buttonLookup = dialogView.findViewById(R.id.button_lookup_postcode);
            Button buttonSubmit = dialogView.findViewById(R.id.button_submit_account);

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .create();

            buttonLookup.setOnClickListener(lookup -> {
                String postcode = editPostcode.getText().toString().trim();
                if (postcode.isEmpty()) {
                    Toast.makeText(getContext(), "Enter a postcode first", Toast.LENGTH_SHORT).show();
                    return;
                }

                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

                new Thread(() -> {
                    try {
                        List<Address> addresses = geocoder.getFromLocationName(postcode, 5);
                        if (addresses != null && !addresses.isEmpty()) {
                            List<String> displayList = new ArrayList<>();
                            for (Address addr : addresses) {
                                String line = addr.getAddressLine(0);
                                if (line != null) displayList.add(line);
                            }

                            requireActivity().runOnUiThread(() -> {
                                AlertDialog.Builder picker = new AlertDialog.Builder(getContext());
                                picker.setTitle("Select an address");
                                picker.setItems(displayList.toArray(new String[0]), (d, which) -> {
                                    Address selected = addresses.get(which);

                                    String street = selected.getThoroughfare() != null ? selected.getThoroughfare() : "";

                                    // Better town extraction using address line
                                    String fullLine = selected.getAddressLine(0);
                                    String town = "";
                                    String postcodeFinal = selected.getPostalCode() != null ? selected.getPostalCode() : "";

                                    if (fullLine != null && fullLine.contains(",")) {
                                        String[] parts = fullLine.split(",");
                                        if (parts.length >= 2) {
                                            String townAndPostcode = parts[1].trim();
                                            if (townAndPostcode.contains(postcodeFinal)) {
                                                town = townAndPostcode.replace(postcodeFinal, "").trim();
                                            } else {
                                                town = townAndPostcode;
                                            }
                                        }
                                    }

                                    editStreet.setText(street);
                                    String number = editNumber.getText().toString().trim();

                                    String fullAddress = number + " " + street + "\n" + town + "\n" + postcodeFinal;
                                    selectedAddress[0] = fullAddress;
                                    Log.d("FULL_ADDRESS_LOG", fullAddress);

                                    editPostcode.setText(postcodeFinal);
                                });
                                picker.show();
                            });

                        } else {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "No matching addresses found", Toast.LENGTH_SHORT).show());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Failed to fetch addresses", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            });

            buttonSubmit.setOnClickListener(btn -> {
                String username = editUsername.getText().toString().trim();
                String password = editPassword.getText().toString();
                String confirmPassword = editConfirmPassword.getText().toString();
                String mobile = editMobile.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String address = selectedAddress[0];

                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                        || mobile.isEmpty() || email.isEmpty() || address.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!email.contains("@") || !email.contains(".")) {
                    Toast.makeText(getContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!mobile.matches("\\d{10,15}")) {
                    Toast.makeText(getContext(), "Enter a valid mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                User newUser = new User();
                newUser.username = username;
                newUser.password = password;
                newUser.mobile = mobile;
                newUser.email = email;
                newUser.address = address;

                AppDatabase db = Room.databaseBuilder(requireContext(),
                                AppDatabase.class, "UpDoggData")
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build();

                db.userDao().insert(newUser);

                Toast.makeText(getContext(), "Account created for: " + username, Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                // Build JSON object
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("username", username);
                    jsonBody.put("password", password); // optional - be careful if sensitive
                    jsonBody.put("mobile", mobile);
                    jsonBody.put("email", email);
                    jsonBody.put("address", address);
                    jsonBody.put("stamps", 0);
                    jsonBody.put("points", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Send to Google Sheets using POST
                RequestQueue queue = Volley.newRequestQueue(requireContext());

                String url = "https://script.google.com/macros/s/AKfycbyh9T7br3EvMc5QUUIBIEt4cqAI672IF9fq3YDnIIdwasjgPBCG-A84pRd5pdSBnOop6w/exec?route=register";
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonBody,
                        response -> Log.d("SHEETS", "Success: " + response.toString()),
                        error -> Log.e("SHEETS", "Error sending to Sheets: " + error.toString())
                );

                queue.add(request);

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
