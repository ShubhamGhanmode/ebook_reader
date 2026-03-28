package com.shubhamghanmode.inkfold.feature.home

data class HomeBook(
    val id: Long,
    val title: String,
    val author: String?,
    val coverPath: String?,
    val progressionPercent: Float?
)

data class HomeUiState(
    val continueReading: HomeBook?,
    val recentImports: List<HomeBook>,
    val allBooks: List<HomeBook>,
    val isEmpty: Boolean,
    val transientMessage: String?
)
