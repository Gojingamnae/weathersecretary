package com.ilsa1000ri.weatherSecretary.ui.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.ilsa1000ri.weatherSecretary.MainActivity
import com.ilsa1000ri.weatherSecretary.R
//import com.ilsa1000ri.weatherSecretary.ui.api.KakaoCalendarApi
import com.kakao.sdk.auth.model.OAuthToken
import pub.devrel.easypermissions.EasyPermissions

class KakaoStyleTestFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private var accessToken: String? = null
    private lateinit var nameEditText: EditText
    private lateinit var sharedPreferences: SharedPreferences
    companion object {
        private const val TAG = "KakaoStyleTestFragment"
    }

    // 선택된 상태의 배경 이미지 리소스를 지정
    val selectedBackgroundResourceLong =
        R.drawable.style_toggle_long_on // 활동 스타일 토글의 선택된 상태 배경
    val selectedBackgroundResourceShort =
        R.drawable.style_toggle_short_on // 더위 민감도 및 추위 민감도 토글의 선택된 상태 배경

    val defaultBackgroundResourceLong =
        R.drawable.style_toggle_long_off // 활동 스타일 토글의 기본 상태 배경
    val defaultBackgroundResourceShort =
        R.drawable.style_toggle_long_off // 더위 민감도 및 추위 민감도 스타일 토글의 기본 상태 배경


    //위치권한 요청 시 사용
    private var fusedLocationClient: FusedLocationProviderClient? = null

    // 활동 스타일 그룹
    private lateinit var activityToggles: List<ToggleButton>

    // 더위 민감도 그룹
    private lateinit var heatToggles: List<ToggleButton>

    // 추위 민감도 그룹
    private lateinit var coldToggles: List<ToggleButton>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_kakao_styletest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance() // mAuth 초기화
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        val userId = mAuth.currentUser?.uid ?: ""
        accessToken = arguments?.getString("accessToken")
        sharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)


        //활동 스타일
        val physicalToggle = view.findViewById<ToggleButton>(R.id.physical)
        val intellectualToggle = view.findViewById<ToggleButton>(R.id.intellectual)
        val socialToggle = view.findViewById<ToggleButton>(R.id.social)
        val creativeToggle = view.findViewById<ToggleButton>(R.id.creative)

        //더위 민감도
        val heatSensitiveToggle = view.findViewById<ToggleButton>(R.id.heatSensitive)
        val heatAverageToggle = view.findViewById<ToggleButton>(R.id.heatAverage)
        val heatResistantToggle = view.findViewById<ToggleButton>(R.id.heatResistant)

        //추위 민감도
        val coldSensitiveToggle = view.findViewById<ToggleButton>(R.id.coldSensitive)
        val coldAverageToggle = view.findViewById<ToggleButton>(R.id.coldAverage)
        val coldResistantToggle = view.findViewById<ToggleButton>(R.id.coldResistant)

        nameEditText = view.findViewById(R.id.nameBox)
        val doneSignUpButton = view.findViewById<Button>(R.id.doneSignUp)

        // 회원가입하기 버튼 초기에는 비활성화
        doneSignUpButton.isEnabled = false

        // 활동 스타일 그룹 초기화
        activityToggles = listOf(physicalToggle, intellectualToggle, socialToggle, creativeToggle)
        // 더위 민감도 그룹 초기화
        heatToggles = listOf(heatSensitiveToggle, heatAverageToggle, heatResistantToggle)
        // 추위 민감도 그룹 초기화
        coldToggles = listOf(coldSensitiveToggle, coldAverageToggle, coldResistantToggle)

        // 각 그룹의 토글 버튼에 대한 상태 변경 리스너 등록
        activityToggles.forEach { toggle ->
            toggle.setOnClickListener {
                // 현재 클릭된 토글의 상태를 저장
                val isChecked = toggle.isChecked

                // 이전에 선택된 토글의 배경을 원래대로 변경
                activityToggles.filter { it.isChecked && it != toggle }
                    .forEach { it.setBackgroundResource(defaultBackgroundResourceLong) }

                // 현재 클릭된 토글을 제외하고 모든 토글 해제
                activityToggles.filter { it != toggle }.forEach { it.isChecked = false }

                // 현재 클릭된 토글의 상태를 다시 설정
                toggle.isChecked = isChecked

                // 선택된 토글의 배경을 변경
                toggle.setBackgroundResource(if (isChecked) selectedBackgroundResourceLong else defaultBackgroundResourceLong)

                // 회원가입하기 버튼 활성화 여부 설정
                doneSignUpButton.isEnabled = isOneToggleSelected(activityToggles) &&
                        isOneToggleSelected(heatToggles) &&
                        isOneToggleSelected(coldToggles)
            }
        }

        heatToggles.forEach { toggle ->
            toggle.setOnClickListener {
                // 현재 클릭된 토글의 상태를 저장
                val isChecked = toggle.isChecked

                // 이전에 선택된 토글의 배경을 원래대로 변경
                heatToggles.filter { it.isChecked && it != toggle }
                    .forEach { it.setBackgroundResource(defaultBackgroundResourceShort) }

                // 현재 클릭된 토글을 제외하고 모든 토글 해제
                heatToggles.filter { it != toggle }.forEach { it.isChecked = false }

                // 현재 클릭된 토글의 상태를 다시 설정
                toggle.isChecked = isChecked

                // 선택된 토글의 배경을 변경
                toggle.setBackgroundResource(if (isChecked) selectedBackgroundResourceShort else defaultBackgroundResourceShort)

                // 회원가입하기 버튼 활성화 여부 설정
                doneSignUpButton.isEnabled = isOneToggleSelected(activityToggles) &&
                        isOneToggleSelected(heatToggles) &&
                        isOneToggleSelected(coldToggles)
            }
        }

        coldToggles.forEach { toggle ->
            toggle.setOnClickListener {
                // 현재 클릭된 토글의 상태를 저장
                val isChecked = toggle.isChecked

                // 이전에 선택된 토글의 배경을 원래대로 변경
                coldToggles.filter { it.isChecked && it != toggle }
                    .forEach { it.setBackgroundResource(defaultBackgroundResourceShort) }

                // 현재 클릭된 토글을 제외하고 모든 토글 해제
                coldToggles.filter { it != toggle }.forEach { it.isChecked = false }

                // 현재 클릭된 토글의 상태를 다시 설정
                toggle.isChecked = isChecked

                // 선택된 토글의 배경을 변경
                toggle.setBackgroundResource(if (isChecked) selectedBackgroundResourceShort else defaultBackgroundResourceShort)

                // 회원가입하기 버튼 활성화 여부 설정
                doneSignUpButton.isEnabled = isOneToggleSelected(activityToggles) &&
                        isOneToggleSelected(heatToggles) &&
                        isOneToggleSelected(coldToggles)
            }
        }

        doneSignUpButton.setOnClickListener {
            // 사용자가 선택한 토글들에 따라 각 변수에 값 할당
//            oidc()
            processKakaoUserInfo(userId)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            // 위치 권한을 요청
            requestLocationPermission()

        }
    }

    // 그룹 내에서 하나의 토글만 선택되었는지 확인하는 함수
    fun isOneToggleSelected(toggles: List<ToggleButton>): Boolean {
        val selectedToggles = toggles.filter { it.isChecked }
        return selectedToggles.size == 1
    }

    private fun requestLocationPermission() {
        val permissionResult =
            requireContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionResult == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            //위치 권한을 거절했다가 설정에서 다시 권한을 허용한 경우
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    // 위치 권한 요청 결과를 처리하는 콜백
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "위치 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
//                requestSystemLocationEnable()
            }
        }

    private fun requestSystemLocationEnable() {
        Toast.makeText(requireContext(), "앱의 위치 권한을 설정에서 허용해주세요.", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", requireContext().packageName, null)
        startActivity(intent)
    }

    // 사용자 정보를 데이터베이스에 저장하는 메서드
    private fun processKakaoUserInfo(userId: String) {
        val db = Firebase.firestore
        val user = mAuth.currentUser
        val name = nameEditText.text.toString()

        val coldSensitivity =
            if (coldToggles[0].isChecked) 0 else if (coldToggles[1].isChecked) 1 else 2
        val heatSensitivity =
            if (heatToggles[0].isChecked) 0 else if (heatToggles[1].isChecked) 1 else 2
        val lifestyle =
            if (activityToggles[0].isChecked) 0 else if (activityToggles[1].isChecked) 1
            else if (activityToggles[2].isChecked) 2 else 3

        val userData = hashMapOf(
            "coldSensitivity" to coldSensitivity,
            "heatSensitivity" to heatSensitivity,
            "lifestyle" to lifestyle,
            "userid" to userId,
            "platform" to "google",
            "name" to name
        )


        val alarmData = hashMapOf(
            "isAlarmEnabled" to true,
            "briefingTime" to "09:00"
        )

        user?.let {
            val userId = it.uid
            db.collection(userId).document("UserInfo").set(userData)
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            db.collection(userId).document("Alarm").set(alarmData)
                .addOnSuccessListener { Log.d(TAG, "Alarm Setting successfully written!")
                    setLoggedIn(true)}
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }
    }


    private fun setLoggedIn(isLoggedIn: Boolean) {
        // SharedPreferences에 로그인 상태 저장
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }
}