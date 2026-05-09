package com.ghost.zeku.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SearchTopBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    onFilterClick: () -> Unit,
    badgeCount: Int,
    isFilterPanelOpen: Boolean,
) {
    val focusManager = LocalFocusManager.current
    val searchShape = RoundedCornerShape(28.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // =========================================================
        // SEARCH FIELD
        // =========================================================

        OutlinedTextField(
            value = query,

            onValueChange = onQueryChange,

            modifier = Modifier
                .weight(1f)
                .height(58.dp),

            singleLine = true,

            shape = searchShape,

            placeholder = {
                Text(
                    text = placeholder,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },

            textStyle = MaterialTheme.typography.bodyLarge,

            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null
                )
            },

            trailingIcon = {

                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {

                    FilledIconButton(
                        onClick = {
                            onQueryChange("")
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear Search",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor =
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),

                unfocusedBorderColor =
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),

                focusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHigh,

                unfocusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainer,

                cursorColor =
                    MaterialTheme.colorScheme.primary
            )
        )

        // =========================================================
        // FILTER BUTTON
        // =========================================================

        BadgedBox(
            badge = {
                Row {
                    AnimatedVisibility(
                        visible = badgeCount > 0,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {

                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = if (badgeCount > 99) "99+" else badgeCount.toString()
                            )
                        }
                    }
                }
            }
        ) {

            FilledTonalIconButton(
                onClick = {
                    focusManager.clearFocus()
                    onFilterClick()
                },

                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),

                colors = IconButtonDefaults.filledTonalIconButtonColors(

                    containerColor =
                        if (isFilterPanelOpen) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },

                    contentColor =
                        if (isFilterPanelOpen) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                )
            ) {

                Crossfade(
                    targetState = isFilterPanelOpen
                ) { expanded ->

                    Icon(
                        imageVector =
                            if (expanded) {
                                Icons.Rounded.Tune
                            } else {
                                Icons.Rounded.FilterList
                            },
                        contentDescription = "Filters"
                    )
                }
            }
        }
    }
}