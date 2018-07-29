package com.example.helloqiu;

import android.util.Log;
import android.view.View;    
import android.view.View.OnClickListener;    
import android.widget.Button;    
import android.widget.Toast;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import com.example.helloqiu.MySocket;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class MainActivity extends Activity {
	private Button btnshow;   
    private EditText editText1,editText2;
    private String ip;
    private int dk;
    public static final String TAG = "CrashHandler";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnshow = (Button)findViewById(R.id.btnshow);   
        btnshow.setOnClickListener(new OnClickListener() {    
            @Override    
            public void onClick(View v) {  
            	Toast.makeText(getApplicationContext(), "正在连接", Toast.LENGTH_SHORT).show();    
            	editText1 =(EditText)findViewById(R.id.editText1);
            	ip=editText1.getText().toString();
            	editText2 =(EditText)findViewById(R.id.editText2);
            	dk=Integer.parseInt(editText2.getText().toString());
            	new Thread() { 
            		@Override 
            		public void run() { 
            		try { 
            		Socket client1 = new Socket(ip,dk);
            		PrintStream out = new PrintStream(client1.getOutputStream());
            		out.println("connection succeeded");
            		((MySocket)getApplication()).setSocket(client1);
            		Intent intent = new Intent(MainActivity.this, TwoActivity.class);
            		intent.putExtra("ip", ip);
                    intent.putExtra("dk", dk);
                    startActivity(intent);
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
	