/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.codelab.friendlychat

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.codelab.friendlychat.MainActivity.Companion.ANONYMOUS
import com.google.firebase.codelab.friendlychat.databinding.MessageBinding
import com.google.firebase.codelab.friendlychat.model.FriendlyMessage
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

// The FirebaseRecyclerAdapter class and options come from the FirebaseUI library
// See: https://github.com/firebase/FirebaseUI-Android
class FriendlyMessageAdapter(
    private val options: FirebaseRecyclerOptions<FriendlyMessage>,
    private val currentUserName: String?
) :
    FirebaseRecyclerAdapter<FriendlyMessage, ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            val view = inflater.inflate(R.layout.message, parent, false)
            val binding = MessageBinding.bind(view)
            return MessageViewHolder(binding)
        } else {
            val view = inflater.inflate(R.layout.message_right, parent, false)
            val binding = MessageBinding.bind(view)
            return RightMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: FriendlyMessage) {
        if (options.snapshots[position].text != null) {
            when (holder.itemViewType) {
                1 -> (holder as RightMessageViewHolder).bind(model)
                2 -> (holder as MessageViewHolder).bind(model)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (options.snapshots[position].name != currentUserName) VIEW_TYPE_MESSAGE_RECEIVED else VIEW_TYPE_MESSAGE_SENT
    }

    inner class MessageViewHolder(
        private val binding: MessageBinding,
    ) : ViewHolder(binding.root) {
        fun bind(item: FriendlyMessage) {
            binding.messageTextView.text = item.text
            binding.messengerTextView.text = if (item.name == null) ANONYMOUS else item.name

            if (item.photoUrl != null) {
                loadImageIntoView(binding.messengerImageView, item.photoUrl!!)
            } else {
                binding.messengerImageView.setImageResource(R.drawable.ic_account_circle_black_36dp)
            }

        }
    }

    inner class RightMessageViewHolder(
        private val binding: MessageBinding,
    ) : ViewHolder(binding.root) {
        fun bind(item: FriendlyMessage) {
            binding.messageTextView.text = item.text
            binding.messengerTextView.text = if (item.name == null) ANONYMOUS else item.name

            if (item.photoUrl != null) {
                loadImageIntoView(binding.messengerImageView, item.photoUrl!!)
            } else {
                binding.messengerImageView.setImageResource(R.drawable.ic_account_circle_black_36dp)
            }
        }
    }

    private fun loadImageIntoView(view: ImageView, url: String) {
        if (url.startsWith("gs://")) {
            val storageReference = Firebase.storage.getReferenceFromUrl(url)
            storageReference.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    Glide.with(view.context)
                        .load(downloadUrl)
                        .into(view)
                }
                .addOnFailureListener { e ->
                    Log.w(
                        TAG,
                        "Getting download url was not successful.",
                        e
                    )
                }
        } else {
            Glide.with(view.context).load(url).into(view)
        }
    }

    companion object {
        const val TAG = "MessageAdapter"
        const val VIEW_TYPE_MESSAGE_SENT = 1
        const val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }
}
