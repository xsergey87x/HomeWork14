package com.example.homework14.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Films(
    @ColumnInfo(name = "film_title")
    val filmTitle: String?,
    @ColumnInfo(name = "film_vote_average")
    val filmVoteAverage: String?,
    @ColumnInfo(name = "film_overview")
    val filmOverview: String?,
    @SerializedName("release_date")
    val releaseDate: String,
    @ColumnInfo(name = "film_poster")
    val filmPoster: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}