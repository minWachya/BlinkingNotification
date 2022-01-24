package com.example.blinkingnotification

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.blinkingnotification.databinding.ActivityMainBinding

private lateinit var binding: ActivityMainBinding

// 메인 화면
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 서브 메뉴 달기
        setSupportActionBar(binding.toolBar)

    }


    // 서브 메뉴 등록
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.sub_menu, menu)
        return true
    }

    // 메뉴 클릭 시 동작하는 메소드
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help -> {
                Toast.makeText(applicationContext, "도움!", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.ask -> {
                // 문의하기 액티비티로 이동
                val intent = Intent(this@MainActivity, AskActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return false
    }

}