//package com.ghost.zeku.presentation.screen
//
//import androidx.compose.foundation.BorderStroke
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//
//// ==========================================
//// MOCK DATA MODELS (For unified Anime/Manga handling)
//// ==========================================
//
//data class MediaDetail(
//    val id: Int,
//    val title: String,
//    val bannerImageUrl: String,
//    val coverImageUrl: String,
//    val genres: List<String>,
//    val score: Double,
//    val year: Int,
//    val formatInfo: String, // e.g., "24 Episodes" or "105 Chapters"
//    val studioOrAuthor: String,
//    val synopsis: String,
//    val characters: List<CharacterRole>,
//    val information: Map<String, String>,
//    val relations: List<MediaRelation>
//)
//
//data class CharacterRole(
//    val characterName: String,
//    val role: String,
//    val imageUrl: String,
//    val actorName: String? = null // Null for manga
//)
//
//data class MediaRelation(
//    val title: String,
//    val relationType: String, // e.g., "Prequel", "Spin-off"
//    val format: String,
//    val imageUrl: String
//)
//
//// ==========================================
//// MAIN SCREEN COMPOSABLE
//// ==========================================
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MediaDetailScreen(
//    media: MediaDetail,
//    onBackClick: () -> Unit = {},
//    modifier: Modifier = Modifier
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(text = "Details", style = MaterialTheme.typography.titleMedium) },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* Share */ }) {
//                        Icon(Icons.Default.Share, contentDescription = "Share")
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color.Transparent,
//                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
//                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
//                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
//                )
//            )
//        },
//        // We set container to background to handle the dark cinematic theme
//        containerColor = MaterialTheme.colorScheme.background
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = modifier
//                .fillMaxSize()
//                .padding(top = 0.dp), // Start at the very top, under the transparent app bar
//            contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom fab/nav
//        ) {
//            // 1. Cinematic Hero Header
//            item {
//                HeroHeaderSection(media = media)
//            }
//
//            // 2. Synopsis
//            item {
//                SectionTitle(title = "Synopsis", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
//                Text(
//                    text = media.synopsis,
//                    style = MaterialTheme.typography.bodyLarge,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
//                )
//            }
//
//            // 3. Characters & Cast
//            if (media.characters.isNotEmpty()) {
//                item {
//                    SectionTitle(
//                        title = "Characters & Cast",
//                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
//                        actionText = "View All"
//                    )
//                    CharacterGrid(characters = media.characters)
//                }
//            }
//
//            // 4. Sidebar / Information Panel translated to Mobile Vertical Layout
//            item {
//                Spacer(modifier = Modifier.height(24.dp))
//                InformationPanel(information = media.information)
//            }
//
//            // 5. Relations
//            if (media.relations.isNotEmpty()) {
//                item {
//                    Spacer(modifier = Modifier.height(24.dp))
//                    SectionTitle(title = "Relations", modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp))
//                    RelationsList(relations = media.relations)
//                }
//            }
//        }
//    }
//}
//
//// ==========================================
//// SECTIONS & COMPONENTS
//// ==========================================
//
//@Composable
//private fun HeroHeaderSection(media: MediaDetail) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(550.dp) // Large immersive header
//    ) {
//        // Background Banner Image
//        SharedNetworkImage(
//            url = media.bannerImageUrl,
//            modifier = Modifier.fillMaxSize()
//        )
//
//        // Gradient Scrim (Fades to background color at the bottom)
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    Brush.verticalGradient(
//                        colors = listOf(
//                            Color.Transparent,
//                            MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
//                            MaterialTheme.colorScheme.background
//                        ),
//                        startY = 0f,
//                        endY = Float.POSITIVE_INFINITY
//                    )
//                )
//        )
//
//        // Content overlay aligned to bottom
//        Column(
//            modifier = Modifier
//                .align(Alignment.BottomStart)
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            // Genres (Chips)
//            LazyRow(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                modifier = Modifier.padding(bottom = 12.dp)
//            ) {
//                items(media.genres) { genre ->
//                    Surface(
//                        shape = CircleShape,
//                        color = MaterialTheme.colorScheme.secondaryContainer,
//                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
//                    ) {
//                        Text(
//                            text = genre.uppercase(),
//                            style = MaterialTheme.typography.labelMedium,
//                            fontWeight = FontWeight.Bold,
//                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
//                        )
//                    }
//                }
//            }
//
//            // Title
//            Text(
//                text = media.title,
//                style = MaterialTheme.typography.displaySmall,
//                fontWeight = FontWeight.ExtraBold,
//                color = MaterialTheme.colorScheme.onSurface,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Metadata Row
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Icon(Icons.Default.Star, contentDescription = "Score", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
//                Text(text = media.score.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//
//                MetadataDot()
//                Text(text = media.year.toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
//
//                MetadataDot()
//                Text(text = media.formatInfo, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
//
//                MetadataDot()
//                Text(text = media.studioOrAuthor, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Action Buttons
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Button(
//                    onClick = { /* Play/Read action */ },
//                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(56.dp),
//                    shape = CircleShape
//                ) {
//                    Icon(Icons.Default.PlayArrow, contentDescription = null)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Start", style = MaterialTheme.typography.titleMedium)
//                }
//
//                OutlinedButton(
//                    onClick = { /* Add to list */ },
//                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
//                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(56.dp),
//                    shape = CircleShape
//                ) {
//                    Icon(Icons.Default.Add, contentDescription = null)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Watchlist", style = MaterialTheme.typography.titleMedium)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun CharacterGrid(characters: List<CharacterRole>) {
//    LazyRow(
//        contentPadding = PaddingValues(horizontal = 16.dp),
//        horizontalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        items(characters) { character ->
//            GlassCard(
//                modifier = Modifier.width(260.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(12.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    SharedNetworkImage(
//                        url = character.imageUrl,
//                        modifier = Modifier
//                            .size(56.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(text = character.characterName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
//                        Text(text = character.role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
//                    }
//                    if (character.actorName != null) {
//                        Column(horizontalAlignment = Alignment.End) {
//                            Text(text = character.actorName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
//                            Text(text = "VA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun InformationPanel(information: Map<String, String>) {
//    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
//        GlassCard(modifier = Modifier.fillMaxWidth()) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Text("INFORMATION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
//
//                information.forEach { (key, value) ->
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(text = key, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
//                        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun RelationsList(relations: List<MediaRelation>) {
//    Column(
//        modifier = Modifier.padding(horizontal = 16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        relations.forEach { relation ->
//            GlassCard(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { /* Navigate to relation */ }
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    SharedNetworkImage(
//                        url = relation.imageUrl,
//                        modifier = Modifier
//                            .size(width = 56.dp, height = 80.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                    )
//                    Spacer(modifier = Modifier.width(16.dp))
//                    Column {
//                        Text(
//                            text = relation.relationType.uppercase(),
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.primary,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Spacer(modifier = Modifier.height(2.dp))
//                        Text(
//                            text = relation.title,
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Spacer(modifier = Modifier.height(2.dp))
//                        Text(
//                            text = relation.format,
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//// ==========================================
//// REUSABLE UI ELEMENTS
//// ==========================================
//
//@Composable
//private fun SectionTitle(title: String, modifier: Modifier = Modifier, actionText: String? = null) {
//    Row(
//        modifier = modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.Bottom
//    ) {
//        Text(
//            text = title,
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.Bold,
//            color = MaterialTheme.colorScheme.onSurface
//        )
//        if (actionText != null) {
//            Text(
//                text = actionText,
//                style = MaterialTheme.typography.labelLarge,
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.clickable { /* Action */ }
//            )
//        }
//    }
//}
//
//@Composable
//private fun MetadataDot() {
//    Box(
//        modifier = Modifier
//            .size(4.dp)
//            .clip(CircleShape)
//            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
//    )
//}
//
///**
// * Replaces the raw CSS "glass-panel"
// * Uses Material 3 tonal elevation/surface variants
// */
//@Composable
//private fun GlassCard(
//    modifier: Modifier = Modifier,
//    content: @Composable () -> Unit
//) {
//    Card(
//        modifier = modifier,
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
//        ),
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
//    ) {
//        content()
//    }
//}
//
///**
// * Placeholder for AsyncImage (Coil/Kamel)
// */
//@Composable
//private fun SharedNetworkImage(
//    url: String,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
//        contentAlignment = Alignment.Center
//    ) {
//        // Replace with AsyncImage
//        Icon(
//            imageVector = Icons.Default.Image,
//            contentDescription = null,
//            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
//            modifier = Modifier.size(32.dp)
//        )
//    }
//}
//
//// ==========================================
//// PREVIEW
//// ==========================================
//
//@Preview(showBackground = true, backgroundColor = 0xFF111317)
//@Composable
//private fun MediaDetailScreenPreview() {
//    // Ensuring the preview looks like the dark HTML layout requested
//    MaterialTheme(
//        colorScheme = darkColorScheme(
//            background = Color(0xFF111317),
//            surface = Color(0xFF1A1C1F),
//            surfaceVariant = Color(0xFF2E2E48),
//            primary = Color(0xFFC1C1FF),
//            secondaryContainer = Color(0xFF5C5CFF),
//            onSecondaryContainer = Color.White,
//            onSurfaceVariant = Color(0xFFC6C4D8)
//        )
//    ) {
//        val sampleMedia = MediaDetail(
//            id = 1,
//            title = "Neon Vanguard: Legacy of the Core",
//            bannerImageUrl = "",
//            coverImageUrl = "",
//            genres = listOf("Cyberpunk", "Sci-Fi", "Action"),
//            score = 9.2,
//            year = 2024,
//            formatInfo = "24 Episodes",
//            studioOrAuthor = "Studio Mirai",
//            synopsis = "In the year 2088, the megacity of Nova Arkhos is powered by 'The Core,' a mysterious energy source discovered deep within the tectonic plates. When a series of inexplicable surges threatens to destabilize the city's infrastructure, young technician Ren finds himself at the center of a conspiracy.",
//            characters = listOf(
//                CharacterRole("Ren Arisawa", "Main", "", "Yuki Kaji"),
//                CharacterRole("Luna Vesper", "Leader", "", "Maaya Sakamoto")
//            ),
//            information = mapOf(
//                "Status" to "Currently Airing",
//                "Format" to "TV Series",
//                "Premiered" to "Fall 2024",
//                "Duration" to "24 mins / ep",
//                "Rating" to "PG-13"
//            ),
//            relations = listOf(
//                MediaRelation("Neon Vanguard: Zero", "Prequel", "OVA • 2022", ""),
//                MediaRelation("Arkhos Chronicles", "Spin-off", "Novel • 2023", "")
//            )
//        )
//
//        MediaDetailScreen(media = sampleMedia)
//    }
//}
//
//
//@Preview(showBackground = true, backgroundColor = 0xFF111317, showSystemUi = true, widthDp = 1980, heightDp = 1080)
//@Composable
//private fun MediaDetailDesktopScreenPreview() {
//    // Ensuring the preview looks like the dark HTML layout requested
//    MaterialTheme(
//        colorScheme = darkColorScheme(
//            background = Color(0xFF111317),
//            surface = Color(0xFF1A1C1F),
//            surfaceVariant = Color(0xFF2E2E48),
//            primary = Color(0xFFC1C1FF),
//            secondaryContainer = Color(0xFF5C5CFF),
//            onSecondaryContainer = Color.White,
//            onSurfaceVariant = Color(0xFFC6C4D8)
//        )
//    ) {
//        val sampleMedia = MediaDetail(
//            id = 1,
//            title = "Neon Vanguard: Legacy of the Core",
//            bannerImageUrl = "",
//            coverImageUrl = "",
//            genres = listOf("Cyberpunk", "Sci-Fi", "Action"),
//            score = 9.2,
//            year = 2024,
//            formatInfo = "24 Episodes",
//            studioOrAuthor = "Studio Mirai",
//            synopsis = "In the year 2088, the megacity of Nova Arkhos is powered by 'The Core,' a mysterious energy source discovered deep within the tectonic plates. When a series of inexplicable surges threatens to destabilize the city's infrastructure, young technician Ren finds himself at the center of a conspiracy.",
//            characters = listOf(
//                CharacterRole("Ren Arisawa", "Main", "", "Yuki Kaji"),
//                CharacterRole("Luna Vesper", "Leader", "", "Maaya Sakamoto")
//            ),
//            information = mapOf(
//                "Status" to "Currently Airing",
//                "Format" to "TV Series",
//                "Premiered" to "Fall 2024",
//                "Duration" to "24 mins / ep",
//                "Rating" to "PG-13"
//            ),
//            relations = listOf(
//                MediaRelation("Neon Vanguard: Zero", "Prequel", "OVA • 2022", ""),
//                MediaRelation("Arkhos Chronicles", "Spin-off", "Novel • 2023", "")
//            )
//        )
//
//        MediaDetailScreen(media = sampleMedia)
//    }
//}