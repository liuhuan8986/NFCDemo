package com.example.nfcdemo

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


open class NFCBaseActivity : AppCompatActivity() {
     var mNfcAdapter: NfcAdapter? = null
     var mPendingIntent: PendingIntent? = null
    lateinit var TECHLISTS: Array<Array<String>>
    lateinit var FILTERS: Array<IntentFilter>
    init {
        try {
            TECHLISTS = arrayOf(
                arrayOf(IsoDep::class.java.name), arrayOf(
                    NfcV::class.java.name
                ), arrayOf(NfcF::class.java.name)
            )

            FILTERS = arrayOf(
                IntentFilter(
                    NfcAdapter.ACTION_TECH_DISCOVERED, "*/*"
                )
            )
        } catch (e: Exception) {
        }

    }
    override fun onStart() {
        super.onStart()
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        //一旦截获NFC消息，就会通过PendingIntent调用窗口
        mPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        if(mNfcAdapter==null){
            Toast.makeText(this,"不支持NFC", Toast.LENGTH_SHORT).show()
            return
        }else{
            onNewIntent(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        mNfcAdapter?.enableForegroundDispatch(this, mPendingIntent, FILTERS, TECHLISTS);
    }

    override fun onPause() {
        super.onPause()
        //恢复默认状态
        mNfcAdapter?.disableForegroundDispatch(this);
    }
}