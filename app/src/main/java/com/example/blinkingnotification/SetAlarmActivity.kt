package com.example.blinkingnotification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.blinkingnotification.databinding.ActivitySetAlramBinding

private lateinit var binding: ActivitySetAlramBinding

// 알림 설정 화면
class SetAlarmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetAlramBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 반복 시간 설정 스피너
        // 배열 추가
        val selectTimeSpinner = binding.spinnerSelectTime
        ArrayAdapter.createFromResource(
            this@SetAlarmActivity,
            R.array.select_time_array,
            android.R.layout.simple_dropdown_item_1line
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            selectTimeSpinner.adapter = adapter
            // 어댑터 추가
            val selectTimeSpinnerAdapter = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when(position) {
                        0 -> { Toast.makeText(applicationContext, "1분", Toast.LENGTH_SHORT).show() }
                        1 -> { Toast.makeText(applicationContext, "5분", Toast.LENGTH_SHORT).show() }
                        else -> { Toast.makeText(applicationContext, "다른 거", Toast.LENGTH_SHORT).show() }
                    }
                }
            }
            selectTimeSpinner.onItemSelectedListener = selectTimeSpinnerAdapter
        }
    }
}