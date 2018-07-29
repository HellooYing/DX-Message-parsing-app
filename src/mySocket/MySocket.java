package mySocket;

import java.net.Socket;

import android.app.Application;

public class MySocket extends Application{
    Socket socket = null;
    public Socket getSocket(){
        return socket;
    }
    public void setSocket(Socket socket){
        this.socket = socket;
    }
}