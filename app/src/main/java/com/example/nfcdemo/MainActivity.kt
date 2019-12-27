package com.example.nfcdemo

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import java.io.IOException




class MainActivity : NFCBaseActivity() {

    lateinit var content: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        content = findViewById(R.id.content)
    }

    @ExperimentalStdlibApi
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (mNfcAdapter == null) {
            Toast.makeText(this, "不支持NFC", Toast.LENGTH_SHORT).show()
            return
        }

        //拿来装读取出来的数据，key代表扇区数，后面list存放四个块的内容
        val map = HashMap<String, MutableList<String>>();
        //intent就是onNewIntent方法返回的那个intent
        var tag = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG) as Tag;
        val techList = tag.getTechList()
        Log.e("liu", "标签支持的tachnology类型：")
        Log.e("liu", tag.id.contentToString())
        Log.e("liu", "16进制：" + bytes2HexString(tag.id))
        Log.e("liu", "16进制2：" + toHexString(tag.id))
        Log.e("liu", "10进制：${java.lang.Long.parseLong(toHexString(tag.id),16)}")
        Log.e("liu", "10进制2：${toHexString(tag.id).toBigInteger(16)}")
        for (tech in techList) {
            Log.e("liu", tech)
        }

        var rawArray: Array<Parcelable>? =
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawArray != null) {
            val mNdefMsg = rawArray[0] as NdefMessage;
            val mNdefRecord = mNdefMsg.getRecords()[0] as NdefRecord;
            if (mNdefRecord != null) {
                val readResult = String(mNdefRecord.getPayload(), Charsets.UTF_8)
                Log.i("liu", readResult)
            }
        }


        var mfc = MifareClassic.get(tag);
        //如果当前IC卡不是这个格式的mfc就会为空
        if (null != mfc) {
            try {
                //链接NFC
                mfc.connect();
                //获取扇区数量
                val count = mfc.getSectorCount();
                Log.e("liu", "扇区数量:" + count)
                //用于判断时候有内容读取出来
                var flag = false;
                for (i in 0..count) {
                    //默认密码，如果是自己已知密码可以自己设置
                    var bytes: ByteArray = byteArrayOf(
                        (0xff).toByte(),
                        (0xff).toByte(),
                        (0xff).toByte(),
                        (0xff).toByte(),
                        (0xff).toByte(),
                        (0xff).toByte()
                    )
                    //验证扇区密码，否则会报错（链接失败错误）
                    //这里验证的是密码A，如果想验证密码B也行，将方法中的A换成B就行
                    var isOpen = mfc.authenticateSectorWithKeyA(i, bytes);
                    var list: MutableList<String> = ArrayList()
                    if (isOpen) {
                        //获取扇区里面块的数量
                        val bCount = mfc.getBlockCountInSector(i);
                        Log.e("liu", "一个扇区有多少块:" + bCount)
                        //获取扇区第一个块对应芯片存储器的位置
                        //（我是这样理解的，因为第0扇区的这个值是4而不是0）
                        val bIndex = mfc.sectorToBlock(i);
                        for (j in 0..bCount - 1) {
                            //读取数据，这里是循环读取全部的数据
                            //如果要读取特定扇区的特定块，将i，j换为固定值就行
                            var data = mfc.readBlock(bIndex + j);
                            list.add(byteToString(data));
                        }
                        flag = true;
                    }
                    map.put(i.toString(), list);
                }
                if (flag) {
                    //回调，因为我把方法抽出来了
                    //callback.callBack(map);
                    Log.e("liu：", map.toString())
                } else {
                    Log.e("liu：", "error")
                    //callback.error();
                }
            } catch (e: Exception) {
                //callback.error();
                e.printStackTrace();
            } finally {
                try {
                    mfc.close();
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }

    }

    fun byteToString(src: ByteArray): String {
        var stringBuilder = StringBuilder();
        if (src == null || src.size <= 0) {
            return "";
        }
        var buffer = CharArray(2)
        for (i in 0..src.size - 1) {
            buffer[0] = Character.forDigit((src[i].toInt().ushr(4)) and 0x0F, 16);
            buffer[1] = Character.forDigit(src[i].toInt() and 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    private val HEX_DIGITS =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    private fun bytes2HexString(bytes: ByteArray?): String {
        if (bytes == null) return ""
        val len = bytes.size
        if (len <= 0) return ""
        val ret = CharArray(len shl 1)
        var i = 0
        var j = 0
        while (i < len) {
            ret[j++] = HEX_DIGITS[bytes[i].toInt() shr 4 and 0x0f]
            ret[j++] = HEX_DIGITS[bytes[i].toInt() and 0x0f]
            i++
        }
        return String(ret)
    }

    fun toHexString(byteArray: ByteArray?): String {
        require(!(byteArray == null || byteArray.size < 1)) { "this byteArray must not be null or empty" }

        val hexString = StringBuilder()
        for (i in byteArray.indices) {
            if (byteArray[i].toInt() and 0xff < 0x10)
            //0~F前面不零
                hexString.append("0")
            hexString.append(Integer.toHexString(0xFF and byteArray[i].toInt()))
        }
        return hexString.toString().toLowerCase()
    }

}
