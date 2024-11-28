package com.example.kotlingemini

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide the status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val etPrompt = findViewById<EditText>(R.id.editText)
        val btnSubmit = findViewById<Button>(R.id.button)

        chatAdapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true) // Set to scroll from bottom to top
        recyclerView.adapter = chatAdapter

        btnSubmit.setOnClickListener {
            val prompt = etPrompt.text.toString()
            if (prompt.isNotEmpty()) {
                // Add user's message at the top
                messages.add(0, Message(prompt, true))
                chatAdapter.notifyItemInserted(0)
                recyclerView.scrollToPosition(0)

                // Clear input
                etPrompt.text.clear()

                // Generate response from Gemini
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val response = generateGeminiResponse(prompt)
                        withContext(Dispatchers.Main) {
                            messages.add(0, Message(response, false))
                            chatAdapter.notifyItemInserted(0)
                            recyclerView.scrollToPosition(0)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            messages.add(0, Message("Error: ${e.message}", false))
                            chatAdapter.notifyItemInserted(0)
                            recyclerView.scrollToPosition(0)
                        }
                    }
                }
            }
        }
    }

    // Function to generate response using the GenerativeModel API
    private suspend fun generateGeminiResponse(prompt: String): String {
        return withContext(Dispatchers.IO) {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = api.api // Replace with your actual API key
            )
            // Generate content asynchronously
            val response = generativeModel.generateContent(prompt)
            return@withContext response.text.toString()
        }
    }
}
