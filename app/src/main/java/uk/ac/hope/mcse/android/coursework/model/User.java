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

    @ColumnInfo(name = "stamps")
    public int stamps = 0;

    public void setUsername(String name){
        username = name;
    }

    public String getUsername(){
        return username;
    }

    public void addStamp(){
        if(stamps<5) {
            stamps++;
        }
        else{
            stamps = 0;
        }
    }

    public int getStamps(){
        return stamps;
    }

    public void setStamps(int count){
        stamps = count;
    }

    public void clearStamps(){
        stamps = 0;
    }
}
