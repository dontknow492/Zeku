package com.ghost.zeku.presentation.screen.details

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.ghost.zeku.domain.model.media.MediaStaff
import com.ghost.zeku.domain.model.media.MediaStudio
import com.ghost.zeku.presentation.viewmodel.detail.MediaDetailContract

// Sealed class for mapping Domain Staff/Studio cleanly to UI Chips
sealed class CreditType(val name: String, val label: String, val icon: ImageVector) {
    data class Staff(val staff: MediaStaff) : CreditType(staff.name, staff.role, Icons.Filled.Person)
    data class Studio(val studio: MediaStudio) :
        CreditType(studio.name, if (studio.isAnimationStudio) "Animation Studio" else "Producer", Icons.Filled.Business)

    fun toEvent(): MediaDetailContract.Event = when (this) {
        is Staff -> MediaDetailContract.Event.OnStaffClick(staff)
        is Studio -> MediaDetailContract.Event.OnStudioClick(studio)
    }
}