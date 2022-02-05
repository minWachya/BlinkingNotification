package com.example.blinkingnotification

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blinkingnotification.adapter.Alarm
import com.example.blinkingnotification.adapter.AlarmAdapter
import com.example.blinkingnotification.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

private const val TAG = "mmm"
private lateinit var binding: ActivityMainBinding

// 메인 화면
class MainActivity : AppCompatActivity() {
    // 뒤로가기 연속 클릭 대기 시간
    private var mBackWait : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 리사이클러뷰 매니저 설정
        val layoutManager = LinearLayoutManager(applicationContext)
        binding.recyclerView.layoutManager = layoutManager
        // 리아시클러뷰에 어댑터 달기
        val alarmAdapter = AlarmAdapter()
        binding.recyclerView.adapter = alarmAdapter
        alarmAdapter.arrAlarm.add(Alarm("제목1", "내용1", null))
        alarmAdapter.arrAlarm.add(Alarm("제목2", "내용2", null))
        alarmAdapter.arrAlarm.add(Alarm("제목3", "내용3", null))
        alarmAdapter.notifyDataSetChanged()

        // 서브 메뉴 달기
        setSupportActionBar(binding.toolBar)

        // + 버튼 누르면 '알림 설정 화면'으로 이동
        binding.fab.setOnClickListener {
            val intent = Intent(this@MainActivity, SetAlarmActivity::class.java)
            startActivity(intent)
        }

        // 파이어베이스 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        })

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
                // 도움말 액티비티로 이동
                val intent = Intent(this@MainActivity, HelpActivity::class.java)
                startActivity(intent)
                finish()    // 도움말 엑티비티에서 메인 액티비티로 다시 이동하기 때문에 종료
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

    // 2초 내애 뒤로가기 버튼을 2번 누르면 종료
    override fun onBackPressed() {
        if(System.currentTimeMillis() - mBackWait >= 2000 ) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this@MainActivity,"뒤로가기 버튼을 한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show()
        }
        else finish()
    }

}