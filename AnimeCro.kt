package com.hicham8367.animecro

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.*

class AnimeCro : MainAPI() {
    override var mainUrl = "https://web.animerco.org"
    override var name = "AnimeCro"
    override var lang = "ar"

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select(".anime-card").map {
            val title = it.selectFirst("h3")!!.text()
            val link = it.selectFirst("a")!!.absUrl("href")
            val poster = it.selectFirst("img")!!.absUrl("src")
            newAnimeSearchResponse(title, link) { this.posterUrl = poster }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1")!!.text()
        val poster = doc.selectFirst(".anime-poster img")!!.absUrl("src")
        val episodes = doc.select(".episodes-list a").map {
            Episode(it.text().trim(), it.absUrl("href"))
        }
        return newAnimeLoadResponse(title, url, TvType.Anime, episodes) {
            this.posterUrl = poster
        }
    }

    override suspend fun loadLinks(
        episodeUrl: String,
        referer: String?,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val doc = app.get(episodeUrl).document
        val servers = doc.select(".server a")
        for (server in servers) {
            val serverName = server.text()
            val videoUrl = server.absUrl("data-video").ifEmpty { server.absUrl("href") }
            if (videoUrl.isNotEmpty()) {
                callback(ExtractorLink(serverName, videoUrl, serverName, videoUrl, false))
            }
        }
    }
}
