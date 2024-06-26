package com.ilsa1000ri.weatherSecretary.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ilsa1000ri.weatherSecretary.R
import retrofit2.http.Url

class TermsActivity : AppCompatActivity() {
    //    google
    private lateinit var idToken: String
    companion object {
        private const val TAG = "TermsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        //google 가입 시 전송 정보
        idToken = intent.getStringExtra("idToken") ?: ""

        //kakao 가입 시 전송 정보
        val accessToken = intent.getStringExtra("accessToken") ?: ""


        val checkAllInTerms = findViewById<CheckBox>(R.id.checkAllInTerms)
        val termsOfService = findViewById<CheckBox>(R.id.termsOfService)
        val privacyPolicy= findViewById<CheckBox>(R.id.privatePolicy)
        val termsOfLocationBasedService = findViewById<CheckBox>(R.id.termsOfLocationBasedService)
        val nextButton = findViewById<Button>(R.id.termsNextButton)
        val checkBoxes = listOf(termsOfService, privacyPolicy, termsOfLocationBasedService)

        val termsAndConditionsText = findViewById<TextView>(R.id.TermsAndConditionsText)
        val privatePolicyText = findViewById<TextView>(R.id.privatePolicyText)
        val locationBasedText = findViewById<TextView>(R.id.locationBasedText)

        val termsOfServiceContent = """
            본 약관은 일사천리(이하 "서비스 제공업체")이 무료로 제작한 모바일 기기용 날씨비서 앱(이하 "애플리케이션")에 적용됩니다.</font></font></p><br><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">애플리케이션을 다운로드하거나 활용하면 귀하는 자동으로 다음 약관에 동의하게 됩니다. 애플리케이션을 사용하기 전에 이 약관을 철저히 읽고 이해하는 것이 좋습니다. 애플리케이션, 애플리케이션의 일부 또는 당사 상표의 무단 복사, 수정은 엄격히 금지됩니다. 애플리케이션의 소스 코드를 추출하거나 애플리케이션을 다른 언어로 번역하거나 파생 버전을 만들려는 시도는 허용되지 않습니다. 애플리케이션과 관련된 모든 상표, 저작권, 데이터베이스 권리 및 기타 지적 재산권은 서비스 제공자의 재산으로 유지됩니다.</font></font></p><br><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">서비스 제공업체는 애플리케이션이 최대한 유익하고 효율적이도록 최선을 다해 노력하고 있습니다. 따라서 그들은 언제든지 어떤 이유로든 애플리케이션을 수정하거나 서비스 비용을 청구할 권리를 보유합니다. 서비스 제공업체는 애플리케이션 또는 해당 서비스에 대한 모든 요금이 귀하에게 명확하게 전달될 것임을 보장합니다.</font></font></p><br><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">애플리케이션은 서비스 제공을 위해 귀하가 서비스 제공자에게 제공한 개인 데이터를 저장하고 처리합니다. 휴대폰의 보안과 애플리케이션 액세스를 유지하는 것은 귀하의 책임입니다. 서비스 제공업체는 장치의 공식 운영 체제에서 부과하는 소프트웨어 제한 사항 및 제한 사항을 제거하는 등 휴대폰을 탈옥하거나 루팅하지 말 것을 강력히 권고합니다. 이러한 행위로 인해 귀하의 휴대폰이 맬웨어, 바이러스, 악성 프로그램에 노출될 수 있고, 휴대폰의 보안 기능이 손상될 수 있으며, 애플리케이션이 제대로 작동하지 않거나 전혀 작동하지 않을 수 있습니다.</font></font></p><div><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">애플리케이션은 자체 이용 약관이 있는 제3자 서비스를 활용한다는 점에 유의하세요. 다음은 애플리케이션에서 사용되는 제3자 서비스 제공업체의 이용 약관에 대한 링크입니다.</font></font></p><ul><li><a href="https://policies.google.com/terms" target="_blank" rel="noopener noreferrer"><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">구글 플레이 서비스</font></font></a></li><!----><li><a href="https://www.google.com/analytics/terms/" target="_blank" rel="noopener noreferrer"><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">Firebase용 Google 애널리틱스</font></font></a></li><li><a href="https://firebase.google.com/terms/crashlytics" target="_blank" rel="noopener noreferrer"><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">Firebase Crashlytics</font></font></a></li><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----></ul></div><br><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">서비스 제공업체는 특정 측면에 대해 책임을 지지 않는다는 점에 유의하시기 바랍니다. 애플리케이션의 일부 기능을 사용하려면 Wi-Fi 또는 모바일 네트워크 공급자가 제공하는 활성 인터넷 연결이 필요합니다. Wi-Fi 접속 부족으로 인해 애플리케이션이 전체 용량으로 작동하지 않거나 데이터 허용량을 모두 소진한 경우 서비스 제공업체는 책임을 지지 않습니다.</font></font></p><br><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">Wi-Fi 지역 밖에서 애플리케이션을 사용하는 경우 이동통신사의 계약 조건이 여전히 적용된다는 점에 유의하시기 바랍니다. 결과적으로, 애플리케이션에 연결하는 동안 데이터 사용에 대해 이동통신사로부터 요금이 부과되거나 기타 제3자 요금이 부과될 수 있습니다. 애플리케이션을 사용함으로써 귀하는 데이터 로밍을 비활성화하지 않고 본국(즉, 지역 또는 국가) 외부에서 애플리케이션을 사용하는 경우 로밍 데이터 요금을 포함한 모든 요금에 대한 책임을 집니다. 귀하가 애플리케이션을 사용하는 장치의 요금 납부자가 아닌 경우, 해당 기기는 귀하가 요금 납부자로부터 허가를 받은 것으로 간주합니다.</font></font></p><br><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">마찬가지로, 서비스 제공업체는 귀하의 애플리케이션 사용에 대해 항상 책임을 질 수는 없습니다. 예를 들어, 장치가 충전된 상태로 유지되도록 하는 것은 귀하의 책임입니다. 장치의 배터리가 부족하여 서비스에 액세스할 수 없는 경우 서비스 제공업체는 책임을 지지 않습니다.</font></font></p><br><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">귀하의 애플리케이션 사용에 대한 서비스 제공업체의 책임과 관련하여, 서비스 제공업체는 애플리케이션이 항상 업데이트되고 정확하도록 노력하지만 정보를 제공하기 위해 제3자에게 의존한다는 점에 유의하는 것이 중요합니다. 당신이 그것을 사용할 수 있도록하십시오. 서비스 제공업체는 귀하가 애플리케이션의 이 기능에 전적으로 의존한 결과로 발생하는 직간접적인 손실에 대해 어떠한 책임도 지지 않습니다.</font></font></p><br><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">서비스 제공자는 어느 시점에 애플리케이션을 업데이트하기를 원할 수 있습니다. 애플리케이션은 운영 체제(및 애플리케이션 가용성을 확장하기로 결정한 추가 시스템)의 요구 사항에 따라 현재 사용할 수 있으며, 애플리케이션을 계속 사용하려면 업데이트를 다운로드해야 합니다. 서비스 제공업체는 귀하와 관련이 있거나 귀하의 장치에 설치된 특정 운영 체제 버전과 호환되도록 애플리케이션을 항상 업데이트할 것이라고 보장하지 않습니다. 그러나 귀하는 귀하에게 제공되는 애플리케이션 업데이트를 항상 수락하는 데 동의합니다. 서비스 제공업체는 또한 애플리케이션 제공을 중단하기를 원할 수 있으며 귀하에게 종료 통지를 제공하지 않고 언제든지 애플리케이션 사용을 종료할 수 있습니다. 귀하에게 달리 알리지 않는 한, 종료 시 (a) 본 약관에 따라 귀하에게 부여된 권리와 라이선스는 종료됩니다. (b) 귀하는 애플리케이션 사용을 중단하고 (필요한 경우) 귀하의 장치에서 해당 애플리케이션을 삭제해야 합니다.</font></font></p><br><strong><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">본 이용 약관의 변경</font></font></strong><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">서비스 제공업체는 정기적으로 이용약관을 업데이트할 수 있습니다. 따라서 이 페이지를 정기적으로 검토하여 변경 사항이 있는지 확인하는 것이 좋습니다. 서비스 제공업체는 이 페이지에 새로운 이용 약관을 게시하여 변경 사항을 알려드립니다.</font></font></p><br><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">이 약관은 2024년 5월 31일부터 적용됩니다.</font></font></p><br><strong><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">문의하기</font></font></strong><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">이용약관에 대한 질문이나 제안사항이 있는 경우, nyejinssi@ilsa1000ri.com으로 언제든지 서비스 제공업체에 문의하시기 바랍니다.</font></font></p><hr><p><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">이 이용 약관 페이지는 </font></font><a href="https://app-privacy-policy-generator.nisrulz.com/" target="_blank" rel="noopener noreferrer"><font style="vertical-align: inherit;"><font style="vertical-align: inherit;">앱 개인 정보 보호 정책 생성기 에 의해 생성되었습니다.
             """
        termsAndConditionsText.text = Html.fromHtml(termsOfServiceContent, Html.FROM_HTML_MODE_COMPACT)
        termsAndConditionsText.textSize = 14f
        val privacyPolicyContent = """
      개인 정보 정책

본 개인정보 보호정책은 일사천리(이하 "서비스 제공업체")가 무료 서비스로 제작한 모바일 기기용 날씨비서 앱(이하 "애플리케이션")에 적용됩니다. 이 서비스는 "있는 그대로" 사용하기 위한 것입니다.

정보 수집 및 사용


애플리케이션은 귀하의 장치 위치를 수집하여 서비스 제공업체가 귀하의 대략적인 지리적 위치를 파악하고 다음과 같은 방법으로 활용하는 데 도움을 줍니다.
- 위치정보 서비스: 서비스 제공업체는 위치 데이터를 활용하여 개인화된 콘텐츠, 관련 추천, 위치 기반 서비스 등의 기능을 제공합니다.

서비스 제공업체는 중요한 정보, 필수 공지 사항 및 마케팅 프로모션을 제공하기 위해 수시로 귀하에게 연락하기 위해 귀하가 제공한 정보를 사용할 수 있습니다.

더 나은 경험을 위해 애플리케이션을 사용하는 동안 서비스 제공업체는 귀하에게 특정 개인 식별 정보를 제공하도록 요구할 수 있습니다. 서비스 제공자가 요청한 정보는 본 개인정보 보호정책에 설명된 대로 서비스 제공자가 보유하고 사용합니다.

제3자 액세스

서비스 제공업체가 애플리케이션과 해당 서비스를 개선하는 데 도움이 되도록 집계되고 익명화된 데이터만 주기적으로 외부 서비스로 전송됩니다. 서비스 제공업체는 본 개인정보 보호정책에 설명된 방식으로 귀하의 정보를 제3자와 공유할 수 있습니다.

Please note that the Application utilizes third-party services that have their own Privacy Policy about handling data. Below are the links to the Privacy Policy of the third-party service providers used by the Application:
- Google Play Services: https://www.google.com/policies/privacy/
- Google Analytics for Firebase: https://firebase.google.com/support/privacy
- Firebase Crashlytics: https://firebase.google.com/support/privacy/

The Service Provider may disclose User Provided and Automatically Collected Information:
- as required by law, such as to comply with a subpoena, or similar legal process;
- when they believe in good faith that disclosure is necessary to protect their rights, protect your safety or the safety of others, investigate fraud, or respond to a government request;
- with their trusted services providers who work on their behalf, do not have an independent use of the information we disclose to them, and have agreed to adhere to the rules set forth in this privacy statement.

Opt-Out Rights

You can stop all collection of information by the Application easily by uninstalling it. You may use the standard uninstall processes as may be available as part of your mobile device or via the mobile application marketplace or network.

Data Retention Policy

The Service Provider will retain User Provided data for as long as you use the Application and for a reasonable time thereafter. If you'd like them to delete User Provided Data that you have provided via the Application, please contact them at nyejinssi@ilsa1000ri.com and they will respond in a reasonable time.

Children

The Service Provider does not use the Application to knowingly solicit data from or market to children under the age of 13.

The Application does not address anyone under the age of 5. The Service Provider does not knowingly collect personally identifiable information from children under 13 years of age. In the case the Service Provider discover that a child under 13 has provided personal information, the Service Provider will immediately delete this from their servers. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact the Service Provider (nyejinssi@ilsa1000ri.com) so that they will be able to take the necessary actions.

Security

The Service Provider is concerned about safeguarding the confidentiality of your information. The Service Provider provides physical, electronic, and procedural safeguards to protect information the Service Provider processes and maintains.

Changes

This Privacy Policy may be updated from time to time for any reason. The Service Provider will notify you of any changes to the Privacy Policy by updating this page with the new Privacy Policy. You are advised to consult this Privacy Policy regularly for any changes, as continued use is deemed approval of all changes.

This privacy policy is effective as of 2024-05-31

Your Consent

By using the Application, you are consenting to the processing of your information as set forth in this Privacy Policy now and as amended by us.

Contact Us

애플리케이션 사용 중 개인정보 보호에 관한 질문이 있거나 관행에 대해 질문이 있는 경우 nyejinssi@ilsa1000ri.com으로 이메일을 통해 서비스 제공업체에 문의하시기 바랍니다.

이 개인정보 보호정책 페이지는 App Privacy Policy Generator에 의해 생성되었습니다.
"""

        privatePolicyText.text = Html.fromHtml(privacyPolicyContent , Html.FROM_HTML_MODE_COMPACT)
        privatePolicyText.textSize = 14f
        // "전체 동의하기" 체크박스를 클릭했을 때 모든 체크박스를 선택하도록 설정

        val locationContent = """
            위치기반서비스 이용약관

            본 위치기반서비스 이용약관(이하 "약관")은 날씨비서(이하 "당사")이 제공하는 위치기반서비스(이하 "서비스")를 이용함에 있어 당사와 이용자(이하 "사용자") 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.

            제 1 조 (목적)
            이 약관은 당사가 제공하는 서비스의 이용 조건 및 절차, 당사와 사용자의 권리, 의무 및 책임사항 등 기본적인 사항을 규정하는 것을 목적으로 합니다.

            제 2 조 (정의)

            "서비스"란 당사가 제공하는 위치 정보를 기반으로 한 모든 서비스를 의미합니다.
            "사용자"란 본 약관에 따라 당사가 제공하는 서비스를 이용하는 자를 말합니다.
            "위치 정보"란 사용자의 위치를 의미하는 정보를 말합니다.
            제 3 조 (위치정보의 제공 및 이용)

            당사는 사용자의 위치 정보를 수집하여 서비스를 제공합니다.
            사용자는 서비스 이용 시 정확한 위치 정보를 제공해야 하며, 이를 통해 제공되는 서비스의 질이 결정됩니다.
            위치 정보는 서비스 제공을 위해서만 사용되며, 사용자의 동의 없이 제3자에게 제공되지 않습니다.
            제 4 조 (위치정보의 수집 방법)

            당사는 GPS, Wi-Fi, 기지국 정보 등을 통해 사용자의 위치 정보를 수집합니다.
            사용자는 위치 정보 제공을 위한 설정을 변경할 수 있으며, 이에 따라 서비스 이용이 제한될 수 있습니다.
            제 5 조 (위치정보의 보호)

            당사는 사용자의 위치 정보를 안전하게 보호하기 위해 최선의 노력을 다합니다.
            당사는 위치 정보의 오용, 분실, 도난 등을 방지하기 위해 기술적, 관리적 보안 대책을 마련하고 있습니다.
            제 6 조 (위치정보 이용 동의)

            사용자는 본 약관에 동의함으로써 위치 정보의 수집, 이용, 제공에 동의한 것으로 간주됩니다.
            사용자는 언제든지 위치 정보 이용에 대한 동의를 철회할 수 있으며, 철회 시 서비스 이용이 제한될 수 있습니다.
            제 7 조 (서비스 이용 제한)

            사용자는 다음 각 호의 행위를 해서는 안 됩니다.
            a. 위치 정보를 허위로 제공하는 행위
            b. 타인의 위치 정보를 무단으로 수집, 이용하는 행위
            c. 기타 법령에 위반되는 행위
            당사는 사용자가 본 약관을 위반할 경우 서비스 이용을 제한하거나 중지할 수 있습니다.
            제 8 조 (책임의 한계)

            당사는 서비스 이용과 관련하여 발생한 손해에 대해 책임을 지지 않습니다. 단, 당사의 고의 또는 중대한 과실이 있는 경우는 예외로 합니다.
            당사는 사용자가 제공한 위치 정보의 정확성 및 완전성을 보장하지 않습니다.
            제 9 조 (약관의 변경)

            당사는 필요하다고 인정되는 경우 본 약관을 변경할 수 있습니다.
            약관이 변경되는 경우 당사는 변경된 약관을 서비스 내에 공지하며, 변경된 약관은 공지한 시점부터 효력이 발생합니다.
            제 10 조 (기타)

            본 약관에 명시되지 않은 사항에 대해서는 관련 법령 및 당사의 이용약관에 따릅니다.
            본 약관과 관련된 분쟁은 대한민국 법을 준거법으로 하며, 당사 소재지 관할 법원에서 해결합니다.
        """
        locationBasedText.text = Html.fromHtml(locationContent  , Html.FROM_HTML_MODE_COMPACT)
        locationBasedText.textSize = 14f
        checkAllInTerms.setOnCheckedChangeListener { _, isChecked ->
            checkBoxes.forEach { it.isChecked = isChecked }
        }

        // 모든 체크박스의 상태가 변경될 때마다 모든 체크박스가 선택되었는지 확인
        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, _ ->
                val allChecked = checkBoxes.all { it.isChecked }
                nextButton.isEnabled = allChecked
            }
        }

        // 초기에는 "다음으로" 버튼 비활성화
        nextButton.isEnabled = false
        val termsOfLocationBasedServiceScroll = findViewById<ScrollView>(R.id.termsOfLocationBasedServiceScroll)
        val privatePolicyScroll = findViewById<ScrollView>(R.id.privatePolicyScroll)
        val termsOfServiceScroll = findViewById<ScrollView>(R.id.termsOfServiceScroll)

        //다음 버튼을 누르면, 스타일 테스트로 이동
        nextButton.setOnClickListener {
            //구글 로그인
            if (idToken.isNotEmpty()) {
                val fragment = GoogleStyleTestFragment()
                val bundle = Bundle()
                fragment.arguments = bundle
                termsOfLocationBasedServiceScroll.visibility = View.GONE
                termsOfServiceScroll.visibility = View.GONE
                privatePolicyScroll.visibility = View.GONE


                bundle.putString("idToken", idToken)
                Log.d(TAG, "idToken : $idToken")

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()

            }else{
                val fragment = KakaoStyleTestFragment()
                val bundle = Bundle()
                fragment.arguments = bundle

                bundle.putString("accessToken", accessToken)
                Log.d(TAG, "accessToken : $accessToken")

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}