package com.news.gamersky.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.news.gamersky.entity.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE name=:name")
    User findByName(String name);

    @Query("SELECT * FROM users WHERE name=:name AND password=:password")
    User findByNameAndPassword(String name,byte[] password);

    @Insert
    void insertUser (User user);

    @Delete
    void deleteUser(User user);
}
