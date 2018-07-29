package main;

import java.util.ArrayList;
import java.util.Vector;

import protocol.FrameInfo;

import protocol.Frame;
import protocol.FrameService;

public class haha{
    public static void main(String[] args) throws Exception {
    	ArrayList<Frame> b=new haha().find("7F11D4E784010101017D03030303AAB8C0FF695E");
    	if(b.size()!=0) {
    		System.out.println(new haha().split(b.get(0)));
    	}
    	ArrayList<String> qaq=new haha().DataInfo(b.get(1));
    }
    public ArrayList<Frame> find(String a) {
        //String msg="7F11D4E784010101017D03030303AAB8C0FF695E";
    	String msg=a;
		ArrayList<Frame> list = FrameService.CRCMakeFrame(msg);
		System.out.println(list);
		return list;
    }
    public Vector<String> split(Frame a) {
    	Frame frame=a;
    	FrameInfo frameInfo = FrameInfo.ParameterGetFrameInfoFromRecvBuffer(frame.data,frame.frameLen);
    	Vector<String> vector = FrameInfo.frameInfoToVector(frameInfo);
    	return vector;
    }
    public  ArrayList<String> DataInfo(Frame a ) 
	{
    	Frame frame=a;
    	FrameInfo frameinfo = FrameInfo.ParameterGetFrameInfoFromRecvBuffer(frame.data,frame.frameLen);
		Vector<String> FrameInfoVector = FrameInfo.frameInfoToVector(frameinfo);
		//获取data数组域
		String datainfo = FrameInfoVector.get(7);
		ArrayList<String> listDataInfo = new ArrayList<String>();
		String DataEvent= datainfo.substring(0, 4);
		System.out.println(DataEvent);
		
		switch(DataEvent)
		{
		case "0100": listDataInfo.add("0100:手报事件上报帧");break;
		case "4001": listDataInfo.add("4001:感温事件上报帧");break;
		case "4002": listDataInfo.add("4002:感烟事件上报帧") ;break;
		///
		default:{
			listDataInfo.add("其他事件");
			return listDataInfo;
		}}
		String DataUc = datainfo.substring(4,14);
		listDataInfo.add("设备UC: "+DataUc);
		String DataCC =datainfo.substring(14,78);
		listDataInfo.add("设备CC: "+DataCC);
		String Datatime=datainfo.substring(78,90);
		listDataInfo.add("时间: "+Datatime);
		String DataLA =datainfo.substring(90);
		listDataInfo.add("设备LA: "+DataLA);
		ArrayList<String> list=listDataInfo;
    	for(int i = 0 ;i < list.size() ; i++){
			System.out.println(list.get(i));
		}
		return listDataInfo;
		}

}