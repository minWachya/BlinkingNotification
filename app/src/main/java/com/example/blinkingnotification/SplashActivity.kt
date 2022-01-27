package com.example.blinkingnotification

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.blinkingnotification.databinding.ActivitySplashBinding

private lateinit var binding: ActivitySplashBinding

// 스플래시
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        // 초기 도움말 화면 보이기
        val sharedPreference = getSharedPreferences("help", MODE_PRIVATE)
        val isShow = sharedPreference.getBoolean("isShow", true)
        if(isShow) {
            val intent = Intent(this@SplashActivity, HelpActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            // 메인 액티비티로 이동
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}