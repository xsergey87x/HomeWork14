
package com.example.homework14.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Films::class], version = 1)
abstract class AppBase : RoomDatabase() {
    abstract fun filmDataO(): FilmDataO
}
