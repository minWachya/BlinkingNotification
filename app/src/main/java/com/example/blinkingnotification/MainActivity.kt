package com.example.blinkingnotification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.blinkingnotification.databinding.ActivityMainBinding

private lateinit var binding: ActivityMainBinding

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
                return true
            }
        }
        return false
    }

}