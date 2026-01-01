package dev.minios.pdaiv1.data.local

import android.content.Context
import dev.minios.pdaiv1.data.mappers.FalAiOpenApiParser
import dev.minios.pdaiv1.domain.datasource.FalAiEndpointDataSource
import dev.minios.pdaiv1.domain.entity.FalAiEndpoint
import io.reactivex.rxjava3.core.Single

/**
 * Provides built-in fal.ai endpoints from app assets.
 * Supports nested folder structure like fal-ai/flux-lora/inpainting.json
 */
internal class FalAiEndpointBuiltInDataSource(
    private val context: Context,
) : FalAiEndpointDataSource.BuiltIn {

    override fun getAll(): Single<List<FalAiEndpoint>> = Single.fromCallable {
        findAllJsonFiles(ASSETS_DIR).mapNotNull { path ->
            runCatching {
                val json = context.assets
                    .open(path)
                    .bufferedReader()
                    .use { it.readText() }
                FalAiOpenApiParser.parse(json, isCustom = false, assetPath = path)
            }.getOrNull()
        }
    }

    /**
     * Recursively finds all .json files in the given assets directory.
     */
    private fun findAllJsonFiles(dir: String): List<String> {
        val result = mutableListOf<String>()
        val items = context.assets.list(dir) ?: return emptyList()

        for (item in items) {
            val path = "$dir/$item"
            if (item.endsWith(".json")) {
                result.add(path)
            } else {
                // Check if it's a directory by trying to list it
                val subItems = context.assets.list(path)
                if (!subItems.isNullOrEmpty()) {
                    result.addAll(findAllJsonFiles(path))
                }
            }
        }
        return result
    }

    companion object {
        private const val ASSETS_DIR = "falai-endpoints"
    }
}
