package kz.application.mbti.predictor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val mbtiType = intent.getStringExtra("MBTI_TYPE")

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        mbtiType?.let {
            webView.loadUrl("https://www.16personalities.com/${it.toLowerCase()}-personality")
        }
    }
}
