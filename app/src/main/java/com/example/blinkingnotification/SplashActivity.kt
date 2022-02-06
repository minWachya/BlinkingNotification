package com.example.blinkingnotification

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.example.blinkingnotification.databinding.ActivitySplashBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

private lateinit var binding: ActivitySplashBinding

// 스플래시
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 권한 설정하기 -> 스플래시 -> (도움말)
        setPermission()
    }

    // tedpermission 설정
    private fun setPermission() {
        // 권한 묻는 팝업 만들기
        val permission = object : PermissionListener {
            // 설정해놓은 권한을 허용됐을 때
            override fun onPermissionGranted() {
                // 초기 도움말 화면 보이기
                val sharedPreference = getSharedPreferences("help", MODE_PRIVATE)
                val isShow = sharedPreference.getBoolean("isShow", true)
                if(isShow) {
                    val intent = Intent(this@SplashActivity, HelpActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    // 메인 액티비티로 이동
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            // 설정해놓은 권한을 거부됐을 때
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                // 권한 없어서 요청
                AlertDialog.Builder(applicationContext)
                    .setMessage("권한 거절로 인해 일부 기능이 제한됩니다.")
                    .setPositiveButton("권한 설정하러 가기") { dialog, which ->
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:com.example.blinkingnotification"))
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                            startActivity(intent)
                        }
                    }
                    .show()
            }
        }

        // 권한 설정
        TedPermission.create()
            .setPermissionListener(permission)
            .setRationaleMessage("깜빡 알림을 이용하기 위해 권한을 허용해주세요.")
            .setDeniedMessage("권한을 거부하셨습니다. [앱 설정]->[권한] 항목에서 허용해주세요.")
            .setPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }

}