package com.example.quizgame

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.quizgame.databinding.ActivityResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ResultActivity : AppCompatActivity() {
    private lateinit var resultBinding: ActivityResultBinding

    private val database = FirebaseDatabase.getInstance()
    private val databaseReference = database.reference.child("scores")

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    private var userCorrect = ""
    private var userWrong = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultBinding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(resultBinding.root)

        // Optional: If you kept the imageView and want to hide it
//        resultBinding.imageView4.visibility = View.GONE

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user?.let { currentUser ->
                    val userUID = currentUser.uid

                    userCorrect = snapshot.child(userUID).child("correct").value.toString()
                    userWrong = snapshot.child(userUID).child("wrong").value.toString()

                    resultBinding.textViewScoreCorrect.text = userCorrect
                    resultBinding.textViewScoreWrong.text = userWrong

                    // Calculate rank based on correct answers
                    val scoreList = mutableListOf<Pair<String, Int>>()

                    for (child in snapshot.children) {
                        val uid = child.key ?: continue
                        val correctScore = child.child("correct").value.toString().toIntOrNull() ?: 0
                        scoreList.add(Pair(uid, correctScore))
                    }

                    // Sort list descending by correct score
                    scoreList.sortByDescending { it.second }

                    // Find current user's rank
                    val rank = scoreList.indexOfFirst { it.first == userUID } + 1
                    resultBinding.textViewRank.text = "Your Rank: #$rank"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })

        resultBinding.buttonPlayAgain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        resultBinding.buttonExit.setOnClickListener {
            finish()
        }
    }
}
