//package com.ghost.zeku.presentation.screen.detail
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
//
//
//
//
//// ---------- Dummy sample data for demonstration ----------
//private fun getSampleState() = MediaDetailState(
//    title = "Neon Vanguard: Legacy of the Core",
//    rating = 9.2f,
//    year = 2024,
//    episodeCount = 24,
//    studio = "Studio Mirai",
//    genres = listOf("Cyberpunk", "Sci-Fi", "Action"),
//    synopsis = "In the year 2088, the megacity of Nova Arkhos is powered by \"The Core,\" a mysterious energy source discovered deep within the tectonic plates. When a series of inexplicable surges threatens to destabilize the city's infrastructure, young technician Ren finds himself at the center of a conspiracy involving ancient technology and a rebel group known as the Neon Vanguard.",
//    heroImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAWjZUfqb0_550IM7vp4EOLV7dRvuWqPRzic5kva1zrnlea2Qvbs4lns-bVvVxT6bp6_hP5mP-kwbCoPQbPdNoYaNPLmgBvdwfoEIFgH01Xm53vd3mRRsxR3D7DF-aE19m5nIN87GrADeDL6xo3SUQoZ2uthdm1bZ1dEFq_Ymx8NeT7CDjB5MiV159xdCPHDNEr0jxMof8RsHz5TpkpITR5K2nKrDAO8zPVRbScmbhtaTqYdiPHjkhmEWOsumoVtNCasBQbARseDAw",
//    posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAplDpXhfdPZKZ1pdcyeMMKlWslT5SAz6uVTPg7z-zyBBqZkYBcq8hLO1ot66IFm9Y039EPXhNNjtGJBgVvZ2mFRo2EzQnRT6kyLx-7_00IMFCCjseOa28KXSrn2137AYeA2_rtkbDTpe1RyHbBZvCxD7Gen5nnzrK-ROUawa-UIJHrO8jm92l4svBjVmgvIi30CpIiWadXK1LRiUHc03PFFatUtaVte72mmlGYFJRXL-pI4MThqSZ_wzQQ4KWHBmE5WaS1oA2Bnew",
//    trailerThumbnailUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAWjZUfqb0_550IM7vp4EOLV7dRvuWqPRzic5kva1zrnlea2Qvbs4lns-bVvVxT6bp6_hP5mP-kwbCoPQbPdNoYaNPLmgBvdwfoEIFgH01Xm53vd3mRRsxR3D7DF-aE19m5nIN87GrADeDL6xo3SUQoZ2uthdm1bZ1dEFq_Ymx8NeT7CDjB5MiV159xdCPHDNEr0jxMof8RsHz5TpkpITR5K2nKrDAO8zPVRbScmbhtaTqYdiPHjkhmEWOsumoVtNCasBQbARseDAw",
//    characters = listOf(
//        Character("Ren Arisawa", "Main Protagonist", "Yuki Kaji", "Johnny Yong", "https://lh3.googleusercontent.com/aida-public/AB6AXuCklx0ow0x9tdSskg9mozEpy0Tk__T5MrD7_7BXbtZ2Bz5QFwVl-cfTJ9Q6UqlV6WZQz_7fEqgOxcgnsY0jGiz0UEy0mJq_RwsejfitOSLreXqv1ISWJkTaRugA5tA7XgTxWvTW-biK5m3PbgSu1rrPs4JV25R1Jm_S5IU4st0fx0I_xLEQFRnlmYelxUHOYopOIUOwe2YbAWperZd-BCAM8fLbfQx4HYqnZkflTcQgJ242JcI9WNfwLrpKmToIFfaWy41I6aHfink"),
//        Character("Luna Vesper", "Vanguard Leader", "Maaya Sakamoto", "Erica Mendez", "https://lh3.googleusercontent.com/aida-public/AB6AXuDdONirMFJN0clayQEa2Vx_ru6VZ5odnqWaDJuj5zlC5jwXZEqT65XD1JLXW9emHPwdZi3i30ptF8lEeO8hS5UVdB3JiomtyeL1FufZQR31T-P8GEAh3UpJ8kj8Pa1fQxlKZy9WFYOR9rmRSwb8VCUhFZzN70P6x5-6elg3TKhHMY2VY8aKEEg1B5FfHmmwicvXfItsQV9JHuL8j7QjxrPhKxOeFU5FmYE8shj2s6XPrV5_0FSpWoFqM23eOM-RQzi1K9-rJfwYqMQ")
//    ),
//    episodes = listOf(
//        Episode(1, "The Spark in the Dark", "Ren discovers a hidden encrypted message...", "https://lh3.googleusercontent.com/aida-public/AB6AXuDKk3vjgGyT3Sk0VU3g-wpiykLuJFSraEmroVk4w6S_KiLbNsLQLVQ09JZhNzPoCvsFeOaPKfKSLvZWW7I3XGVpOTVBPiz7AJ0dJ_8aQaKxuoMgCmhJnJ5CbR2WNYsTXn6txpXzoaECwIW0lDDZ6bobDpwNDkSGgOZlQrhjQftkeAWWOpxYCEV-yHHTyXAEFz9axvsREzw35MLRr0yOok064mXOWeFKd_eRfsMNyLIyiIyn2ZkD0mp8Zfl38B3K77QFcPzQ6bxpBv0", "Oct 12, 2024", "24:12", watched = true),
//        Episode(2, "Rain and Neon", "Pursued by security forces...", "https://lh3.googleusercontent.com/aida-public/AB6AXuBtE_J_Kvw8h9bH12H2hn8EHu4-xNBcGffxT-aOR3mwW8kSfYa2eIMP-gOElbdLS5deOJ5vYuhAz0MZrt376ybY58ZmlL5vJcHhjKKZse6EqsTQpfT4eEA18aqvOhxiPrO7AoqGZE4bziYmU6mTb2anuV8AephfhdlPr2fwNYy4Il7qXMXpJX4XHUndHyPACM4_JrC0M6swHd1uBYusfhT-TFKv_9scnLOs1NXVj7zwit20-zXCFhdMipbOUq5P9kqr32SPucmsmfc", "Oct 19, 2024", "23:55", progressPercent = 0.6f)
//    ),
//    reviews = listOf(
//        UserReview("NeonDreamer_88", 5, "The visual fidelity is absolutely insane...", "2d ago", "ND"),
//        UserReview("CyborgSamurai", 4, "Great start to the season...", "1w ago", "CS")
//    ),
//    relations = listOf(
//        Relation("Prequel", "Neon Vanguard: Zero", "OVA", 2022, "https://lh3.googleusercontent.com/aida-public/AB6AXuC4U4wJAUlIX8Y99jzZo4PpTV_uHHvZLmeBE9bJcaAmfnEKH_V0gfltnCfxJbuoVvY1qIPdqGAFbkFCpZ5ocE9YTsAJ1Ds1SQ28_HkCOmN27lmjT6WA-2jlATDerVEwVPeDJ7W9gCdFiIWQ1lkPaOEcUxPWp-Q9GEo7scC4uzzR62XTEdDgDMYaXk8nw8ijGaj6HVkcVE0qKpWbQ1peW-BD9RlB8hsKToYEmk3vM5F0z3wHqWSeeyWKCEmMo3SvO_oWS2IJ2TG33GM"),
//        Relation("Spin-off", "Arkhos Chronicles", "Novel", 2023, "https://lh3.googleusercontent.com/aida-public/AB6AXuBFCc0idNmFYbTIdPj6YNwvZ7y2fOXgiQ25YKaBqjFKrYBzGO61IDBbFDNYusxq2JJaHTzwC9l53VS5450gkRIbl2ZnOpZE4Jp8xH7KmAWunm4WqIEA_kuQT2HXeQ3g2oR0nD37w-3mHVo-HO_2gMZkrERsuksvvmE-UeOwpUvs0MarInehsGF1faBPuh7sKgs5uLN5_cebi1_7GTGhdcGq58eyeu34lk_FpXylnKhWLyfWijVm04zu9aLHzFQp46uMG4kLZC33Mn0")
//    ),
//    externalLinks = listOf(
//        ExternalLink("Crunchyroll", "https://crunchyroll.com", IconType.CRUNCHYROLL),
//        ExternalLink("Netflix", "https://netflix.com", IconType.NETFLIX),
//        ExternalLink("Official Site", "https://official.com", IconType.WEBSITE),
//        ExternalLink("Twitter/X", "https://twitter.com", IconType.TWITTER)
//    ),
//    recommendations = listOf(
//        MediaThumbnail("Ghost in the Mesh", "https://lh3.googleusercontent.com/aida-public/AB6AXuC4U4wJAUlIX8Y99jzZo4PpTV_uHHvZLmeBE9bJcaAmfnEKH_V0gfltnCfxJbuoVvY1qIPdqGAFbkFCpZ5ocE9YTsAJ1Ds1SQ28_HkCOmN27lmjT6WA-2jlATDerVEwVPeDJ7W9gCdFiIWQ1lkPaOEcUxPWp-Q9GEo7scC4uzzR62XTEdDgDMYaXk8nw8ijGaj6HVkcVE0qKpWbQ1peW-BD9RlB8hsKToYEmk3vM5F0z3wHqWSeeyWKCEmMo3SvO_oWS2IJ2TG33GM", listOf("Sci-Fi", "Action")),
//        MediaThumbnail("Sky Citadel", "https://lh3.googleusercontent.com/aida-public/AB6AXuBFCc0idNmFYbTIdPj6YNwvZ7y2fOXgiQ25YKaBqjFKrYBzGO61IDBbFDNYusxq2JJaHTzwC9l53VS5450gkRIbl2ZnOpZE4Jp8xH7KmAWunm4WqIEA_kuQT2HXeQ3g2oR0nD37w-3mHVo-HO_2gMZkrERsuksvvmE-UeOwpUvs0MarInehsGF1faBPuh7sKgs5uLN5_cebi1_7GTGhdcGq58eyeu34lk_FpXylnKhWLyfWijVm04zu9aLHzFQp46uMG4kLZC33Mn0", listOf("Fantasy", "Drama"))
//    )
//)
//
//// ---------- Intent ----------
//sealed interface MediaDetailIntent {
//    data object ToggleWatchlist : MediaDetailIntent
//    data class MarkEpisodeWatched(val episodeNumber: Int, val watched: Boolean) : MediaDetailIntent
//    data class UpdateEpisodeProgress(val episodeNumber: Int, val progress: Float) : MediaDetailIntent
//    data object TrailerPlayClicked : MediaDetailIntent
//    data object WatchNowClicked : MediaDetailIntent
//    data class WriteReview(val rating: Int, val text: String) : MediaDetailIntent
//    data object LoadData : MediaDetailIntent
//}
//
//// ---------- ViewModel ----------
//class MediaDetailViewModel : ViewModel() {
//    private val _state = MutableStateFlow(MediaDetailState())
//    val state: StateFlow<MediaDetailState> = _state
//
//    init { processIntent(MediaDetailIntent.LoadData) }
//
//    fun processIntent(intent: MediaDetailIntent) {
//        viewModelScope.launch {
//            when (intent) {
//                is MediaDetailIntent.LoadData -> {
//                    _state.update { it.copy(isLoading = true) }
//                    // Simulate network fetch – replace with real repository
//                    val sampleState = getSampleState()
//                    _state.update { sampleState.copy(isLoading = false) }
//                }
//                is MediaDetailIntent.ToggleWatchlist ->
//                    _state.update { it.copy(isWatchlisted = !it.isWatchlisted) }
//                is MediaDetailIntent.MarkEpisodeWatched ->
//                    _state.update { state ->
//                        state.copy(episodes = state.episodes.map { ep ->
//                            if (ep.number == intent.episodeNumber) ep.copy(watched = intent.watched)
//                            else ep
//                        })
//                    }
//                is MediaDetailIntent.UpdateEpisodeProgress ->
//                    _state.update { state ->
//                        state.copy(episodes = state.episodes.map { ep ->
//                            if (ep.number == intent.episodeNumber) ep.copy(progressPercent = intent.progress)
//                            else ep
//                        })
//                    }
//                else -> { /* handle api calls */ }
//            }
//        }
//    }
//}