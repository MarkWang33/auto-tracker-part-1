package com.example.auto_tracker_part_1.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.auto_tracker_part_1.databinding.ActivityThirdBinding

class ThirdActivity: AppCompatActivity() {
    private lateinit var binding: ActivityThirdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}