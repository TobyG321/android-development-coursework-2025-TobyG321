package uk.ac.hope.mcse.android.coursework.model;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM User WHERE username = :username LIMIT 1")
    User findByUsername(String username);
}