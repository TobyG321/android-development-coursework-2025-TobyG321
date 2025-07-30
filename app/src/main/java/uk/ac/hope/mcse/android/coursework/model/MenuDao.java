package uk.ac.hope.mcse.android.coursework.model;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MenuDao {
    @Insert
    void insert(MenuItems item);

    @Query("SELECT * FROM MenuItems")
    List<MenuItems> getAll();

    @Query("SELECT * FROM MenuItems WHERE item_name = :itemName")
    MenuItems getMenuItemByName(String itemName);

    @Query("DELETE FROM MenuItems WHERE item_name = :itemName")
    void deleteItem(String itemName);

    @Query("SELECT * FROM MenuItems ORDER BY RANDOM() LIMIT 1")
    MenuItems getRandomDog();
}