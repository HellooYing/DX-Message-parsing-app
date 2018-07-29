package protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.topscomm.util.BCD8421Operater;
import com.topscomm.util.CRC16;



public class FrameService {
	public static ConcurrentLinkedQueue<FrameInfo> pRecvQueue 
	=new ConcurrentLinkedQueue<FrameInfo>();// 获取到CRC帧，并进行域解析后存储到该队列
	public static ArrayList<Frame> CRCMakeFrame(String s) {
		//String s = "7F10B510231195711100600800100015B85BB055667788";
		byte[] bytes = BCD8421Operater.string2Bcd(s);
		InputStream sbs = new ByteArrayInputStream(bytes);
		ArrayList<Frame>list = new ArrayList<Frame>();
		try {
			for (Frame frame =new Frame();(frame=getFrame(sbs))!=null;)
			{
				list.add(frame);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
		return list;	
	}
	public static Frame getFrame(InputStream inStream) throws IOException {
		if (inStream == null||inStream.available()==0) {
			return null;// 流已经关闭
		}
		Frame temFrame = new Frame();
		byte[] temp = new byte[1];// 一次读一个字节
		byte[] len = new byte[2];//
		int curFrmDataLen = 0;
		int streamFlag1 = 0;// 流是否结束的标志
		while (((streamFlag1 = inStream.read(temp)) != -1) && temp[0] != 0x7F) {
			continue;
		}
		if (streamFlag1 == -1)// 流结束
		{
			return null;
		}

		temFrame.data[temFrame.frameLen++] = temp[0];// 存储帧头，增加长度

		int streamFlag2 = 0;// 流是否结束的标志
		if ((streamFlag2 = inStream.read(len)) == -1
				|| streamFlag2 != len.length) {
			return null;
		}
		if ((len[0] & 0x80) >0) {// 说明Len域是拓展的
			curFrmDataLen = len[0] & 0x7F;
			curFrmDataLen += len[1] * 0x80;
		} else {
			curFrmDataLen = len[0] & 0x7F;
		}

		temFrame.data[temFrame.frameLen++] = len[0];// 存储Len域第一个字节，增加长度
		temFrame.data[temFrame.frameLen++] = len[1];// 存储Len域第一个字节，增加长度

		curFrmDataLen -= 2;// 减去已经存储的Len域的长度

		int streamFlag3 = inStream.read(temFrame.data, temFrame.frameLen,
				curFrmDataLen);// 存储从Len域后到Data域截止的数据
		if ((streamFlag3 == -1) || (streamFlag3 != curFrmDataLen)) {
			return null;
		}

		temFrame.frameLen += curFrmDataLen;// 增加长度

		byte[] crcBytes = new byte[2];
		int streamFlag4 = 0;// 流是否结束的标志
		if ((streamFlag4 = inStream.read(crcBytes)) == -1
				|| streamFlag4 != crcBytes.length) {
			return null;
		}
		temFrame.data[temFrame.frameLen++] = crcBytes[0];// 存储FCS域第一个字节，增加长度
		temFrame.data[temFrame.frameLen++] = crcBytes[1];// 存储FCS域第二个字节，增加长度

		// 进行CRC16校验
		int crcNum1 = CRC16.calcCrc16(temFrame.data, temFrame.frameLen);
		if (crcNum1 == 0)// 校验通过
		{
			return temFrame;
		} else {
			return null;
		}

	}
}
