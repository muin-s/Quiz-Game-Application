package com.example.quizgame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.quizgame.databinding.ActivityQuizBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.random.Random

class QuizActivity : AppCompatActivity() {
    private lateinit var quizBinding: ActivityQuizBinding

    private val database = FirebaseDatabase.getInstance()
    private val databaseReference = database.reference.child("questions")

    private var question = ""
    private var answerA = ""
    private var answerB = ""
    private var answerC = ""
    private var answerD = ""
    private var correctAnswer = ""
    private var questionList: MutableList<DataSnapshot> = mutableListOf()
    private var questionIndex = 0

    private var userAnswer = ""
    private var userCorrect = 0
    private var userWrong = 0

    private lateinit var timer: CountDownTimer
    private val totalTime = 25000L
    private var timerContinue = false
    private var leftTime = totalTime

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    private val scoreRef = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quizBinding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(quizBinding.root)

        loadQuestionsFromFirebase()

        quizBinding.buttonNext.setOnClickListener {
            resetTimer()
            gameLogic()
        }

        quizBinding.buttonFinish.setOnClickListener {
            sendScore()
        }

        setupAnswerButtons()
    }

    private fun loadQuestionsFromFirebase() {
        databaseReference.limitToLast(5).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questionList.clear()
                for (child in snapshot.children) {
                    questionList.add(child)
                }
                questionList.shuffle()  // Shuffle the order if needed
                gameLogic()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun gameLogic() {
        restoreOptions()

        if (questionIndex < questionList.size) {
            val currentQuestion = questionList[questionIndex++]

            question = currentQuestion.child("q").value.toString()
            answerA = currentQuestion.child("a").value.toString()
            answerB = currentQuestion.child("b").value.toString()
            answerC = currentQuestion.child("c").value.toString()
            answerD = currentQuestion.child("d").value.toString()
            correctAnswer = currentQuestion.child("answer").value.toString()

            quizBinding.textViewQuestion.text = question
            quizBinding.textViewA.text = answerA
            quizBinding.textViewB.text = answerB
            quizBinding.textViewC.text = answerC
            quizBinding.textViewD.text = answerD

            quizBinding.progressBarQuiz.visibility = View.INVISIBLE
            quizBinding.linearLayoutInfo.visibility = View.VISIBLE
            quizBinding.linearLayoutQuestion.visibility = View.VISIBLE
            quizBinding.linearLayoutButtons.visibility = View.VISIBLE

            startTimer()
        } else {
            showCompletionDialog()
        }
    }

    private fun setupAnswerButtons() {
        quizBinding.textViewA.setOnClickListener { handleAnswer("a", quizBinding.textViewA) }
        quizBinding.textViewB.setOnClickListener { handleAnswer("b", quizBinding.textViewB) }
        quizBinding.textViewC.setOnClickListener { handleAnswer("c", quizBinding.textViewC) }
        quizBinding.textViewD.setOnClickListener { handleAnswer("d", quizBinding.textViewD) }
    }

    private fun handleAnswer(answer: String, view: View) {
        pauseTimer()
        userAnswer = answer
        if (correctAnswer == userAnswer) {
            view.setBackgroundColor(Color.GREEN)
            userCorrect++
            quizBinding.textViewCorrect.text = userCorrect.toString()
        } else {
            view.setBackgroundColor(Color.RED)
            userWrong++
            quizBinding.textViewWrong.text = userWrong.toString()
            findAnswer()
        }
        disableClickableOfOptions()
    }

    private fun findAnswer() {
        when (correctAnswer) {
            "a" -> quizBinding.textViewA.setBackgroundColor(Color.GREEN)
            "b" -> quizBinding.textViewB.setBackgroundColor(Color.GREEN)
            "c" -> quizBinding.textViewC.setBackgroundColor(Color.GREEN)
            "d" -> quizBinding.textViewD.setBackgroundColor(Color.GREEN)
        }
    }

    private fun disableClickableOfOptions() {
        quizBinding.textViewA.isClickable = false
        quizBinding.textViewB.isClickable = false
        quizBinding.textViewC.isClickable = false
        quizBinding.textViewD.isClickable = false
    }

    private fun restoreOptions() {
        quizBinding.textViewA.setBackgroundColor(Color.WHITE)
        quizBinding.textViewB.setBackgroundColor(Color.WHITE)
        quizBinding.textViewC.setBackgroundColor(Color.WHITE)
        quizBinding.textViewD.setBackgroundColor(Color.WHITE)

        quizBinding.textViewA.isClickable = true
        quizBinding.textViewB.isClickable = true
        quizBinding.textViewC.isClickable = true
        quizBinding.textViewD.isClickable = true
    }

    private fun startTimer() {
        timer = object : CountDownTimer(leftTime, 1000) {
            override fun onTick(millisUntilFinish: Long) {
                leftTime = millisUntilFinish
                updateCountDownText()
            }

            override fun onFinish() {
                disableClickableOfOptions()
                resetTimer()
                updateCountDownText()
                quizBinding.textViewQuestion.text = "Sorry, Time is up! Continue with next question."
                timerContinue = false
            }
        }.start()

        timerContinue = true
    }

    private fun updateCountDownText() {
        val remainingTime: Int = (leftTime / 1000).toInt()
        quizBinding.textViewTime.text = remainingTime.toString()
    }

    private fun pauseTimer() {
        timer.cancel()
        timerContinue = false
    }

    private fun resetTimer() {
        pauseTimer()
        leftTime = totalTime
        updateCountDownText()
    }

    private fun sendScore() {
        user?.let {
            val userUID = it.uid
            scoreRef.child("scores").child(userUID).child("correct").setValue(userCorrect)
            scoreRef.child("scores").child(userUID).child("wrong").setValue(userWrong).addOnSuccessListener {
                Toast.makeText(applicationContext, "Scores sent to database successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@QuizActivity, ResultActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showCompletionDialog() {
        val dialogMessage = AlertDialog.Builder(this@QuizActivity)
        dialogMessage.setTitle("Quiz Game")
        dialogMessage.setMessage("Congratulations!!!\nYou have answered all the questions. Do you want to see the result?")
        dialogMessage.setCancelable(false)
        dialogMessage.setPositiveButton("See Result") { _, _ -> sendScore() }
        dialogMessage.setNegativeButton("Play Again") { _, _ ->
            val intent = Intent(this@QuizActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        dialogMessage.create().show()
    }
}
