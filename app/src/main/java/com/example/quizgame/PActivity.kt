package com.example.quizgame

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class PActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_question)

        val questionEditText = findViewById<EditText>(R.id.editTextQuestion)
        val optionAEditText = findViewById<EditText>(R.id.editTextOptionA)
        val optionBEditText = findViewById<EditText>(R.id.editTextOptionB)
        val optionCEditText = findViewById<EditText>(R.id.editTextOptionC)
        val optionDEditText = findViewById<EditText>(R.id.editTextOptionD)
        val correctAnswerRadioGroup = findViewById<RadioGroup>(R.id.radioGroupCorrectAnswer)
        val submitButton = findViewById<Button>(R.id.buttonSubmitQuestion)
        val backButton = findViewById<Button>(R.id.buttonBack)

        // Handle Submit Button
        submitButton.setOnClickListener {
            val questionText = questionEditText.text.toString()
            val optionA = optionAEditText.text.toString()
            val optionB = optionBEditText.text.toString()
            val optionC = optionCEditText.text.toString()
            val optionD = optionDEditText.text.toString()

            val selectedId = correctAnswerRadioGroup.checkedRadioButtonId
            if (selectedId == -1 || questionText.isBlank() || optionA.isBlank() || optionB.isBlank() || optionC.isBlank() || optionD.isBlank()) {
                Toast.makeText(this, "Please fill all fields and select correct option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioButton = findViewById<RadioButton>(selectedId)
            val correctOptionKey = when (selectedRadioButton.id) {
                R.id.radioA -> "a"
                R.id.radioB -> "b"
                R.id.radioC -> "c"
                R.id.radioD -> "d"
                else -> ""
            }

            val questionRef = FirebaseDatabase.getInstance().getReference("questions")
            val questionMap = mapOf(
                "q" to questionText,
                "a" to optionA,
                "b" to optionB,
                "c" to optionC,
                "d" to optionD,
                "answer" to correctOptionKey
            )

            questionRef.push().setValue(questionMap).addOnSuccessListener {
                Toast.makeText(this, "Question added!", Toast.LENGTH_SHORT).show()

                // Reset all fields after successful submission
                questionEditText.text.clear()
                optionAEditText.text.clear()
                optionBEditText.text.clear()
                optionCEditText.text.clear()
                optionDEditText.text.clear()
                correctAnswerRadioGroup.clearCheck()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to add question", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Back Button
        backButton.setOnClickListener {
            // Navigate back to MainActivity
            onBackPressed()
        }
    }

    // Handle the back button press to go back to the MainActivity
    override fun onBackPressed() {
        super.onBackPressed()
        finish()  // This will close the current activity and go back to the previous one
    }
}
