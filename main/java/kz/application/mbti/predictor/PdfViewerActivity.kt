package kz.application.mbti.predictor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_pdf_viewer.*

class PdfViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        val mbtiType = intent.getStringExtra("MBTI_TYPE")
        mbtiType?.let {
            pdfViewer.fromAsset("$it.pdf").load()
        }

    }
}
