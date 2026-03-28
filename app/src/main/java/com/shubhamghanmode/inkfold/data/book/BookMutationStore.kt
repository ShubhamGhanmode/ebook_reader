package com.shubhamghanmode.inkfold.data.book

interface BookMutationStore {
    suspend fun findByContentHash(contentHash: String): BookEntity?
    suspend fun insert(book: BookEntity): Long
    suspend fun updateLastOpenedAt(bookId: Long, timestamp: Long)
    suspend fun delete(bookId: Long)
}

class RoomBookMutationStore(
    private val bookDao: BookDao
) : BookMutationStore {
    override suspend fun findByContentHash(contentHash: String): BookEntity? =
        bookDao.getByContentHash(contentHash)

    override suspend fun insert(book: BookEntity): Long =
        bookDao.insert(book)

    override suspend fun updateLastOpenedAt(bookId: Long, timestamp: Long) {
        bookDao.updateLastOpenedAt(bookId, timestamp)
    }

    override suspend fun delete(bookId: Long) {
        bookDao.deleteById(bookId)
    }
}
