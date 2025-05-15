package uk.ac.hope.mcse.android.coursework.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, MenuItems.class}, version = 9)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract MenuDao menuDao();
}
