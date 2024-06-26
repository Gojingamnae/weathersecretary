package com.ilsa1000ri.weatherSecretary.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.google.android.gms.common.SignInButton
import com.ilsa1000ri.weatherSecretary.MainActivity
import com.ilsa1000ri.weatherSecretary.R
import com.kakao.sdk.common.util.Utility

class LoginActivity :AppCompatActivity(){
    private lateinit var loginPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        Log.d("LoginActivity", "isLoggedIn: ${isLoggedIn()}")

        if (isLoggedIn()) {
            navigateToMainActivity()
            return
        }

        // Google 로그인 버튼을 찾습니다.
        val googleSignInButton = findViewById<SignInButton>(R.id.googleSignInButton)
        val kakaoSignInButton = findViewById<ImageButton>(R.id.kakaoSignInButton)

        // Google 로그인 버튼 클릭 이벤트 핸들러를 설정합니다.
        googleSignInButton.setOnClickListener {
            val intent = Intent(this, GoogleLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        kakaoSignInButton.setOnClickListener {
            val intent = Intent(this, AuthCodeHandlerActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun isLoggedIn(): Boolean {
        // SharedPreferences에서 로그인 상태 확인
        return  loginPreferences.getBoolean("isLoggedIn", false)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}