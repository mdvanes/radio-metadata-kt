package nl.mdworld.radiometadata.strategies

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import nl.mdworld.radiometadata.BroadcastInfo
import nl.mdworld.radiometadata.PickPath
import nl.mdworld.radiometadata.RadioMetadata
import nl.mdworld.radiometadata.RadioSchema
import nl.mdworld.radiometadata.SongInfo
import nl.mdworld.radiometadata.TimeInfo
import nl.mdworld.radiometadata.presets.NPO2_PRESET
import nl.mdworld.radiometadata.presets.SKY_PRESET

object ApiMetadataUtil {
    val PRESETS = mapOf(
        "npo2" to NPO2_PRESET,
        "sky" to SKY_PRESET
    )

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
        engine {
            maxConnectionsCount = 1000
            endpoint {
                maxConnectionsPerRoute = 100
                pipelineMaxSize = 20
                keepAliveTime = 5000
                connectTimeout = 10000
                connectAttempts = 5
            }
        }
    }

    suspend fun getRadioMetaData(config: Any): List<RadioMetadata> {
        val schema = when (config) {
            is String -> PRESETS[config] ?: throw IllegalArgumentException("No schema found for config $config")
            is RadioSchema -> config
            else -> throw IllegalArgumentException("Config must be a string preset name or RadioSchema object")
        }
        return getRadioMetaDataBySchema(schema)
    }

    private suspend fun getRadioMetaDataBySchema(schema: RadioSchema): List<RadioMetadata> {
        if (!isValidSchema(schema)) {
            println("ApiMetadataUtil: Invalid schema: $schema")
            return emptyList()
        }
        return coroutineScope {
            val responses = schema.urls.map { urlConfig -> async { urlConfig.name to fetchJson(urlConfig.url, urlConfig.headers) } }.awaitAll().toMap()
            val tracks = pickFrom(responses, schema.paths.tracks) as? JsonArray ?: return@coroutineScope emptyList()
            tracks.mapNotNull { trackElement ->
                try {
                    val track = trackElement.jsonObject
                    RadioMetadata(
                        time = TimeInfo(
                            start = pickFromAsString(track, schema.paths.time?.start),
                            end = pickFromAsString(track, schema.paths.time?.end)
                        ),
                        broadcast = BroadcastInfo(
                            title = pickFromAsString(responses, schema.paths.broadcast?.title),
                            presenters = pickFromAsString(responses, schema.paths.broadcast?.presenters),
                            imageUrl = pickFromAsString(responses, schema.paths.broadcast?.imageUrl)
                        ),
                        song = SongInfo(
                            artist = pickFromAsString(track, schema.paths.song.artist),
                            title = pickFromAsString(track, schema.paths.song.title) ?: "",
                            imageUrl = pickFromAsString(track, schema.paths.song.imageUrl),
                            listenUrl = pickFromAsString(track, schema.paths.song.listenUrl)
                        )
                    )
                } catch (_: Exception) { null }
            }
        }
    }

    private fun isValidSchema(schema: RadioSchema): Boolean =
        schema.name.isNotEmpty() && schema.urls.isNotEmpty() && schema.urls.all { it.url.isNotEmpty() && it.name.isNotEmpty() } && schema.paths.tracks.isNotEmpty()

    private suspend fun fetchJson(url: String, headers: Map<String, String>?): JsonElement {
        return try {
            client.get(url) {
                headers?.forEach { (key, value) -> 
                    header(key, value) 
                }
            }.body<JsonElement>()
        } catch (e: Exception) {
            println("ApiMetadataUtil: Exception: ${e::class.simpleName} - ${e.message}")
            JsonObject(emptyMap())
        }
    }

    private fun pickFrom(data: Any?, path: PickPath?): Any? {
        if (path == null || data == null) return null
        return path.fold(data as Any?) { acc, next ->
            when {
                acc == null -> null
                acc is Map<*, *> && next is String -> acc[next]
                acc is JsonObject && next is String -> acc[next]
                acc is List<*> && next is Int && next < acc.size -> acc[next]
                acc is JsonArray && next is Int && next < acc.size -> acc[next]
                else -> null
            }
        }
    }

    private fun pickFromAsString(data: Any?, path: PickPath?): String? = when (val result = pickFrom(data, path)) {
        is JsonPrimitive -> result.contentOrNull
        is String -> result
        else -> result?.toString()
    }
}
