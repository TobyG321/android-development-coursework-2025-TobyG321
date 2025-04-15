package uk.ac.hope.mcse.android.coursework;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;

import uk.ac.hope.mcse.android.coursework.databinding.ActivityMainBinding;
import uk.ac.hope.mcse.android.coursework.model.AppDatabase;
import uk.ac.hope.mcse.android.coursework.model.MenuDao;
import uk.ac.hope.mcse.android.coursework.model.MenuItems;
import uk.ac.hope.mcse.android.coursework.model.User;
import uk.ac.hope.mcse.android.coursework.model.UserDao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    public static User currentUser;
    public static List<MenuItems> menuItems = new ArrayList<>();
    public static List<MenuItems> basket = new ArrayList<>();
    public static AppDatabase db;

    // Replace this with your actual published sheet CSV URL
    private final String SHEET_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vQhEgMuJQ_lJ5JqY_DETjgReLyv0gfUDTQ5ekY1XDYA_blMWcy566lza95hlJYSnVge7uy31k6nZ0zV/pub?gid=1518700805&single=true&output=csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

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
                new NavController.OnDestinationChangedListener() {
                    @Override
                    public void onDestinationChanged(@NonNull NavController controller,
                                                     @NonNull NavDestination destination,
                                                     @Nullable Bundle arguments) {
                        if (destination.getId() == R.id.FirstFragment) {
                            binding.fab.hide();
                        } else {
                            binding.fab.show();
                        }
                    }
                });

        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "UpDoggData")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        UserDao userDao = db.userDao();

        // Fetch Google Sheets Data
        fetchSheetData();
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

            // Optional: Parse CSV here and use in app
            String[] rows = result.split("\n");
            if (rows.length < 2) return; // No data

            String[] headers = rows[0].split(","); // optional: validate if needed

            MenuDao menuDao = db.menuDao();

            for (int i = 1; i < rows.length; i++) {
                String[] cols = rows[i].split(",", -1); // -1 to keep empty fields

                if (cols.length < 4) continue; // skip malformed

                String itemName = cols[0].trim();
                String lastUpdate = cols[1].trim();
                String region = cols[2].trim();
                String price = cols[3].trim();

                // Simplified category grouping (customize this logic if needed)
                String bread = cols[4].trim().equals("1") ? "Brioche" : cols[5].trim().equals("1") ? "Wrap" : "N/A";
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
                    item.bread = bread;
                    item.cheese = cheese.toString();
                    item.sauces = sauces.toString();
                    item.toppings = toppings.toString();

                    menuItems.add(item);

                    menuDao.insert(item);
                    Log.d("DB_INSERT", "Inserted: " + itemName);
                } else if (!existing.last_update.equals(lastUpdate)) {
                    // update existing
                    existing.last_update = lastUpdate;
                    existing.region = region;
                    existing.price = Double.parseDouble(price);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
