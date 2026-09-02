package com.senswear.app.core.designsystem

sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : UiEvent
    data class Navigate(val route: String) : UiEvent
    object VibrateSuccess : UiEvent
}
