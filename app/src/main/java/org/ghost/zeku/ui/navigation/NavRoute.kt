package org.ghost.zeku.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute {

    @Serializable
    object Settings : NavRoute
}


@Serializable
sealed interface SettingsRoute {
    @Serializable
    object GeneralSettings : SettingsRoute

    @Serializable
    object FileAndDirectorySettings : SettingsRoute

    @Serializable
    object InterfaceSettings : SettingsRoute

    @Serializable
    object MediaSettings : SettingsRoute

    @Serializable
    object NetworkSettings : SettingsRoute

    @Serializable
    object PostProcessingSettings : SettingsRoute

    @Serializable
    object SubtitlesSettings : SettingsRoute


    @Serializable
    object AdvancedSettings : SettingsRoute

    @Serializable
    object About : SettingsRoute

}
