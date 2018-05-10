package com.ping.android.presentation.view.adapter.delegate

import android.content.res.ColorStateList
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.ping.android.model.enums.VoiceType
import com.ping.android.presentation.view.adapter.ViewType
import com.ping.android.presentation.view.adapter.ViewTypeDelegateAdapter
import kotlinx.android.synthetic.main.item_voice_type.view.*

data class VoiceTypeItem(
        val voiceType: VoiceType,
        var isSelected: Boolean = false
) : ViewType {
    val resource: Int
        get() {
            return when (voiceType) {
                VoiceType.CHIPMUNK -> R.drawable.ic_chipmunk
                VoiceType.ROBOT -> R.drawable.ic_robot
                VoiceType.FEMALE -> R.drawable.ic_female
                VoiceType.MALE -> R.drawable.ic_male_face
                else -> R.drawable.ic_person
            }
        }

    override fun getViewType(): Int {
        return 1
    }
}

class VoiceTypeDelegateAdapter(var clickListener: (item: VoiceTypeItem) -> Unit): ViewTypeDelegateAdapter {
    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(parent, clickListener)

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        (holder as ViewHolder).bindItem(item as VoiceTypeItem)
    }

    class ViewHolder(parent: ViewGroup, clickListener: (item: VoiceTypeItem) -> Unit): RecyclerView.ViewHolder(parent.inflate(R.layout.item_voice_type)) {
        private lateinit var voiceTypeItem: VoiceTypeItem

        init {
            itemView.setOnClickListener { clickListener(voiceTypeItem) }
        }

        fun bindItem(voiceTypeItem: VoiceTypeItem) {
            this.voiceTypeItem = voiceTypeItem
            itemView.imageVoiceType.setImageResource(voiceTypeItem.resource)
            if (voiceTypeItem.isSelected) {
                itemView.imageVoiceType.backgroundTintList = ColorStateList
                        .valueOf(ContextCompat.getColor(itemView.context, R.color.av_color3))
            } else {
                itemView.imageVoiceType.backgroundTintList = ColorStateList
                        .valueOf(ContextCompat.getColor(itemView.context, R.color.white))
            }
        }
    }
}