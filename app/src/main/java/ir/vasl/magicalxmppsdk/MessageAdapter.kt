package ir.vasl.magicalxmppsdk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.vasl.magicalxmppsdk.repository.model.MagicalOutgoingMessage

public class MessageAdapter: ListAdapter<MagicalOutgoingMessage, MessageAdapter.ViewHolder>(MessageDiffUtil()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.tvMessage).text = getItem(position).message
    }
}