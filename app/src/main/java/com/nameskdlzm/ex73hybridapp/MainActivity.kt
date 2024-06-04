package com.mrhi2024.ex73hybridapp

import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.nameskdlzm.ex73hybridapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //웹뷰 설정
        binding.wv.settings.javaScriptEnabled= true
        binding.wv.settings.allowFileAccess= true  // file:// 에서도 ajax 기술 사용 허용
        binding.wv.settings.builtInZoomControls= true
        binding.wv.settings.displayZoomControls= false
        binding.wv.settings.domStorageEnabled= true

        binding.wv.webViewClient= WebViewClient()
        binding.wv.webChromeClient= WebChromeClient()

        //웹뷰가 보여줄 웹페이지를 로딩하기
        binding.wv.loadUrl("file:///android_asset/index.html")

        //1) native app 에서 web js를 제어하기
        binding.btn.setOnClickListener {
            //웹뷰에 보낼 메세지
            var message:String = binding.inputLayout.editText!!.text.toString()
            message = message.replace("'","&apos;")
            binding.wv.loadUrl("javascript:aaa('$message')")
        }

        //2) 웹뷰에서 보여주는 웹문서의 JS와 연결할 객체 생성 및 추가
        binding.wv.addJavascriptInterface( MyWebViewConnector(),"Droid" )
    }//onCreate method..

    //2)웹뷰의 javascript와 통신을 담당할 연결자 객체 클래스 정의
    inner class MyWebViewConnector{

        @JavascriptInterface
        fun setTextView(message:String){
            binding.tv.text= message
        }

        @JavascriptInterface
        fun requestTranslate(message:String){
            //ML kit - 머신러닝 턴키 라이브러리의 언어번역 온디바이스 AI에게 번역 요청
            val options= TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.KOREAN).setTargetLanguage(TranslateLanguage.ENGLISH).build()
            val translator= Translation.getClient(options)

            //번역에 필요한 모델을 필요하면 다운로드.. 단, WIFI 가능할 때만
            val conditions = DownloadConditions.Builder().requireWifi().build()
            translator.downloadModelIfNeeded(conditions).addOnSuccessListener {
                // 다운로드가 필요없거나 완료되었을 대 발동하는 영역
                Toast.makeText(this@MainActivity, "model ready", Toast.LENGTH_SHORT).show()

                translator.translate(message).addOnSuccessListener {
                    val msg:String = it.replace("'","&apos;") // HTML 엔티티 코드
                    binding.wv.loadUrl("javascript:aaa('$msg')")
                }
            }


        }

    }

}//MainActivity class..