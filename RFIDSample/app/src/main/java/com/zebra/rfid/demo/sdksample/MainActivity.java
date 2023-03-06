package com.zebra.rfid.demo.sdksample;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.demo.sdksample.ui.BaseActivity;

public class MainActivity extends BaseActivity implements RFIDHandler.ResponseHandlerInterface, RFIDHandler.TextResponseCallBack {

    public TextView statusTextViewRFID = null;
    private TextView textrfid;
    private TextView testStatus;

    RFIDHandler rfidHandler;
    final static String TAG = "RFID_SAMPLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // UI
        statusTextViewRFID = findViewById(R.id.textStatus);
        textrfid = findViewById(R.id.textViewdata);
        testStatus = findViewById(R.id.testStatus);

        // RFID Handler
        rfidHandler = new RFIDHandler();
        rfidHandler.init(this);



        // set up button click listener
        Button test = findViewById(R.id.button);
        test.setOnClickListener(v -> {
            String result = rfidHandler.Test1();
            testStatus.setText(result);
        });

        Button test2 = findViewById(R.id.button2);
        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = rfidHandler.Test2();
                testStatus.setText(result);
            }
        });

        Button defaultButton = findViewById(R.id.button3);
        defaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = rfidHandler.Defaults();
                testStatus.setText(result);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        rfidHandler.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        String status = rfidHandler.onResume();
        statusTextViewRFID.setText(status);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandler.onDestroy();
    }


    @Override
    public void handleTagdata(TagData[] tagData) {
        final StringBuilder sb = new StringBuilder();
        for (TagData tagDatum : tagData) {
            sb.append(tagDatum.getTagID()).append("\n");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textrfid.append(sb.toString());
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textrfid.setText("");
                }
            });
            rfidHandler.performInventory();
        } else
            rfidHandler.stopInventory();
    }

    @Override
    public void TextChanged(String text) {
        statusTextViewRFID.setText(text);
    }
}
