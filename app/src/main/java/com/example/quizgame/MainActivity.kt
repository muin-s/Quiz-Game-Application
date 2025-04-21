package com.example.quizgame

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quizgame.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        // Show/hide Create Question button based on admin email
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser?.email == "m@gmail.com") {
            mainBinding.buttonCreateQuestion.visibility = View.VISIBLE
        } else {
            mainBinding.buttonCreateQuestion.visibility = View.GONE
        }

        // Create Question button click
        mainBinding.buttonCreateQuestion.setOnClickListener {
            val intent = Intent(this, PActivity::class.java)
            startActivity(intent)
        }

        mainBinding.buttonStartQuiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
            finish()
        }

        mainBinding.buttonSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Sign out is successful", Toast.LENGTH_LONG).show()
                }
            }

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
