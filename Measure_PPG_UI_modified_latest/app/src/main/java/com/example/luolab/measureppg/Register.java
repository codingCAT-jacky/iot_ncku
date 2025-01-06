
/*
	為註冊畫面，將使用者所註冊的用戶資訊存入資料庫
*/


package com.example.luolab.measureppg;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
//import android.support.annotation.Nullable;
import androidx.annotation.Nullable;
//import android.support.v4.app.Fragment;
import androidx.fragment.app.Fragment;
//import android.support.v4.content.LocalBroadcastManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import static com.example.luolab.measureppg.MainActivity.PlaceholderFragment.ARG_SECTION_NUMBER;

public class Register extends Fragment {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private View ResigsterView;
    private View dialogView;

    private Spinner mySpinner;

    private String SpinnerSelected;

    private LayoutInflater LInflater;

    private TextView time_val;
    private TextView net_val;
    private TextView psd;
    private TextView account;
    private TextView Name;
    private TextView accName_tv;
    private TextView age_TV;

    private Button register;
    private Button setUiInfo_btn;

    private ArrayList<String> usrInfo_Array;
    private ArrayAdapter<String> usrInfo_Adapter;

    private Context _context;

    private PendingIntent pi;
    private BroadcastReceiver br;
    private AlarmManager am;

    private boolean firstClick = true;

    private AlertDialog.Builder UsrInfoDialog_Builder;
    private AlertDialog UsrInfoDialog;

    private TextView[] UsrInfo = new TextView[2];

    ConnectivityManager cm;

    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){

//        mDatabase = FirebaseDatabase.getInstance();
//        mDatabaseRef = mDatabase.getReference("message");
//
//        // 写入数据
////        mDatabaseRef.setValue("Hello, Firebase!");
//        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date current1 = new Date();
//
//        String userN = "User1";
//        String header = userN +"_"+ "selectedPara"+"_" + "PPGTime"+"min"+"_"+sdf1.format(current1) ;
//
////        String header = userN + "PPG";
//        String windowed_ppg_str = "12345,23456,34567"; // 示例PPG数据
//        double init_val = 0.1;                         // 初始值
//        double target_val = 0.5;                       // 目标值
//        double feedback_time = 2.5;                    // 反馈时间
//        int voice_ctr = 3;
//
//        Upload_Firebase2(userN, header, "timestr",sdf1.format(current1));
//        Upload_Firebase2(userN, header,"ppgstr",windowed_ppg_str);
//        //Upload_Firebase2(header,"raw_data0",raw_data0_str);
//        //Upload_Firebase2(header,"raw_data1",raw_data1_str);
//        String config = "Inital value: "+Double.toString(init_val)+ ", Target value: "+Double.toString(target_val)+", Feedback time: "+Double.toString(feedback_time);
//        String vc_str = Integer.toString(voice_ctr);
//        Upload_Firebase2(userN, header, "config", config);
//        Upload_Firebase2(userN, header, "feedback counter", vc_str);
















        ResigsterView = inflater.inflate(R.layout.resigster, container, false);

        LInflater = inflater;

        dialogView = View.inflate(inflater.getContext(),R.layout.user_info,null);
        accName_tv = ResigsterView.findViewById(R.id.accName_tv);
        age_TV = ResigsterView.findViewById(R.id.age_ed);
        usrInfo_Array = new ArrayList<String>();
        time_val = ResigsterView.findViewById(R.id.time_val);
        net_val = ResigsterView.findViewById(R.id.net_val);
        setUiInfo_btn = ResigsterView.findViewById(R.id.SetUsrInfo_btn);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd E");
        //  利用 DateFormat 來parse 日期的字串

        Date current = new Date();
        time_val.setText(sdf.format(current));

        setUiInfo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firstClick){
                    setUsrInfo();
                    firstClick = false;
                }
                UsrInfoDialog.show();
            }
        });



        cm = (ConnectivityManager) inflater.getContext().getSystemService(inflater.getContext().CONNECTIVITY_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        inflater.getContext().registerReceiver(mConnReceiver, filter);


        return ResigsterView;
    }


    // 偵測是否連網
    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // 判斷目前有無網路
            if(networkInfo != null &&
                    networkInfo.isConnected()) {
                // 以連線至網路，做更新資料等事情
                Toast.makeText((Activity)LInflater.getContext(), "偵測到網路，可註冊帳號上傳資料", Toast.LENGTH_SHORT).show();
                net_val.setText("網路已連線");
                setUi(0);
            }
            else {
                // 沒有網路
                Toast.makeText((Activity)LInflater.getContext(), "未偵測到網路，請連接網路", Toast.LENGTH_SHORT).show();
                net_val.setText("網路未連線");
                setUi(1);
            }

            //

        };
    };



    // 設定 button Enable 以及 Disable
    private void setUi(int state){
        if(state == 0){

            setUiInfo_btn.setEnabled(true);



        }else{

            setUiInfo_btn.setEnabled(false);


        }
    }



    // 設定使用者資訊 視窗
    private void setUsrInfo()
    {
        UsrInfoDialog_Builder = new AlertDialog.Builder((Activity)LInflater.getContext())
                .setTitle("建立Firebase帳戶")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UsrInfo[0] = UsrInfoDialog.findViewById(R.id.accName_et);

                        if(UsrInfo[0].getText().toString().equals("")  )
                        {
                            Toast.makeText(LInflater.getContext(),"您未輸入任何訊息",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(LInflater.getContext(),"帳號"+UsrInfo[0].getText().toString()+"設定完成",Toast.LENGTH_SHORT).show();
                            GuanView.usrName = UsrInfo[0].getText().toString();
                            History.usrName = UsrInfo[0].getText().toString();

                            accName_tv.setText(UsrInfo[0].getText().toString());
                        }

                        UsrInfo[1] = UsrInfoDialog.findViewById(R.id.age2);

                        if(UsrInfo[1].getText().toString().equals("")  )
                        {
                            Toast.makeText(LInflater.getContext(),"使用預設年齡",Toast.LENGTH_SHORT).show();
                        }
                        else {

                            //Toast.makeText(LInflater.getContext(),"帳號"+UsrInfo[0].getText().toString()+"設定完成",Toast.LENGTH_SHORT).show();
                            GuanView.usrAge = UsrInfo[1].getText().toString();
                            age_TV.setText(UsrInfo[1].getText().toString());
                        }


                    }
                });

        UsrInfoDialog = UsrInfoDialog_Builder.create();
        UsrInfoDialog.setView(dialogView);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());






    }

    public void Upload_Firebase2(String user, String t, String s, String d) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance(); // 取得資料庫連結
        DatabaseReference myRef = database.getReference(user).child(t);   // 新增資料節點，包含 User1
        myRef.child(s).push().setValue(d);
    }


}

