package br.com.gorio.bqcodereader.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.com.gorio.bqcodereader.view.ScanBarcodeActivity

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val CODE_RETURN: Int = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check which request we're responding to
        if (requestCode == CODE_RETURN) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                if (data!!.hasExtra("barcode"))
                    returnCode.text = data.getStringExtra("barcode")
                // Do something with the contact here (bigger example below)
            }
        }
    }

    fun startReaderBarcode(view: View) {
        returnCode.text = ""
        val intent = Intent(this, ScanBarcodeActivity::class.java)
        intent.putExtra("type", "barcode")
        startActivityForResult(intent, CODE_RETURN)
    }

    fun startReaderQRCode(view: View) {
        returnCode.text = ""
        val intent = Intent(this, ScanBarcodeActivity::class.java)
        intent.putExtra("type", "qrcode")
        startActivityForResult(intent, CODE_RETURN)
    }
}
