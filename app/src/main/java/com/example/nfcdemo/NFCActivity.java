package com.example.nfcdemo;

import com.example.nfcdemo.R;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

@SuppressLint("NewApi")
public class NFCActivity extends BaseActivity {

  @SuppressLint("NewApi")
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);

    nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    // check NFC status.
    checkNfc();
    onNewIntent(getIntent());
    initListener();
  }

  private void initListener() {
    readButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (checkBlock()) {
          MifareClassicCard mifareClassicCard = new MifareClassicCard(
              mifareClassic);
          int block = Integer.parseInt(blockIdEditText.getText()
              .toString().trim());
          String content = mifareClassicCard.readCarCode(block,
              keyEdittext.getText().toString().trim());
          if (getResources().getString(R.string.alert_auth_err)
              .equals(content)
              || getResources()
              .getString(R.string.alert_read_err).equals(
                  content)) {
            setHintToContentEd(content);
          } else {
            contentEditText.setText(content);
          }
        }
      }
    });
    wriButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (checkBlock()) {
          MifareClassicCard mifareClassicCard = new MifareClassicCard(
              mifareClassic);
          int block = Integer.parseInt(blockIdEditText.getText()
              .toString().trim());
          String content = contentEditText.getText().toString()
              .trim();
          String result = mifareClassicCard.wirteCarCode(content,
              block, keyEdittext.getText().toString().trim());
          if ("".equals(result)) {
            setHintToContentEd(getResources().getString(R.string.modify_ok));
          } else {
            setHintToContentEd(result);
          }
        }
      }
    });

    modifyButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (checkBlock()) {
          MifareClassicCard mifareClassicCard = new MifareClassicCard(
              mifareClassic);
          int block = Integer.parseInt(blockIdEditText.getText()
              .toString().trim());
          String content = contentEditText.getText().toString()
              .trim();
          String key = keyEdittext.getText().toString().trim();
          String result = mifareClassicCard.modifyPassword(block,
              content, key);
          if ("".equals(result)) {
            setHintToContentEd(getResources().getString(
                R.string.modify_ok));
          } else {
            setHintToContentEd(result);
          }
        }
      }
    });
  }

  @SuppressLint("NewApi")
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    if (nfcAdapter != null) {
      nfcAdapter.enableForegroundDispatch(this, pendingIntent, FILTERS,
          TECHLISTS);
    }
  }

  @SuppressLint("NewApi")
  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
    if (nfcAdapter != null) {
      nfcAdapter.disableForegroundDispatch(this);
    }
  }

  @SuppressLint("NewApi")
  protected void onNewIntent(Intent intent) {
    // TODO Auto-generated method stub
    super.onNewIntent(intent);
    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

    } else {
      if (tag != null) {

        // Get card type & tag lists.
        String[] techList = tag.getTechList();
        StringBuffer techString = new StringBuffer();
        for (int i = 0; i < techList.length; i++) {
          techString.append(techList[i]);
          techString.append(";\n");
        }
        typeEditText.setText(techString.toString());
        // Get card id.
        byte[] id = tag.getId();
        carCodeEditText.setText(ByteArrayToHexString(id));

        // operate mifare card
        mifareClassic = MifareClassic.get(tag);
        if (mifareClassic != null) {
          showView(1);
        } else {
          showView(0);
        }
      }
    }
  }
}
