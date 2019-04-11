package by.softteco.icotera.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speedTestHistoryItems")
data class SpeedTestHistoryItem(
    @PrimaryKey
    val id: Long
)