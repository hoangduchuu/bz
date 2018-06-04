package com.ping.android.presentation.view.flexibleitem.messages

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ping.android.R

import com.ping.android.model.Message
import com.ping.android.model.enums.MessageCallType
import com.ping.android.model.enums.MessageType
import com.ping.android.utils.CommonMethod
import com.ping.android.utils.DateUtils

abstract class CallMessageBaseItem(message: Message) : MessageBaseItem<CallMessageBaseItem.ViewHolder>(message) {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, lastItem: Boolean) {
        holder.bindData(this, lastItem)
    }

    class ViewHolder(itemView: View) : MessageBaseItem.ViewHolder(itemView) {
        private val container: View = itemView.findViewById(R.id.container)
        private val tvCall: TextView = itemView.findViewById(R.id.txtCall)
        private val description: TextView = itemView.findViewById(R.id.txtCallDescription)
        private val tvCallDuration: TextView = itemView.findViewById(R.id.call_duration)
        private val callTypeIcon: ImageView = itemView.findViewById(R.id.call_type);

        init {
            tvCall.setOnClickListener {
                val isVideo = item.message.messageCallType == MessageCallType.VIDEO_CALL
                        || item.message.messageCallType == MessageCallType.MISSED_VIDEO_CALL
                messageListener?.onCall(isVideo)
            }
        }

        override fun bindData(item: MessageBaseItem<*>, lastItem: Boolean) {
            super.bindData(item, lastItem)
            val message = item.message
            updateCallTypeIcon(message.messageCallType)
            updateCallDuration(message)
            updateCallDescription(message)
        }

        private fun updateCallDuration(message: Message) {
            val callDuration = if (message.messageCallType == MessageCallType.VOICE_CALL || message.messageCallType == MessageCallType.VIDEO_CALL) {
                // Show format: duration + timestamp
                val duration = CommonMethod.getCallDuration(itemView.context, message.callDuration)
                val timestamp = DateUtils.toString("h:mm a", message.timestamp)
                "$duration, $timestamp"
            } else {
                // Show timestamp
                DateUtils.toString("h:mm a", message.timestamp)
            }
            tvCallDuration.text = callDuration
        }

        private fun updateCallDescription(message: Message) {
            val opponentName = if (message.opponentUser != null) {
                if (!TextUtils.isEmpty(message.opponentUser.nickName)) {
                    message.opponentUser.nickName
                } else {
                    message.opponentUser.displayName
                }
            } else {
                "user"
            }
            val description = if (message.isFromMe) {
                tvCall.setText(R.string.chat_call_again)
                String.format(itemView.context.getString(message.messageCallType.descriptionFromMe()), opponentName)
            } else {
                tvCall.setText(R.string.chat_call_back)
                String.format(itemView.context.getString(message.messageCallType.descriptionToMe()), opponentName)
            }
            this.description.text = description
        }

        private fun updateCallTypeIcon(messageCallType: MessageCallType) {
            val iconRes = if (messageCallType == MessageCallType.VOICE_CALL
                    || messageCallType == MessageCallType.MISSED_VOICE_CALL) {
                R.drawable.ic_chat_audio
            } else {
                R.drawable.ic_chat_video
            }
            callTypeIcon.setImageResource(iconRes)
        }

        override fun getClickableView(): View? {
            return null
        }

        override fun getSlideView(): View {
            return container
        }
    }
}
