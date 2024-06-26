package com.ilsa1000ri.weatherSecretary.ui.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object LocationApi {
    suspend fun doInBackground(address: String): String {
        if (address.isNullOrEmpty()) return "키워드가 Null입니다."
        Log.d("LocationApi", "LocationApi.doInBackground()의 실행이 시작되었습니다.")

        val currentPage = 1
        val countPerPage = 1 //하나의 동만 출력하므로 현 주소 검색시 가장 상단의 결과만 출력함.
        val resultType = "json"
        val confmKey = "devU01TX0FVVEgyMDI0MDQwMzIxNTgyOTExNDY1ODQ="
        val keyword = URLEncoder.encode(address, "UTF-8")
        Log.d("LocationApi", "keyword: $keyword")

        val apiUrl =
            "https://business.juso.go.kr/addrlink/addrLinkApi.do?currentPage=$currentPage" +
                    "&countPerPage=$countPerPage&keyword=$keyword" +
                    "&confmKey=$confmKey&resultType=$resultType"
        Log.d("LocationApi", "apiUrl: $apiUrl")

        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        return withContext(Dispatchers.IO) {
            try {
                //연결된 InputStream에서 데이터를 읽어올 BufferedReader를 생성
                val br = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val sb = StringBuilder()
                var tempStr: String?
                //BufferedReader를 사용하여 한 줄씩 데이터를 읽어들여 StringBuilder에 추가합니다.
                while (true) {
                    tempStr = br.readLine()
                    if (tempStr == null) break
                    sb.append(tempStr)
                }
                //데이터를 모두 읽은 후에는 BufferedReader를 닫습니다.
                br.close()
                //StringBuilder에 담긴 데이터를 문자열로 변환하여 반환합니다.
                sb.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            } finally {
                connection.disconnect() //정보를 얻은 후 연결 종료
            }
        }
    }

    fun extractValuesFromXml(xmlString: String): String {
        try {
            val jsonObject = JSONObject(xmlString)
            val jusoArray = jsonObject.getJSONObject("results").getJSONArray("juso")
            if (jusoArray.length() > 0) {
                val emdNm = jusoArray.getJSONObject(0).getString("emdNm")
                Log.d("LocationApi", "동 이름: $emdNm")
                return emdNm
            } else {
                Log.d("LocationApi", "주소가 존재하지 않습니다.")
                return "주소 검색에 실패했습니다. 국내 주소인가요?"
            }
        } catch (e: JSONException) {
            Log.e("LocationApi", "JSONException: ${e.message}")
            return "주소 검색에 실패했습니다. 국내 주소인가요?"
        }
    }
}
