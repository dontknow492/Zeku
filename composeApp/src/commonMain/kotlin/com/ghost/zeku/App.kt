package com.ghost.zeku

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.anilist.model.*
import com.ghost.zeku.data.remote.anilist.toMediaDetailsDomain
import com.ghost.zeku.data.remote.mal.MalApi
import com.ghost.zeku.data.remote.mal.model.MalMediaDto
import com.ghost.zeku.data.remote.mal.toMediaDetailsDomain
import com.ghost.zeku.domain.model.enum.*
import com.ghost.zeku.domain.repository.AuthRepository
import com.ghost.zeku.domain.repository.UserSettings
import com.ghost.zeku.presentation.theme.AppTheme
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import java.util.concurrent.TimeUnit


@Composable
fun App() = AppTheme {
//    ZekuAppWrapper()
}

@Composable
fun AnimeDetailPreview() {
    val cachedAniListAnimeDetails = AniListMedia(
        id = 113415,
        type = "ANIME", // Assuming an Enum
        title = AniListTitle(
            romaji = "Jujutsu Kaisen",
            english = "JUJUTSU KAISEN",
            native = "呪術廻戦",
            userPreferred = "JUJUTSU KAISEN"
        ),
        synonyms = listOf(
            "JJK",
            "Sorcery Fight",
            "咒术回战",
            "주술회전",
            "มหาเวทย์ผนึกมาร",
            "جوجوتسو كايسن",
            "Магическая битва",
            "咒術迴戰"
        ),
        countryOfOrigin = "JP",
        coverImage = AniListCoverImage(
            large = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx113415-LHBAeoZDIsnF.jpg",
            extraLarge = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx113415-LHBAeoZDIsnF.jpg",
            medium = null
        ),
        bannerImage = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/113415-jQBSkxWAAk83.jpg",
        description = "A boy fights... for \"the right death.\"\n\nHardship, regret, shame: the negative feelings that humans feel become Curses that lurk in our everyday lives...",
        status = MediaStatus.FINISHED.name,
        format = MediaFormat.TV.name,
        source = MediaSourceMaterial.MANGA.name,
        isAdult = false,
        startDate = AniListDate(year = 2020, month = 10, day = 3),
        endDate = AniListDate(year = 2021, month = 3, day = 27),
        season = MediaSeason.FALL.name,
        seasonYear = 2020,
        genres = listOf("Action", "Drama", "Supernatural"),
        tags = listOf(
            AniListTag(
                name = "Urban Fantasy",
                description = "Set in a world similar to the real world...",
                rank = 93,
                isMediaSpoiler = false,
                category = "Setting-Universe"
            ),
            AniListTag(
                name = "Shounen",
                description = "Target demographic is teenage and young adult males.",
                rank = 92,
                isMediaSpoiler = false,
                category = "Demographic"
            ),
            AniListTag(
                name = "Curses",
                description = "Features a character, object or area that has been cursed...",
                rank = 89,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Super Power",
                description = "Prominently features characters with special abilities...",
                rank = 88,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Youkai",
                description = "Prominently features supernatural creatures from Japanese folklore.",
                rank = 87,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Male Protagonist",
                description = "Main character is male.",
                rank = 85,
                isMediaSpoiler = false,
                category = "Cast-Main Cast"
            ),
            AniListTag(
                name = "Primarily Teen Cast",
                description = "Main cast is mostly composed of teen characters.",
                rank = 84,
                isMediaSpoiler = false,
                category = "Cast-Main Cast"
            ),
            AniListTag(
                name = "Exorcism",
                description = "Involving religious methods of vanquishing youkai...",
                rank = 83,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Demons",
                description = "Prominently features malevolent otherworldly creatures.",
                rank = 80,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Orphan",
                description = "Prominently features a character that is an orphan.",
                rank = 80,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Dissociative Identities",
                description = "A case where one or more people share the same body.",
                rank = 80,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Urban",
                description = "Partly or completely set in a city.",
                rank = 79,
                isMediaSpoiler = false,
                category = "Setting-Scene"
            ),
            AniListTag(
                name = "Body Horror",
                description = "Features characters who undergo horrific transformations...",
                rank = 75,
                isMediaSpoiler = false,
                category = "Theme-Other"
            ),
            AniListTag(
                name = "Primarily Male Cast",
                description = "Main cast is mostly composed of male characters.",
                rank = 72,
                isMediaSpoiler = false,
                category = "Cast-Main Cast"
            ),
            AniListTag(
                name = "Magic",
                description = "Prominently features magical elements...",
                rank = 71,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Martial Arts",
                description = "Centers around the use of traditional hand-to-hand combat.",
                rank = 70,
                isMediaSpoiler = false,
                category = "Theme-Action"
            ),
            AniListTag(
                name = "Shapeshifting",
                description = "Features character(s) who changes one's appearance or form.",
                rank = 68,
                isMediaSpoiler = true,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Ensemble Cast",
                description = "Features a large cast of characters...",
                rank = 68,
                isMediaSpoiler = false,
                category = "Cast-Main Cast"
            ),
            AniListTag(
                name = "Gore",
                description = "Prominently features graphic bloodshed and violence.",
                rank = 64,
                isMediaSpoiler = false,
                category = "Theme-Other"
            ),
            AniListTag(
                name = "Anthropomorphism",
                description = "Contains non-human character(s)...",
                rank = 60,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Swordplay",
                description = "Prominently features the use of swords in combat.",
                rank = 60,
                isMediaSpoiler = false,
                category = "Theme-Action"
            ),
            AniListTag(
                name = "School",
                description = "Partly or completely set in a primary or secondary educational institution.",
                rank = 59,
                isMediaSpoiler = false,
                category = "Setting-Scene"
            ),
            AniListTag(
                name = "Rotoscoping",
                description = "Animation technique...",
                rank = 54,
                isMediaSpoiler = false,
                category = "Technical"
            ),
            AniListTag(
                name = "Slapstick",
                description = "Prominently features comedy based on deliberately clumsy actions...",
                rank = 51,
                isMediaSpoiler = false,
                category = "Theme-Comedy"
            ),
            AniListTag(
                name = "Mythology",
                description = "Prominently features mythological elements...",
                rank = 50,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Boarding School",
                description = "Features characters attending a boarding school.",
                rank = 50,
                isMediaSpoiler = false,
                category = "Setting-Scene"
            ),
            AniListTag(
                name = "Archery",
                description = "Centers around the sport of archery...",
                rank = 50,
                isMediaSpoiler = false,
                category = "Theme-Action"
            ),
            AniListTag(
                name = "Twins",
                description = "Prominently features two or more siblings...",
                rank = 48,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Tragedy",
                description = "Centers around tragic events and unhappy endings.",
                rank = 46,
                isMediaSpoiler = true,
                category = "Theme-Drama"
            ),
            AniListTag(
                name = "Surreal Comedy",
                description = "Prominently features comedic moments that defy casual reasoning...",
                rank = 45,
                isMediaSpoiler = false,
                category = "Theme-Comedy"
            ),
            AniListTag(
                name = "Baseball",
                description = "Centers around the sport of baseball.",
                rank = 35,
                isMediaSpoiler = false,
                category = "Theme-Game-Sport"
            )
        ),
        averageScore = 84,
        meanScore = 84,
        popularity = 896031,
        favourites = 63195,
        episodes = 24,
        duration = 24,
        chapters = null,
        volumes = null,
        nextAiringEpisode = null,
        studios = AniListStudioConnection(
            edges = listOf(
                AniListStudioEdge(
                    isMain = false,
                    node = AniListStudioNode(id = 245, name = "Toho", isAnimationStudio = false)
                ),
                AniListStudioEdge(
                    isMain = true,
                    node = AniListStudioNode(id = 569, name = "MAPPA", isAnimationStudio = true)
                ),
                AniListStudioEdge(
                    isMain = false,
                    node = AniListStudioNode(id = 6570, name = "Shueisha", isAnimationStudio = false)
                ),
                AniListStudioEdge(
                    isMain = false,
                    node = AniListStudioNode(id = 6602, name = "Sumzap", isAnimationStudio = false)
                ),
                AniListStudioEdge(
                    isMain = false,
                    node = AniListStudioNode(id = 143, name = "Mainichi Broadcasting System", isAnimationStudio = false)
                )
            )
        ),
        trailer = AniListTrailer(
            id = "pkKu9hLT-t8",
            site = "YOUTUBE",
            thumbnail = "https://i.ytimg.com/vi/pkKu9hLT-t8/hqdefault.jpg"
        ),
        externalLinks = listOf(
            AniListExternalLink(url = "https://jujutsukaisen.jp/", site = "Official Site", icon = null),
            AniListExternalLink(
                url = "https://twitter.com/animejujutsu",
                site = "Twitter",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/17-R0tMgOvwvhsS.png"
            ),
            AniListExternalLink(
                url = "https://www.crunchyroll.com/jujutsu-kaisen",
                site = "Crunchyroll",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/5-AWN2pVlluCOO.png"
            ),
            AniListExternalLink(
                url = "https://www.netflix.com/title/81278456",
                site = "Netflix",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/10-rVGPom8RCiwH.png"
            ),
            AniListExternalLink(
                url = "https://www.iq.com/album/igc33vhvex",
                site = "iQ",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/122-EPBJ2E0oPt5C.png"
            ),
            AniListExternalLink(
                url = "https://www.bilibili.tv/media/37738",
                site = "Bilibili TV",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/119-NCwGvCjFADGQ.png"
            ),
            AniListExternalLink(
                url = "https://twitter.com/Jujutsu_anime",
                site = "Twitter",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/17-R0tMgOvwvhsS.png"
            ),
            AniListExternalLink(
                url = "https://youtube.com/playlist?list=PLxSscENEp7JisDU6GAJuyNpVwDvCm-f3J",
                site = "YouTube",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/13-ZwR1Xwgtyrwa.png"
            ),
            AniListExternalLink(
                url = "https://twitter.com/Jujutsu_Kaisen_",
                site = "Twitter",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/17-R0tMgOvwvhsS.png"
            ),
            AniListExternalLink(
                url = "https://www.hulu.com/series/jujutsu-kaisen-382ec8bf-3650-4cde-94db-ecd18665f9e0",
                site = "Hulu",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/7-rM06PQyWONGC.png"
            )
        ),
        characters = AniListCharacterConnection(
            edges = listOf(
                AniListCharacterEdge(
                    role = CharacterRole.MAIN.name,
                    node = AniListCharacterNode(
                        id = 126635,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b126635-L0y3I92JSUkN.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.MAIN.name,
                    node = AniListCharacterNode(
                        id = 127212,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b127212-FVm2tD0erQ5B.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.MAIN.name,
                    node = AniListCharacterNode(
                        id = 133700,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b133700-f6sOO3TcgLV6.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.MAIN.name,
                    node = AniListCharacterNode(
                        id = 127691,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b127691-9zqh1xpIubn7.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 133704,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b133704-8wLTGjc234q2.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 157867,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b157867-dHdd8ZECuzHx.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 200767,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b200767-bTqPLS2Jpiqf.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 209694,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b209694-xSS6FxN4al3l.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 172743,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b172743-4Y5SXqED6A3G.jpg")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 189237,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b189237-ZxNJwVPL8DvW.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 200768,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b200768-fgIRyfO8ABk2.jpg")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 157865,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b157865-X9ENX9OzWevS.jpg")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 210846,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b210846-XcRQ643Ne8Pb.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 133702,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b133702-Y7JRG5vAvjIL.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 158154,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b158154-UCqbiULli62P.png")
                    )
                )
            )
        ),
        relations = AniListRelationConnection(
            edges = listOf(
                AniListRelationEdge(
                    relationType = RelationType.ADAPTATION.name,
                    node = AniListMedia(
                        id = 101517,
                        type = MediaType.MANGA.name,
                        title = AniListTitle(
                            romaji = "Jujutsu Kaisen",
                            english = "Jujutsu Kaisen",
                            native = "呪術廻戦",
                            userPreferred = "Jujutsu Kaisen"
                        ),
                        synonyms = null,
                        countryOfOrigin = null,
                        coverImage = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/media/manga/cover/medium/bx101517-H3TdM3g5ZUe9.jpg",
                            extraLarge = null,
                            medium = null
                        ),
                        bannerImage = null,
                        description = null,
                        status = MediaStatus.FINISHED.name,
                        format = MediaFormat.MANGA.name,
                        source = null,
                        isAdult = null,
                        startDate = null,
                        endDate = null,
                        season = null,
                        seasonYear = null,
                        genres = null,
                        tags = null,
                        averageScore = 80,
                        meanScore = null,
                        popularity = null,
                        favourites = null,
                        episodes = null,
                        duration = null,
                        chapters = 272,
                        volumes = null,
                        nextAiringEpisode = null,
                        studios = null,
                        trailer = null,
                        externalLinks = null,
                        characters = null,
                        relations = null,
                        staff = null,
                        mediaListEntry = null
                    )
                ),
                AniListRelationEdge(
                    relationType = RelationType.PREQUEL.name,
                    node = AniListMedia(
                        id = 131573,
                        type = MediaType.ANIME.name,
                        title = AniListTitle(
                            romaji = "Jujutsu Kaisen 0",
                            english = "JUJUTSU KAISEN 0",
                            native = "呪術廻戦 0",
                            userPreferred = "JUJUTSU KAISEN 0"
                        ),
                        synonyms = null,
                        countryOfOrigin = null,
                        coverImage = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx131573-rpl82vDEDRm6.jpg",
                            extraLarge = null,
                            medium = null
                        ),
                        bannerImage = null,
                        description = null,
                        status = MediaStatus.FINISHED.name,
                        format = MediaFormat.MOVIE.name,
                        source = null,
                        isAdult = null,
                        startDate = null,
                        endDate = null,
                        season = null,
                        seasonYear = null,
                        genres = null,
                        tags = null,
                        averageScore = 83,
                        meanScore = null,
                        popularity = null,
                        favourites = null,
                        episodes = 1,
                        duration = null,
                        chapters = null,
                        volumes = null,
                        nextAiringEpisode = null,
                        studios = null,
                        trailer = null,
                        externalLinks = null,
                        characters = null,
                        relations = null,
                        staff = null,
                        mediaListEntry = null
                    )
                ),
                AniListRelationEdge(
                    relationType = RelationType.SEQUEL.name,
                    node = AniListMedia(
                        id = 145064,
                        type = MediaType.ANIME.name,
                        title = AniListTitle(
                            romaji = "Jujutsu Kaisen 2nd Season",
                            english = "JUJUTSU KAISEN Season 2",
                            native = "呪術廻戦 第2期",
                            userPreferred = "JUJUTSU KAISEN Season 2"
                        ),
                        synonyms = null,
                        countryOfOrigin = null,
                        coverImage = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx145064-hSNRJM03pvv1.jpg",
                            extraLarge = null,
                            medium = null
                        ),
                        bannerImage = null,
                        description = null,
                        status = MediaStatus.FINISHED.name,
                        format = MediaFormat.TV.name,
                        source = null,
                        isAdult = null,
                        startDate = null,
                        endDate = null,
                        season = null,
                        seasonYear = null,
                        genres = null,
                        tags = null,
                        averageScore = 86,
                        meanScore = null,
                        popularity = null,
                        favourites = null,
                        episodes = 23,
                        duration = null,
                        chapters = null,
                        volumes = null,
                        nextAiringEpisode = null,
                        studios = null,
                        trailer = null,
                        externalLinks = null,
                        characters = null,
                        relations = null,
                        staff = null,
                        mediaListEntry = null
                    )
                ),
                AniListRelationEdge(
                    relationType = RelationType.OTHER.name,
                    node = AniListMedia(
                        id = 147463,
                        type = MediaType.ANIME.name,
                        title = AniListTitle(
                            romaji = "Jujutsu Kaisen PV",
                            english = null,
                            native = "呪術廻戦PV",
                            userPreferred = "Jujutsu Kaisen PV"
                        ),
                        synonyms = null,
                        countryOfOrigin = null,
                        coverImage = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/b147463-LmoLwL9DbYAn.jpg",
                            extraLarge = null,
                            medium = null
                        ),
                        bannerImage = null,
                        description = null,
                        status = MediaStatus.FINISHED.name,
                        format = MediaFormat.ONA.name,
                        source = null,
                        isAdult = null,
                        startDate = null,
                        endDate = null,
                        season = null,
                        seasonYear = null,
                        genres = null,
                        tags = null,
                        averageScore = 67,
                        meanScore = null,
                        popularity = null,
                        favourites = null,
                        episodes = 3,
                        duration = null,
                        chapters = null,
                        volumes = null,
                        nextAiringEpisode = null,
                        studios = null,
                        trailer = null,
                        externalLinks = null,
                        characters = null,
                        relations = null,
                        staff = null,
                        mediaListEntry = null
                    )
                )
            )
        ),
        staff = AniListStaffConnection(
            edges = listOf(
                AniListStaffEdge(
                    role = "ADR Director (English)",
                    node = AniListStaffNode(
                        id = 95512,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Michael Sorich"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n95512-ZF7RSh28tB0l.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Series Composition",
                    node = AniListStaffNode(
                        id = 99012,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroshi Seko"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n99012-H3OCMnoTqmEV.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Script (eps 1-24)",
                    node = AniListStaffNode(
                        id = 99012,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroshi Seko"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n99012-H3OCMnoTqmEV.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "ADR Director (Latin American Spanish)",
                    node = AniListStaffNode(
                        id = 100029,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Patricia Acevedo"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/5029.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Storyboard (eps 4, 5)",
                    node = AniListStaffNode(
                        id = 100812,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Yoshiaki Kawajiri"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n100812-1ZrgDInanSvE.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Storyboard (ep 1)",
                    node = AniListStaffNode(
                        id = 101185,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Tadashi Hiramatsu"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n101185-CJLT9YKxSDZC.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Core Director (eps 2, 3, 6)",
                    node = AniListStaffNode(
                        id = 101185,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Tadashi Hiramatsu"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n101185-CJLT9YKxSDZC.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Juju Sanpo Chief Animation Director (eps 13, 15)",
                    node = AniListStaffNode(
                        id = 101185,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Tadashi Hiramatsu"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n101185-CJLT9YKxSDZC.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Character Design",
                    node = AniListStaffNode(
                        id = 101185,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Tadashi Hiramatsu"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n101185-CJLT9YKxSDZC.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Chief Animation Director (OP1, OP2, ep 1)",
                    node = AniListStaffNode(
                        id = 101185,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Tadashi Hiramatsu"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n101185-CJLT9YKxSDZC.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Art Director (OP1, OP2)",
                    node = AniListStaffNode(
                        id = 103102,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Yuusuke Takeda"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n103102-aEjIrYuaUaVn.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Script (German)",
                    node = AniListStaffNode(
                        id = 103757,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "René Dawn-Claude"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/8757.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "ADR Director (German)",
                    node = AniListStaffNode(
                        id = 103757,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "René Dawn-Claude"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/8757.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Storyboard (ep 13)",
                    node = AniListStaffNode(
                        id = 103979,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hironori Tanaka"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n103979-iNgo68ikjKgx.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Episode Director (ep 13)",
                    node = AniListStaffNode(
                        id = 103979,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hironori Tanaka"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n103979-iNgo68ikjKgx.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Animation Director (ep 13)",
                    node = AniListStaffNode(
                        id = 103979,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hironori Tanaka"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n103979-iNgo68ikjKgx.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Key Animation (eps 4, 13, 20, 24)",
                    node = AniListStaffNode(
                        id = 103979,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hironori Tanaka"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n103979-iNgo68ikjKgx.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Key Animation (ep 6)",
                    node = AniListStaffNode(
                        id = 104457,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Toshiya Washida"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n104457-2IKwEQWGcwLm.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Cursed Spirit Design (OP1, eps 1-24)",
                    node = AniListStaffNode(
                        id = 106114,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroya Iijima"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n106114-WqnSCKNql5TU.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Key Animation (ep 12)",
                    node = AniListStaffNode(
                        id = 106114,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroya Iijima"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n106114-WqnSCKNql5TU.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Prop Design (eps 3-24)",
                    node = AniListStaffNode(
                        id = 106114,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroya Iijima"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n106114-WqnSCKNql5TU.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Storyboard (ep 3)",
                    node = AniListStaffNode(
                        id = 106475,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroaki Andou"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n106475-kUCzyMZZKBfu.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Storyboard (eps 12, 18, 20)",
                    node = AniListStaffNode(
                        id = 107199,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Fuminori Kizaki"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n107199-4je9Gs4pEXju.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Key Animation (eps 13, 16, 23)",
                    node = AniListStaffNode(
                        id = 107357,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Toshiyuki Satou"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n107357-96PgjYcdwBFt.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Chief Animation Director (eps 4, 8, 13, 16, 19, 23)",
                    node = AniListStaffNode(
                        id = 107584,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Terumi Nishii"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n107584-CPAvAAk43Ih0.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                )
            )
        ),
        mediaListEntry = AniListMediaListEntry(
            id = 442863659,
            mediaId = null,
            status = TrackStatus.CURRENT.name,
            progress = 4,
            score = 0.0,
            media = null
        )
    )

    val rawJsonMal: String = """
        {
          "id": 40748,
          "title": "Jujutsu Kaisen",
          "main_picture": {
            "medium": "https://myanimelist.net/images/anime/1171/109222.webp",
            "large": "https://myanimelist.net/images/anime/1171/109222l.webp"
          },
          "alternative_titles": {
            "synonyms": [
              "Sorcery Fight",
              "JJK"
            ],
            "en": "Jujutsu Kaisen",
            "ja": "呪術廻戦"
          },
          "start_date": "2020-10-03",
          "end_date": "2021-03-27",
          "synopsis": "Idly indulging in baseless paranormal activities with the Occult Club, high schooler Yuuji Itadori spends his days at either the clubroom or the hospital, where he visits his bedridden grandfather. However, this leisurely lifestyle soon takes a turn for the strange when he unknowingly encounters a cursed item. Triggering a chain of supernatural occurrences, Yuuji finds himself suddenly thrust into the world of Curses—dreadful beings formed from human malice and negativity—after swallowing the said item, revealed to be a finger belonging to the demon Sukuna Ryoumen, the King of Curses.\n\nYuuji experiences first-hand the threat these Curses pose to society as he discovers his own newfound powers. Introduced to the Tokyo Prefectural Jujutsu High School, he begins to walk down a path from which he cannot return—the path of a Jujutsu sorcerer.\n\n[Written by MAL Rewrite]",
          "mean": 8.51,
          "rank": 160,
          "popularity": 11,
          "num_list_users": 3027506,
          "num_scoring_users": 1995706,
          "nsfw": "white",
          "created_at": "2019-11-20T09:38:26+00:00",
          "updated_at": "2024-10-02T16:16:51+00:00",
          "media_type": "tv",
          "status": "finished_airing",
          "genres": [
            {
              "id": 1,
              "name": "Action"
            },
            {
              "id": 46,
              "name": "Award Winning"
            },
            {
              "id": 23,
              "name": "School"
            },
            {
              "id": 27,
              "name": "Shounen"
            },
            {
              "id": 37,
              "name": "Supernatural"
            }
          ],
          "my_list_status": {
            "status": "watching",
            "score": 0,
            "num_episodes_watched": 4,
            "is_rewatching": false,
            "updated_at": "2026-04-18T03:11:08+00:00",
            "start_date": "2024-11-18"
          },
          "num_episodes": 24,
          "start_season": {
            "year": 2020,
            "season": "fall"
          },
          "broadcast": {
            "day_of_the_week": "saturday",
            "start_time": "01:25"
          },
          "source": "manga",
          "average_episode_duration": 1435,
          "rating": "r",
          "pictures": [
            {
              "medium": "https://myanimelist.net/images/anime/1909/104931.jpg",
              "large": "https://myanimelist.net/images/anime/1909/104931l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1046/107701.jpg",
              "large": "https://myanimelist.net/images/anime/1046/107701l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1171/109222.jpg",
              "large": "https://myanimelist.net/images/anime/1171/109222l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1937/111424.jpg",
              "large": "https://myanimelist.net/images/anime/1937/111424l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1132/128063.jpg",
              "large": "https://myanimelist.net/images/anime/1132/128063l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1610/128064.jpg",
              "large": "https://myanimelist.net/images/anime/1610/128064l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1844/128065.jpg",
              "large": "https://myanimelist.net/images/anime/1844/128065l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1568/128066.jpg",
              "large": "https://myanimelist.net/images/anime/1568/128066l.jpg"
            }
          ],
          "background": "Winner of the Anime of the Year (TV Series) at the 2022 Tokyo Anime Award Festival (TAAF).",
          "related_anime": [
            {
              "node": {
                "id": 38777,
                "title": "Jujutsu Kaisen Official PVs",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1792/96959.webp",
                  "large": "https://myanimelist.net/images/anime/1792/96959l.webp"
                }
              },
              "relation_type": "other",
              "relation_type_formatted": "Other"
            },
            {
              "node": {
                "id": 48561,
                "title": "Jujutsu Kaisen 0 Movie",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1121/119044.jpg",
                  "large": "https://myanimelist.net/images/anime/1121/119044l.jpg"
                }
              },
              "relation_type": "prequel",
              "relation_type_formatted": "Prequel"
            },
            {
              "node": {
                "id": 51009,
                "title": "Jujutsu Kaisen 2nd Season",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1792/138022.jpg",
                  "large": "https://myanimelist.net/images/anime/1792/138022l.jpg"
                }
              },
              "relation_type": "sequel",
              "relation_type_formatted": "Sequel"
            },
            {
              "node": {
                "id": 52558,
                "title": "Vivid Vice",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1067/126222.webp",
                  "large": "https://myanimelist.net/images/anime/1067/126222l.webp"
                }
              },
              "relation_type": "other",
              "relation_type_formatted": "Other"
            },
            {
              "node": {
                "id": 56243,
                "title": "Jujutsu Kaisen 2nd Season Recaps",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1190/137716.jpg",
                  "large": "https://myanimelist.net/images/anime/1190/137716l.jpg"
                }
              },
              "relation_type": "summary",
              "relation_type_formatted": "Summary"
            }
          ],
          "related_manga": [],
          "recommendations": [
            {
              "node": {
                "id": 38000,
                "title": "Kimetsu no Yaiba",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1286/99889.webp",
                  "large": "https://myanimelist.net/images/anime/1286/99889l.webp"
                }
              },
              "num_recommendations": 73
            },
            {
              "node": {
                "id": 44511,
                "title": "Chainsaw Man",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1806/126216.webp",
                  "large": "https://myanimelist.net/images/anime/1806/126216l.webp"
                }
              },
              "num_recommendations": 50
            },
            {
              "node": {
                "id": 269,
                "title": "Bleach",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1541/147774.webp",
                  "large": "https://myanimelist.net/images/anime/1541/147774l.webp"
                }
              },
              "num_recommendations": 41
            },
            {
              "node": {
                "id": 20,
                "title": "Naruto",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1141/142503.jpg",
                  "large": "https://myanimelist.net/images/anime/1141/142503l.jpg"
                }
              },
              "num_recommendations": 32
            },
            {
              "node": {
                "id": 20507,
                "title": "Noragami",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1886/128266.webp",
                  "large": "https://myanimelist.net/images/anime/1886/128266l.webp"
                }
              },
              "num_recommendations": 17
            },
            {
              "node": {
                "id": 11061,
                "title": "Hunter x Hunter (2011)",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1337/99013.webp",
                  "large": "https://myanimelist.net/images/anime/1337/99013l.webp"
                }
              },
              "num_recommendations": 15
            },
            {
              "node": {
                "id": 1735,
                "title": "Naruto: Shippuuden",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1565/111305.webp",
                  "large": "https://myanimelist.net/images/anime/1565/111305l.webp"
                }
              },
              "num_recommendations": 13
            },
            {
              "node": {
                "id": 9919,
                "title": "Ao no Exorcist",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/10/75195.webp",
                  "large": "https://myanimelist.net/images/anime/10/75195l.webp"
                }
              },
              "num_recommendations": 13
            },
            {
              "node": {
                "id": 31964,
                "title": "Boku no Hero Academia",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/10/78745.jpg",
                  "large": "https://myanimelist.net/images/anime/10/78745l.jpg"
                }
              },
              "num_recommendations": 12
            },
            {
              "node": {
                "id": 32182,
                "title": "Mob Psycho 100",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/8/80356.webp",
                  "large": "https://myanimelist.net/images/anime/8/80356l.webp"
                }
              },
              "num_recommendations": 9
            }
          ],
          "studios": [
            {
              "id": 569,
              "name": "MAPPA"
            }
          ],
          "statistics": {
            "status": {
              "watching": "365179",
              "completed": "2368148",
              "on_hold": "49783",
              "dropped": "36296",
              "plan_to_watch": "207842"
            },
            "num_list_users": 3027248
          }
        }
    """.trimIndent()

    val json = Json {
//        ignoreUnknownKeys = true // prevents crashes if extra fields exist
        isLenient = true         // allows relaxed JSON
    }

    val cachedMalAnimeDetails = json.decodeFromString<MalMediaDto>(rawJsonMal)


    val aniListAnimeId: Int = 113415 // Jujustu kaisen
    val malAnimeId: Int = 40748
    val aniListApi: AniListApi = koinInject()
    val malApi: MalApi = koinInject()

    var aniListAnimeDetails: AniListMedia? by remember { mutableStateOf(null) }
    var malAnimeDetails: MalMediaDto? by remember { mutableStateOf(null) }
//    malAnimeDetails = user
//    var mangaDetails: MangaDetails? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (cachedAniListAnimeDetails != null) {
            aniListAnimeDetails = cachedAniListAnimeDetails
        } else {
            val media = aniListApi.getMediaDetails(aniListAnimeId, MediaType.ANIME).data?.media
            aniListAnimeDetails = media
        }

    }

    LaunchedEffect(Unit) {
        if (rawJsonMal != null) {
            malAnimeDetails = cachedMalAnimeDetails
        } else {
            val media = malApi.getMediaDetails(MediaType.MANGA, malAnimeId)
            malAnimeDetails = media
        }

    }

    SelectionContainer {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Text(text = "Anime Details", style = MaterialTheme.typography.displayLarge)
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Text(text = "AniList Anime Details:", style = MaterialTheme.typography.titleLarge)
            Text(
                text = aniListAnimeDetails?.toMediaDetailsDomain().toString(),
                style = MaterialTheme.typography.bodyLarge
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth().height(5.dp))
            Text(text = "Mal Anime Details", style = MaterialTheme.typography.titleLarge)
            Text(
                text = malAnimeDetails?.toMediaDetailsDomain(mediaType = MediaType.ANIME).toString(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun MangaDetailPreview() {
    val cachedAniListMangaDetails = AniListMedia(
        id = 138222,
        type = MediaType.MANGA.name,
        title = AniListTitle(
            romaji = "Singwa Hamkke Level Up",
            english = "Level Up with the Gods",
            native = "신과 함께 레벨업",
            userPreferred = "Level Up with the Gods"
        ),
        synonyms = listOf("Leveling With The Gods", "เลเวลอัปไปกับเทพเจ้า", "神と共にレベルアップ"),
        countryOfOrigin = "KR",
        coverImage = AniListCoverImage(
            large = "https://s4.anilist.co/file/anilistcdn/media/manga/cover/medium/bx138222-PyVZk5MOWP07.png",
            extraLarge = "https://s4.anilist.co/file/anilistcdn/media/manga/cover/large/bx138222-PyVZk5MOWP07.png",
            medium = null
        ),
        bannerImage = "https://s4.anilist.co/file/anilistcdn/media/manga/banner/138222-sfWXqL0VfFl9.jpg",
        description = "“Maybe Inner Gods can never defeat Outer Gods…” so thought Yu-Won Kim, a plucky, high-ranking warrior, after a bitter defeat. But Yu-Won is too tenacious to give up. His loss becomes the dawn of a new journey as a returnee. With renewed determination, Yu-Won starts back from where he began, smashing monster after monster and relearning his skills before taking on the Tower once again. But can he fight his way through the tutorials and level up once more, or will his conquest crumble before him?\n\n(Source: Tapas, edited)",
        status = MediaStatus.RELEASING.name,
        format = MediaFormat.MANGA.name,
        source = MediaSourceMaterial.NOVEL.name,
        isAdult = false,
        startDate = AniListDate(year = 2021, month = 8, day = 14),
        endDate = AniListDate(year = null, month = null, day = null),
        season = null,
        seasonYear = null,
        genres = listOf("Action", "Fantasy"),
        tags = listOf(
            AniListTag(
                name = "Gods",
                description = "Prominently features a character of divine or religious nature.",
                rank = 89,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Full Color",
                description = "Manga that were initially published in full color.",
                rank = 82,
                isMediaSpoiler = false,
                category = "Technical"
            ),
            AniListTag(
                name = "Male Protagonist",
                description = "Main character is male.",
                rank = 82,
                isMediaSpoiler = false,
                category = "Cast-Main Cast"
            ),
            AniListTag(
                name = "Mythology",
                description = "Prominently features mythological elements, especially those from religious or cultural tradition.",
                rank = 81,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Time Manipulation",
                description = "Prominently features time-traveling or other time-warping phenomena.",
                rank = 78,
                isMediaSpoiler = false,
                category = "Theme-Sci-Fi"
            ),
            AniListTag(
                name = "Magic",
                description = "Prominently features magical elements or the use of magic.",
                rank = 77,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Survival",
                description = "Centers around the struggle to live in spite of extreme obstacles.",
                rank = 75,
                isMediaSpoiler = false,
                category = "Theme-Other"
            ),
            AniListTag(
                name = "Death Game",
                description = "Features characters participating in a game, where failure results in death.",
                rank = 70,
                isMediaSpoiler = false,
                category = "Theme-Other"
            ),
            AniListTag(
                name = "Age Regression",
                description = "Prominently features a character who was returned to a younger state.",
                rank = 65,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Post-Apocalyptic",
                description = "Partly or completely set in a world or civilization after a global disaster.",
                rank = 62,
                isMediaSpoiler = false,
                category = "Setting-Universe"
            ),
            AniListTag(
                name = "Long Strip",
                description = "Manga originally published in a vertical, long-strip format, designed for viewing on smartphones. Also known as webtoons.",
                rank = 20,
                isMediaSpoiler = false,
                category = "Technical"
            )
        ),
        averageScore = 78,
        meanScore = 78,
        popularity = 26812,
        favourites = 1043,
        episodes = null,
        duration = null,
        chapters = null,
        volumes = null,
        nextAiringEpisode = null,
        studios = null,
        trailer = AniListTrailer(
            id = "xhObyLPUq6w",
            site = "Youtube",
            thumbnail = "https://i.ytimg.com/vi/xhObyLPUq6w/hqdefault.jpg"
        ),
        externalLinks = listOf(
            AniListExternalLink(
                url = "https://page.kakao.com/home?seriesId=57311327",
                site = "KakaoPage",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/55-Q8bDHOAd7vBl.png"
            ),
            AniListExternalLink(
                url = "https://tapas.io/series/level-up-with-the-gods/info",
                site = "Tapas",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/75-RqhaL2l9Eya2.png"
            ),
            AniListExternalLink(
                url = "https://piccoma.com/web/product/82357",
                site = "Piccoma",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/63-6UOHPZ06XAp7.png"
            ),
            AniListExternalLink(
                url = "https://webtoon.kakao.com/content/신과-함께-레벨업/2708",
                site = "Kakao Webtoon",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/137-wC5kGLPmxvKz.png"
            ),
            AniListExternalLink(
                url = "https://yenpress.com/series/level-up-with-the-gods",
                site = "Yen Press",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/183-l4ZkVEmBY5Af.png"
            )
        ),
        characters = AniListCharacterConnection(
            edges = listOf(
                AniListCharacterEdge(
                    role = CharacterRole.MAIN.name,
                    node = AniListCharacterNode(
                        id = 266306,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b266306-TelgD3HC5EhO.jpg")
                    )
                )
            )
        ),
        relations = AniListRelationConnection(edges = emptyList()),
        staff = AniListStaffConnection(
            edges = listOf(
                AniListStaffEdge(
                    role = "Original Story",
                    node = AniListStaffNode(
                        id = 170305,
                        name = AniListTitle(romaji = null, english = null, native = null, userPreferred = "Heugain"),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/default.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Story & Art",
                    node = AniListStaffNode(
                        id = 245252,
                        name = AniListTitle(romaji = null, english = null, native = null, userPreferred = "O-Hyeon"),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n245252-eFkNz54VSTPf.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Editing (French)",
                    node = AniListStaffNode(
                        id = 368010,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Douglas De Almeida"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/default.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                )
            )
        ),
        mediaListEntry = AniListMediaListEntry(
            id = 441278851,
            mediaId = null,
            status = TrackStatus.CURRENT.name,
            progress = 167,
            score = 8.9,
            media = null
        )
    )

    val rawJsonMal: String = """
        {
  "id": 148009,
  "title": "Level Up with the Gods",
  "main_picture": {
    "medium": "https://myanimelist.net/images/manga/2/308830.webp",
    "large": "https://myanimelist.net/images/manga/2/308830l.webp"
  },
  "alternative_titles": {
    "synonyms": [
      "Leveling with the Gods",
      "Sin-gwa Hamkke Level Up"
    ],
    "en": "Level Up with the Gods",
    "ja": "신과 함께 레벨업"
  },
  "start_date": "2021-08-14",
  "synopsis": "\"Maybe Inner Gods can never defeat Outer Gods...\" so thought Yuwon Kim, a plucky, high-ranking warrior, after a bitter defeat. But Yuwon is too tenacious to give up. His loss becomes the dawn of a new journey as a returnee. With renewed determination, Yuwon starts back from where he began, smashing monster after monster and relearning his skills before taking on the Tower once again. But can he fight his way through the tutorials and level up once more, or will his conquest crumble before him?\n\n(Source: Tapas Media)",
  "mean": 7.65,
  "rank": 2331,
  "popularity": 880,
  "num_list_users": 25056,
  "num_scoring_users": 7752,
  "nsfw": "white",
  "created_at": "1970-01-01T00:00:00+00:00",
  "updated_at": "2025-08-18T00:21:11+00:00",
  "media_type": "manhwa",
  "status": "currently_publishing",
  "genres": [
    {
      "id": 1,
      "name": "Action"
    },
    {
      "id": 2,
      "name": "Adventure"
    },
    {
      "id": 10,
      "name": "Fantasy"
    },
    {
      "id": 79,
      "name": "Time Travel"
    }
  ],
  "my_list_status": {
    "status": "reading",
    "is_rereading": false,
    "num_volumes_read": 0,
    "num_chapters_read": 167,
    "score": 9,
    "updated_at": "2026-04-29T01:16:18+00:00",
    "start_date": "2025-05-13"
  },
  "num_chapters": 0,
  "num_volumes": 0,
  "pictures": [
    {
      "medium": "https://myanimelist.net/images/manga/2/262686.jpg",
      "large": "https://myanimelist.net/images/manga/2/262686l.jpg"
    },
    {
      "medium": "https://myanimelist.net/images/manga/2/308830.jpg",
      "large": "https://myanimelist.net/images/manga/2/308830l.jpg"
    }
  ],
  "background": "Level Up with the Gods is originally a webtoon series that has been published in book format by D&C Media (디앤씨미디어) since April 29, 2024. It has been published digitally in English by Tapas Media since January 11, 2022. The volumes have been published in English by Yen Press since July 22, 2025.",
  "related_anime": [],
  "related_manga": [],
  "recommendations": [
    {
      "node": {
        "id": 121496,
        "title": "Solo Leveling",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/3/222295.webp",
          "large": "https://myanimelist.net/images/manga/3/222295l.webp"
        }
      },
      "num_recommendations": 2
    },
    {
      "node": {
        "id": 132214,
        "title": "Omniscient Reader's Viewpoint",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/2/238873.webp",
          "large": "https://myanimelist.net/images/manga/2/238873l.webp"
        }
      },
      "num_recommendations": 2
    },
    {
      "node": {
        "id": 130216,
        "title": "Tomb Raider King",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/1/239408.jpg",
          "large": "https://myanimelist.net/images/manga/1/239408l.jpg"
        }
      },
      "num_recommendations": 2
    },
    {
      "node": {
        "id": 146385,
        "title": "The Divine Twilight's Return",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/1/264059.jpg",
          "large": "https://myanimelist.net/images/manga/1/264059l.jpg"
        }
      },
      "num_recommendations": 2
    },
    {
      "node": {
        "id": 122663,
        "title": "Tower of God",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/2/223694.jpg",
          "large": "https://myanimelist.net/images/manga/2/223694l.jpg"
        }
      },
      "num_recommendations": 1
    },
    {
      "node": {
        "id": 132247,
        "title": "A Returner's Magic Should Be Special",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/2/239089.webp",
          "large": "https://myanimelist.net/images/manga/2/239089l.webp"
        }
      },
      "num_recommendations": 1
    },
    {
      "node": {
        "id": 147324,
        "title": "Second Life Ranker",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/2/261257.webp",
          "large": "https://myanimelist.net/images/manga/2/261257l.webp"
        }
      },
      "num_recommendations": 1
    },
    {
      "node": {
        "id": 147727,
        "title": "Overgeared",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/3/303099.jpg",
          "large": "https://myanimelist.net/images/manga/3/303099l.jpg"
        }
      },
      "num_recommendations": 1
    },
    {
      "node": {
        "id": 147995,
        "title": "The Player Who Can't Level Up",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/3/262642.jpg",
          "large": "https://myanimelist.net/images/manga/3/262642l.jpg"
        }
      },
      "num_recommendations": 1
    },
    {
      "node": {
        "id": 150210,
        "title": "After Ten Millennia in Hell",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/1/266274.webp",
          "large": "https://myanimelist.net/images/manga/1/266274l.webp"
        }
      },
      "num_recommendations": 1
    }
  ],
  "authors": [
    {
      "node": {
        "id": 58860
      },
      "role": "Story"
    },
    {
      "node": {
        "id": 58861
      },
      "role": "Art"
    }
  ]
}
    """.trimIndent()

    val json = Json {
//        ignoreUnknownKeys = true // prevents crashes if extra fields exist
        isLenient = true         // allows relaxed JSON
    }

    val cachedMalMangaDetails = json.decodeFromString<MalMediaDto>(rawJsonMal)


    val aniListMangaId: Int = 138222 // Leveling up with gods
    val malMangaId: Int = 148009
    val aniListApi: AniListApi = koinInject()
    val malApi: MalApi = koinInject()

    var aniListMangaDetails: AniListMedia? by remember { mutableStateOf(null) }
    var malMangaDetails: MalMediaDto? by remember { mutableStateOf(null) }
//    malMangaDetails = user
//    var mangaDetails: MangaDetails? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (cachedAniListMangaDetails != null) {
            aniListMangaDetails = cachedAniListMangaDetails
        } else {
            val media = aniListApi.getMediaDetails(aniListMangaId, MediaType.MANGA).data?.media
            aniListMangaDetails = media
        }

    }

    LaunchedEffect(Unit) {
        if (rawJsonMal != null) {
            malMangaDetails = cachedMalMangaDetails
        } else {
            val media = malApi.getMediaDetails(MediaType.MANGA, malMangaId)
            malMangaDetails = media
        }

    }

    SelectionContainer {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Text(text = "Manga Details", style = MaterialTheme.typography.displayLarge)
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Text(text = "AniList Manga Details:", style = MaterialTheme.typography.titleLarge)
            Text(
                text = aniListMangaDetails.toString(),
                style = MaterialTheme.typography.bodyLarge
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth().height(5.dp))
            Text(text = "Mal Manga Details", style = MaterialTheme.typography.titleLarge)
            Text(
                text = malMangaDetails?.toMediaDetailsDomain(MediaType.MANGA).toString(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun Token() {
//    val userRepository: UserRepository = koinInject()
//    val users by userRepository.allUsers.collectAsState(initial = listOf())
//    Column {
//        users.forEach {
//            UserProfileItem(
//                user = it,
//                onAvatarClick = {},
//                onLogout = {},
//                isExpanded = true,
//                onClick = {},
//                modifier = Modifier
//            )
//        }
//    }
    val userSettings: UserSettings = koinInject()
    LaunchedEffect(Unit) {
        userSettings.updatePreferences {
            it.copy(
                activeProvider = ProviderType.MYANIMELIST,
                homeTimeout = TimeUnit.DAYS.toMillis(30) // TODO: fix in production
            )
        }

    }

    val authRepository: AuthRepository = koinInject()

    var token by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        token = authRepository.getAccessToken(ProviderType.MYANIMELIST)
    }

    SelectionContainer {
        Text(token ?: "No token provided")
    }
}




