package com.shubhamghanmode.inkfold.data.book

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [Index(value = ["contentHash"], unique = true)]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contentHash: String,
    val title: String,
    val author: String?,
    val identifier: String,
    val mediaType: String,
    val bookPath: String,
    val coverPath: String?,
    val progressionJson: String?,
    val progressionPercent: Float?,
    val importedAt: Long,
    val lastOpenedAt: Long
)
