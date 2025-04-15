package uk.ac.hope.mcse.android.coursework.model;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface MenuDao {
    @Insert
    void insert(MenuItems item);

    @Query("SELECT * FROM MenuItems")
    MenuItems getAll();

    @Query("SELECT * FROM MenuItems WHERE item_name = :itemName")
    MenuItems getMenuItemByName(String itemName);

    @Query("DELETE FROM MenuItems WHERE item_name = :itemName")
    void deleteItem(String itemName);
}