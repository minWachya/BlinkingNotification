package com.example.blinkingnotification.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blinkingnotification.R
import kotlinx.android.synthetic.main.list_item_alarm.view.*
import java.net.URL

data class Alarm(val title: String,
                 val content: String,
                 var imgUrl: String? = null,
                 val repeatTime: String,
                 val alarmType: String)

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

    // 알림 뷰 생성
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setItem(item: Alarm) {
            itemView.tvTitle.text = item.title
            itemView.tvContent.text = item.content
            if(item.imgUrl != null) {
                itemView.imgView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(item.imgUrl)
                    .error(R.drawable.ic_launcher_background)                  // 오류 시 이미지
                    .apply(RequestOptions().centerCrop())
                    .into(itemView.imgView)
            }
        }
    }
}