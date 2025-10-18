import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.ghost.zeku.core.enum.MediaType
import org.ghost.zeku.database.models.HistoryItem
import org.ghost.zeku.R
import org.ghost.zeku.database.models.Format
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow



/**
 * A stylized card to display an item from the download history.
 *
 * @param modifier The modifier to be applied to the card.
 * @param historyItem The data item to display.
 * @param onClick The callback that is triggered when the card is clicked.
 * @param onDeleteClick The callback that is triggered when the delete icon is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryCard(
    modifier: Modifier = Modifier,
    historyItem: HistoryItem,
    selected: Boolean = false,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDeleteClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable( // 2. Handle both click and long-press
                onClick = onClick,
                onLongClick = onLongPress
            ),
        border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
        colors = if (selected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors() // Default colors
        }
    ) {
        Box(

        ){
            HistoryItemContent(
                modifier = Modifier.height(IntrinsicSize.Min),
                historyItem = historyItem,
                onDeleteClick = onDeleteClick
            )

        }

    }

}


@Composable
private fun HistoryItemContent(
    modifier: Modifier = Modifier,
    historyItem: HistoryItem,
    onDeleteClick: () -> Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier // Ensures children can fill height
    ) {
        // Section 1: Thumbnail
        ThumbnailSection(
            thumbnailUrl = historyItem.thumb,
            duration = historyItem.duration,
            mediaType = historyItem.type
        )

        // Section 2: Details

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = historyItem.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = historyItem.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.weight(1f)) // Pushes the bottom row down
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatFileSize(historyItem.filesize),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatRelativeTime(historyItem.time),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete from history",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        }

    }
}

@Composable
private fun ThumbnailSection(
    thumbnailUrl: String,
    duration: String,
    mediaType: MediaType
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .crossfade(true)
                .build(),
            // Replace with your actual placeholder/error drawables
            // placeholder = painterResource(id = R.drawable.placeholder),
            // error = painterResource(id = R.drawable.error),
            contentDescription = "Video thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Scrim for better text visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        )

        // Media Type Icon
        MediaTypeIcon(
            mediaType = mediaType,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
        )

        // Duration Text
        if (duration.isNotBlank()) {
            Text(
                text = duration,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun MediaTypeIcon(mediaType: MediaType, modifier: Modifier = Modifier) {
    val icon: ImageVector = when (mediaType) {
        MediaType.VIDEO -> ImageVector.vectorResource(R.drawable.round_videocam_24)
        MediaType.AUDIO -> ImageVector.vectorResource(R.drawable.rounded_music_note_24)
        MediaType.COMMAND -> ImageVector.vectorResource(R.drawable.rounded_terminal_24)
        MediaType.AUTO -> ImageVector.vectorResource(R.drawable.round_videocam_24)
    }
    Icon(
        imageVector = icon,
        contentDescription = "Media type: ${mediaType.name}",
        tint = Color.White,
        modifier = modifier
    )
}

/**
 * Formats file size in bytes to a human-readable string (KB, MB, GB).
 */
private fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(sizeBytes.toDouble()) / log10(1024.0)).toInt()
    return "${DecimalFormat("#,##0.#").format(sizeBytes / 1024.0.pow(digitGroups.toDouble()))} ${units[digitGroups]}"
}

/**
 * Formats a UNIX timestamp into a relative "time ago" string.
 * This is a simplified implementation. For more accuracy and localization,
 * consider using a library like `Android-Utils-KTX`'s `getTimeAgo()`.
 */
private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "$days${"d"} ago"
        hours > 0 -> "$hours${"h"} ago"
        minutes > 0 -> "$minutes${"m"} ago"
        else -> "Just now"
    }
}


// --- Preview ---
@Preview(showBackground = true, name = "Light Mode", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun HistoryCardPreview() {
    val sampleItem = HistoryItem(
        id = 1,
        url = "https://example.com",
        title = "A Great Video About Jetpack Compose Layouts and Modifiers",
        author = "Awesome Android Dev",
        duration = "12:34",
        thumb = "https://picsum.photos/seed/picsum/400/300",
        type = MediaType.VIDEO,
        time = System.currentTimeMillis() - 3600000 * 5, // 5 hours ago
        filesize = 1024L * 1024 * 58 + 300,
        downloadPath = listOf("downloads", "videos", "sample.mp4"),
        downloadId = 123456789L,
        website = "Youtube",
        format = Format()
    )
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HistoryCard(
                modifier = Modifier.padding(16.dp),
                historyItem = sampleItem,
                selected = !false,
                onClick = {},
                onLongPress = {},
                onDeleteClick = {}
            )
        }
    }
}