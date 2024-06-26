//AreaManageFragment
package com.ilsa1000ri.weatherSecretary.ui.location

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.media.RouteListingPreference
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilsa1000ri.weatherSecretary.MainActivity
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.ui.api.RealShortApi
import com.ilsa1000ri.weatherSecretary.ui.home.HomeFragment
import com.ilsa1000ri.weatherSecretary.ui.login.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException
import java.io.InputStream
import org.json.JSONArray

class AreaManageFragment: Fragment() {
    private val TAG = "AreaManageFragment"
    private val areaList = mutableListOf<Area>()

    private lateinit var nowAreaWeather: TextView
    private lateinit var AddedArea1Weather: TextView
    private lateinit var AddedArea2Weather: TextView
    private lateinit var AddedArea3Weather: TextView
    private lateinit var AddedArea4Weather: TextView

    private lateinit var nowAreaTemperature: TextView
    private lateinit var AddedArea1Temperature: TextView
    private lateinit var AddedArea2Temperature: TextView
    private lateinit var AddedArea3Temperature: TextView
    private lateinit var AddedArea4Temperature: TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var adapter: SearchAdapter

    private lateinit var addedArea1Layout: LinearLayout
    private lateinit var addedArea1Text: TextView
    private lateinit var areaManageText1: TextView
    private lateinit var deleteAreaManageText1: ImageButton

    private lateinit var addedArea2Layout: LinearLayout
    private lateinit var addedArea2Text: TextView
    private lateinit var areaManageText2: TextView
    private lateinit var deleteAreaManageText2: ImageButton

    private lateinit var addedArea3Layout: LinearLayout
    private lateinit var addedArea3Text: TextView
    private lateinit var areaManageText3: TextView
    private lateinit var deleteAreaManageText3: ImageButton

    private lateinit var addedArea4Layout: LinearLayout
    private lateinit var addedArea4Text: TextView
    private lateinit var areaManageText4: TextView
    private lateinit var deleteAreaManageText4: ImageButton
    val nowPlus1HourText = mutableListOf<String>() // 날씨와 관련된 문자열을 저장하기 위한 리스트

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loginPreferences: SharedPreferences
    private var latitude: Double= 0.0
    private var longitude: Double = 0.0
    private var address: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_area_manage, container, false)

        sharedPreferences =
            requireContext().getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        latitude = sharedPreferences.getString("latitude", "0.0")!!.toDouble()
        longitude = sharedPreferences.getString("longitude", "0.0")!!.toDouble()
        address = sharedPreferences.getString("address", "") ?: ""

        loginPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)


        nowAreaWeather = view.findViewById(R.id.nowAreaWeather)
        AddedArea1Weather = view.findViewById(R.id.AddedArea1Weather)
        AddedArea2Weather = view.findViewById(R.id.AddedArea2Weather)
        AddedArea3Weather = view.findViewById(R.id.AddedArea3Weather)
        AddedArea4Weather = view.findViewById(R.id.AddedArea4Weather)

        nowAreaTemperature = view.findViewById(R.id.nowAreaTemperature)
        AddedArea1Temperature = view.findViewById(R.id.AddedArea1Temperature)
        AddedArea2Temperature = view.findViewById(R.id.AddedArea2Temperature)
        AddedArea3Temperature = view.findViewById(R.id.AddedArea3Temperature)
        AddedArea4Temperature = view.findViewById(R.id.AddedArea4Temperature)

        addedArea1Layout = view.findViewById(R.id.AddedArea1Layout)
        addedArea1Text = view.findViewById(R.id.AddedArea1Text)
        areaManageText1 = view.findViewById(R.id.AreaManageText1)
        deleteAreaManageText1 = view.findViewById(R.id.DeleteAreaManageText1)

        addedArea2Layout = view.findViewById(R.id.AddedArea2Layout)
        addedArea2Text = view.findViewById(R.id.AddedArea2Text)
        areaManageText2 = view.findViewById(R.id.AreaManageText2)
        deleteAreaManageText2 = view.findViewById(R.id.DeleteAreaManageText2)

        addedArea3Layout = view.findViewById(R.id.AddedArea3Layout)
        addedArea3Text = view.findViewById(R.id.AddedArea3Text)
        areaManageText3 = view.findViewById(R.id.AreaManageText3)
        deleteAreaManageText3 = view.findViewById(R.id.DeleteAreaManageText3)

        addedArea4Layout = view.findViewById(R.id.AddedArea4Layout)
        addedArea4Text = view.findViewById(R.id.AddedArea4Text)
        areaManageText4 = view.findViewById(R.id.AreaManageText4)
        deleteAreaManageText4 = view.findViewById(R.id.DeleteAreaManageText4)

        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        recyclerView = view.findViewById(R.id.recyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)

        loadAreaListFromJson() // JSON 파일을 파싱하여 areaList에 데이터를 로드합니다.
        fetchDataWeatherRealShort(
            latitude,
            longitude,
            nowAreaWeather,
            nowAreaTemperature
        ) //현재 위치 날씨와 기온 출력
        fetchWeatherForSelectedAreas()

        adapter = SearchAdapter(areaList, ::onAreaButtonClick)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 검색 기능 구현
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        searchEditText.setOnClickListener {
            performSearch()
        }

        // UI 요소 초기화
        val nowAreaTextView = view.findViewById<TextView>(R.id.nowAreaText)
        val sharedPreferences =
            requireContext().getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        val address = sharedPreferences.getString("address", "")
        nowAreaTextView.text = address ?: "결과 없음"


        // Firebase에서 사용자의 선택한 지역을 불러와 TextView에 표시
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection(userId).document("Area").get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val selectedAreas = document.toObject(SelectedAreas::class.java)
                        selectedAreas?.let {
                            addedArea1Text.text = it.selectedArea1
                            areaManageText1.text = it.selectedArea1

                            addedArea2Text.text = it.selectedArea2
                            areaManageText2.text = it.selectedArea2

                            addedArea3Text.text = it.selectedArea3
                            areaManageText3.text = it.selectedArea3

                            addedArea4Text.text = it.selectedArea4
                            areaManageText4.text = it.selectedArea4

                            displaySelectedArea(
                                view.findViewById(R.id.AddedArea1Layout),
                                addedArea1Text,
                                areaManageText1,
                                deleteAreaManageText1,
                                it.selectedArea1!!
                            )
                            displaySelectedArea(
                                view.findViewById(R.id.AddedArea2Layout),
                                addedArea2Text,
                                areaManageText2,
                                deleteAreaManageText2,
                                it.selectedArea2!!
                            )
                            displaySelectedArea(
                                view.findViewById(R.id.AddedArea3Layout),
                                addedArea3Text,
                                areaManageText3,
                                deleteAreaManageText3,
                                it.selectedArea3!!
                            )
                            displaySelectedArea(
                                view.findViewById(R.id.AddedArea4Layout),
                                addedArea4Text,
                                areaManageText4,
                                deleteAreaManageText4,
                                it.selectedArea4!!
                            )

                        }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }


        // 지역 목록 버튼 클릭 리스너 설정
        view.findViewById<ImageButton>(R.id.AreaManageButton).setOnClickListener {
            view.findViewById<FrameLayout>(R.id.areaManageFrame).visibility = VISIBLE
        }

        view.findViewById<ImageButton>(R.id.closeAreaManageFrame).setOnClickListener {
            view.findViewById<FrameLayout>(R.id.areaManageFrame).visibility = View.INVISIBLE
        }

        // 홈으로 가는 버튼
        view.findViewById<ImageButton>(R.id.goToHomeButton).setOnClickListener {
            val navController = findNavController()
            val bottomNavigationView =
                requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            val selectedItemId = bottomNavigationView.selectedItemId
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.mobile_navigation, true) // Replace 'nav_graph' with your nav graph ID
                .build()

            when (selectedItemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.action_areaManageFragment_to_navigation_home, null, navOptions)
                }

                R.id.navigation_weather -> {
                    navController.navigate(R.id.action_areaManageFragment_to_navigation_weather, null, navOptions)
                }
            }
        }

        view.findViewById<ImageButton>(R.id.setAlarmButton).setOnClickListener {
            val alarmFragment = AlarmFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_area_manage_container, alarmFragment)
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<ImageButton>(R.id.logoutButton).setOnClickListener {
            Firebase.auth.signOut()
            setLoggedIn(false)

            // MainActivity 종료
            requireActivity().finishAffinity()

            // LoginActivity 시작
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            requireActivity().startActivity(intent)
        }


        // 각 삭제 버튼에 클릭 리스너 설정
        setDeleteButtonClickListener(
            deleteAreaManageText1,
            areaManageText1,
            addedArea1Text,
            "selectedArea1"
        )
        setDeleteButtonClickListener(
            deleteAreaManageText2,
            areaManageText2,
            addedArea2Text,
            "selectedArea2"
        )
        setDeleteButtonClickListener(
            deleteAreaManageText3,
            areaManageText3,
            addedArea3Text,
            "selectedArea3"
        )
        setDeleteButtonClickListener(
            deleteAreaManageText4,
            areaManageText4,
            addedArea4Text,
            "selectedArea4"
        )

        return view
    }

    private fun loadAreaListFromJson() {
        var json: String? = null
        try {
            val inputStream: InputStream = requireContext().resources.openRawResource(R.raw.output)
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }

        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val firstLevel = jsonObject.getString("1단계")
                val secondLevel = jsonObject.getString("2단계")
                val thirdLevel = jsonObject.getString("3단계")
                val latitude = jsonObject.getDouble("경도(초/100)")
                val longitude = jsonObject.getDouble("위도(초/100)")
                val area = Area(firstLevel, secondLevel, thirdLevel, latitude, longitude)
                areaList.add(area)
                //HomeApi : latitude, longitude를 이용해 날씨 정보를 불러옴
                //RealShorApi :latitude, longitude -> NX, NY -> 기온
            }
        } catch (jsonException: JSONException) {
            jsonException.printStackTrace()
        }
    }


    private fun displaySelectedArea(
        layout: LinearLayout,
        fragmentText: TextView,
        frameText: TextView,
        button: ImageButton,
        area: String
    ) {
        if (area.isEmpty()) {
            layout.visibility = View.GONE
            button.visibility = View.GONE
        } else {
            layout.visibility = View.VISIBLE
            button.visibility = View.VISIBLE
        }
    }

    private fun setDeleteButtonClickListener(
        button: ImageButton,
        manageTextView: TextView,
        addedText: TextView,
        field: String
    ) {
        button.setOnClickListener {
            deleteArea(field, manageTextView, addedText, button)
        }

        // 초기 상태에서 TextView가 빈 문자열인지 확인하여 버튼 가시성 설정
        if (manageTextView.text.isEmpty()) {
            button.visibility = View.GONE
        }
    }

    private fun deleteArea(
        field: String,
        manageTextView: TextView,
        addedText: TextView,
        button: ImageButton
    ) {
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        userId?.let {
            db.collection(it).document("Area")
                .update(field, "")
                .addOnSuccessListener {
                    Log.d(TAG, "$field successfully updated!")
                    manageTextView.text = ""
                    addedText.text = ""
                    // 삭제 후 버튼을 숨김
                    button.visibility = View.GONE
                    // 삭제된 지역의 UI 요소들을 숨기기
                    displaySelectedArea(
                        if (field == "selectedArea1") addedArea1Layout
                        else if (field == "selectedArea2") addedArea2Layout
                        else if (field == "selectedArea3") addedArea3Layout
                        else addedArea4Layout,
                        manageTextView,
                        addedText,
                        button,
                        ""
                    )
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating $field", e)
                }
        }
    }


    data class SelectedAreas(
        val selectedArea1: String? = "",
        val selectedArea2: String? = "",
        val selectedArea3: String? = "",
        val selectedArea4: String? = ""
    )

    // 검색 기능을 수행하는 함수입니다.
    private fun performSearch() {
        val searchText = searchEditText.text.toString().trim()
        Log.d(TAG, "검색어: $searchText") // 입력한 검색어를 로그로 출력합니다.
        val filteredList = ArrayList<Area>()

        for (area in areaList) {
            val fullName = "${area.firstLevel} ${area.secondLevel} ${area.thirdLevel}"
            if (fullName.contains(searchText, ignoreCase = true)) {
                filteredList.add(area)
            }
        }
        adapter.filteredAreaList = filteredList
        adapter.notifyDataSetChanged()

        Log.d(TAG, "필터링된 결과 목록: $filteredList") // 필터링된 결과 목록을 로그로 출력합니다.

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onAreaButtonClick(area: String) {
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        userId?.let {
            db.collection(it).document("Area").get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val selectedAreas = document.toObject(SelectedAreas::class.java)

                        selectedAreas?.let {
                            // 중복 검사
                            if (it.selectedArea1 == area || it.selectedArea2 == area || it.selectedArea3 == area || it.selectedArea4 == area) {
                                Toast.makeText(
                                    requireContext(),
                                    "이미 선택된 지역입니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            if (it.selectedArea1!!.isEmpty()) {
                                updateSelectedArea(
                                    "selectedArea1",
                                    area,
                                    addedArea1Layout,
                                    addedArea1Text,
                                    areaManageText1,
                                    deleteAreaManageText1
                                )
                            } else if (it.selectedArea2!!.isEmpty()) {
                                updateSelectedArea(
                                    "selectedArea2",
                                    area,
                                    addedArea2Layout,
                                    addedArea2Text,
                                    areaManageText2,
                                    deleteAreaManageText2
                                )
                            } else if (it.selectedArea3!!.isEmpty()) {
                                updateSelectedArea(
                                    "selectedArea3",
                                    area,
                                    addedArea3Layout,
                                    addedArea3Text,
                                    areaManageText3,
                                    deleteAreaManageText3
                                )
                            } else if (it.selectedArea4!!.isEmpty()) {
                                updateSelectedArea(
                                    "selectedArea4",
                                    area,
                                    addedArea4Layout,
                                    addedArea4Text,
                                    areaManageText4,
                                    deleteAreaManageText4
                                )
                            } else {
                                Log.d(TAG, "All areas are already selected")
                            }
                        }

                    } else {  // If "Area" collection doesn't exist, create it and update selected area
                        val newSelectedAreas = SelectedAreas(selectedArea1 = area)
                        db.collection(it).document("Area").set(newSelectedAreas)
                            .addOnSuccessListener {
                                Log.d(TAG, "Area collection created successfully")
                                // Now proceed to update the selected area
                                updateSelectedArea(
                                    "selectedArea1",
                                    area,
                                    addedArea1Layout,
                                    addedArea1Text,
                                    areaManageText1,
                                    deleteAreaManageText1
                                )
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error creating Area collection", e)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }

    private fun updateSelectedArea(
        field: String,
        area: String,
        layout: LinearLayout,
        addedText: TextView,
        manageText: TextView,
        button: ImageButton
    ) {
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        userId?.let {
            db.collection(it).document("Area")
                .update(field, area)
                .addOnSuccessListener {
                    Log.d(TAG, "$field successfully updated with $area!")
                    // Update the UI elements
                    manageText.text = area
                    addedText.text = area
                    // Show the layout
                    layout.visibility = View.VISIBLE
                    // Show the delete button
                    button.visibility = View.VISIBLE
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating $field", e)
                }
        }
    }

    //날씨, 기온 출력
    fun fetchDataWeatherRealShort(
        latitude: Double,
        longitude: Double,
        weatherTextView: TextView,
        temperatureTextView: TextView
    ) {
        Log.d("AreaManageFragment", "fetchDataWeatherRealShort called")
        GlobalScope.launch(Dispatchers.Main) {
            // real - 시간, 기온
            val realShortIndex = RealShortApi.getRealShortIndex(latitude, longitude)
            Log.d("AreaManageFragment", "realShortIndex: $realShortIndex") // 추가
            val nowPlus1HourSky = mutableListOf<String>()
            val nowPlus1HourT1H = mutableListOf<String>()
            val skyIndex = realShortIndex.indexOfFirst { it.startsWith("SKY") }
            val t1hIndex = realShortIndex.indexOfFirst { it.startsWith("T1H") }

            val skyValue =
                if (skyIndex != -1) realShortIndex.getOrNull(skyIndex)?.split(":")?.getOrNull(1)
                    ?.trim() else ""
            val t1hValue =
                if (t1hIndex != -1) realShortIndex.getOrNull(t1hIndex)?.split(":")?.getOrNull(1)
                    ?.trim() else ""


            realShortIndex.forEach { data ->
                when {
                    data.startsWith("PTY") -> {
                        val ptyValue = data.split(":")[1].split(",")[0].trim()
                        Log.d("AreaManageFragment", "ptyValue : ${ptyValue}")
                        val weatherIcon = when (ptyValue) {
                            "비" -> R.drawable.ic_weather_rain_fi
                            "눈" -> R.drawable.ic_weather_snow
                            else -> 0 // 강수 없음(맑음, 구름많음, 흐림)
                        }
                        if (weatherIcon != 0) {
                            nowPlus1HourSky.add(weatherIcon.toString())
                            nowPlus1HourText.add(ptyValue)
                        }
                    }

                    data.startsWith("SKY") -> {
                        val skyValue = data.split(":")[1].split(",")[0].trim()
                        Log.d("AreaManageFragment", "skyValue : $skyValue")
                        val weatherIcon = when (skyValue) {
                            "맑음" -> R.drawable.ic_weather_sunny_fi
                            "구름많음" -> R.drawable.ic_weather_cloud_fi
                            "흐림" -> R.drawable.ic_weather_cloud_fi
                            else -> 0 // 기본값
                        }
                        if (weatherIcon != 0) {
                            nowPlus1HourSky.add(weatherIcon.toString())
                            nowPlus1HourText.add(skyValue)
                        }
                    }

                    data.startsWith("T1H") -> {
                        val t1hValue = data.split(":")[1].split(",")[0].trim()
                        Log.d("AreaManageFragment", "t1hValue : $t1hValue")
                        nowPlus1HourT1H.add(t1hValue)
                    }
                }
            }

            val iconId = nowPlus1HourSky.getOrNull(0)?.toInt() ?: 0
            weatherTextView.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0)
            temperatureTextView.text = "    ${nowPlus1HourT1H.getOrNull(0) ?: ""}°"
        }
    }

    suspend fun loadAreaInfo() {
        coroutineScope {
            // areaList를 순회하면서 각 지역의 날씨 정보를 가져옴
            for (i in 0 until areaList.size) {
                val area = areaList[i]
                val latitude = area.latitude
                val longitude = area.longitude
                // 코루틴 빌더를 사용하여 새로운 코루틴을 생성
                val weatherInfoList = async {
                    RealShortApi.getRealShortIndex(latitude, longitude)
                }
                // 최대 5개 지역의 정보만 출력
                if (i < 5) {
                    // await() 함수를 사용하여 결과를 기다린 후 처리
                    val result = weatherInfoList.await()
                    for (weatherInfo in result) {
                        Log.d("WeatherInfo", weatherInfo)
                    }
                }
            }
        }
    }

    fun dfs_xy_conv(v1: Int, v2: Int): Point {
        val RE = 6371.00877     // 지구 반경(km)
        val GRID = 5.0          // 격자 간격(km)
        val SLAT1 = 30.0        // 투영 위도1(degree)
        val SLAT2 = 60.0        // 투영 위도2(degree)
        val OLON = 126.0        // 기준점 경도(degree)
        val OLAT = 38.0         // 기준점 위도(degree)
        val XO = 43             // 기준점 X좌표(GRID)
        val YO = 136            // 기준점 Y좌표(GRID)
        val DEGRAD = Math.PI / 180.0
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)

        var ra = Math.tan(Math.PI * 0.25 + (v1) * DEGRAD * 0.5)
        ra = re * sf / Math.pow(ra, sn)
        var theta = v2 * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn

        val x = (ra * Math.sin(theta) + XO + 0.5).toInt()
        val y = (ro - ra * Math.cos(theta) + YO + 0.5).toInt()

        return Point(x, y)
    }

    // fetchWeatherForSelectedAreas() 함수 내에서 Firebase Firestore의 실시간 업데이트 리스너를 추가합니다.
    fun fetchWeatherForSelectedAreas() {
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            db.collection(user.uid).document("Area").addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Listen failed", exception)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val selectedAreas = snapshot.toObject(SelectedAreas::class.java)
                    selectedAreas?.let {
                        val areas = listOf(
                            it.selectedArea1 to (AddedArea1Weather to AddedArea1Temperature),
                            it.selectedArea2 to (AddedArea2Weather to AddedArea2Temperature),
                            it.selectedArea3 to (AddedArea3Weather to AddedArea3Temperature),
                            it.selectedArea4 to (AddedArea4Weather to AddedArea4Temperature)
                        )

                        areas.forEach { (areaName, views) ->
                            if (areaName!!.isNotEmpty()) {
                                val area = areaList.find { area ->
                                    "${area.firstLevel} ${area.secondLevel} ${area.thirdLevel}" == areaName
                                }
                                area?.let {
                                    lifecycleScope.launch {
                                        fetchDataWeatherRealShort(
                                            it.latitude,
                                            it.longitude,
                                            views.first,
                                            views.second
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
        }
    }
    private fun setLoggedIn(isLoggedIn: Boolean) {
        // SharedPreferences에 로그인 상태 저장
        loginPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }
}