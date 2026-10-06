package com.devhjs.garim.core.navigation
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute : NavKey
@Serializable data class EditorRoute(val photoId: String) : NavKey
@Serializable data class ResultRoute(val outputId: String) : NavKey
