package uk.ac.hope.mcse.android.coursework.model;

import android.location.Address;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import java.util.List;

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

    @ColumnInfo(name = "points")
    public int points = 0;

    @ColumnInfo(name = "mobile")
    public String mobile;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "address")
    public String address;

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

    public int getPoints(){
        return points;
    }

    public void setPoints(int count){
        points = count;
    }
}
