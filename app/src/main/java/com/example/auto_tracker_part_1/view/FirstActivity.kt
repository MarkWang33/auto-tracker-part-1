package com.example.auto_tracker_part_1.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.auto_tracker_part_1.databinding.ActivityFirstBinding

class FirstActivity: AppCompatActivity() {
    private lateinit var binding: ActivityFirstBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        binding.buttonSwitchToSecond.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }

        binding.buttonSwitchToThird.setOnClickListener {
            startActivity(Intent(this, ThirdActivity::class.java))
        }
    }
}