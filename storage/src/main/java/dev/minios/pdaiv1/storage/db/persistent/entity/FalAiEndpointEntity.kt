package dev.minios.pdaiv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.minios.pdaiv1.storage.db.persistent.contract.FalAiEndpointContract

@Entity(tableName = FalAiEndpointContract.TABLE)
data class FalAiEndpointEntity(
    @PrimaryKey
    @ColumnInfo(name = FalAiEndpointContract.ID)
    val id: String,

    @ColumnInfo(name = FalAiEndpointContract.ENDPOINT_ID)
    val endpointId: String,

    @ColumnInfo(name = FalAiEndpointContract.TITLE)
    val title: String,

    @ColumnInfo(name = FalAiEndpointContract.DESCRIPTION)
    val description: String,

    @ColumnInfo(name = FalAiEndpointContract.CATEGORY)
    val category: String,

    @ColumnInfo(name = FalAiEndpointContract.GROUP, defaultValue = "Custom")
    val group: String,

    @ColumnInfo(name = FalAiEndpointContract.THUMBNAIL_URL)
    val thumbnailUrl: String,

    @ColumnInfo(name = FalAiEndpointContract.PLAYGROUND_URL)
    val playgroundUrl: String,

    @ColumnInfo(name = FalAiEndpointContract.DOCUMENTATION_URL)
    val documentationUrl: String,

    @ColumnInfo(name = FalAiEndpointContract.IS_CUSTOM, defaultValue = "1")
    val isCustom: Boolean,

    @ColumnInfo(name = FalAiEndpointContract.SCHEMA_JSON)
    val schemaJson: String,
)
