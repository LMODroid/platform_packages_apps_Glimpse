/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.recyclerview

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import org.lineageos.glimpse.R
import org.lineageos.glimpse.models.Media
import org.lineageos.glimpse.models.MediaType
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

class ThumbnailAdapter(
    private val onItemSelected: (media: Media, position: Int) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val headersPositions = sortedSetOf<Int>()

    var data: Array<Media> = arrayOf()
        set(value) {
            if (value.contentEquals(field)) {
                return
            }

            field = value

            field.let {
                @Suppress("NotifyDataSetChanged") notifyDataSetChanged()
            }
        }

    init {
        setHasStableIds(true)
    }

    override fun getItemCount() = data.size.takeIf { it > 0 }
        ?.let { it + (headersPositions.size.takeIf { headerCount -> headerCount > 0 } ?: 1) } ?: 0

    override fun getItemId(position: Int) = if (headersPositions.contains(position)) {
        position.toLong()
    } else {
        data[getTruePosition(position)].id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LayoutInflater.from(parent.context).let { layoutInflater ->
            when (viewType) {
                ViewTypes.ITEM.ordinal -> ThumbnailViewHolder(
                    layoutInflater.inflate(R.layout.thumbnail_view, parent, false), onItemSelected
                )

                ViewTypes.HEADER.ordinal -> DateHeaderViewHolder(
                    layoutInflater.inflate(R.layout.date_header_view, parent, false)
                )

                else -> throw Exception("Unknown view type $viewType")
            }
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val truePosition = getTruePosition(position)

        when (holder.itemViewType) {
            ViewTypes.ITEM.ordinal -> {
                val thumbnailViewHolder = holder as ThumbnailViewHolder
                thumbnailViewHolder.bind(data[truePosition], truePosition)
            }

            ViewTypes.HEADER.ordinal -> {
                val dateHeaderViewHolder = holder as DateHeaderViewHolder
                dateHeaderViewHolder.bind(data[truePosition].dateAdded)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (headersPositions.contains(position)) {
            return ViewTypes.HEADER.ordinal
        }

        if (position == 0) {
            // First element must always be a header
            addHeaderOffset(position)
            return ViewTypes.HEADER.ordinal
        }

        val previousPosition = position - 1

        if (headersPositions.contains(previousPosition)) {
            // Before this position we have a header, next up there's a thumbnail
            return ViewTypes.ITEM.ordinal
        }

        val truePosition = getTruePosition(position)
        val previousTruePosition = truePosition - 1

        val currentMedia = data[truePosition]
        val previousMedia = data[previousTruePosition]

        val before = previousMedia.dateAdded.toInstant().atZone(ZoneId.systemDefault())
        val after = currentMedia.dateAdded.toInstant().atZone(ZoneId.systemDefault())
        val days = ChronoUnit.DAYS.between(after, before)

        if (days >= 1 || before.dayOfMonth != after.dayOfMonth) {
            addHeaderOffset(position)
            return ViewTypes.HEADER.ordinal
        }

        return ViewTypes.ITEM.ordinal
    }

    private fun getTruePosition(position: Int) =
        position - headersPositions.filter { it < position }.size

    private fun addHeaderOffset(position: Int) {
        headersPositions.add(position)
    }

    class ThumbnailViewHolder(
        private val view: View,
        private val onItemSelected: (media: Media, position: Int) -> Unit,
    ) : RecyclerView.ViewHolder(view) {
        // Views
        private val videoOverlayImageView =
            view.findViewById<ImageView>(R.id.videoOverlayImageView)!!
        private val thumbnailImageView = view.findViewById<ImageView>(R.id.thumbnailImageView)!!

        private lateinit var media: Media
        private var position = -1

        init {
            view.setOnClickListener {
                onItemSelected(media, position)
            }
        }

        fun bind(media: Media, position: Int) {
            this.media = media
            this.position = position

            thumbnailImageView.load(media.externalContentUri) {
                memoryCacheKey("thumbnail_${media.id}")
                size(DisplayAwareGridLayoutManager.MAX_THUMBNAIL_SIZE)
                placeholder(R.drawable.thumbnail_placeholder)
            }
            videoOverlayImageView.isVisible = media.mediaType == MediaType.VIDEO
        }
    }

    class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Views
        private val textView = view as TextView

        fun bind(date: Date) {
            textView.text = DateUtils.getRelativeTimeSpanString(
                date.time,
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS
            )
        }
    }

    enum class ViewTypes {
        ITEM,
        HEADER,
    }
}