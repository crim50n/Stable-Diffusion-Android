package dev.minios.pdaiv1.storage.db.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.minios.pdaiv1.storage.converters.ListConverters
import dev.minios.pdaiv1.storage.converters.MapConverters
import dev.minios.pdaiv1.storage.db.cache.CacheDatabase.Companion.DB_VERSION
import dev.minios.pdaiv1.storage.db.cache.dao.ServerConfigurationDao
import dev.minios.pdaiv1.storage.db.cache.dao.StableDiffusionEmbeddingDao
import dev.minios.pdaiv1.storage.db.cache.dao.StableDiffusionHyperNetworkDao
import dev.minios.pdaiv1.storage.db.cache.dao.StableDiffusionLoraDao
import dev.minios.pdaiv1.storage.db.cache.dao.StableDiffusionModelDao
import dev.minios.pdaiv1.storage.db.cache.dao.StableDiffusionSamplerDao
import dev.minios.pdaiv1.storage.db.cache.dao.SwarmUiModelDao
import dev.minios.pdaiv1.storage.db.cache.entity.ServerConfigurationEntity
import dev.minios.pdaiv1.storage.db.cache.entity.StableDiffusionEmbeddingEntity
import dev.minios.pdaiv1.storage.db.cache.entity.StableDiffusionHyperNetworkEntity
import dev.minios.pdaiv1.storage.db.cache.entity.StableDiffusionLoraEntity
import dev.minios.pdaiv1.storage.db.cache.entity.StableDiffusionModelEntity
import dev.minios.pdaiv1.storage.db.cache.entity.StableDiffusionSamplerEntity
import dev.minios.pdaiv1.storage.db.cache.entity.SwarmUiModelEntity

@Database(
    version = DB_VERSION,
    exportSchema = true,
    entities = [
        ServerConfigurationEntity::class,
        StableDiffusionModelEntity::class,
        StableDiffusionSamplerEntity::class,
        StableDiffusionLoraEntity::class,
        StableDiffusionHyperNetworkEntity::class,
        StableDiffusionEmbeddingEntity::class,
        SwarmUiModelEntity::class,
    ],
)
@TypeConverters(
    MapConverters::class,
    ListConverters::class,
)
internal abstract class CacheDatabase : RoomDatabase() {
    abstract fun serverConfigurationDao(): ServerConfigurationDao
    abstract fun sdModelDao(): StableDiffusionModelDao
    abstract fun sdSamplerDao(): StableDiffusionSamplerDao
    abstract fun sdLoraDao(): StableDiffusionLoraDao
    abstract fun sdHyperNetworkDao(): StableDiffusionHyperNetworkDao
    abstract fun sdEmbeddingDao(): StableDiffusionEmbeddingDao
    abstract fun swarmUiModelDao(): SwarmUiModelDao

    companion object {
        const val DB_VERSION = 1
    }
}
