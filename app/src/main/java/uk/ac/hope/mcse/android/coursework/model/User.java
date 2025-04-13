package uk.ac.hope.mcse.android.coursework.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "password")
    public String password;

    public void setUsername(String name){
        username = name;
    }

    public String getUsername(){
        return username;
    }
}
