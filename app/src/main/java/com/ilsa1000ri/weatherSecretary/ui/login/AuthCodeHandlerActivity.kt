package com.ilsa1000ri.weatherSecretary.ui.login

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.ilsa1000ri.weatherSecretary.MainActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class AuthCodeHandlerActivity: AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "카카오계정으로 로그인 실패", error)
        } else if (token != null) {
            Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )
        auth = Firebase.auth
        sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)


        KakaoSdk.init(this, "a7f9f27f78e8d6ecf8c8691c1aeb5aa6")
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    oidc()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val db = Firebase.firestore
            val userId = user.uid
            db.collection(userId).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // Collection does not exist, navigate to TermsActivity
                        val intent = Intent(this, TermsActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Collection exists, navigate to MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        setLoggedIn(true)
                    }
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        } else {
            // Handle user being null (e.g., navigate to login screen)
        }
    }

    fun oidc() {
        val providerBuilder = OAuthProvider.newBuilder("oidc.kakao")
        signInWithProvider(providerBuilder)
    }

    private fun signInWithProvider(providerBuilder: OAuthProvider.Builder) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()

        // Firebase Auth에 OIDC provider 추가
        auth.startActivityForSignInWithProvider(this, providerBuilder.build())
            .addOnSuccessListener { authResult ->
                val user = auth.currentUser
                updateUI(user)
            }
            .addOnFailureListener { e ->
                handleSignInFailure(e)
            }
    }

//    private fun kakaoSignIn() {
//        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
//        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
//            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
//                if (error != null) {
//                    Log.e(TAG, "카카오톡으로 로그인 실패", error)
//
//                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
//                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
//                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
//                        return@loginWithKakaoTalk
//                    }
//
//                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
//                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
////                    Toast.makeText(this, "카카오톡을 설치한 후 카카오 로그인을 사용해주세요.", Toast.LENGTH_SHORT).show()
////                    finish()
//                }
//                // 로그인 성공
//                else if (token != null) {
//                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
//                    UserApiClient.instance.me { user, error ->
//                        if (error != null) {
//                            Log.e(TAG, "사용자 정보 요청 실패", error)
//                        } else if (user != null) {
//                            Log.i(
//                                TAG, "사용자 정보 요청 성공" +
//                                        "\n이메일: ${user.kakaoAccount?.email}"
//                            )
//                            val scopes = mutableListOf<String>()
//                            if (user.kakaoAccount?.emailNeedsAgreement == true) {
//                                scopes.add("account_email")
//                            }//email
//                            if (user.kakaoAccount?.emailNeedsAgreement == true) {
//                                scopes.add("talk_calendar")
//                            }//톡캘린더 및 일정 생성, 조회, 편집/삭제
//                            if (user.kakaoAccount?.emailNeedsAgreement == true) {
//                                scopes.add("talk_calendar_task")
//                            }//톡캘린더 내 할 일 생성, 조회, 편집/삭제
//
//                            if (scopes.count() > 0) {
//                                Log.d(TAG, "사용자에게 추가 동의를 받아야 합니다.")
//
//                                // OpenID Connect 사용 시
//                                // scope 목록에 "openid" 문자열을 추가하고 요청해야 함
//                                // 해당 문자열을 포함하지 않은 경우, ID 토큰이 재발급되지 않음
//                                // scopes.add("openid")
//
//                                //scope 목록을 전달하여 카카오 로그인 요청
//                                UserApiClient.instance.loginWithNewScopes(
//                                    this,
//                                    scopes
//                                ) { token, error ->
//                                    if (error != null) {
//                                        Log.e(TAG, "사용자 추가 동의 실패", error)
//                                    } else {
//                                        Log.d(TAG, "allowed scopes: ${token!!.scopes}")
//
//                                        // 사용자 정보 재요청
//                                        UserApiClient.instance.me { user, error ->
//                                            if (error != null) {
//                                                Log.e(TAG, "사용자 정보 요청 실패", error)
//                                            } else if (user != null) {
//                                                Log.i(TAG, "사용자 정보 요청 성공")
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
////                            val intent = Intent(this, TermsActivity::class.java)
////                            Log.i(TAG, "Token : ${token}}")
////                            intent.putExtra("accessToken", token.accessToken)
////                            startActivity(intent)
////                            finish()
//        } else {
//            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
//        }
//    }

    private fun handleSignInFailure(exception: Exception) {
        Log.e(TAG, "로그인 실패", exception)
    }

    private fun setLoggedIn(isLoggedIn: Boolean) {
        // SharedPreferences에 로그인 상태 저장
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }
}
