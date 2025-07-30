package uk.ac.hope.mcse.android.coursework;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavHostController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;

import uk.ac.hope.mcse.android.coursework.databinding.ActivityMainBinding;
import uk.ac.hope.mcse.android.coursework.model.AppDatabase;
import uk.ac.hope.mcse.android.coursework.model.BasketItem;
import uk.ac.hope.mcse.android.coursework.model.MenuDao;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;
import uk.ac.hope.mcse.android.coursework.model.PastOrder;
import uk.ac.hope.mcse.android.coursework.model.User;
import uk.ac.hope.mcse.android.coursework.model.UserDao;
import uk.ac.hope.mcse.android.coursework.model.deals.Deal;
import uk.ac.hope.mcse.android.coursework.model.rewards.Reward;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    private AppBarConfiguration appBarConfiguration;
    public static ActivityMainBinding binding;

    public static User currentUser;
    public static List<MenuItems> menuItems = new ArrayList<>();
    public static List<BasketItem> basket = new ArrayList<>();

    public static List<Deal> deals = new ArrayList<>();
    public static List<Reward> rewards = new ArrayList<>();
    public static List<PastOrder> pastOrders = new ArrayList<>();

    public static AppDatabase db;

    private final String SHEET_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vQhEgMuJQ_lJ5JqY_DETjgReLyv0gfUDTQ5ekY1XDYA_blMWcy566lza95hlJYSnVge7uy31k6nZ0zV/pub?gid=1518700805&single=true&output=csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Order Doggs", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });

        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) -> {
                    int destId = destination.getId();

                    // Hide FAB completely on FirstFragment
                    if (destId == R.id.FirstFragment) {
                        binding.fab.hide();
                        binding.logout.setVisibility(View.GONE);
                        binding.toolbar.setVisibility(View.GONE);
                        return;
                    } else {
                        binding.logout.setVisibility(View.GONE);
                        binding.getRoot().postDelayed(() -> {
                            binding.fab.show();
                            binding.logout.setVisibility(View.VISIBLE);
                        }, 700);
                    }

                    binding.fab.show();
                    binding.toolbar.setVisibility(View.GONE);

                    boolean basketNotEmpty = !basket.isEmpty();
                    boolean dealNotEmpty = !deals.isEmpty();
                    boolean rewardNotEmpty = !rewards.isEmpty();

                    Log.d("DESTINATION", "Basket: " + basketNotEmpty);
                    Log.d("DESTINATION", "Deals: " + dealNotEmpty);
                    Log.d("DESTINATION", "Rewards: " + rewardNotEmpty);

                    if (destId == R.id.SixthFragment) {
                        // Always show home icon in checkout
                        binding.fab.setImageResource(R.drawable.home_fab);
                        binding.fab.setOnClickListener(v -> navController.navigate(R.id.action_home));
                    } else if (destId == R.id.ThirdFragment) {
                        // Always show basket icon on menu
                        binding.fab.setImageResource(R.drawable.cart_fab);
                        binding.fab.setOnClickListener(v -> navController.navigate(R.id.action_checkout));
                    } else if (destId == R.id.SecondFragment ||
                            destId == R.id.ForthFragment ||
                            destId == R.id.FifthFragment ||
                            destId == R.id.SeventhFragment) {
                        if (basketNotEmpty || dealNotEmpty || rewardNotEmpty) {
                            binding.fab.setImageResource(R.drawable.cart_fab);
                            binding.fab.setOnClickListener(v -> navController.navigate(R.id.action_checkout));
                        } else {
                            binding.fab.setImageResource(R.drawable.hotdog_fab);
                            if (destId == R.id.SecondFragment) {
                                // Valid only in SecondFragment
                                binding.fab.setOnClickListener(v -> navController.navigate(R.id.action_SecondFragment_to_ThirdFragment));
                            } else {
                                // Global fallback
                                binding.fab.setOnClickListener(v -> navController.navigate(R.id.action_menu));
                            }
                        }
                    }
                });


        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "UpDoggData")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        menuItems = db.menuDao().getAll();
        UserDao userDao = db.userDao();

        // Fetch Google Sheets Data
        fetchSheetData();

        binding.logout.setOnClickListener(view ->
                navController.navigate(R.id.action_logout));
                currentUser = new User();
                basket = new ArrayList<>();
                rewards = new ArrayList<>();
                deals = new ArrayList<>();
                pastOrders = new ArrayList<>();
    }

    private void fetchSheetData() {
        new FetchSheetDataTask().execute(SHEET_URL);
    }

    private static class FetchSheetDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setUseCaches(false);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }

                reader.close();
            } catch (Exception e) {
                Log.e("FetchError", "Error fetching data: " + e.getMessage());
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("CSV_RESULT", result);

            if (result == null || result.trim().isEmpty()) {
                Toast.makeText(binding.getRoot().getContext(), "Menu not found online. Showing saved items.", Toast.LENGTH_LONG).show();
                return;
            }

            menuItems.clear();

            String[] rows = result.split("\n");
            if (rows.length < 2) return; // No data

            String[] headers = rows[0].split(",");

            MenuDao menuDao = db.menuDao();

            for (int i = 1; i < rows.length; i++) {
                String[] cols = rows[i].split(",", -1);

                if (cols.length < 4) continue;

                String itemName = cols[0].trim();
                String lastUpdate = cols[1].trim();
                String region = cols[2].trim();
                String price = cols[3].trim();

                String bread =  cols[4].trim().equals("1") ? "Brioche" : cols[5].trim().equals("1") ? "Wrap" : "N/A";
                String dog =    cols[6].trim().equals("1") ? "Frankfurter" :
                                cols[7].trim().equals("1") ? "Ft Frank" :
                                cols[8].trim().equals("1") ? "Short Frank" :
                                cols[9].trim().equals("1") ? "Bratwurst" :
                                cols[10].trim().equals("1") ? "Chorizo" :
                                cols[11].trim().equals("1") ? "Bacon Wrapped" : "N/A";

                StringBuilder cheese = new StringBuilder();
                for (int j = 11; j <= 17; j++) {
                    if (cols.length > j && cols[j].trim().equals("1")) {
                        cheese.append(headers[j]).append(", ");
                    }
                }

                StringBuilder sauces = new StringBuilder();
                for (int j = 18; j <= 24; j++) {
                    if (cols.length > j && cols[j].trim().equals("1")) {
                        sauces.append(headers[j]).append(", ");
                    }
                }

                StringBuilder toppings = new StringBuilder();
                for (int j = 25; j < cols.length; j++) {
                    if (cols[j].trim().equals("1")) {
                        toppings.append(headers[j]).append(", ");
                    }
                }

                MenuItems existing = menuDao.getMenuItemByName(itemName);

                if (existing == null) {
                    // insert new
                    MenuItems item = new MenuItems();
                    item.item_name = itemName;
                    item.last_update = lastUpdate;
                    item.region = region;
                    item.price = Double.parseDouble(price);
                    item.dog = dog;
                    item.bread = bread;
                    item.cheese = cheese.toString();
                    item.sauces = sauces.toString();
                    item.toppings = toppings.toString();

                    if(!lastUpdate.equals("DELETED")) {
                        menuItems.add(item);
                    }

                    menuDao.insert(item);
                    Log.d("DB_INSERT", "Inserted: " + itemName);
                } else if (!existing.last_update.equals(lastUpdate)) {
                    // update existing
                    existing.last_update = lastUpdate;
                    existing.region = region;
                    existing.price = Double.parseDouble(price);
                    existing.dog = dog;
                    existing.bread = bread;
                    existing.cheese = cheese.toString();
                    existing.sauces = sauces.toString();
                    existing.toppings = toppings.toString();

                    menuDao.deleteItem(itemName);
                    menuDao.insert(existing);
                    menuItems.add(existing);

                    Log.d("DB_UPDATE", "Updated: " + itemName);
                } else if (existing.last_update.equals("DELETED")){
                    menuDao.deleteItem(itemName);
                    Log.d("DB_DELETE", "Deleted: " + itemName);
                } else {
                    Log.d("DB_SKIP", "No change for: " + itemName);
                    menuItems.add(existing);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            currentUser = new User();

            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.FirstFragment);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public MenuItems getDogOfTheDay() {
        SharedPreferences prefs = getSharedPreferences("DogOfTheDayPrefs", MODE_PRIVATE);
        long lastChosenTime = prefs.getLong("lastChosenTime", 0);
        String dogName = prefs.getString("dogName", null);

        long currentTime = System.currentTimeMillis();
        long timeLeft = (24 * 60 * 60 * 1000) - (currentTime - lastChosenTime);

        if (dogName == null || timeLeft <= 0) {
            // Choose a new dog
            if (menuItems.isEmpty()) return null;

            MenuItems randomDog = menuItems.get(new Random().nextInt(menuItems.size()));

            // Save the selection
            prefs.edit()
                    .putString("dogName", randomDog.item_name)
                    .putLong("lastChosenTime", currentTime)
                    .apply();

            Log.d("DOG_OF_DAY", "New Dog Selected: " + randomDog.item_name + " — 24h timer starts now.");
            return randomDog;
        } else {
            // Log time remaining
            long seconds = timeLeft / 1000 % 60;
            long minutes = timeLeft / (1000 * 60) % 60;
            long hours = timeLeft / (1000 * 60 * 60);

            String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            Log.d("DOG_OF_DAY", "Current Dog: " + dogName + " — Time left: " + timeFormatted);

            for (MenuItems item : menuItems) {
                if (item.item_name.equals(dogName)) {
                    return item;
                }
            }
        }

        return null;
    }

    public static final String CHANNEL_ID = "order_channel";

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Order Notifications";
            String description = "Notifies when orders are complete";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
