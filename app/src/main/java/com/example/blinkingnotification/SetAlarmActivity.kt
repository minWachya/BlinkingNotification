package com.example.blinkingnotification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.example.blinkingnotification.databinding.ActivitySetAlramBinding

private lateinit var binding: ActivitySetAlramBinding

// 알림 설정 화면
class SetAlarmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetAlramBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 제목/내용 입력할 때마다 실시간으로 미리보기 변경
        binding.editTitle.doOnTextChanged { _, _, _, _ ->
            val title = binding.editTitle.text.toString()
            binding.tvTitle.text = title
        }
        binding.editContent.doOnTextChanged { _, _, _, _ ->
            val content = binding.editContent.text.toString()
            binding.tvContent.text = content
        }

        // 반복 시간 스피너 설정
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

        // 알림 형식 스피너 설정
        // 배열 추가
        val selectModeSpinner = binding.spinnerSelectMode
        ArrayAdapter.createFromResource(
            this@SetAlarmActivity,
            R.array.select_mode_array,
            android.R.layout.simple_dropdown_item_1line
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            selectModeSpinner.adapter = adapter
            // 어댑터 추가
            val selectModeSpinnerAdapter = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when(position) {
                        0 -> {
                            // 4. 이미지 선택 안보이게
                            binding.textView4.visibility = View.INVISIBLE
                            binding.view4.visibility = View.INVISIBLE
                            binding.btnSelectImage.visibility = View.INVISIBLE
                        }
                        1 -> {
                            // 4. 이미지 선택 보이기
                            binding.textView4.visibility = View.VISIBLE
                            binding.view4.visibility = View.VISIBLE
                            binding.btnSelectImage.visibility = View.VISIBLE
                            Toast.makeText(applicationContext, "이미지", Toast.LENGTH_SHORT).show()
                        }
                        else -> { Toast.makeText(applicationContext, "다시 선택해주세요.", Toast.LENGTH_SHORT).show() }
                    }
                }
            }
            selectModeSpinner.onItemSelectedListener = selectModeSpinnerAdapter
        }

    }

}