package com.dexian.tcporderhost;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    String IP = "192.168.0.103";
    int PORT = 6969;

    private Timer timer;
    infoData infoData;

    Button btn_addItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btn_addItem = findViewById(R.id.btn_addItem);

        // Create a new instance of Gson
        final Gson gson = new Gson();

        timer = new Timer();
        //Set the schedule function and rate
        timer.scheduleAtFixedRate(new TimerTask() {

                              @Override
                              public void run() {
                                  String infoDataJson = gson.toJson(infoData);

                                  //BackgroundTask backgroundTask = new BackgroundTask();
                                  //backgroundTask.execute("Sending Data");

                                  new Handler(Looper.getMainLooper()).post(new Runnable() {
                                      @Override
                                      public void run() {
                                          //adapter.notifyDataSetChanged();
                                      }
                                  });


                                  Log.i("XIAN", "sampleJson = " + infoDataJson);
                              }

                          },
        //Set how long before to start calling the TimerTask (in milliseconds)
        0,
        //Set the amount of time between each execution (in milliseconds)
        5000);

        /*
        //Start Server
        Thread thread = new Thread(new PersonalServer());
        thread.start();
        */

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        timer.cancel();
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

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
                        }
                    });



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
            TV_itemPrice.setText(""+itemList.get(position).getItemPrice());

            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            return view;
        }
    }

    public class CustomAdapterOrderList extends BaseAdapter {
        private Context context;
        private List<OrderList> orderLists;

        private TextView TV_itemName, TV_Quantity, TV_tableNo;

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
            view = LayoutInflater.from(context).inflate(R.layout.single_item_list, parent, false);

            //add data to UI
            TV_itemName = view.findViewById(R.id.TV_itemName);
            TV_Quantity = view.findViewById(R.id.TV_Quantity);
            TV_tableNo = view.findViewById(R.id.TV_tableNo);

            TV_itemName.setText(orderLists.get(position).getItemName());
            TV_Quantity.setText(""+orderLists.get(position).getItemQuantity());
            TV_tableNo.setText(""+orderLists.get(position).getTableNo());

            return view;
        }
    }

}
