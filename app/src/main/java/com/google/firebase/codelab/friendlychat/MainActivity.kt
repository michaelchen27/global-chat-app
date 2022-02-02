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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.codelab.friendlychat.databinding.ActivityMainBinding
import com.google.firebase.codelab.friendlychat.model.FriendlyMessage
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var linearLayoutManager: LinearLayoutManager

    private val openDocument = registerForActivityResult(MyOpenDocumentContract()) { uri ->
        onImageSelected(uri)
    }

    private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseDatabase
    private lateinit var adapter: FriendlyMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        if (BuildConfig.DEBUG) {
//            Firebase.database.useEmulator("10.0.2.2", 9000)
//            Firebase.auth.useEmulator("10.0.2.2", 9099)
//            Firebase.storage.useEmulator("10.0.2.2", 9199)
//        }

        // This codelab uses View Binding
        // See: https://developer.android.com/topic/libraries/view-binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and check if the user is signed in
        // TODO: implement

        // Initialize Realtime Database and FirebaseRecyclerAdapter
        db = Firebase.database
        auth = Firebase.auth

        if (auth.currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        val messageRef = db.reference.child(MESSAGES_CHILD)

        val options = FirebaseRecyclerOptions.Builder<FriendlyMessage>()
            .setQuery(messageRef, FriendlyMessage::class.java)
            .build()

        adapter = FriendlyMessageAdapter(options, getUserName())
        binding.progressBar.visibility = ProgressBar.INVISIBLE
        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true

        binding.messageRecyclerView.layoutManager = linearLayoutManager
        binding.messageRecyclerView.adapter = adapter

        adapter.registerAdapterDataObserver(MyScrollToBottomObserver(binding.messageRecyclerView, adapter, linearLayoutManager))

        // Disable the send button when there's no text in the input field
        // See MyButtonObserver for details
        binding.messageEditText.addTextChangedListener(MyButtonObserver(binding.sendButton))

        // When the send button is clicked, send a text message
        binding.messageEditText.addTextChangedListener(MyButtonObserver(binding.sendButton))

        binding.sendButton.setOnClickListener {
            var friendlyMessage = FriendlyMessage(
                binding.messageEditText.text.toString(),
                getUserName(),
                getPhotoUrl(),
                null
            )
            db.reference.child(MESSAGES_CHILD).push().setValue(friendlyMessage)
            binding.messageEditText.setText("")
        }
//        // When the image button is clicked, launch the image picker
//        binding.addMessageImageView.setOnClickListener {
//            openDocument.launch(arrayOf("image/*"))
//        }


    }

    public override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }
    }

    public override fun onPause() {
        adapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        adapter.startListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onImageSelected(uri: Uri) {
        // TODO: implement
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri, key: String?) {
        // Upload the image to Cloud Storage
        // TODO: implement
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this)
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    private fun getPhotoUrl(): String? {
        val user = auth.currentUser
        return user?.photoUrl?.toString()
    }

    private fun getUserName(): String? {
        val user = auth.currentUser
        return if (user != null) {
            user.displayName
        } else ANONYMOUS

    }

    companion object {
        private const val TAG = "MainActivity"
        const val MESSAGES_CHILD = "messages"
        const val ANONYMOUS = "anonymous"
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }
}
