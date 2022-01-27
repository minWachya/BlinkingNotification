package com.example.blinkingnotification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.blinkingnotification.databinding.ActivityHelpBinding

private lateinit var binding: ActivityHelpBinding

// 초기 도움말 화면
class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val pager = binding.pager
        val indicator = binding.indicator

        // 어댑터 연결
        pager.adapter = VpagerAdapter(this)
        pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        pager.offscreenPageLimit = 4
        // 다음/이해했어요! 설정 리스너
        pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when(position){
                    0 -> binding.btnNextAndOK.text = "다음"
                    1 -> binding.btnNextAndOK.text = "다음"
                    2 -> binding.btnNextAndOK.text = "이해했어요!"
                    // 디폴트는 시작 페이지로
                    else -> {
                        pager.currentItem = 0
                        binding.btnNextAndOK.text = "다음"
                    }
                }
            }
        })


        // indicator 설정
        indicator.setViewPager(pager)                         // 페이저는 pager로 사용
        indicator.createIndicators(3, 0)    // 전체 3, 현재 0

        // 프레그먼트 선택할 때마다 하단에 몇 번째 페이지인지 토스트로 보여주기
        pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                indicator.animatePageSelected(position) // 현재 포지션으로 바뀌는 애니메이션 실핼
            }
        })

        // '다음'버튼 누르면 해당 다음 프레그먼트로 이동
        binding.btnNextAndOK.setOnClickListener {
            when(pager.currentItem){
                0 -> pager.currentItem = 1
                1 -> pager.currentItem = 2
                2 -> {
                    // 다음에도 보이게 설정
                    val sharedPreference = getSharedPreferences("help", MODE_PRIVATE)
                    val editor = sharedPreference.edit()
                    editor.putBoolean("isShow", true)
                    editor.apply()
                    finish()
                }
                // 디폴트는 시작 페이지로
                else -> pager.currentItem = 0
            }
        }
        // '다시 보지 않기' 버튼 클릭
        binding.btnNeverSee.setOnClickListener {
            // 다음에는 안 보이게 설정
            val sharedPreference = getSharedPreferences("help", MODE_PRIVATE)
            val editor = sharedPreference.edit()
            editor.putBoolean("isShow", false)
            editor.apply()
            finish()
            finish()
        }

    }

    inner class VpagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        // 어댑터에서 다루는 아이템의 갯수
        override fun getItemCount(): Int {
            return 3    // 도움말 이미지 3장
        }

        // 현재 선택된 프레그먼트 번호에 따른 프레그먼트 보여주기
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> HelpFragment1()
                1 -> HelpFragment2()
                2 -> HelpFragment3()
                // 디폴트는 시작 페이지로
                else -> HelpFragment1()
            }
        }
    }

}