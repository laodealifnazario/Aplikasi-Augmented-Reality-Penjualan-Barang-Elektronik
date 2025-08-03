package com.tugas_akhir.alifnzr.Diskusi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tugas_akhir.alifnzr.R

class AdapterDetailDiskusi(private val messageList: List<ModelDetailDiskusi>, private val currentUserId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1

    inner class ViewHolderDetailDiskusi(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTv: TextView = itemView.findViewById(R.id.messageTv)
        val imageIv: ImageView = itemView.findViewById(R.id.messageIv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, ): RecyclerView.ViewHolder {
        return if (viewType == MESSAGE_TYPE_RIGHT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_chat_right, parent, false)
            ViewHolderDetailDiskusi(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_chat_left, parent, false)
            ViewHolderDetailDiskusi(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = messageList[position]
        lihatGambarBarang(holder as ViewHolderDetailDiskusi, model)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].fromUid == currentUserId) {
            MESSAGE_TYPE_RIGHT
        } else {
            MESSAGE_TYPE_LEFT
        }
    }

    private fun lihatGambarBarang(a: ViewHolderDetailDiskusi, b: ModelDetailDiskusi) {
        a.messageTv.text = b.message
        if (!b.imageUri.isNullOrEmpty()) {
            a.imageIv.visibility = View.VISIBLE
            Glide.with(a.itemView.context)
                .load(b.imageUri)
                .into(a.imageIv)
        } else {
            a.imageIv.visibility = View.GONE
        }
    }
}