package com.shubhamghanmode.inkfold.data.book

class DeleteBookUseCase(
    private val fileStore: LocalLibraryFileStore,
    private val bookStore: BookMutationStore
) {
    suspend fun delete(book: BookEntity): Result<Unit> =
        runCatching {
            fileStore.deleteBookFiles(book)
            bookStore.delete(book.id)
        }
}
