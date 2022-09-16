package kz.application.mbti.predictor

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_image_prediction.*
import kotlinx.android.synthetic.main.layout_result.*
import kotlinx.android.synthetic.main.layout_text_prediction.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val SAVED_PREDICTED_TYPE = "PREDICTED_TYPE"

    private enum class Type {
        IMAGE,
        TEXT
    }

    private val CAMERA = 50001
    private val GALLERY = 50002

    private var currentFunction = Type.TEXT

    private var predictedType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        savedInstanceState?.let { bundle ->
            predictedType = bundle.getString(SAVED_PREDICTED_TYPE)
            predictedType?.let { setPredictedType(it) }
        }

        btnPredict.setOnClickListener {
            hideSoftKeyboard(this)
            if (isInputTextValid(etInputText.text.toString())) {
                predictedType = Predictor.mbti_types[(Math.random() * 16).toInt()]
                predictedType?.let { type -> setPredictedType(type) }
            }
        }

        btnReadAboutType.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("MBTI_TYPE", predictedType)
            startActivity(intent)
        }

        val content = SpannableString(getString(R.string.read_about_type))
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        btnReadAboutType.text = content

        ivTextFragment.setOnClickListener {
            incText.visibility = View.VISIBLE
            incImage.visibility = View.GONE
            if (currentFunction == Type.IMAGE)
                incResult.visibility = View.GONE
            currentFunction = Type.TEXT
            ivTextFragment.setColorFilter(
                ContextCompat.getColor(this, R.color.colorRed),
                android.graphics.PorterDuff.Mode.MULTIPLY
            )
            ivImageFragment.setColorFilter(
                ContextCompat.getColor(this, R.color.colorBlack),
                android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }

        ivImageFragment.setOnClickListener {
            incText.visibility = View.GONE
            incImage.visibility = View.VISIBLE
            if (currentFunction == Type.TEXT)
                incResult.visibility = View.GONE
            currentFunction = Type.IMAGE

            ivTextFragment.setColorFilter(
                ContextCompat.getColor(this, R.color.colorBlack),
                android.graphics.PorterDuff.Mode.MULTIPLY
            )
            ivImageFragment.setColorFilter(
                ContextCompat.getColor(this, R.color.colorRed),
                android.graphics.PorterDuff.Mode.MULTIPLY
            )
        }

        btnUploadImage.setOnClickListener {
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        selectImage(this@MainActivity)
                    }
                })
                .check()

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SAVED_PREDICTED_TYPE, predictedType)
    }

    private fun setPredictedType(type: String) {
        tvPredictedType.text = type
        YoYo.with(Techniques.FadeIn)
            .duration(300)
            .playOn(tvPredictedType)
        incResult?.visibility = View.VISIBLE
    }

    private fun isInputTextValid(text: String): Boolean {
        if (text.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_text), Toast.LENGTH_SHORT).show()
            YoYo.with(Techniques.Shake)
                .duration(300)
                .playOn(etInputText)
            return false
        } else {
            if (text.length < 50) {
                Toast.makeText(this, getString(R.string.error_min_10), Toast.LENGTH_SHORT).show()
                YoYo.with(Techniques.Shake)
                    .duration(300)
                    .playOn(etInputText)
                return false
            }
        }
        return true
    }

    private fun hideSoftKeyboard(activity: Activity) {
        try {
            val inputMethodManager = activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken, 0
            )
        } catch (ex: Exception) {
            Log.e("MainActivity", ex.message.toString())
        }
    }

    private fun selectImage(context: Context) {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose your profile picture")

        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA)

                }
                options[item] == "Choose from Gallery" -> {
                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )

                    startActivityForResult(galleryIntent, GALLERY)

                }
                options[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    ivSelectedImage.setImageBitmap(bitmap)
                    predictedType = Predictor.mbti_types[(Math.random() * 16).toInt()]
                    predictedType?.let { type -> setPredictedType(type) }
                    incResult.visibility = View.VISIBLE

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Failed!", Toast.LENGTH_SHORT).show()
                }

            }

        } else if (requestCode == CAMERA) {
            val thumbnail = data?.extras?.get("data") as Bitmap
            ivSelectedImage.setImageBitmap(thumbnail)
            incResult.visibility = View.VISIBLE
            predictedType = Predictor.mbti_types[(Math.random() * 16).toInt()]
            predictedType?.let { type -> setPredictedType(type) }
//            Toast.makeText(this@MainActivity, "Image Saved!", Toast.LENGTH_SHORT).show()
        }
    }


}
