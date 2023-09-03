/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.lineageos.glimpse.GlimpseApplication
import org.lineageos.glimpse.repository.MediaRepository

class MediaViewerViewModel(
    savedStateHandle: SavedStateHandle,
    mediaRepository: MediaRepository,
) : MediaViewModel(savedStateHandle, mediaRepository) {
    /**
     * The current height of top and bottom sheets, used to apply padding to media view UI.
     */
    val sheetsHeightLiveData = MutableLiveData<Pair<Int, Int>>()

    /**
     * Fullscreen mode, set by the user with a single tap on the viewed media.
     */
    val fullscreenModeLiveData = MutableLiveData(false)

    /**
     * Toggle fullscreen mode.
     */
    fun toggleFullscreenMode() {
        fullscreenModeLiveData.value = when (fullscreenModeLiveData.value) {
            true -> false
            else -> true
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MediaViewerViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    mediaRepository = (this[APPLICATION_KEY] as GlimpseApplication).mediaRepository,
                )
            }
        }
    }
}