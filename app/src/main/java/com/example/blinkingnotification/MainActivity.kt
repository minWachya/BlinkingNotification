package com.example.blinkingnotification

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blinkingnotification.adapter.Alarm
import com.example.blinkingnotification.adapter.AlarmAdapter
import com.example.blinkingnotification.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "mmmMainActivity"
private lateinit var binding: ActivityMainBinding

// 메인 화면
class MainActivity : AppCompatActivity() {
    // 뒤로가기 연속 클릭 대기 시간
    private var mBackWait : Long = 0
    private var token : String = "-1"
    lateinit var alarmAdapter : AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 리사이클러뷰 매니저 설정
        val layoutManager = LinearLayoutManager(applicationContext)
        binding.recyclerView.layoutManager = layoutManager
        // 리아시클러뷰에 어댑터 달기
        alarmAdapter = AlarmAdapter()
        binding.recyclerView.adapter = alarmAdapter

        // 파이어베이스 토큰 가져오기 + 알림 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(applicationContext, "다시 접속해주세요: 토큰 가져오기 실패", Toast.LENGTH_SHORT).show()
                return@OnCompleteListener
            }
            token = task.result
            getAlarms(token)
        })

        // 서브 메뉴 달기
        setSupportActionBar(binding.toolBar)

        // '알림 설정 화면'으로 이동 후 실행할 작업: Firebase에서 알림 정보 가져오기
        val setAlarmActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) getAlarms(token)
            else Toast.makeText(applicationContext, "알림 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
        // + 버튼 누르면 '알림 설정 화면'으로 이동
        binding.fab.setOnClickListener {
            val intent = Intent(this@MainActivity, SetAlarmActivity::class.java)
            setAlarmActivityResult.launch(intent)
        }

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

    // 사용자가 설정한 알림 정보 가져오기 + 어댑터에 아이템 추가
    private fun getAlarms(token: String) {
        if (token == "-1") {
            Toast.makeText(applicationContext, "종료 후 다시 접속해주세요: 토큰 발급 실패", Toast.LENGTH_SHORT).show()
            return
        }

        // 알림 데이터 가져오기
        val db = Firebase.firestore
        val docRef = db.collection(token)
        docRef.get()
            .addOnSuccessListener { result ->
                // 배열 초기화
                alarmAdapter.arrAlarm.clear()

                // 배열에 알림 정보 아이템 담기
                for (document in result) {
                    val map = document.data as HashMap<String, Any>
                    val title : String = map["title"] as String
                    val content : String = map["content"] as String
//                    val img : String = map["title"] as String
                    val repeatTime : String = map["repeatTime"] as String
                    val alarmType : String = map["alarmType"] as String

                    // 어댑터에 데이터 넣기
                    alarmAdapter.arrAlarm.add(Alarm(title, content, null, repeatTime, alarmType))
                }
                // 어댑터 업데이트
                alarmAdapter.notifyDataSetChanged()

                // 알림 갯수 0개가 아니면 안내 문구 지우기
                if (alarmAdapter.itemCount != 0) binding.infoText.visibility = View.INVISIBLE
                else binding.infoText.visibility = View.VISIBLE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "다시 시도해주세요: 데이터 가져오기 실패", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "get failed with ", exception)
            }
    }

}