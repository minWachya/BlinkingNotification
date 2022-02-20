package com.example.blinkingnotification

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.example.blinkingnotification.databinding.ActivitySetAlramBinding
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.result.contract.ActivityResultContracts
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.core.widget.addTextChangedListener
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.IOException
import android.graphics.Bitmap
import android.graphics.Matrix
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blinkingnotification.adapter.Alarm
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.list_item_alarm.view.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


private const val TAG = "mmmSetAlarmActivity"
private lateinit var binding: ActivitySetAlramBinding

// 알림 설정 화면
class SetAlarmActivity : AppCompatActivity() {
    // 제목, 내용 입력 여부
    var checkTitle = false
    var checkContent = false

    private var token : String? = null   // firebase 토큰
    private var uri : Uri? = null       // 저장 전 사진 uri
    private var url : String? = null    // 저장 후 사진 url

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef : StorageReference

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetAlramBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        storage = Firebase.storage
        storageRef = storage.reference

        // 파이어베이스 토큰 가져오기 + 알림 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(applicationContext, "다시 접속해주세요: 토큰 가져오기 실패", Toast.LENGTH_SHORT).show()
                return@OnCompleteListener
            }
            token = task.result

            // 수정하기에서 접근
            val timeStamp = intent.getStringExtra("timeStamp")
            if(timeStamp != null) settingForEdit(timeStamp)
        })

        // 푸시 알림 실시간으로 미리보기
        binding.editTitle.doOnTextChanged { _, _, _, _ ->
            val title = binding.editTitle.text.toString()
            binding.tvTitle.text = title
        }
        binding.editContent.doOnTextChanged { _, _, _, _ ->
            val content = binding.editContent.text.toString()
            binding.tvContent.text = content
        }

        // 1번: 제목, 내용 입력
        binding.editTitle.addTextChangedListener {
            checkTitle = binding.editTitle.text.trim().toString().isNotEmpty()
            btnEnableCheck()    // 제목, 내용 모두 입력해야 버튼 활성화
        }
        binding.editContent.addTextChangedListener {
            checkContent = binding.editContent.text.trim().toString().isNotEmpty()
            btnEnableCheck()    // 제목, 내용 모두 입력해야 버튼 활성화
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
        }

        // 3번: 알림 형식 스피너 설정
        // 배열 추가
        val selectModeSpinner = binding.spinnerSelectType
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
                            btnEnableCheck()
                        }
                        1 -> {
                            // 4. 이미지 선택 보이기
                            binding.textView4.visibility = View.VISIBLE
                            binding.view4.visibility = View.VISIBLE
                            binding.btnSelectImage.visibility = View.VISIBLE
                            binding.imgView.visibility = View.VISIBLE
                            btnEnableCheck()
                        }
                        else -> { Toast.makeText(applicationContext, "다시 선택해주세요.", Toast.LENGTH_SHORT).show() }
                    }
                }
            }
            selectModeSpinner.onItemSelectedListener = selectModeSpinnerAdapter
        }

        // 4번: 이미지 설정
        // 갤러리에서 사진 선택 후 실행
        val getFromAlbumResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                uri = result.data?.data // 선택한 이미지의 주소
                // 이미지 파일 읽어와서 설정하기
                if (uri != null) {
                    // 사진 가져오기
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri!!))
                    // 사진의 회전 정보 가져오기
                    val orientation = getOrientationOfImage(uri!!).toFloat()
                    // 이미지 회전하기
                    val newBitmap = getRotatedBitmap(bitmap, orientation)
                    // 회전된 이미지로 imaView 설정
                    binding.imgView.setImageBitmap(newBitmap)
                }
                else binding.imgView.setImageResource(R.drawable.ic_launcher_background)

                btnEnableCheck()
            }
        }
        // 이미지 추가하기
        binding.btnSelectImage.setOnClickListener {
            // 갤러리에서 사진 선택해서 가져오기
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"     // 모든 이미지
            getFromAlbumResultLauncher.launch(intent)
        }

        // 완료 버튼
        binding.btnOK.setOnClickListener {
            val title = binding.editTitle.text.toString()
            val content = binding.editContent.text.toString()
            val repeatTime = binding.spinnerSelectTime.selectedItem.toString()
            val alarmType = binding.spinnerSelectType.selectedItem.toString()

            // 저장: token/날짜시간/알림내용
            val timeStamp =
                if (binding.btnOK.text == "완료") SimpleDateFormat("yyMMdd_HHmmss").format(Date())
                else intent.getStringExtra("timeStamp")!!
            val alarm = Alarm(title, content, null, repeatTime, alarmType, timeStamp)
            // DB에 저장
            saveAlarm(alarm, timeStamp)

            // 알림 울리기
//            startAlarm(alarm)
        }

    }

    // 이미지 회전 정보 가져오기
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getOrientationOfImage(uri: Uri): Int {
        val inputStream = contentResolver.openInputStream(uri)
        val exif: ExifInterface? = try {
            ExifInterface(inputStream!!)
        } catch (e: IOException) {
            e.printStackTrace()
            return -1
        }
        inputStream.close()

        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        if (orientation != -1) {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> return 90
                ExifInterface.ORIENTATION_ROTATE_180 -> return 180
                ExifInterface.ORIENTATION_ROTATE_270 -> return 270
            }
        }
        return 0
    }

    // 이미지 회전하기
    @Throws(Exception::class)
    private fun getRotatedBitmap(bitmap: Bitmap?, degrees: Float): Bitmap? {
        if (bitmap == null) return null
        if (degrees == 0F) return bitmap
        val m = Matrix()
        m.setRotate(degrees, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    // 모두 입력 시 버튼 활성화
    private fun btnEnableCheck() {
        val alarmType = binding.spinnerSelectType.selectedItem.toString()
        if (alarmType == "기본") binding.btnOK.isEnabled = checkTitle && checkContent
        else binding.btnOK.isEnabled = checkTitle && checkContent && (uri != null || url != null)

    }

    // <완료> 버튼 클릭 시 DB에 알림 정보 저장
    private fun saveAlarm(alarm: Alarm, timeStamp: String) {
        if(token == null) {
            Toast.makeText(applicationContext, "토큰 발급에 실패했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 사진 저장
        if (uri != null) {
            // 사진 저장
            url = saveImg(timeStamp)
            alarm.imgUrl = url
            Log.d(TAG, url!!)
        }
        // 알림 정보 저장
        Firebase.firestore.collection(token!!).document(timeStamp).set(alarm)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    Toast.makeText(applicationContext, "알림을 저장했습니다.", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                else Toast.makeText(applicationContext, "다시 시도해주세요: 저장 실패", Toast.LENGTH_SHORT).show()
            }

        // FCM 보내기
//        val bundle = Bundle()
//        bundle.putString("title", alarm.title)
//        bundle.putString("message", alarm.content)
//        if (uri != null) {
//            bundle.putString("imgUrl", alarm.imgUrl)
//            bundle.putString("type", NotificationType.IMAGE.toString())
//            bundle.putString("imgUri", uri.toString())
//        }
//        else bundle.putString("type", NotificationType.NORMAL.toString())
//        val remoteMessage = RemoteMessage(bundle)
//        val fcm = MyFirebaseMessagingService(applicationContext)
//        fcm.onMessageReceived(remoteMessage)
    }

    // "이미지" 선택 시: 이미지 저장 + URL 반환
    private fun saveImg(timeStamp: String): String {
        val fileName = "$timeStamp.jpg" // 파일명명
        val imgRef = storageRef.child(token!!).child(fileName)
        imgRef.putFile(uri!!)
            .addOnFailureListener {
                Toast.makeText(applicationContext, "이미지 저장에 실패했습니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
            }

        return "https://firebasestorage.googleapis.com/v0/b/blinkingnotification.appspot.com/o/" +
                token + "%2F" + fileName + "?alt=media"
    }

    // 알림 시작
    private fun startAlarm(alarm: Alarm) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(applicationContext, MainActivity::class.java).let { intent ->
            PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
        }
//        alarmManager?.setInexactRepeating(
//                AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(1), // AlarmManager.INTERVAL_FIFTEEN_MINUTES,
//                AlarmManager.INTERVAL_HALF_HOUR,
//                alarmIntent
//        )
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 17)
            set(Calendar.MINUTE, 2)
        }
        alarmManager?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            TimeUnit.MINUTES.toMillis(1),
            alarmIntent
        )
    }

    // 수정하기: 알림 정보 보여주기 + 버튼 text "수정 완료"로 변경
    private fun settingForEdit(timeStamp: String) {
        val db = Firebase.firestore
        val docRef = db.collection(token!!).document(timeStamp)

        // 알림 데이터 가져오기
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val map = document.data as HashMap<String, Any>
                    val title : String = map["title"] as String
                    val content : String = map["content"] as String
                    url = map["imgUrl"] as String?  // "완료" 버튼 enable 체크를 위함
                    val strRepeatTime : String = map["repeatTime"] as String
                    val strAlarmType : String = map["alarmType"] as String
                    val repeatTime =
                        when(strRepeatTime) {
                            "1분" -> 0
                            "5분" -> 1
                            "10분" -> 2
                            "20분" -> 3
                            "30분" -> 4
                            "40분" -> 5
                            "50분" -> 6
                            "1시간" -> 7
                            else -> 0
                        }
                    val alarmType = if(strAlarmType == "기본") 0 else 1

                    // 설정하기
                    binding.editTitle.setText(title)
                    binding.editContent.setText(content)
                    binding.spinnerSelectTime.setSelection(repeatTime)
                    binding.spinnerSelectType.setSelection(alarmType)
                    if(url != null) {
                        Glide.with(applicationContext)
                            .load(url)
                            .error(R.drawable.ic_launcher_background)                  // 오류 시 이미지
                            .apply(RequestOptions().centerCrop())
                            .into(binding.imgView)
                    }
                    binding.btnOK.text = "수정 완료"

                } else {
                    Toast.makeText(applicationContext, "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "알림 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "get failed with ", exception)
            }
    }

}