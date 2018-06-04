package com.ping.android.presentation.view.flexibleitem.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ping.android.R

import com.ping.android.model.Message
import com.ping.android.model.enums.MessageCallType
import com.ping.android.model.enums.MessageType

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
            val description = if (message.isFromMe) {
                tvCall.setText(R.string.chat_call_again)
                String.format(itemView.context.getString(message.messageCallType.descriptionFromMe()), message.opponentUser.nickName)
            } else {
                tvCall.setText(R.string.chat_call_back)
                String.format(itemView.context.getString(message.messageCallType.descriptionToMe()), message.opponentUser.nickName)
            }
            this.description.text = description
        }

        override fun getClickableView(): View? {
            return null
        }

        override fun getSlideView(): View {
            return container
        }
    }
}
