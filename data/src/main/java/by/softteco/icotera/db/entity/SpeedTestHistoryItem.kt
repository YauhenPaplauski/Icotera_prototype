package by.softteco.icotera.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "speedTestHistoryItems")
data class SpeedTestHistoryItem(
    @PrimaryKey
    val id: Long
)