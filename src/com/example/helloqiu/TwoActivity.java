package com.example.helloqiu;
import android.os.Bundle;
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

import android.app.Activity;
import android.content.Intent;
import android.widget.EditText;

public class TwoActivity extends Activity {
	private Button btnshow;   
    private EditText editText1;
    private String bw;
    private Socket client1;
    private String echo="nothing";
    private Boolean isConnecting;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_two);
		client1 = ((MySocket)getApplication()).getSocket();
		final NetSocketThread netSocketThread = new NetSocketThread();
		netSocketThread.start();
		btnshow = (Button)findViewById(R.id.btnshow);   
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
                  try { 
               		Socket client1 = ((MySocket)getApplication()).getSocket();
                  	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    BufferedReader buf =  new BufferedReader(new InputStreamReader(client1.getInputStream()));
                    echo = buf.readLine();
            		Log.e("!!!!", echo);
                  	} catch (UnknownHostException e) { 
                  	e.printStackTrace(); 
                  	} catch (IOException e) { 
                  	e.printStackTrace(); 
                  	} 
				}
			}
		}
	};
}
