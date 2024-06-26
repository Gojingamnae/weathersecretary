//package com.ilsa1000ri.weatherSecretary.ui.login
//
//import android.Manifest
//import android.accounts.AccountManager
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.net.ConnectivityManager
//import android.os.Bundle
//import android.util.Log
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.common.GoogleApiAvailability
//import com.google.api.client.extensions.android.http.AndroidHttp
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
//import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
//import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
//import com.google.api.client.json.gson.GsonFactory
//import com.google.api.client.util.DateTime
//import com.google.api.client.util.ExponentialBackOff
//import com.google.api.services.calendar.Calendar
//import com.google.api.services.calendar.CalendarScopes
//import com.ilsa1000ri.weatherSecretary.R
//import com.ilsa1000ri.weatherSecretary.ui.api.GoogleCalendarApi
//import com.ilsa1000ri.weatherSecretary.ui.api.GoogleCalendarApi.PREF_ACCOUNT_NAME
//import com.ilsa1000ri.weatherSecretary.ui.api.GoogleCalendarApi.REQUEST_ACCOUNT_PICKER
//import com.ilsa1000ri.weatherSecretary.ui.api.GoogleCalendarApi.REQUEST_AUTHORIZATION
//import com.ilsa1000ri.weatherSecretary.ui.api.GoogleCalendarApi.REQUEST_GOOGLE_PLAY_SERVICES
//import com.ilsa1000ri.weatherSecretary.ui.api.GoogleCalendarApi.REQUEST_PERMISSION_GET_ACCOUNTS
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import pub.devrel.easypermissions.EasyPermissions
//import java.io.IOException
//
//class GetGoogleEventFragment : Fragment() {
//
////    private var _binding: FragmentGetEventBinding? = null
////    private val binding get() = _binding!!
//
//
//    private var mCredential: GoogleAccountCredential? = null //hesabımıza erişim için
//    private var mService: Calendar? = null //Takvime erişim için
//
////    private var mProgressBar: ProgressBar? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.d("GetEventFragment", "GetEventFragment로 이동되었습니다.")
//        initCredentials()
//        getResultsFromApi()
//    }
////
////    override fun onCreateView(
////        inflater: LayoutInflater, container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View? {
////
////
////
////
////    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (requestCode) {
//            REQUEST_GOOGLE_PLAY_SERVICES -> {
//                if (resultCode != Activity.RESULT_OK) {
////                    binding.txtOut.text =
//                        "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app."
//                } else {
//                    getResultsFromApi()
//                }
//            }
//            REQUEST_ACCOUNT_PICKER -> {
//                if (resultCode == Activity.RESULT_OK && data != null &&
//                    data.extras != null
//                ) {
//                    val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
//                    if (accountName != null) {
//                        val settings = this.activity?.getPreferences(Context.MODE_PRIVATE)
//                        val editor = settings?.edit()
//                        editor?.putString(PREF_ACCOUNT_NAME, accountName)
//                        editor?.apply()
//                        mCredential!!.selectedAccountName = accountName
//                        getResultsFromApi()
//                    }
//                }
//            }
//            REQUEST_AUTHORIZATION -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    getResultsFromApi()
//                }
//            }
//        }
//    }
//
////    private fun initView() {
////        mProgressBar = binding.progressBar
////
////        with(binding) {
////                txtOut.text = ""
////                getResultsFromApi()
////        }
////    }
//
//    private fun initCredentials() {
//        Log.d("GetEventFragment", "initCredentials가 실행되었습니다.")
//        mCredential = GoogleAccountCredential.usingOAuth2(
//            requireContext(),
//            arrayListOf(CalendarScopes.CALENDAR)
//        )
//            .setBackOff(ExponentialBackOff())
//        initCalendarBuild(mCredential)
//    }
//
//    private fun initCalendarBuild(credential: GoogleAccountCredential?) {
//        val transport = AndroidHttp.newCompatibleTransport()
//        val jsonFactory = GsonFactory.getDefaultInstance()
//        mService = Calendar.Builder(
//            transport, jsonFactory, credential
//        )
//            .setApplicationName("GetEventCalendar")
//            .build()
//        Log.d("GetEventFragment", "api에 요청을 보냈습니다.")
//    }
//
//    private fun getResultsFromApi() {
//        if (!isGooglePlayServicesAvailable()) {
//            Log.d("GetEventFragment", "GooglePlayService가 사용 불가능합니다.")
//            acquireGooglePlayServices()
//        } else if (mCredential!!.selectedAccountName == null) {
//            chooseAccount()
//        } else if (!isDeviceOnline()) {
//            Log.d("GetEventFragment","No network connection available.")
//        } else {
//            makeRequestTask()
//        }
//    }
//
//    private fun chooseAccount() {
//        if (EasyPermissions.hasPermissions(
//                requireContext(), Manifest.permission.GET_ACCOUNTS
//            )
//        ) {
//            val accountName = this.activity?.getPreferences(Context.MODE_PRIVATE)
//                ?.getString(PREF_ACCOUNT_NAME, null)
//            if (accountName != null) {
//                mCredential!!.selectedAccountName = accountName
//                getResultsFromApi()
//            } else {
//                // Start a dialog from which the user can choose an account
//                startActivityForResult(
//                    mCredential!!.newChooseAccountIntent(),
//                    REQUEST_ACCOUNT_PICKER
//                )
//            }
//        } else {
//            // Request the GET_ACCOUNTS permission via a user dialog
//            EasyPermissions.requestPermissions(
//                this,
//                "This app needs to access your Google account (via Contacts).",
//                REQUEST_PERMISSION_GET_ACCOUNTS,
//                Manifest.permission.GET_ACCOUNTS
//            )
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
//    }
//
//    //Google consolea erişim izni olup olmadıgına bakıyoruz
//    private fun isGooglePlayServicesAvailable(): Boolean {
//        val apiAvailability = GoogleApiAvailability.getInstance()
//        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(requireContext())
//        return connectionStatusCode == ConnectionResult.SUCCESS
//    }
//
//    //Cihazın Google play servislerini destekleyip desteklemediğini kontrol ediyor
//    private fun acquireGooglePlayServices() {
//        val apiAvailability = GoogleApiAvailability.getInstance()
//        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(requireContext())
//        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
//            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
//        }
//    }
//
//    fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
//        val apiAvailability = GoogleApiAvailability.getInstance()
//        val dialog = apiAvailability.getErrorDialog(
//            this,
//            connectionStatusCode,
//            REQUEST_GOOGLE_PLAY_SERVICES
//        )
//        dialog?.show()
//    }
//
//    private fun isDeviceOnline(): Boolean {
//        val connMgr =
//            this.activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkInfo = connMgr.activeNetworkInfo
//        return networkInfo != null && networkInfo.isConnected
//    }
//
//    private fun makeRequestTask() {
//        var mLastError: Exception? = null
//
//        lifecycleScope.executeAsyncTask(
//            onStart = {
////                mProgressBar!!.visibility = View.VISIBLE
//            },
//            doInBackground = {
//                try {
//                    getDataFromCalendar()
//                } catch (e: Exception) {
//                    mLastError = e
//                    null // 취소하지 않음
//                }
//            },
//            onPostExecute = { output ->
////                mProgressBar!!.visibility = View.GONE
//                if (output == null || output.size == 0) {
//                    Log.d("Google", "veri yok")
//                } else {
//                    for (index in 0 until output.size) {
////                        binding.txtOut.text = (TextUtils.join("\n", output))
//                        Log.d(
//                            "Google",
//                            "식별 아이디: "+output[index].iCalUID + " 제목: " + output[index].summary + " 시작일: " + output[index].startDate +
//                                    " 종료일 : "+ output[index].endDate + " 설명 : " + output[index].description +
//                                    " 알림 : "+(output[index].reminderMinutes ?: "알림이 없음") + " 일정 색" + output[index].backgroundColor
//                        )
//                    }
//                }
//            },
//            onCancelled = {
////                mProgressBar!!.visibility = View.GONE
//                if (mLastError != null) {
//                    if (mLastError is GooglePlayServicesAvailabilityIOException) {
//                        showGooglePlayServicesAvailabilityErrorDialog(
//                            (mLastError as GooglePlayServicesAvailabilityIOException)
//                                .connectionStatusCode
//                        )
//                    } else if (mLastError is UserRecoverableAuthIOException) {
//                        this.startActivityForResult(
//                            (mLastError as UserRecoverableAuthIOException).intent,
//                            REQUEST_AUTHORIZATION
//                        )
//                    } else {
//                        Log.e("Google", "Error occurred:", mLastError!!)
////                        binding.txtOut.text =
////                            "The following error occurred:\n" + mLastError!!.message
//                    }
//                } else {
//                    Log.d("Google", "Request cancelled.")
////                    binding.txtOut.text = "Request cancelled."
//                }
//            }
//        )
//    }
//
//
//    fun getDataFromCalendar(): MutableList<GoogleCalendarApi.GetEventModel> {
//        val now = DateTime(System.currentTimeMillis())
//        val eventStrings = ArrayList<GoogleCalendarApi.GetEventModel>()
//        try {
//            val events = mService!!.events().list("primary")
//                    //한 번에 가져오는 수는 2500개까지
//                .setTimeMin(now)
//                .setOrderBy("startTime")
//                .setSingleEvents(true)
//                .execute()
//            val items = events.items
//
//            for (event in items) {
//                var start = event.start.dateTime
//                if (start == null) {
//                    start = event.start.date
//                }
//
//                var end = event.end.dateTime
//                if (end == null) {
//                    end = event.end.date
//                }
//
//                val reminders = if (event.reminders != null && event.reminders.useDefault) {
//                    // 구글 캘린더에서 기본 알림을 사용하는 경우
//                    "30"
//                } else if (event.reminders != null && event.reminders.overrides != null) {
//                    // 구글 캘린더에서 알림을 직접 설정한 경우
//                    val reminderOverrides = event.reminders.overrides
//                    val reminderMinutesList = ArrayList<String>()
//                    for (reminder in reminderOverrides) {
//                        reminderMinutesList.add("${reminder.minutes}")
//                    }
//                    reminderMinutesList.joinToString(", ")
//                } else {
//                    // 알림이 없는 경우
//                    "알림이 없음"
//                }
//
//                var location = event.location
//                if (location == null) {
//                    location = event.location
//                }
//
//                var iCalUID = event.iCalUID
//                if (iCalUID == null) {
//                    iCalUID = event.iCalUID
//                }
//
//                var backgroundColor: Int? = null
//
//                event.colorId?.let { colorId ->
//                    val colorResourceId = resources.getIdentifier(
//                        "color_${colorId}_background",
//                        "color",
//                        requireContext().packageName
//                    )
//                    backgroundColor = if (colorResourceId != 0) {
//                        ContextCompat.getColor(requireContext(), colorResourceId)
//                    } else {
//                        // Default color if resource not found
//                        ContextCompat.getColor(requireContext(), R.color.main)
//                    }
//                }
//
//                eventStrings.add(
//                    GoogleCalendarApi.GetEventModel(
//                        summary = event.summary,
//                        description = event.description,
//                        startDate = start.toString(),
//                        endDate = end.toString(),
//                        reminderMinutes = reminders,
//                        location = location,
//                        iCalUID = iCalUID,
//                        backgroundColor = backgroundColor?.let { String.format("#%06X", 0xFFFFFF and it) }
//                    )
//                )
//            }
//            return eventStrings
//
//        } catch (e: IOException) {
//            Log.d("Google", e.message.toString())
//        }
//        return eventStrings
//    }
//
//    fun <R> CoroutineScope.executeAsyncTask(
//        onStart: () -> Unit,
//        doInBackground: () -> R,
//        onPostExecute: (R) -> Unit,
//        onCancelled: () -> Unit
//    ) = launch {
//        onStart()
//        val result = withContext(Dispatchers.IO) {
//            doInBackground() // runs in background thread without blocking the Main Thread
//        }
//        onPostExecute(result) // runs in Main Thread
//        onCancelled()
//    }
//}