package com.example.blinkingnotification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.blinkingnotification.databinding.ActivitySetAlramBinding

private lateinit var binding: ActivitySetAlramBinding

// 알림 설정 화면
class SetAlarmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetAlramBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}