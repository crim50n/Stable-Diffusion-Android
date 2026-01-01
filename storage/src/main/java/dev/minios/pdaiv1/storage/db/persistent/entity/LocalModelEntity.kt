package dev.minios.pdaiv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.minios.pdaiv1.storage.db.persistent.contract.LocalModelContract

@Entity(tableName = LocalModelContract.TABLE)
data class LocalModelEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = LocalModelContract.ID)
    val id: String,
    @ColumnInfo(name = LocalModelContract.TYPE, defaultValue = "onnx")
    val type: String,
    @ColumnInfo(name = LocalModelContract.NAME)
    val name: String,
    @ColumnInfo(name = LocalModelContract.SIZE)
    val size: String,
    @ColumnInfo(name = LocalModelContract.SOURCES)
    val sources: List<String>,
    @ColumnInfo(name = LocalModelContract.CHIPSET_SUFFIX, defaultValue = "NULL")
    val chipsetSuffix: String? = null,
    @ColumnInfo(name = LocalModelContract.RUN_ON_CPU, defaultValue = "0")
    val runOnCpu: Boolean = false,
)
