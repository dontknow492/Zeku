package com.ghost.zeku.presentation.components.media.relation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.enum.TitleLanguage


enum class RelationCardLayout {
    POSTER, WIDE;
}

@Immutable
data class MediaRelationCardConfig(

    // Layout
    val layout: RelationCardLayout = RelationCardLayout.POSTER,

    // Dimensions
    val width: Dp = 140.dp,
    val height: Dp = 90.dp, // used for WIDE
    val aspectRatio: Float = 0.7f, // poster style
    val shape: Shape = RoundedCornerShape(18.dp),
    val contentPadding: PaddingValues = PaddingValues(10.dp),

    // Title
    val preferredTitleLanguage: TitleLanguage = TitleLanguage.ROMAJI,

    // Colors
    val containerColor: Color = Color(0xFF1C1C1E),
    val gradient: Brush = Brush.verticalGradient(
        listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
    ),
    val titleColor: Color = Color.White,
    val subtitleColor: Color = Color.White.copy(alpha = 0.75f),

    // Typography
    val titleTextStyle: TextStyle = TextStyle.Default,
    val subtitleTextStyle: TextStyle = TextStyle.Default,

    // Badges
    val showRelationBadge: Boolean = true,
    val showFormatBadge: Boolean = true,

    // Elevation
    val elevation: Dp = 2.dp,
    val hoverElevation: Dp = 10.dp,
    val pressedElevation: Dp = 1.dp,

    // Animation
    val scaleOnHover: Float = 1.05f,
    val scaleOnPress: Float = 0.96f,
    val imageHoverScale: Float = 1.08f,
    val animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
    val elevationAnimationSpec: AnimationSpec<Dp> = spring(),

    // Image
    val contentScale: ContentScale = ContentScale.Crop,

    val clickable: Boolean = true
)