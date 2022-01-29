package com.example.blinkingnotification

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.example.blinkingnotification.databinding.ActivitySetAlramBinding
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.result.contract.ActivityResultContracts

import androidx.core.app.ActivityCompat.startActivityForResult

private const val PICK_FROM_ALBUM = 0
private lateinit var binding: ActivitySetAlramBinding

// 알림 설정 화면
class SetAlarmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetAlramBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 갤러리에서 사진 선택 후 실행
        val getFromAlbumResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data // 선택한 이미지의 주소
                // 이미지 파일 읽어와서 설정하기
                if (uri != null) binding.imgView.setImageBitmap(BitmapFactory.decodeStream(contentResolver.openInputStream(uri)))
                else binding.imgView.setImageResource(R.drawable.ic_launcher_background)
            }
        }

        // 제목/내용 입력할 때마다 실시간으로 미리보기 변경
        binding.editTitle.doOnTextChanged { _, _, _, _ ->
            val title = binding.editTitle.text.toString()
            binding.tvTitle.text = title
        }
        binding.editContent.doOnTextChanged { _, _, _, _ ->
            val content = binding.editContent.text.toString()
            binding.tvContent.text = content
        }

        // 2번: 반복 시간 스피너 설정
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

        // 3번: 알림 형식 스피너 설정
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
                            binding.imgView.visibility = View.INVISIBLE
                        }
                        1 -> {
                            // 4. 이미지 선택 보이기
                            binding.textView4.visibility = View.VISIBLE
                            binding.view4.visibility = View.VISIBLE
                            binding.btnSelectImage.visibility = View.VISIBLE
                            binding.imgView.visibility = View.VISIBLE
                        }
                        else -> { Toast.makeText(applicationContext, "다시 선택해주세요.", Toast.LENGTH_SHORT).show() }
                    }
                }
            }
            selectModeSpinner.onItemSelectedListener = selectModeSpinnerAdapter
        }

        // 4번: 이미지 설정
        // 이미지 추가하기
        binding.btnSelectImage.setOnClickListener {
            // 갤러리에서 사진 선택해서 가져오기
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"     // 모든 이미지
            getFromAlbumResultLauncher.launch(intent)
        }

    }

}