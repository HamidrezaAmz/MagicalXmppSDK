package ir.vasl.magicalxmppsdk

import androidx.recyclerview.widget.DiffUtil
import ir.vasl.magicalxmppsdk.repository.model.MagicalOutgoingMessage

public class MessageDiffUtil(): DiffUtil.ItemCallback<MagicalOutgoingMessage>() {
    override fun areItemsTheSame(
        oldItem: MagicalOutgoingMessage,
        newItem: MagicalOutgoingMessage
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: MagicalOutgoingMessage,
        newItem: MagicalOutgoingMessage
    ): Boolean {
        return oldItem == newItem
    }
}