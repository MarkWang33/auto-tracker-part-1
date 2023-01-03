package com.example.auto_tracker_part_1.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.auto_tracker_part_1.databinding.ActivitySecondBinding

class SecondActivity: AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}