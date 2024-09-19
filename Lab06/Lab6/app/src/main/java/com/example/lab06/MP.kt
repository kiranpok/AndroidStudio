package com.example.lab06



data class MP(
    val name: String,
    val constituency: String,
    val party: String,
    val imageUrl: String
)

val mpList = listOf(
    MP("Hafiz Ahmed", "Constituency A", "Party X", "https://via.placeholder.com/150"),
    MP("Mamita Sharma", "Constituency B", "Party Y", "https://via.placeholder.com/150"),
    MP("Anish Patel", "Constituency C", "Party Z", "https://via.placeholder.com/150"),
    MP("Peter Andersen", "Constituency D", "Party W", "https://via.placeholder.com/150"),
    MP("Jarkko Virtanen", "Constituency E", "Party V", "https://via.placeholder.com/150")
)
