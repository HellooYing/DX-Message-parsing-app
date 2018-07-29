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
    private String ip;
    private int dk;
    private String echo="nothing";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_two);
		btnshow = (Button)findViewById(R.id.btnshow);   
		

        btnshow.setOnClickListener(new OnClickListener() {    
            @Override    
            public void onClick(View v) {    
            	Toast.makeText(getApplicationContext(), "正在上传", Toast.LENGTH_SHORT).show();    
            	editText1 =(EditText)findViewById(R.id.editText1);
            	bw=editText1.getText().toString();
            	Intent intent = getIntent();
                ip = intent.getStringExtra("ip");
                dk = intent.getIntExtra("dk",0);
        		new Thread() { 
              		@Override 
              		public void run() { 
              		try { 
              		Socket client1 = ((MySocket)getApplication()).getSocket();
              		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    PrintStream out = new PrintStream(client1.getOutputStream());
                    BufferedReader buf =  new BufferedReader(new InputStreamReader(client1.getInputStream()));
                    out.println(bw);
                    String echo = buf.readLine();
        			Log.e("!!!!", echo);
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
}