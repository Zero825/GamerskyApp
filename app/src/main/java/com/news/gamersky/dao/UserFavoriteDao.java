package com.news.gamersky.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.news.gamersky.entity.UserFavorite;

import java.util.List;

@Dao
public interface UserFavoriteDao {
    @Query("SELECT * FROM user_favorites WHERE user_name=:userName AND type=:type")
    List<UserFavorite> findByUserNameAndType(String userName,int type);

    @Query("SELECT * FROM user_favorites WHERE user_name=:userName AND href=:href")
    List<UserFavorite> findByUserNameAndHref(String userName,String href);

    @Query("Delete FROM user_favorites WHERE user_name=:userName AND href=:href")
    void deleteByUserNameAndHref(String userName,String href);

    @Insert
    void insertUserFavorite (UserFavorite userFavorite);

    @Delete
    void deleteUserFavorite(UserFavorite userFavorite);
}
