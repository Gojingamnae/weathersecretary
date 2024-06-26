//WeatherFragment
package com.ilsa1000ri.weatherSecretary.ui.weather

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.databinding.FragmentWeatherBinding
import com.ilsa1000ri.weatherSecretary.ui.api.LocationApi
import com.ilsa1000ri.weatherSecretary.ui.location.AreaManageFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class WeatherFragment : Fragment() {
    private var _binding: FragmentWeatherBinding? = null
    private lateinit var weatherFragmentRealShort: WeatherFragmentRealShort
    private lateinit var weatherFragmentOpen: WeatherFragmentOpen
    private lateinit var weatherFragmentMid: WeatherFragmentMid
    private lateinit var weatherFragmentWeek: WeatherFragmentWeek
    private lateinit var weatherFragmentDay5: WeatherFragmentDay5
    private lateinit var weatherFragmentShort: WeatherFragmentShort
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val binding get() = _binding!!

    private var locationResult: String? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        latitude = sharedPreferences.getString("latitude", "0.0")?.toDoubleOrNull() ?: 0.0
        longitude= sharedPreferences.getString("longitude", "0.0")?.toDoubleOrNull() ?: 0.0
        locationResult = sharedPreferences.getString("address", "")

        val gpsButton = binding.gpsButton
        gpsButton.setOnClickListener {
            initLocation() //다시 위치 추적
            binding.locationTextView.text = locationResult
        }
        // 위치 권한이 있는지 확인
        if (checkLocationPermission()) {
            // 위치 권한이 허용되어 있으면 위치 요청 초기화
            binding.locationTextView.text = locationResult
        } else {
            // 위치 권한을 요청
            requestLocationPermission()
        }

        val goToAreaManage = binding.goToAreaManage
        goToAreaManage.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_navigation_weather_to_areaManageFragment)

            // FrameLayout을 보이도록 함
            binding.fragmentAreaManageContainer.visibility = View.VISIBLE
        }

        weatherFragmentRealShort = WeatherFragmentRealShort()
        weatherFragmentRealShort.realViews(binding)
        weatherFragmentRealShort.wfetchDataWeatherRealShort(latitude, longitude, requireContext().resources)

        weatherFragmentOpen = WeatherFragmentOpen()
        weatherFragmentOpen.openViews(binding)
        weatherFragmentOpen.ofetchDataWeather(latitude, longitude)

        weatherFragmentMid = WeatherFragmentMid()
        weatherFragmentMid.midViews(binding)
        weatherFragmentMid.fetchDataWeatherMid()

        weatherFragmentWeek = WeatherFragmentWeek()
        weatherFragmentWeek.weekViews(binding)
        weatherFragmentWeek.fetchDataWeatherWeek()

        weatherFragmentDay5 = WeatherFragmentDay5()
        weatherFragmentDay5.day5Views(binding)
        weatherFragmentDay5.fetchDataWeatherDay5(latitude, longitude)

        weatherFragmentShort = WeatherFragmentShort()
        weatherFragmentShort.shortViews(binding)
        weatherFragmentShort.fetchDataWeatherShort(latitude, longitude)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun convertLocationToAddress(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.let {
                if (it.isNotEmpty()) {
                    val address = it[0]
                    val addressText = address.thoroughfare
                    if(addressText != null){
                        binding.locationTextView.text = addressText
                    }
                    else {

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
                with(sharedPreferences.edit()) {
                    putString("address", locationResult)
                    apply()
                }
            }
        }
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
