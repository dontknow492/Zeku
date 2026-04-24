package com.ghost.zeku

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.toEntity
import com.ghost.zeku.data.repository.DataResult
import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.model.media.AnimeDetails
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.repository.MediaRepository
import com.ghost.zeku.domain.repository.UserSettings
import com.ghost.zeku.presentation.components.hero.HeroCarousel
import com.ghost.zeku.presentation.components.hero.toHeroUiData
import com.ghost.zeku.presentation.components.list.MediaListCard
import com.ghost.zeku.presentation.components.list.toMediaListUiData
import com.ghost.zeku.presentation.components.poster.MediaPosterCard
import com.ghost.zeku.presentation.components.poster.PosterStyle.OVERLAY
import com.ghost.zeku.presentation.components.poster.toPosterUiData
import com.ghost.zeku.presentation.components.section.MediaSection
import com.ghost.zeku.presentation.components.section.SectionLayout
import com.ghost.zeku.presentation.theme.AppTheme
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.seconds


@Composable
fun App() {


//    val redirectListener: AuthRedirectListener = koinInject()
//    TestAuthScreen(
//        viewModel = AuthViewModel(koinInject(), koinInject())
//    )

    val mediaRepository: MediaRepository = koinInject()
    val userSettings: UserSettings = koinInject()

    val preferences by userSettings.preferences.collectAsStateWithLifecycle()

    Text(preferences.toString())

    LaunchedEffect(Unit) {
        delay(3.seconds)
        userSettings.updatePreferences {
            it.copy(activeProvider = ProviderType.ANILIST)
        }
    }


//    userSettings.setActiveProvider(ProviderType.MYANIMELIST)

    var animeList by remember { mutableStateOf<Flow<PagingData<Anime>>?>(null) }


    val mediaId = when (preferences.activeProvider) {
        ProviderType.MYANIMELIST -> 40748
        ProviderType.ANILIST -> 113415
    }

//    val animeData: PagingData<Anime> by remember { mutableStateOf(PagingData.empty()) }


//    LaunchedEffect(Unit) {
//        val animeDetails = mediaRepository.getAnimeRecommendations(199221).first()
//        Napier.i("Details: $animeDetails")
//    }


//    animeList?.collectAsLazyPagingItems().let { pagingItems ->
//        Text(
//            text = "Total Anime: ${pagingItems?.itemCount}",
//            style = MaterialTheme.typography.displayLarge
//        )
//    }

//    TestReview(mediaRepository)

//    TestApp()


    TestAnimeDetail(mediaRepository, mediaId)


}


@Composable
fun TestReview(mediaRepository: MediaRepository) {
    val pagerFlow = remember { mediaRepository.getAnimeReviews(52991) } // Flow<PagingData<Anime>>
    val lazyItems = pagerFlow.collectAsLazyPagingItems()

    LaunchedEffect(lazyItems.itemCount) {
        Napier.i("Paging items count: ${lazyItems.itemCount}")
        // Log first few items
        for (i in 0 until minOf(10, lazyItems.itemCount)) {
            val item = lazyItems.peek(i) ?: continue
            Napier.i("Item[$i]: $item")
        }
    }


    LazyColumn {
        item {
            Text(text = "Total Anime: ${lazyItems.itemCount}", style = MaterialTheme.typography.displayLarge)
        }
        items(lazyItems.itemCount) { anime ->
            val anime = lazyItems[anime] ?: return@items
            Text(text = anime.toString(), style = MaterialTheme.typography.displayMedium)
        }
    }
}


@Composable
fun TestRecommendations(mediaRepository: MediaRepository) {
    val pagerFlow = remember { mediaRepository.getAnimeRecommendations(52991) } // Flow<PagingData<Anime>>
    val lazyItems = pagerFlow.collectAsLazyPagingItems()

    LaunchedEffect(lazyItems.itemCount) {
        Napier.i("Paging items count: ${lazyItems.itemCount}")
        // Log first few items
        for (i in 0 until minOf(10, lazyItems.itemCount)) {
            val item = lazyItems.peek(i) ?: continue
            Napier.i("Item[$i]: $item")
        }
    }


    LazyColumn {
        item {
            Text(text = "Total Anime: ${lazyItems.itemCount}", style = MaterialTheme.typography.displayLarge)
        }
        items(lazyItems.itemCount) { anime ->
            val anime = lazyItems[anime] ?: return@items
            Text(text = anime.toString(), style = MaterialTheme.typography.displayMedium)
        }
    }
}

@Composable
fun TestAnimeDetail(mediaRepository: MediaRepository, id: Int) {
    val animeDetailsFlow = remember { mediaRepository.getAnimeDetails(id) } // Flow<PagingData<Anime>>
    var animeDetails: DataResult<AnimeDetails>? by remember { mutableStateOf(null) }

    LaunchedEffect(animeDetailsFlow) {
        Napier.i("Anime details: $animeDetailsFlow")
        animeDetailsFlow.collect {
            Napier.i("Anime details: $it")
            animeDetails = it
        }
    }

    Text(
        text = animeDetails.toString(),
        style = MaterialTheme.typography.displaySmall
    )

}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TestApp() {
    val database: AppDatabase = koinInject()
    val mangaList = listOf(
        Manga(
            id = 2,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Berserk",
                english = "Berserk",
                native = "?????"
            ),
            coverImage = "https://myanimelist.net/images/manga/1/157897l.webp",
            bannerImage = null,
            description = "Guts, a former mercenary now known as the Black Swordsman, is out for revenge. After a tumultuous childhood, he finally finds someone he respects and believes he can trust, only to have everything fall apart when this person takes away everything important to Guts for the purpose of fulfilling his own desires. Now marked for death, Guts becomes condemned to a fate in which he is relentlessly pursued by demonic beings.\n\nSetting out on a dreadful quest riddled with misfortune, Guts, armed with a massive sword and monstrous strength, will let nothing stop him, not even death itself, until he is finally able to take the head of the one who stripped him—and his loved one—of their humanity.\n\n[Written by MAL Rewrite]\n\nIncluded one-shot:\nVolume 14: Berserk: The Prototype",
            genres = listOf(
                "Action",
                "Adventure",
                "Award Winning",
                "Drama",
                "Fantasy",
                "Gore",
                "Horror",
                "Military",
                "Psychological",
                "Seinen"
            ),
            status = MediaReleaseStatus.RELEASING,
            score = 9.46f,
            startDate = MediaDate(year = 1989, month = 8, day = 25),
            chapters = 0,
            volumes = 0,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 1706,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "JoJo no Kimyou na Bouken Part 7: Steel Ball Run",
                english = "JoJo's Bizarre Adventure Part 7: Steel Ball Run",
                native = "ジョジョの奇妙な冒険 Part7 STEEL BALL RUN"
            ),
            coverImage = "https://myanimelist.net/images/manga/3/179882l.webp",
            bannerImage = null,
            description = "In the American Old West, the world's greatest race is about to begin. Thousands line up in San Diego to travel over six thousand kilometers for a chance to win the grand prize of fifty million dollars. With the era of the horse reaching its end, contestants are allowed to use any kind of vehicle they wish. Competitors will have to endure grueling conditions, traveling up to a hundred kilometers a day through uncharted wastelands. The Steel Ball Run is truly a one-of-a-kind event.\n\nThe youthful Johnny Joestar, a crippled former horse racer, has come to San Diego to watch the start of the race. There he encounters Gyro Zeppeli, a racer with two steel balls at his waist instead of a gun. Johnny witnesses Gyro using one of his steel balls to unleash a fantastical power, compelling a man to fire his gun at himself during a duel. In the midst of the action, Johnny happens to touch the steel ball and feels a power surging through his legs, allowing him to stand up for the first time in two years. Vowing to find the secret of the steel balls, Johnny decides to compete in the race, and so begins his bizarre adventure across America on the Steel Ball Run.\n\n[Written by MAL Rewrite]",
            genres = listOf("Action", "Adventure", "Historical", "Mystery", "Seinen", "Shounen", "Supernatural"),
            status = MediaReleaseStatus.FINISHED,
            score = 9.34f,
            startDate = MediaDate(year = 2004, month = 1, day = 19),
            chapters = 96,
            volumes = 24,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 656,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Vagabond",
                english = "Vagabond",
                native = "バガボンド"
            ),
            coverImage = "https://myanimelist.net/images/manga/1/259070l.webp",
            bannerImage = null,
            description = "In 16th-century Japan, Shinmen Takezou is a wild, rough young man in both his appearance and his actions. His aggressive nature has won him the collective reproach and fear of his village, leading him and his best friend, Matahachi Honiden, to run away in search of something grander than provincial life. The pair enlist in the Toyotomi army, yearning for glory—but when the Toyotomi suffer a crushing defeat at the hands of the Tokugawa Clan at the Battle of Sekigahara, the friends barely make it out alive.\n\nAfter the two are separated, Shinmen returns home on a self-appointed mission to notify the Hon'iden family of Matahachi's survival. He instead finds himself a wanted criminal, framed for his friend's supposed murder based on his history of violence. Upon being captured, he is strung up on a tree and left to die. An itinerant monk, the distinguished Takuan Soho, takes pity on the \"devil child,\" secretly freeing Shinmen and christening him with a new name to avoid pursuit by the authorities: Musashi Miyamoto.\n\nVagabond is the fictitious retelling of the life of one of Japan's most renowned swordsmen, the \"Sword Saint\" Musashi Miyamoto—his rise from a swordsman with no desire other than to become \"Invincible Under the Heavens\" to an enlightened warrior who slowly learns of the importance of close friends, self-reflection, and life itself.\n\n[Written by MAL Rewrite]",
            genres = listOf("Action", "Adventure", "Award Winning", "Historical", "Samurai", "Seinen"),
            status = MediaReleaseStatus.HIATUS,
            score = 9.27f,
            startDate = MediaDate(year = 1998, month = 9, day = 3),
            chapters = 327,
            volumes = 37,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 13,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "One Piece",
                english = "One Piece",
                native = "ONE PIECE"
            ),
            coverImage = "https://myanimelist.net/images/manga/2/253146l.jpg",
            bannerImage = null,
            description = "Gol D. Roger, a man referred to as the King of the Pirates, is set to be executed by the World Government. But just before his demise, he confirms the existence of a great treasure, One Piece, located somewhere within the vast ocean known as the Grand Line. Announcing that One Piece can be claimed by anyone worthy enough to reach it, the King of the Pirates is executed and the Great Age of Pirates begins.\n\nTwenty-two years later, a young man by the name of Monkey D. Luffy is ready to embark on his own adventure, searching for One Piece and striving to become the new King of the Pirates. Armed with just a straw hat, a small boat, and an elastic body, he sets out on a fantastic journey to gather his own crew and a worthy ship that will take them across the Grand Line to claim the greatest status on the high seas.\n\n[Written by MAL Rewrite]",
            genres = listOf("Action", "Adventure", "Fantasy", "Shounen"),
            status = MediaReleaseStatus.RELEASING,
            score = 9.21f,
            startDate = MediaDate(year = 1997, month = 7, day = 22),
            chapters = 0,
            volumes = 0,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 162032,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Guimi Zhi Zhu",
                english = "Lord of Mysteries",
                native = "鬼灭之柱"
            ),
            coverImage = "https://myanimelist.net/images/manga/2/287344l.webp",
            bannerImage = null,
            description = "Through the storm of steam and machinery, who can achieve the extraordinary? In the mist of history and darkness, who whispers to me? When I woke up from the haze of mystery, I found myself in a world of guns, cannons, giant ships, airships, difference engines; potions, divination, curses, hanged men, and sealed artifacts. But the light still shines between it all, and the mystery is never more than two steps away. This is the legend of the Fool.\n\n(Source: Yen Press)",
            genres = listOf("Action", "Fantasy", "Isekai", "Mystery", "Suspense", "Urban Fantasy"),
            status = MediaReleaseStatus.FINISHED,
            score = 9.17f,
            startDate = MediaDate(year = 2020, month = 5, day = 1),
            chapters = 366,
            volumes = 18,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 1,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Monster",
                english = "Monster",
                native = "MONSTER"
            ),
            coverImage = "https://myanimelist.net/images/manga/3/258224l.webp",
            bannerImage = null,
            description = "Kenzou Tenma, a renowned Japanese neurosurgeon working in post-war Germany, faces a difficult choice: to operate on Johan Liebert, an orphan boy on the verge of death, or on the mayor of Düsseldorf. In the end, Tenma decides to gamble his reputation by saving Johan, effectively leaving the mayor for dead.\n\nAs a consequence of his actions, hospital director Heinemann strips Tenma of his position, and Heinemann's daughter Eva breaks off their engagement. Disgraced and shunned by his colleagues, Tenma loses all hope of a successful career—that is, until the mysterious killing of Heinemann gives him another chance.\n\nNine years later, Tenma is the head of the surgical department and close to becoming the director himself. Although all seems well for him at first, he soon becomes entangled in a chain of gruesome murders that have taken place throughout Germany. The culprit is a monster—the same one that Tenma saved on that fateful day nine years ago.\n\n[Written by MAL Rewrite]",
            genres = listOf("Adult Cast", "Award Winning", "Drama", "Mystery", "Psychological", "Seinen"),
            status = MediaReleaseStatus.FINISHED,
            score = 9.16f,
            startDate = MediaDate(year = 1994, month = 12, day = 5),
            chapters = 162,
            volumes = 18,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 51,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Slam Dunk",
                english = "Slam Dunk",
                native = "SLAM DUNK"
            ),
            coverImage = "https://myanimelist.net/images/manga/2/258749l.webp",
            bannerImage = null,
            description = "Hanamichi Sakuragi, a tall, boisterous teenager with flame-red hair and physical strength beyond his years, is eager to put an end to his rejection streak of 50 and finally score a girlfriend as he begins his first year of Shohoku High. However, his reputation for delinquency and destructiveness precedes him, and most of his fellow students subsequently avoid him like the plague. As his first day of school ends, he is left with two strong thoughts: \"I hate basketball\" and \"I need a girlfriend.\"\n\nHaruko Akagi, ignorant of Hanamichi's history of misbehavior, notices his immense height and unwittingly approaches him, asking whether or not he likes basketball. Overcome by the fact that a girl is speaking to him, the red-haired giant blurts out a yes despite his true feelings. At the gym, Haruko asks if he can do a slam dunk. Though a complete novice, Hanamachi palms the ball and makes the leap...but overshoots, slamming his head into the backboard. Amazed by his near-inhuman physical abilities, Haruko quickly notifies the school's basketball captain of his feat. With this, Hanamichi is unexpectedly thrust into a world of competition for a girl he barely knows, but he soon discovers that there is perhaps more to basketball than he once thought.\n\n[Written by MAL Rewrite]",
            genres = listOf("Award Winning", "School", "Shounen", "Sports", "Team Sports"),
            status = MediaReleaseStatus.FINISHED,
            score = 9.09f,
            startDate = MediaDate(year = 1990, month = 9, day = 18),
            chapters = 276,
            volumes = 31,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 642,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Vinland Saga",
                english = "Vinland Saga",
                native = "ヴィンランド・サガ"
            ),
            coverImage = "https://myanimelist.net/images/manga/2/188925l.jpg",
            bannerImage = null,
            description = "Thorfinn, son of one of the Vikings' greatest warriors, is among the finest fighters in the merry band of mercenaries run by the cunning Askeladd, an impressive feat for a person his age. However, Thorfinn is not part of the group for the plunder it entails—instead, for having caused his family great tragedy, the boy has vowed to kill Askeladd in a fair duel. Not yet skilled enough to defeat him, but unable to abandon his vengeance, Thorfinn spends his boyhood with the mercenary crew, honing his skills on the battlefield among the war-loving Danes, where killing is just another pleasure of life.\n\nOne day, when Askeladd receives word that Danish prince Canute has been taken hostage, he hatches an ambitious plot—one that will decide the next King of England and drastically alter the lives of Thorfinn, Canute, and himself. Set in 11th-century Europe, Vinland Saga tells a bloody epic in an era where violence, madness, and injustice are inescapable, providing a paradise for the battle-crazed and utter hell for the rest who live in it.\n\n[Written by MAL Rewrite]\n\nIncluded one-shots:\nVolume 23: Sayounara ga Chikai node (For Our Farewell Is Near)\nVolume 25: \"Assassin's Creed Valhalla\" Collab Bangai-hen (Assassin's Creed x Vinland Saga)",
            genres = listOf("Action", "Adventure", "Award Winning", "Drama", "Historical", "Seinen"),
            status = MediaReleaseStatus.FINISHED,
            score = 9.09f,
            startDate = MediaDate(year = 2005, month = 4, day = 13),
            chapters = 224,
            volumes = 29,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 147272,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "The Greatest Estate Developer",
                english = "The Greatest Estate Developer",
                native = "최고의 부동산 개발자"
            ),
            coverImage = "https://myanimelist.net/images/manga/1/290131l.webp",
            bannerImage = null,
            description = "There is no bigger scum of the earth than Lloyd Frontera. He is the eldest son, yet all he does is drink and intimidate others, depleting what little is left of his family's wealth. The Fronteras' knight, Javier Asrahan, is destined to become a renowned swordmaster, and Lloyd's family will face pitiful deaths as their entire land falls to ruin under insurmountable debt.\n\nThough at some point Kim Suho was an average civil engineering student in Korea, he suddenly wakes up on a dirt road as none other than Lloyd, an ungrateful hooligan from the beginning of a book following Javier, the protagonist. While a sassy status window offers some clarity about his new identity, Suho is rather worried about his imminent downfall.\n\nTo avoid becoming a beggar—and ultimately lead a sweet and comfortable life—Suho decides to fix Lloyd's scumbag image. With his engineering expertise and magical construction skills boost, Suho introduces modern innovations and city developments to this medieval-like world, drastically improving the people's quality of life—all for a nice sum of money.\n\n[Written by MAL Rewrite]",
            genres = listOf("Adventure", "Comedy", "Fantasy", "Isekai", "Reincarnation"),
            status = MediaReleaseStatus.FINISHED,
            score = 9.06f,
            startDate = MediaDate(year = 2021, month = 7, day = 30),
            chapters = 222,
            volumes = 0,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 70345,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Grand Blue",
                english = "Grand Blue Dreaming",
                native = "グランブルー"
            ),
            coverImage = "https://myanimelist.net/images/manga/2/166124l.webp",
            bannerImage = null,
            description = "Among the seaside town of Izu's ocean waves and rays of shining sun, Iori Kitahara is just beginning his freshman year at Izu University. As he moves into his uncle's scuba diving shop, Grand Blue, he eagerly anticipates his dream college life, filled with beautiful girls and good friends.\n\nBut things don't exactly go according to plan. Upon entering the shop, he encounters a group of rowdy, naked upperclassmen, who immediately coerce him into participating in their alcoholic activities. Though unwilling at first, Iori quickly gives in and becomes the heart and soul of the party. Unfortunately, this earns him the scorn of his cousin, Chisa Kotegawa, who walks in at precisely the wrong time. Undeterred, Iori still vows to realize his ideal college life, but will things go according to plan this time, or will his situation take yet another dive?\n\n[Written by MAL Rewrite]",
            genres = listOf("Adult Cast", "Comedy", "Gag Humor", "Seinen"),
            status = MediaReleaseStatus.RELEASING,
            score = 9.05f,
            startDate = MediaDate(year = 2014, month = 4, day = 7),
            chapters = 0,
            volumes = 0,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 25,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Fullmetal Alchemist",
                english = "Fullmetal Alchemist",
                native = "鋼の錬金術師"
            ),
            coverImage = "https://myanimelist.net/images/manga/3/243675l.jpg",
            bannerImage = null,
            description = "Alchemists are knowledgeable and naturally talented individuals who can manipulate and modify matter due to their art. Yet despite the wide range of possibilities, alchemy is not as all-powerful as most would believe. Human transmutation is strictly forbidden, and whoever attempts it risks severe consequences. Even so, siblings Edward and Alphonse Elric decide to ignore this great taboo and bring their mother back to life. Unfortunately, not only do they fail in resurrecting her, they also pay an extremely high price for their arrogance: Edward loses his left leg and Alphonse his entire body. Furthermore, Edward also gives up his right arm in order to seal his brother's soul into a suit of armor.\n\nYears later, the young alchemists travel across the country looking for the Philosopher's Stone, in the hopes of recovering their old bodies with its power. However, their quest for the fated stone also leads them to unravel far darker secrets than they could ever imagine.\n\n[Written by MAL Rewrite]",
            genres = listOf("Action", "Adventure", "Award Winning", "Drama", "Fantasy", "Military", "Shounen"),
            status = MediaReleaseStatus.FINISHED,
            score = 9.04f,
            startDate = MediaDate(year = 2001, month = 7, day = 12),
            chapters = 116,
            volumes = 27,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 16765,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Kingdom",
                english = "Kingdom",
                native = "キングダム"
            ),
            coverImage = "https://myanimelist.net/images/manga/2/171872l.webp",
            bannerImage = null,
            description = "During the Warring States period in China, Xin and Piao are two brother-like youngsters who dream of becoming Great Generals, despite their low status as orphaned slaves. One day, they encounter a man of nobility, who gives Piao an opportunity to undertake an important duty within the state of Qin's royal palace. Parting ways, Xin and Piao promise each other to one day become the greatest generals in the world. However, after a fierce coup d'état occurs in the palace, Xin meets with a dying Piao, whose last words spur him into action and lead him to encounter the young and soon-to-be king of Qin, Zheng Ying.\n\nAlthough initially on bad terms, Xin and Zheng become comrades and start on a path filled with trials and bloodshed. Zheng's objective is to bring all the warring states under Qin, and Xin seeks to climb to the very top of the army ranks. Against a backdrop of constant tactical battle between states and great political unrest, both outside and within the palace, the two endeavor towards their monumental ambitions that will change history forever.\n\n[Written by MAL Rewrite]",
            genres = listOf("Action", "Award Winning", "Historical", "Military", "Seinen"),
            status = MediaReleaseStatus.RELEASING,
            score = 9.02f,
            startDate = MediaDate(year = 2006, month = 1, day = 26),
            chapters = 0,
            volumes = 0,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 44489,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Houseki no Kuni",
                english = "Land of the Lustrous",
                native = "宝石の国"
            ),
            coverImage = "https://myanimelist.net/images/manga/1/115443l.webp",
            bannerImage = null,
            description = "Long ago, Earth was struck by six meteorites, creating six moons and leaving a lone island in their wake. Those who could not make it to the island sank to the bottom of the ocean, where, over time, they slowly turned to crystal. As countless millennia passed, 28 of these crystallized lifeforms, known as the Lustrous, rose from the depths and resided on the island. Led by the wise Kongou-sensei, they must defend themselves against the seemingly infinite number of Lunarians, inhabitants of the moons who seek to harvest their valuable bodies for crafting weapons and jewelry.\n\nHouseki no Kuni centers around Phosphophyllite, a young gem about to turn three hundred years old. Clumsy, extremely fragile, and having no visible talent, they are one of the weakest gems around, unable to help in the fight against the Lunarians in any meaningful way. But as the battle with the otherworldly invaders rages on, they soon learn that power comes at a cost that no one should have to pay.\n\n[Written by MAL Rewrite]",
            genres = listOf("Action", "Anthropomorphic", "Drama", "Fantasy", "Seinen"),
            status = MediaReleaseStatus.FINISHED,
            score = 9.0f,
            startDate = MediaDate(year = 2012, month = 10, day = 25),
            chapters = 108,
            volumes = 13,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 143441,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Omniscient Reader's Viewpoint",
                english = "Omniscient Reader's Viewpoint",
                native = "전지적 독자 시점"
            ),
            coverImage = "https://myanimelist.net/images/manga/1/265768l.webp",
            bannerImage = null,
            description = "In the web novel Three Ways to Survive the Apocalypse, people are forced to fight monsters and each other for coins, food, and—ultimately—survival. While mysterious \"Constellations\" watch the unwilling participants for their own entertainment, one man emerges as the protagonist: Yoo Joonghyuk, who repeatedly lives and dies in an endless struggle to close the book on this indiscriminate torture.\n\nKim Dokja has lived a challenging and action-packed life through Joonghyuk for over a decade. While other readers abandoned the story early on, Dokja has remained its sole reader until the very last chapter. At that time, he receives a puzzling message from the author, and the novel's inhumane world suddenly becomes reality.\n\nAs the only person aware of what lies ahead, Dokja realizes that he must utilize his omniscience to navigate the deadly scenarios and stay alive. Dokja was never meant to play a main part, but in defiance of those who wish to spell his fate, he might just have to take the lead in writing his own conclusion.\n\n[Written by MAL Rewrite]",
            genres = listOf("Action", "Adventure", "Fantasy", "Urban Fantasy"),
            status = MediaReleaseStatus.FINISHED,
            score = 9.0f,
            startDate = MediaDate(year = 2022, month = 1, day = 20),
            chapters = 105,
            volumes = 20,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 133431,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Uma Musume Cinderella Gray",
                english = "",
                native = "ウマ娘 シンデレラグレイ"
            ),
            coverImage = "https://myanimelist.net/images/manga/3/240780l.webp",
            bannerImage = null,
            description = "Uma Musume Cinderella Gray is a spin-off title of the Uma Musume project by Cygames. It follows Oguri Cap through her time at Kasamatsu Training Center Academy and on her journey of becoming a legendary horse girl.\n\n(Source: MU)",
            genres = listOf("Drama", "Seinen", "Slice of Life", "Sports"),
            status = MediaReleaseStatus.FINISHED,
            score = 8.98f,
            startDate = MediaDate(year = 2020, month = 6, day = 11),
            chapters = 211,
            volumes = 23,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 657,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Real",
                english = "Real",
                native = "リアル"
            ),
            coverImage = "https://myanimelist.net/images/manga/2/115969l.webp",
            bannerImage = null,
            description = "Tomomi Nomiya, former captain of his high school's basketball team turned delinquent, decides to drop out of school after he is crushed by the guilt of ruining a young girl's life in a traffic accident. As he dedicates his free time to helping her, he stumbles upon Kiyoharu Togawa, a former sprinter who has lost the use of his right leg and now plays wheelchair basketball as an alternate outlet.\n\nAfter challenging Kiyoharu to a one-on-one game, Tomomi is completely defeated. Inspired by this encounter, he realizes that he can't let his love for basketball die so easily and decides that he will do what he can to help others while striving to become a professional player. Meanwhile, Hisanobu Takahashi, Tomomi's replacement as the high school's team captain, gets into an accident and finds himself permanently paralyzed below the waist. Real tells the touching tale of these three young men as they struggle to overcome their disabilities and inner conflicts in order to achieve their dreams while igniting a passion that will bring them together.\n\n[Written by MAL Rewrite]",
            genres = listOf("Award Winning", "Drama", "Psychological", "Seinen", "Sports", "Team Sports"),
            status = MediaReleaseStatus.RELEASING,
            score = 8.98f,
            startDate = MediaDate(year = 1999, month = 10, day = 28),
            chapters = 0,
            volumes = 0,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 4632,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Oyasumi Punpun",
                english = "Goodnight Punpun",
                native = "おやすみプンプン"
            ),
            coverImage = "https://myanimelist.net/images/manga/3/266834l.jpg",
            bannerImage = null,
            description = "Punpun Onodera is a normal 11-year-old boy living in Japan. Hopelessly idealistic and romantic, Punpun begins to see his life take a subtle—though nonetheless startling—turn to the adult when he meets the new girl in his class, Aiko Tanaka. It is then that the quiet boy learns just how fickle maintaining a relationship can be, and the surmounting difficulties of transitioning from a naive boyhood to a convoluted adulthood. When his father assaults his mother one night, Punpun realizes another thing: those whom he looked up to were not as impressive as he once thought.\n\nAs his problems increase, Punpun's once shy demeanor turns into voluntary reclusiveness. Rather than curing him of his problems and conflicting emotions, this merely intensifies them, sending him down the dark path of maturity in this grim coming-of-age saga.\n\n[Written by MAL Rewrite]",
            genres = listOf("Drama", "Psychological", "Seinen", "Slice of Life"),
            status = MediaReleaseStatus.FINISHED,
            score = 8.98f,
            startDate = MediaDate(year = 2007, month = 3, day = 15),
            chapters = 147,
            volumes = 13,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 34053,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Umineko no Naku Koro ni Chiru - Episode 8: Twilight of the Golden Witch",
                english = "Umineko When They Cry Episode 8: Twilight of the Golden Witch",
                native = "うみねこのなく頃に散 - Episode 8: Twilight of the Golden Witch"
            ),
            coverImage = "https://myanimelist.net/images/manga/3/206205l.webp",
            bannerImage = null,
            description = "It's time once again for the annual family gathering on Rokkenjima! Ange Ushiromiya is just getting over a cold, but that won't dampen her excitement at seeing her cousins again! Granddad couldn't be more thrilled to see his beloved grandchildren and throws a special Halloween party where everyone gets lots of treats! By lunchtime on the first day, the aunts and uncles are chatting up a storm, and the cousins are eagerly planning the afternoon's fun. It's the very picture of a happy family—one that in no way resembles the bickering, bitter Ushiromiya clan! What is going on?!\n\n(Source: Yen Press)",
            genres = listOf("Drama", "Fantasy", "Mystery", "Shounen", "Supernatural"),
            status = MediaReleaseStatus.FINISHED,
            score = 8.98f,
            startDate = MediaDate(year = 2012, month = 1, day = 21),
            chapters = 42,
            volumes = 9,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 130826,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Tian Guan Cifu",
                english = "Heaven Official's Blessing: Tian Guan Ci Fu",
                native = "天官赐福"
            ),
            coverImage = "https://myanimelist.net/images/manga/3/258775l.jpg",
            bannerImage = null,
            description = "Born the crown prince of a prosperous kingdom, Xie Lian was renowned for his beauty, strength, and purity. His years of dedication and noble deeds allowed him to ascend to godhood. But those who rise, can also fall...and fall he does, cast from the Heavens again and again and banished to the mortal realm.\n\nEight hundred years after his mortal life, Xie Lian has ascended to godhood for the third time. Now only a lowly scrap collector, he is dispatched to wander the earthly realm to take on tasks appointed by the heavens to pay back debts and maintain his divinity. Aided by old friends and foes alike, and graced with the company of a mysterious young man with whom he feels an instant connection, Xie Lian must confront the horrors of his past in order to dispel the curse of his present.\n\n(Source: Seven Seas Entertainment)",
            genres = listOf("Action", "Adventure", "Boys Love", "Fantasy", "Historical", "Mythology"),
            status = MediaReleaseStatus.FINISHED,
            score = 8.97f,
            startDate = MediaDate(year = 2021, month = 2, day = 4),
            chapters = 0,
            volumes = 6,
            author = null,
            trackEntry = null
        ),
        Manga(
            id = 1303,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Ashita no Joe",
                english = "Ashita no Joe: Fighting for Tomorrow",
                native = "あしたのジョー"
            ),
            coverImage = "https://myanimelist.net/images/manga/1/268827l.jpg",
            bannerImage = null,
            description = "Joe Yabuki has learned how to toughen up and stop trusting others after a troubling youth spent bouncing between orphanages and fending off bullies. After finally escaping the cycle of violent temporary homes, Joe adopts a delinquent lifestyle in the streets of San'ya, a slum in Tokyo. He makes his way through the world with his fists, picking fights with anyone he pleases.\n\nDanpei Tange, a washed-up and alcoholic former boxing coach, notices Joe's animalistic combat ability and attempts to mentor the wild teenager. Despite developing an explosive relationship, the two form a close bond and rely on each other to look after the other homeless children in San'ya.\n\nUnable to abandon his destructive habits, Joe is arrested for petty crimes. In jail, he meets and fights with lifelong friends and rivals, including the boxing prodigy Tooru Rikiishi. Continuously challenging Rikiishi to fights, Joe is knocked out time after time due to his savage brawling style failing in the face of Rikiishi's controlled boxing technique. But eventually, Joe stuns his rival with a knockout blow, earning Rikiishi's respect and the promise that they will one day meet again inside the professional ring.\n\nFinding a reason to live within boxing, Joe takes life head-on with a new purpose. As he rises through Japan's amateur and professional boxing scenes, he contends with painful loss and increasing injury. Joe's passion for fighting is all-consuming, and he grapples with the idea that a life without boxing may not be one worth living.\n\n[Written by MAL Rewrite]",
            genres = listOf("Combat Sports", "Drama", "Shounen", "Slice of Life", "Sports"),
            status = MediaReleaseStatus.FINISHED,
            score = 8.96f,
            startDate = MediaDate(year = 1968, month = 1, day = 1),
            chapters = 171,
            volumes = 20,
            author = null,
            trackEntry = null
        )
    )


    LaunchedEffect(Unit) {
        Napier.i("Upserting Manga in DB: ${mangaList.size}")
        database.mangaDao().upsertAll(mangaList.map { it.toEntity() })
        Napier.i("Successfully upserted Manga in DB: ${mangaList.size}")
    }

    AppTheme {


        val errorMessage by remember { mutableStateOf<String?>(null) }
        val isLoading by remember { mutableStateOf(false) }

        var currentPage by remember { mutableIntStateOf(0) }

        Scaffold { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                HeroCarousel(
                    items = mangaList.take(5).map { it.toHeroUiData() },
                    currentPage = currentPage,
                    onPageChange = { currentPage = it },
                    onWatchClick = {},
                    onDetailsClick = {},
                )


                MediaSection(
                    title = "Trending Now",
                    items = mangaList,
                    layout = SectionLayout.HORIZONTAL_ROW,
                    onViewAllClick = { /* Navigate to grid */ }
                ) { item, modifier ->
                    MediaPosterCard(
                        data = item.toPosterUiData(),
                        style = OVERLAY,
                        onClick = {},
                        modifier = modifier,
                    )
                }

                MediaSection(
                    title = "Popular",
                    items = mangaList,
                    layout = SectionLayout.HORIZONTAL_ROW,
                    onViewAllClick = { /* Navigate to grid */ }
                ) { item, modifier ->
                    MediaPosterCard(
                        data = item.toPosterUiData(),
                        style = OVERLAY,
                        onClick = {},
                        modifier = modifier
                    )
                }

                MediaSection(
                    title = "Upcoming",
                    items = mangaList,
                    layout = SectionLayout.HORIZONTAL_ROW,
                    onViewAllClick = { /* Navigate to grid */ }
                ) { item, modifier ->
                    MediaPosterCard(
                        data = item.toPosterUiData(),
                        style = OVERLAY,
                        onClick = {},
                        modifier = modifier
                    )
                }


                MediaSection(
                    title = "Trending Now",
                    items = mangaList,
                    layout = SectionLayout.HORIZONTAL_ROW,
                    onViewAllClick = { /* Navigate to grid */ }
                ) { item, modifier ->
                    MediaPosterCard(
                        data = item.toPosterUiData(),
                        style = OVERLAY,
                        onClick = {},
                        modifier = modifier
                    )
                }

                MediaSection(
                    title = "Recommended for You",
                    items = mangaList,
                    layout = SectionLayout.VERTICAL_LIST,
                    isLoading = !true,
                    onViewAllClick = {}
                ) { item, modifier ->
                    MediaListCard(
                        data = item.toMediaListUiData(),
                        onClick = { TODO() },
                        modifier = modifier
                    )
                }

            }
        }


    }

}

