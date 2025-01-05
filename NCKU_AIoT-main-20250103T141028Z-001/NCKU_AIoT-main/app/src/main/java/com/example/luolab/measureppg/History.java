/*

	更新所量到的數據，顯示於手機第三個畫面

	主要動作為連到資料庫將所要的資料撈出來並顯示

*/

package com.example.luolab.measureppg;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.os.Vibrator;
//import android.support.annotation.Nullable;
import androidx.annotation.Nullable;
//import android.support.annotation.RequiresApi;
import androidx.annotation.RequiresApi;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class History extends Fragment /*implements Update*/{

    private View HistoryView;

    public static int tTime = 1;
    public static double[] HRData;
    public static double[] BRData;
    public static double[] SDNNData;
    public static double[] HFData;
    //public static double[] LF_HFData;

    private View dialogView;
    private AlertDialog Dialog;
    private AlertDialog.Builder Dialog_Builder;

    private View dialogView2;
//    private GraphView historyDataGraph;
    private TextView maxSDNNTv;
    private TextView minBRTv;
    private TextView maxHFTv;
    private TextView ParaTv;
    private TextView debug_tv;
    private AlertDialog Dialog2;
    private AlertDialog.Builder Dialog_Builder2;

    private LayoutInflater LInflater;


    public static int feedbackCounter = 0;
    public static double maxSDNN = 0;
    public static double minBR = 0;
    public static double maxHF = 0;

    private int dataNum;
    private int graphUpper;
    private int graphLower;
    private Handler mHandler;
    private int dayRange = 3;
    private Handler recordsHandler;
    private ArrayList<String> userDataDateRecord;
    static public String usrName = "NoneUser";
    private ArrayList<String> userDataDateRecordList = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        fetchFromFirebase();
        Log.d("usrName", usrName);



        recordsHandler = new Handler();
        HistoryView = inflater.inflate(R.layout.history, container, false);

        TextView textView = HistoryView.findViewById(R.id.textView9);
        textView.setText(usrName + " - 歷史訓練次數查詢");

        GraphView historyDataGraph = HistoryView.findViewById(R.id.historyGraph);

        historyDataGraph.getViewport().setMinX(0);
        historyDataGraph.getViewport().setMaxX(20);
        historyDataGraph.getViewport().setXAxisBoundsManual(true);



        Spinner dateRangeSpinner = HistoryView.findViewById(R.id.dateRangeSpinner);

        Button updateGraph_btn = HistoryView.findViewById(R.id.updateGraph);



        updateGraph_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
//                Log.d("userDataDateRecordList", userDataDateRecordList.toString());
//
//                Log.d("dayRange", String.valueOf(dayRange));
                updateGraph(historyDataGraph, dayRange);
            }
        });



        dateRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.e("event start", String.valueOf(position));
                switch (position) {
                    case 0: // 近三天
                        dayRange = 3;
//                        updateGraph(historyDataGraph, 3);


                        break;
                    case 1: // 近一周
                        dayRange = 7;
//                        updateGraph(historyDataGraph, 7);

                        break;
                    case 2: // 近一個月
                        dayRange = 30;
//                        updateGraph(historyDataGraph, 30);


                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 預設顯示近一周
                updateGraph(historyDataGraph, 7);
            }
        });
        return HistoryView;
    };


//    public DataPoint[] data(double[] arr){
//        DataPoint[] values = new DataPoint[arr.length];     //creating an object of type DataPoint[] of size 'n'
//        for(int i=0;i<arr.length;i++){
//            DataPoint v = new DataPoint(i,arr[i]);
//            values[i] = v;
//        }
//        return values;
//    };


    public void updateGraph(GraphView historyDataGraph, int dayRange){

        ViewGroup.LayoutParams params = historyDataGraph.getLayoutParams();


        historyDataGraph.removeAllSeries();


        String[] dateLabels = getDateRangeLabels(dayRange);

        LineGraphSeries<DataPoint> series = generateCustomData(dayRange, dateLabels);
        historyDataGraph.addSeries(series);

        historyDataGraph.getViewport().setMinY(0);
        historyDataGraph.getViewport().setMaxY(dayRange);
        historyDataGraph.getViewport().setYAxisBoundsManual(true);


        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(historyDataGraph);
        staticLabelsFormatter.setVerticalLabels(dateLabels);

        historyDataGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        historyDataGraph.getGridLabelRenderer().setHumanRounding(false);
        historyDataGraph.getGridLabelRenderer().setNumVerticalLabels(dayRange);

        historyDataGraph.invalidate();
    }

    private LineGraphSeries<DataPoint> generateCustomData(int dayRange, String[] dateLabels) {

        Log.d("userDataDateRecordList", userDataDateRecordList.toString());

        Log.d("dateLabels", Arrays.toString(dateLabels));



        DataPoint[] dataPoints = new DataPoint[dateLabels.length]; // 生成 3 个数据点
        for (int i = 0; i < dateLabels.length; i++) {
            int count = 0;
            for (int j = 0; j < userDataDateRecordList.size(); j++) {
                if(dateLabels[i].equals((userDataDateRecordList.get(j)))){
                    count += 1;
                }
            }
//            Log.d("count", String.valueOf(count));
            dataPoints[i] = new DataPoint(count, i);
        }
        for (int i = 0; i < dataPoints.length; i++) {
            if (dataPoints[i] != null) {
                Log.d("DataPoint", "Index " + i + ": ("
                        + dataPoints[i].getX() + ", " + dataPoints[i].getY() + ")");
            } else {
                Log.d("DataPoint", "Index " + i + ": null");
            }
        }


         // X = 2, Y = 今天日期时间戳
//        dataPoints[1] = new DataPoint(5, getDateDaysAgo(1)); // X = 5, Y = 昨天日期时间戳
//        dataPoints[2] = new DataPoint(10, getDateDaysAgo(2)); // X = 10, Y = 前天日期时间戳
//
//        dataPoints[3] = new DataPoint(15, getDateDaysAgo(3)); // X = 10, Y = 前天日期时间戳
        return new LineGraphSeries<>(dataPoints);
    }

    private double getDateDaysAgo(int daysAgo) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo);
//
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//
//        Log.e("here", String.valueOf(calendar.getTimeInMillis()));
//        return calendar.getTimeInMillis();
        return daysAgo;
    }

    private String[] getDateRangeLabels(int daysRange) {
        String[] labels = new String[daysRange + 1]; // 包括今天
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        for (int i = daysRange; i >= 0; i--) {
            labels[i] = dateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            Log.e("here", labels[i]);
        }



        return labels;
    }

    public void fetchFromFirebase() {
        // 获取 Firebase 数据库实例和引用
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference timestrRef = database.getReference("User1");
//        userDataDateRecordList = new ArrayList<>();
        // 添加值监听器
        timestrRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    // 获取当前的 key
                    String key = childSnapshot.getKey();
                    if (key != null) {
                        String[] parts = key.split("_");
                        String timePart = parts[parts.length - 1].split(" ")[0];
                        String yearPart = timePart.split("-")[0].substring(2);
                        String monthPart = timePart.split("-")[1];
                        String dayPart = timePart.split("-")[2];

                        userDataDateRecordList.add(yearPart + "/" + monthPart + "/" + dayPart);
                    }

                }
                Log.d("userDataDateRecordList", userDataDateRecordList.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

}
