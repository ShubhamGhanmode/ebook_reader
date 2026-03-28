package com.shubhamghanmode.inkfold.data.book

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BookEntity::class],
    version = 1,
    exportSchema = false
)
abstract class InkFoldDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}
