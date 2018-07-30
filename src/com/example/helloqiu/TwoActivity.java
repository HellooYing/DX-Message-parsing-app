package com.example.helloqiu;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;    
import android.view.View.OnClickListener;    
import android.widget.Button;    
import android.widget.Toast;
import com.example.helloqiu.MySocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;
import main.haha;
import protocol.Frame;
public class TwoActivity extends Activity {
	private Button btnshow;  
	private Button gw;
	private Button gy;
	private Button sd;
    private EditText editText1;
    private String bw;
    private Socket client1;
    private String echo="nothing";
    private Boolean isConnecting;
    private UIHandle mHandle;
    private TextView tvUpdate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_two);
		client1 = ((MySocket)getApplication()).getSocket();
		mHandle = new UIHandle();
		final NetSocketThread netSocketThread = new NetSocketThread();
		netSocketThread.start();
		tvUpdate = (TextView)findViewById(R.id.txtOne);
		btnshow = (Button)findViewById(R.id.btnshow);   
		gw = (Button)findViewById(R.id.gw); 
		gy = (Button)findViewById(R.id.gy); 
		sd = (Button)findViewById(R.id.sd); 
        btnshow.setOnClickListener(new OnClickListener() {    
            @Override    
            public void onClick(View v) {    
            	Toast.makeText(getApplicationContext(), "正在发送", Toast.LENGTH_SHORT).show();    
            	editText1 =(EditText)findViewById(R.id.editText1);
            	bw=editText1.getText().toString();
        		new Thread() { 
              		@Override 
              		public void run() { 
              		try { 
                    PrintStream out = new PrintStream(client1.getOutputStream());
                    out.println(bw);
              		} catch (UnknownHostException e) { 
              		e.printStackTrace(); 
              		} catch (IOException e) { 
              		e.printStackTrace(); 
              		} 
              		} 
              		}.start();
            }
        });
        gw.setOnClickListener(new OnClickListener() {    
            @Override    
            public void onClick(View v) {    
            	Toast.makeText(getApplicationContext(), "正在发送", Toast.LENGTH_SHORT).show(); 
            	bw="7F37F48401010101013840011F040404040A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A07070707070703B60E";
        		new Thread() { 
              		@Override 
              		public void run() { 
              		try { 
                    PrintStream out = new PrintStream(client1.getOutputStream());
                    out.println(bw);
              		} catch (UnknownHostException e) { 
              		e.printStackTrace(); 
              		} catch (IOException e) { 
              		e.printStackTrace(); 
              		} 
              		} 
              		}.start();
            }
        });
        gy.setOnClickListener(new OnClickListener() {    
            @Override    
            public void onClick(View v) {    
            	Toast.makeText(getApplicationContext(), "正在发送", Toast.LENGTH_SHORT).show();    
            	bw="7F37F48401010101013840022A050505050A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0707070707070432B7";
        		new Thread() { 
              		@Override 
              		public void run() { 
              		try { 
                    PrintStream out = new PrintStream(client1.getOutputStream());
                    out.println(bw);
              		} catch (UnknownHostException e) { 
              		e.printStackTrace(); 
              		} catch (IOException e) { 
              		e.printStackTrace(); 
              		} 
              		} 
              		}.start();
            }
        });
        sd.setOnClickListener(new OnClickListener() {    
            @Override    
            public void onClick(View v) {    
            	Toast.makeText(getApplicationContext(), "正在发送", Toast.LENGTH_SHORT).show();    
            	bw="7F37F484010101010138010017020202020A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A0A07070707070701638A";
        		new Thread() { 
              		@Override 
              		public void run() { 
              		try { 
                    PrintStream out = new PrintStream(client1.getOutputStream());
                    out.println(bw);
              		} catch (UnknownHostException e) { 
              		e.printStackTrace(); 
              		} catch (IOException e) { 
              		e.printStackTrace(); 
              		} 
              		} 
              		}.start();
            }
        });
	}
	private class NetSocketThread extends Thread {
		public void run() {
			try {
				while (!client1.isConnected()) {//当socket不在连接
					Thread.sleep(1000);//强制当前正在执行的线程休眠
					if (client1.isConnected()) {//如果在连接了
						break;//就打断while
					}
				}
				isConnecting = true;
			} catch (Exception e) {

				isConnecting = false;
				return;
			}
			if (isConnecting) {
				while(true) {
					Message msg = new Message();
		            Bundle data = new Bundle();
                  try { 
               		Socket client1 = ((MySocket)getApplication()).getSocket();
                  	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    BufferedReader buf =  new BufferedReader(new InputStreamReader(client1.getInputStream()));
                    echo = buf.readLine();
                    Log.e("!!!!", echo);
                    haha haha = new haha();
                    ArrayList<Frame> b = haha.find(echo);
                    if(b!= null && b.size() !=0) {
                    int i;
                    for(i = 0 ;i < b.size() ; i++){
                    	Log.e("!!!!",(haha.DataInfo(b.get(i)).toString()));
            		}
                    data.putString("color", (haha.DataInfo(b.get(i-1)).toString()));
                    msg.setData(data);
                    mHandle.sendMessage(msg);
                    }
                    } catch (UnknownHostException e) { 
                  	e.printStackTrace(); 
                  	} catch (IOException e) { 
                  	e.printStackTrace(); 
                  	} 
				}
			}
		}
	};


	private class UIHandle extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg!= null) {
            Bundle dataBundle = msg.getData();
            String color = dataBundle.getString("color");
            tvUpdate.setText(color);
            }
        }   
    }
}
