package com.dexian.tcporderhost;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    String IP = "192.168.0.104";
    int PORT = 6969;

    private Timer timer;
    InfoData infoData;

    Button btn_addItem, btn_settings;
    ListView LV_itemList, LV_orderQueue;

    CustomAdapterItemList customAdapterItemList;
    CustomAdapterOrderList customAdapterOrderList;

    Gson gson;
    Thread thread;


    List<ItemList> HOST_ITEM_LIST = new ArrayList<ItemList>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btn_addItem = findViewById(R.id.btn_addItem);
        LV_itemList = findViewById(R.id.LV_itemList);
        LV_orderQueue = findViewById(R.id.LV_orderQueue);
        btn_settings = findViewById(R.id.btn_settings);

        infoData = new InfoData();

        // Create a new instance of Gson
        gson = new Gson();

        timer = new Timer();
        //Set the schedule function and rate
        timer.scheduleAtFixedRate(new TimerTask() {

                              @Override
                              public void run() {

                                  syncData();


                              }

                          },
        //Set how long before to start calling the TimerTask (in milliseconds)
        0,
        //Set the amount of time between each execution (in milliseconds)
        10000);


        //Start Server
        thread = new Thread(new PersonalServer());
        thread.start();




        customAdapterItemList = new CustomAdapterItemList(getApplicationContext(), infoData.getItemList());
        LV_itemList.setAdapter(customAdapterItemList);

        customAdapterOrderList = new CustomAdapterOrderList(getApplicationContext(), infoData.getOrderList());
        LV_orderQueue.setAdapter(customAdapterOrderList);


        btn_addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // custom dialog
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.add_item_custom);
                //dialog.setTitle("Title...");

                // set the custom dialog components - text, image and button
                final EditText ET_itemName = dialog.findViewById(R.id.ET_itemName);
                final EditText ET_itemPrice = dialog.findViewById(R.id.ET_itemPrice);
                Button btn_addItem = dialog.findViewById(R.id.btn_addItem);

                // if button is clicked, close the custom dialog
                btn_addItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(!ET_itemName.getText().toString().equals("") && !ET_itemPrice.getText().toString().equals("")){

                            int price = Integer.parseInt(ET_itemPrice.getText().toString());
                            String name = ET_itemName.getText().toString();

                            //infoData.getItemList().add(new ItemList(name, price));
                            infoData.AddItem(name, price);

                            Toast.makeText(getApplicationContext(), "Item Added", Toast.LENGTH_LONG).show();

                            HOST_ITEM_LIST = infoData.getItemList();

                            syncData();
                            dialog.dismiss();

                        }else{
                            Toast.makeText(getApplicationContext(), "Quantity or Name missing", Toast.LENGTH_LONG).show();
                        }

                    }
                });


                dialog.setCancelable(true);
                dialog.getWindow().setLayout(((getWidth(getApplicationContext()) / 100) * 90), ((getHeight(getApplicationContext()) / 100) * 50));
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.show();


            }
        });

        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // custom dialog
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.setting_dialog);
                //dialog.setTitle("Title...");

                // set the custom dialog components - text, image and button
                final EditText ET_IP = dialog.findViewById(R.id.ET_IP);
                Button btn_save = dialog.findViewById(R.id.btn_save);

                ET_IP.setText(getIP());


                // if button is clicked, close the custom dialog
                btn_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(!ET_IP.getText().toString().equals("")){

                            IP = ET_IP.getText().toString();

                            setIP(IP);

                            dialog.dismiss();

                        }else{
                            Toast.makeText(getApplicationContext(), "IP missing", Toast.LENGTH_LONG).show();
                        }



                    }
                });


                dialog.setCancelable(true);
                dialog.getWindow().setLayout(((getWidth(getApplicationContext()) / 100) * 90), ((getHeight(getApplicationContext()) / 100) * 50));
                dialog.getWindow().setGravity(Gravity.CENTER);

                dialog.show();


            }
        });



    }

    private void syncData(){
        String infoDataJson = gson.toJson(infoData);

        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.execute(infoDataJson);
        IP = getIP();

        Log.i("XIAN", "infoDataJson = " + infoDataJson);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                //customAdapterItemList.notifyDataSetChanged();
                //customAdapterOrderList.notifyDataSetChanged();

                try{
                    customAdapterItemList = new CustomAdapterItemList(getApplicationContext(), infoData.getItemList());
                    LV_itemList.setAdapter(customAdapterItemList);
                    customAdapterOrderList = new CustomAdapterOrderList(getApplicationContext(), infoData.getOrderList());
                    LV_orderQueue.setAdapter(customAdapterOrderList);

                }catch (Exception e){
                    Log.i("XIAN", "ERROR syncData : "+e);
                }


            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        timer.cancel();
    }



    public static int getWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    class BackgroundTask extends AsyncTask<String, Void, String> {

        Socket socket;
        DataOutputStream dataOutputStream;

        @Override
        protected String doInBackground(String... msg) {

            String data = msg[0];

            try {
                socket = new Socket(IP, PORT);

                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(data);


                dataOutputStream.close();
                socket.close();

            } catch (IOException e) {
                Log.i("XIAN", "ERROR ** BackgroundTask : "+e);
                e.printStackTrace();
            }

            return null;
        }
    }

    class PersonalServer implements Runnable{

        ServerSocket serverSocket;
        Socket socket;
        DataInputStream dataInputStream;

        String data;


        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(PORT);

                Log.i("XIAN", "Waiting for client DATA");

                while (true){
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    data = dataInputStream.readUTF();

                    /*new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
                        }
                    });*/

                    /*new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
                        }
                    });*/
                    Log.i("XIAN", "HOST INFODATA RECEIVE : "+data);


                    infoData = gson.fromJson(data, InfoData.class);
                    infoData.setItemList(HOST_ITEM_LIST);
                }


            } catch (IOException e) {
                Log.i("XIAN", "ERROR PersonalServer : "+e);
                e.printStackTrace();
            }
        }
    }


    public class CustomAdapterItemList extends BaseAdapter {
        private Context context;
        private List<ItemList> itemList;

        private TextView TV_itemName, TV_itemPrice;
        Button btn_delete;

        public CustomAdapterItemList(Context context, List<ItemList> itemList) {
            this.context = context;
            this.itemList = itemList;
        }
        @Override
        public int getCount() {
            return itemList.size();
        }
        @Override
        public Object getItem(int position) {
            return position;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.single_item_list, parent, false);



            //add data to UI
            TV_itemName = view.findViewById(R.id.TV_itemName);
            TV_itemPrice = view.findViewById(R.id.TV_itemPrice);
            btn_delete = view.findViewById(R.id.btn_delete);

            TV_itemName.setText(itemList.get(position).getItemName());
            TV_itemPrice.setText("TK "+itemList.get(position).getItemPrice());

            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    infoData.getItemList().remove(position);
                    syncData();
                }
            });

            return view;
        }
    }

    public class CustomAdapterOrderList extends BaseAdapter {
        private Context context;
        private List<OrderList> orderLists;

        private TextView TV_itemName, TV_orderQuantity, TV_tableNo;

        public CustomAdapterOrderList(Context context, List<OrderList> orderLists) {
            this.context = context;
            this.orderLists = orderLists;
        }
        @Override
        public int getCount() {
            return orderLists.size();
        }
        @Override
        public Object getItem(int position) {
            return position;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.single_order, parent, false);

            //add data to UI
            TV_itemName = view.findViewById(R.id.TV_itemName);
            TV_orderQuantity = view.findViewById(R.id.TV_orderQuantity);
            TV_tableNo = view.findViewById(R.id.TV_tableNo);

            TV_itemName.setText(orderLists.get(position).getItemName());
            TV_orderQuantity.setText(orderLists.get(position).getItemQuantity()+"x");
            TV_tableNo.setText("Table#"+orderLists.get(position).getTableNo());

            return view;
        }
    }


    private void setIP(String ip){
        SharedPreferences mSharedPreferences = getSharedPreferences("DATA", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("IP",ip);

        mEditor.apply();
    }

    private String getIP(){
        SharedPreferences mSharedPreferences = getSharedPreferences("DATA", MODE_PRIVATE);
        String ip = mSharedPreferences.getString("IP","192.168.0.102");

        return ip;
    }

}
