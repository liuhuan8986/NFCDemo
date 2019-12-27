package com.example.nfcdemo

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


open class NFCBaseActivity : AppCompatActivity() {
     var mNfcAdapter: NfcAdapter? = null
     var mPendingIntent: PendingIntent? = null

    override fun onStart() {
        super.onStart()
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        //一旦截获NFC消息，就会通过PendingIntent调用窗口
        mPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, javaClass), 0)
        if(mNfcAdapter==null){
            Toast.makeText(this,"不支持NFC", Toast.LENGTH_SHORT).show()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        mNfcAdapter?.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    override fun onPause() {
        super.onPause()
        //恢复默认状态
        mNfcAdapter?.disableForegroundDispatch(this);
    }
}