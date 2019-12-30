package br.com.gorio.bqcodereader.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import br.com.gorio.bqcodereader.Febraban
import br.com.gorio.bqcodereader.R
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_scan_barcode.*
import java.io.IOException


class ScanBarcodeActivity : AppCompatActivity() {

    private var TYPE: Int = 0
    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getStringExtra("type").equals("barcode")) {
            TYPE = 1
        } else if (intent.getStringExtra("type").equals("qrcode")) {
            TYPE = 2
        } else {
            val intent = Intent()
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }

        checkPermission()
    }

    private fun checkPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_CAMERA
            )
        } else {
            // Permission has already been granted
            cameraSource()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    cameraSource()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun cameraSource() {
        setContentView(R.layout.activity_scan_barcode)

        val barcodeDetector = BarcodeDetector.Builder(this)
//            .setBarcodeFormats(Barcode.CODABAR or Barcode.QR_CODE or Barcode.ALL_FORMATS)
            .build()
        val cameraSource =
            CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1920, 1080)
                .build()

        caera_preview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (ActivityCompat.checkSelfPermission(
                        this@ScanBarcodeActivity,
                        android.Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                try {
                    cameraSource.start(caera_preview.holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {

            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val sparseArray = detections.detectedItems

                if (TYPE == 1)
                    sparseArray.forEach { key, value ->

                        if (value.displayValue.matches("-?\\d+(\\.\\d+)?".toRegex())) {
                            val febraban =
                                Febraban(value.displayValue)

                            if (!febraban.hasError()) {
                                val intent = Intent()
                                intent.putExtra("barcode", febraban.linhaDigitavel)
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
                        }
                    }
                else if (TYPE == 2)
                    if (sparseArray.size() > 0) {
                        val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP)

                        val intent = Intent()
                        intent.putExtra("barcode", sparseArray.valueAt(0).displayValue)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
//                if(sparseArray.size()>0){
//                    val intent = Intent()
//                    intent.putExtra("barcode", sparseArray.valueAt(0))
//                    setResult(CommonStatusCodes.SUCCESS,intent)
//                    finish()
//                }
            }
        })
    }


}