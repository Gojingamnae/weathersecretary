//HomeFragment
package com.ilsa1000ri.weatherSecretary.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.widget.LinearLayout
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.ilsa1000ri.weatherSecretary.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.ui.api.HomeApi
import com.ilsa1000ri.weatherSecretary.ui.api.LocationApi
import com.ilsa1000ri.weatherSecretary.ui.api.OpenApi
import com.ilsa1000ri.weatherSecretary.ui.location.AreaManageFragment
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date

class HomeFragment : Fragment() {
    private lateinit var homeFragmentRealShort: HomeFragmentRealShort
    private lateinit var homeFragmentShort: HomeFragmentShort

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser
    private val currentDate = Date() // 변수 선언 변경
    private val calendar = Calendar.getInstance().apply {
        time = currentDate // 초기화 블록 안에서 값 할당
    }
    private val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private lateinit var scheduleLinearLayout: LinearLayout
    private lateinit var userStyle1TextView: TextView
    private lateinit var userStyle2TextView: TextView
    private lateinit var userStyle3TextView: TextView
    private lateinit var item1TextView: TextView
    private lateinit var item2TextView: TextView
    private lateinit var item3TextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationResult: String? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var address: String = ""
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root
        userStyle1TextView = binding.userStyle1TextView
        userStyle2TextView = binding.userStyle2TextView
        userStyle3TextView = binding.userStyle3TextView

        item1TextView = binding.item1TextView
        item2TextView = binding.item2TextView
        item3TextView = binding.item3TextView

        scheduleLinearLayout = binding.scheduleLinearLayout


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        sharedPreferences =
            requireContext().getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        latitude = sharedPreferences.getString("latitude", "0.0")?.toDoubleOrNull() ?: 0.0
        longitude = sharedPreferences.getString("longitude", "0.0")?.toDoubleOrNull() ?: 0.0
        address = sharedPreferences.getString("address", "") ?: ""
        fetchUserStyle()
        fetchDataAndPopulateViews()
        PTYInfo()
        fetchScheduleData()


        // 날씨 출력 코드
        homeFragmentRealShort = HomeFragmentRealShort()
        homeFragmentRealShort.initializeViews(binding)
        homeFragmentRealShort.fetchDataWeatherRealShort(latitude, longitude)
        homeFragmentRealShort.fetchDataWeather(latitude, longitude, requireContext().resources)

        homeFragmentShort = HomeFragmentShort()
        homeFragmentShort.initializeViews2(binding)
        homeFragmentShort.fetchDataWeatherShort(latitude, longitude)


        val gpsButton = binding.gpsButton
        gpsButton.setOnClickListener {
            initLocation() //다시 위치 추적
            binding.locationTextView.text = locationResult
        }
        // 위치 권한이 있는지 확인
        if (checkLocationPermission()) {
            initLocation()
            // 위치 권한이 허용되어 있으면 위치 요청 초기화
            binding.locationTextView.text = locationResult
        } else {
            // 위치 권한을 요청
            requestLocationPermission()
        }

        val goToAreaManage = binding.goToAreaManage
        goToAreaManage.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_navigation_home_to_areaManageFragment)

            // FrameLayout을 보이도록 함
            binding.fragmentAreaManageContainer.visibility = View.VISIBLE
        }

        binding.locationTextView.text = address
    }

    // 일정
    private fun fetchScheduleData() {
        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        Log.d("오늘 날짜", "$todayDateString")
        currentUser?.let { user ->
            val userId = user.uid
            val userDocument = db.collection(userId).document(todayDateString)
            userDocument.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        Log.d("HomeFragment", "fetchScheduleData의 documentSnapshot(191):${documentSnapshot.data}")
                        val scheduleData = documentSnapshot.data
                        if (scheduleData != null) {
                            val scheduleList = mutableListOf<Pair<String, String>>()
                            for ((key, value) in scheduleData) {
                                if (value is Map<*, *>) {
                                    val summary = value["summary"] as? String
                                    if (summary != null) {
                                        val startHour = key.substring(0, 2).toInt()
                                        val startMinute = key.substring(2, 4).toInt()
                                        val endHour = key.substring(4, 6).toInt()
                                        val endMinute = key.substring(6, 8).toInt()

                                        val startTimeString = String.format("%02d:%02d", startHour, startMinute)
                                        val endTimeString = String.format("%02d:%02d", endHour, endMinute)

                                        val itemText = "$startTimeString ~ $endTimeString                   $summary"
                                        scheduleList.add(Pair(key, itemText))

                                        Log.d("일정", "Time Range: $startTimeString ~ $endTimeString, Summary: $summary")
                                    }
                                }
                            }

                            // Sort the schedule list by the key (start time)
                            scheduleList.sortBy { it.first }

                            // Add sorted schedule items to the layout
                            for ((_, itemText) in scheduleList) {
                                addScheduleItemToLayout(itemText)
                            }
                        }
                    } else {
                        Log.d("일정", "No documents found for $todayDateString")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ScheduleManager", "Error fetching document for $todayDateString: ${e.message}")
                }
        }
    }

    private fun addScheduleItemToLayout(item: String) {
        val textView = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // MATCH_PARENT로 변경하여 전체 너비를 차지하도록 함
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20, 0, 30, 10)
            }
            text = item
            textSize = 20f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            setBackgroundResource(R.drawable.rounded_rectangle)
            setTypeface(null, Typeface.BOLD)
            textAlignment = View.TEXT_ALIGNMENT_TEXT_START // 왼쪽 정렬 유지
        }
        scheduleLinearLayout.addView(textView)
    }

    //추천 아이템
    private fun fetchDataAndPopulateViews() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val feellike = OpenApi.getWeatherStatus(latitude, longitude)
                val randomItems = OpenApi.getRandomItems(latitude, longitude, feellike)
                Log.d("randomItems", "$randomItems")
                randomItems?.let { items ->
                    // Set item text for item1TextView and item2TextView
                    item1TextView.text = items.first
                    item2TextView.text = items.second

                } ?: run {
                    // If items are not available, set appropriate text
                    item1TextView.text = "멀티비타민"
                    item2TextView.text = "텀블러"
                }
            } catch (e: Exception) {
                // Handle exceptions
                Log.e("HomeFragment", "Error: ${e.message}")
            }
        }
    }

    private fun PTYInfo() {
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        Log.d("시작부분", "출력됨")

        GlobalScope.launch(Dispatchers.Main) {
            val shortIndex = HomeApi.getShortIndex(latitude, longitude)
            Log.d("이거봐", "$shortIndex")
            calendar.add(Calendar.DAY_OF_MONTH, 0)

            val futureDateString = formatter.format(calendar.time)
            var ptyItem: String? = null

            for (index in shortIndex.indices) {
                val data = shortIndex[index]
                if (data.startsWith("PTY") && data.contains("Date: $futureDateString")) {
                    val ptyValue = data.split(": ")[1].split(",")[0]
                    Log.d("PTYValue", ptyValue)
                    ptyItem = ActivitiesWithWeather(ptyValue)
                    Log.d("PTYItem", ptyItem)
                    item3TextView.text = ptyItem
                    break // PTY 값을 찾으면 루프를 종료
                } else if (data.startsWith("SKY") && data.contains("Date: $futureDateString")) {
                    val skyValue = data.split(": ")[1].split(",")[0]
                    Log.d("SKYValue", skyValue)
                    ptyItem = ActivitiesWithWeather(skyValue)
                    Log.d("Passed to ActivitiesWithWeather", skyValue)
                    Log.d("SKYItem", ptyItem)
                    item3TextView.text = ptyItem
                    break // SKY 값을 찾으면 루프를 종료
                }
            }

            // PTY 값이 설정되지 않은 경우 기본값 "립밤"을 사용
            if (ptyItem == null) {
                Log.d("기본값", "립밤으로 설정됨")
                item3TextView.text = "립밤"
            }
        }
    }

    private fun ActivitiesWithWeather(lifestyle: String): String {
        val activities = when (lifestyle) {
            "비" -> listOf("우산", "레인부츠", "비타민C")
            "눈" -> listOf("눈오리", "장갑", "담요", "핫팩", "마스크")
            "구름" -> listOf("비타민D", "오메가3", "루테인", "헤드셋")
            "해" -> listOf("썬글라스", "모자", "물", "멀티비타민")
            //"흐림" -> listOf("모자", "크로스백", "스니커즈", "썬크림")

            else -> emptyList()
        }
        // activities 리스트에서 랜덤하게 3개의 활동을 선택하여 반환
        return activities.shuffled().take(1).joinToString(" ")
    }

    // 추천 스타일
    private fun fetchUserStyle() { // 추천 스타일
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val openIndex = OpenApi.getOpenIndexInCelsius(latitude, longitude)
                currentUser?.let { user ->
                    val userId = user.uid
                    Log.d("FirestoreData", "UserId: $userId")
                    val docRef = db.collection(userId).document("UserInfo")
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val userData = document.data
                                Log.d("FirestoreData", "UserData: $userData")

                                val style = userData?.get("lifestyle") as? Number
                                val intStyle = style?.toInt() ?: 0
                                val cold = userData?.get("coldSensitivity") as? Number
                                val heat = userData?.get("heatSensitivity") as? Number

                                var userStyleItems = listOf<String>() // 리스트로 변경
                                if (intStyle in 0..3) {
                                    userStyleItems = when (intStyle) {
                                        0 -> {
                                            when {
                                                openIndex != null -> {
                                                    when {
                                                        openIndex >= 20 -> getRandomActivities("summer")
                                                        openIndex <= 0 -> getRandomActivities("winter")
                                                        else -> getRandomActivities("Spring")
                                                    }
                                                }

                                                else -> listOf("") // 빈 리스트 반환
                                            }
                                        }

                                        1 -> getRandomActivities("1")
                                        2 -> getRandomActivities("2")
                                        3 -> getRandomActivities("3")
                                        else -> listOf("알 수 없는 에러")
                                    }
                                } else {
                                    userStyleItems = listOf("활동 추천: No recommendation")
                                }

                                Log.d("Style", "Item: $userStyleItems")
                                if (userStyleItems.size >= 3) {
                                    userStyle1TextView.text = userStyleItems[0]
                                    userStyle2TextView.text = userStyleItems[1]
                                    userStyle3TextView.text = userStyleItems[2]
                                }
                            } else {
                                Log.d("FirestoreData", "No such document")
                                userStyle1TextView.text = "아이템 정보를 가져오는 동안 오류가 발생했습니다."
                                userStyle2TextView.text = ""
                                userStyle3TextView.text = ""
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("FirestoreData", "get failed with ", exception)
                            userStyle1TextView.text = "아이템 정보를 가져오는 동안 오류가 발생했습니다."
                            userStyle2TextView.text = ""
                            userStyle3TextView.text = ""
                        }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error: ${e.message}")
                userStyle1TextView.text = "아이템 정보를 가져오는 동안 오류가 발생했습니다."
                userStyle2TextView.text = ""
                userStyle3TextView.text = ""
            }
        }
    }

    // 추천 스타일
    private fun getRandomActivities(lifestyle: String): List<String> {
        Log.d("getRandomIn", "$lifestyle")
        val activities = when (lifestyle) {
            "Spring" -> listOf(
                "산책", "자전거", "낚시", "조깅", "등산", "피크닉", "꽃구경", "캠프파이어", "요가", "필라테스",
                "GYM", "테니스", "스쿼시", "수영", "골프", "야구", "볼링", "탁구", "당구", "배드민턴", "홈트",
                "복싱", "주짓수", "스트레칭", "태권도", "점핑댄스", "클라이밍", "사격", "방탈출", "양궁", "승마",
                "VR", "루지"
            )

            "summer" -> listOf(
                "산책", "자전거", "낚시", "조깅", "등산", "수영", "서핑", "비치발리볼", "카누/카약", "수상레저",
                "수상스키", "요트", "요가", "필라테스", "GYM", "테니스", "스쿼시", "수영", "골프", "야구", "볼링",
                "탁구", "당구", "배드민턴", "홈트", "복싱", "주짓수", "스트레칭", "태권도", "점핑댄스", "클라이밍",
                "사격", "방탈출", "양궁", "승마", "VR", "루지"
            )

            "winter" -> listOf(
                "산책", "자전거", "낚시", "조깅", "등산", "스케이트", "스키장", "온천", "요가", "필라테스", "GYM",
                "테니스", "스쿼시", "수영", "골프", "야구", "볼링", "탁구", "당구", "배드민턴", "홈트", "복싱",
                "주짓수", "스트레칭", "태권도", "점핑댄스", "클라이밍", "사격", "방탈출", "양궁", "승마", "VR",
                "루지"
            )

            "1" -> listOf(
                "독서", "스도쿠", "방탈출", "전시회", "영화", "연극", "뮤지컬", "퍼즐", "보드게임", "클래식듣기",
                "악기연주", "책 필사", "그림그리기", "신문보기", "블로그탐방", "지식유튜브보기", "다큐멘터리보기",
                "주식공부", "체스", "바둑", "큐브", "논문"
            )

            "2" -> listOf(
                "놀이공원",
                "당근",
                "유기견",
                "동물원",
                "토론",
                "교육봉사",
                "스터디",
                "번개만남",
                "블로그글쓰기",
                "가족모임",
                "친구모임",
                "Club가입",
                "기부",
                "여행",
                "신문",
                "OTT 같이보기",
                "게임하기",
                "집들이",
                "파자마파티",
                "포트럭파티"
            )

            "3" -> listOf(
                "산책", "명상", "독서", "음악감상", "전시회", "사진찍기", "팝업스토어가기", "잠자기", "일기쓰기",
                "낮잠자기", "드라이브가기", "여행가기", "친구만나기", "등산하기", "쇼핑", "그림그리기", "컬러링북",
                "플러팅하기", "가구조립", "원데이클래스", "요리", "요가"
            )

            else -> emptyList()
        }

        // activities 리스트에서 랜덤하게 3개의 활동을 선택하여 반환
        return activities.shuffled().take(3)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 바인딩 객체 해제
        _binding = null
    }

    // 위치 권한을 확인하는 함수
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 위치 권한을 요청하는 함수
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun initLocation() {
        // 위치 권한이 있는지 확인
        if (checkLocationPermission()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            // 위치 업데이트 요청
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location: Location? ->
                // 최근 위치를 가져왔을 때 실행됩니다.
                location?.let {
                    // 위치 정보를 사용하여 TextView에 표시합니다.
                    val latitude = it.latitude
                    val longitude = it.longitude

                    with(sharedPreferences.edit()) {
                        putString("latitude", latitude.toString())
                        putString("longitude", longitude.toString())
                        apply()
                    }

                    convertLocationToAddress(latitude, longitude)
                }
            }.addOnFailureListener { e ->
                // 위치 정보를 가져오지 못했을 때 실행됩니다.
                Log.e("MainActivity", "위치 가져오기 실패: ${e.message}")
                // 실패에 대한 처리를 수행할 수 있습니다.
            }
        } else {
            // 위치 권한이 없는 경우 위치 권한을 요청
            requestLocationPermission()
        }
    }

    private fun convertLocationToAddress(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.let {
                Log.d("HomeFragment", "addresses : $addresses")
                if (it.isNotEmpty()) {
                    val address = it[0]
                    Log.d("HomeFragment", "val address : $address")
                    val addressText = address.thoroughfare
                    if (addressText != null) {
                        binding.locationTextView.text = addressText
                        with(sharedPreferences.edit())
                        {
                            putString("address", addressText)
                            apply()
                        }
                    } else {

                        var fullAddress = address.getAddressLine(0)

                        fullAddress = fullAddress.replaceFirst("대한민국", "").trim()

                        val addressComponents = fullAddress.split(" ")

                        // 4번째와 5번째 값을 추출하여 결합
                        val trimmedAddressText = if (addressComponents.size >= 4) {
                            "${addressComponents[2]} ${addressComponents[3]}"
                        } else {
                            "주소 정보 부족"
                        }

                        // 변환된 주소를 LocationApi로 보냅니다.
                        sendAddressToLocationApi(trimmedAddressText)
                    }
                } else {
                    // 주소를 찾을 수 없는 경우 처리
                    binding.locationTextView.text = "주소를 찾을 수 없습니다"
                }
            }
        } catch (e: Exception) {
            // 예외 처리
            binding.locationTextView.text = "주소 변환 중 오류 발생"
            e.printStackTrace()
        }
    }


    private fun sendAddressToLocationApi(address: String) {
        GlobalScope.launch {
            locationResult = LocationApi.extractValuesFromXml(LocationApi.doInBackground(address))
            // UI 스레드에서 TextView에 결과를 설정합니다.
            withContext(Dispatchers.Main) {
                // 결과를 TextView에 표시합니다.
                binding.locationTextView.text = locationResult
                Log.d("HomeFragment", "LocationResult : ${locationResult}")
                with(sharedPreferences.edit()) {
                    putString("address", locationResult)
                    apply()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 부여된 경우 위치 요청 초기화
                initLocation()
            } else {
                // 위치 권한이 거부된 경우 설정으로 위치 권한을 요청
                Toast.makeText(requireContext(), "앱의 위치 권한을 설정에서 허용해주세요.", Toast.LENGTH_SHORT)
                    .show()
                requestLocationPermission()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}