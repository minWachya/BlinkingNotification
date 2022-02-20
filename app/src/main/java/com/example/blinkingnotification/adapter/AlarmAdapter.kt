package com.example.blinkingnotification.adapter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blinkingnotification.AlarmReceiver
import com.example.blinkingnotification.R
import com.example.blinkingnotification.SetAlarmActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.list_item_alarm.view.*

data class Alarm(val title: String,
                 val content: String,
                 var imgUrl: String? = null,
                 val repeatTime: String,
                 val alarmType: String,
                 val timeStamp: String)

class AlarmAdapter : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {
    val arrAlarm = ArrayList<Alarm>() // 알림 배열

    // 뷰홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmAdapter.ViewHolder {
        // list_item_alarm.xml 파일과 연결
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_alarm, parent, false)

        return ViewHolder(itemView).apply {
            itemView.setOnClickListener {
                Toast.makeText(parent.context, arrAlarm[adapterPosition].title, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // position 번째 아이템 설정하기
    override fun onBindViewHolder(holder: AlarmAdapter.ViewHolder, position: Int) = holder.setItem(arrAlarm[position])

    // 아이템 갯수 리턴
    override fun getItemCount() = arrAlarm.size

    // -----------------데이터 조작함수 추가-----------------

    // 알림 뷰 생성
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setItem(item: Alarm) {
            itemView.item_tvTitle.text = item.title
            itemView.item_tvContent.text = item.content
            itemView.item_info.text = "${item.repeatTime}동안 반복 | ${item.alarmType}"
            if(item.imgUrl != null) {
                itemView.item_imgView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(item.imgUrl)
                    .error(R.drawable.ic_launcher_background)                  // 오류 시 이미지
                    .apply(RequestOptions().centerCrop())
                    .into(itemView.item_imgView)
            }

            // 수정하기
            itemView.imgbtnEdit.setOnClickListener {
                editData(this.layoutPosition, itemView)
            }
            // 삭제하기
            itemView.imgbtnDelete.setOnClickListener {
                removeData(this.layoutPosition, itemView)
            }
            // 토글 버튼 클릭
            val alarmManager = itemView.context.applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
            // 알람 조건 충족 시 리시버로 전달될 인텐트 설정
            val intent = Intent(itemView.context, AlarmReceiver::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)  // Activity가 아닌곳에서 startActivity() 사용
            val pendingIntent = PendingIntent.getBroadcast(
                itemView.context, AlarmReceiver.NOTIFICATION_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

            itemView.toggle.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
                val toastMessage = if (isChecked) {
                    val repeatInterval: Long = AlarmManager.INTERVAL_FIFTEEN_MINUTES
                    val triggerTime = (SystemClock.elapsedRealtime()    // 1
                            + repeatInterval)
                    // 인자 설정: ELAPSED_REALTIME
                    // 기기가 부팅된 후 경과한 시간을 기준, 상대적인 시간을 사용하여 알람을 발생
                    // 기기가 절전모드에 있을 때는 알람을 발생시키지 않고 해제되면 발생
                    alarmManager.setInexactRepeating(
                        AlarmManager.ELAPSED_REALTIME,
                        triggerTime, repeatInterval,
                        pendingIntent)
                    "Onetime Alarm On"
                } else {
                    alarmManager.cancel(pendingIntent)    // 알람 퓌소 시 등록한 pendingIntent를 인자로 전달
                    "Onetime Alarm Off"
                }
                Toast.makeText(itemView.context, toastMessage, Toast.LENGTH_SHORT).show()
            })
        }
    }

    // position 위치의 데이터를 삭제 후 어댑터 갱신
    fun removeData(position: Int, itemView: View) {
        // 파이어베이스 토큰 가져오기 + 알림 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(itemView.context, "다시 접속해주세요: 토큰 가져오기 실패", Toast.LENGTH_SHORT).show()
                return@OnCompleteListener
            }
            val token = task.result
            val timeStamp = arrAlarm[position].timeStamp

            // 문서 삭제
            val db = Firebase.firestore
            val docRef = db.collection(token).document(timeStamp)
            docRef.delete()
                .addOnSuccessListener {
                    arrAlarm.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(itemView.context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(itemView.context, "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }

            // 이미지 삭제
            val storageRef = Firebase.storage.reference
            val deserRef = storageRef.child(token).child("$timeStamp.jpg")
            deserRef.delete()
                .addOnFailureListener {
                    Toast.makeText(itemView.context, "사진 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }

        })
    }

    // position 위치의 데이터 수정
    fun editData(position: Int, itemView: View) {
        // 알림 정보 가지고 알림 생성 화면으로 아동
        val timeStamp = arrAlarm[position].timeStamp
        val intent = Intent(itemView.context, SetAlarmActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)  // Activity가 아닌곳에서 startActivity() 사용
        intent.putExtra("timeStamp", timeStamp)
        itemView.context.applicationContext.startActivity(intent)
    }
}