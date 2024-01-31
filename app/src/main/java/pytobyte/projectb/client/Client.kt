package pytobyte.projectb.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import pytobyte.projectb.client.DTO.AgentDTO


class MyClient() {

    val client = HttpClient(CIO) {
        BrowserUserAgent()
    }

    suspend fun getAgents(agents: MutableList<AgentDTO>): JsonArray {
        val response: HttpResponse = client.request("https://valorant-api.com/v1/agents?language=ru-RU&isPlayableCharacter=true") {
            method = HttpMethod.Get
        }
        println(response.bodyAsText())
        println(Json.decodeFromString<JsonElement>(response.bodyAsText()).jsonObject["data"])

        return Json.decodeFromString<JsonElement>(response.bodyAsText()).jsonObject["data"]!!.jsonArray
    }

    fun close() {
        client.close()
    }

}