package com.shubhamghanmode.inkfold.data.book

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert
    suspend fun insert(book: BookEntity): Long

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    suspend fun getById(bookId: Long): BookEntity?

    @Query("SELECT * FROM books WHERE contentHash = :contentHash LIMIT 1")
    suspend fun getByContentHash(contentHash: String): BookEntity?

    @Query(
        """
        SELECT * FROM books
        WHERE progressionPercent IS NOT NULL
          AND progressionPercent > 0
          AND progressionPercent < 1
        ORDER BY lastOpenedAt DESC
        LIMIT 1
        """
    )
    fun observeContinueReading(): Flow<BookEntity?>

    @Query("SELECT * FROM books ORDER BY importedAt DESC LIMIT 10")
    fun observeRecentImports(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY title COLLATE NOCASE ASC, importedAt DESC")
    fun observeAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books")
    fun observeBookCount(): Flow<Int>

    @Query("UPDATE books SET lastOpenedAt = :timestamp WHERE id = :bookId")
    suspend fun updateLastOpenedAt(bookId: Long, timestamp: Long)

    @Query(
        """
        UPDATE books
        SET progressionJson = :progressionJson,
            progressionPercent = :progressionPercent,
            lastOpenedAt = :lastOpenedAt
        WHERE id = :bookId
        """
    )
    suspend fun updateProgression(
        bookId: Long,
        progressionJson: String?,
        progressionPercent: Float?,
        lastOpenedAt: Long
    )

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteById(bookId: Long)
}
