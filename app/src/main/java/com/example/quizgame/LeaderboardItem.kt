package com.example.quizgame

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

data class LeaderboardItem(
    val uid: String = "",
    val name: String = "",
    val correct: Int = 0
)
