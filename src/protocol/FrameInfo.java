package protocol;

import java.util.Vector;

import com.topscomm.util.BitOperator;



/**
 * @author yuchen
 *
 */
/**
 * @author yuchen
 * 
 */
public class FrameInfo {

	/**
	 * 1、帧起始符
	 */
	public byte[] frmHEAD = new byte[1];// 帧起始符

	/**
	 * 2、长度域
	 */
	public byte[] frmLEN = new byte[2];// 长度域
	public short _frmLENLen;// 长度域占字节数
	public short _frmLENSend;

	/**
	 * 3、控制码CTR域
	 */
	public byte[] frmCTR = new byte[16];// 控制码CTR域
	public short _frmCTRLen;// 控制码CTR域字节数

	/**
	 * 4、地址域
	 */
	public byte[] frmMMADR = new byte[1];// 地址域
	public short _frmMMADRLen;// 地址域字节数
	public byte[]  frmMA = new byte[16];// 主站地址
	public short _frmMALen;// 主站地址占字节数
	public byte[] frmSA = new byte[16 * 8];// 从站地址
	public short _frmSALen;// 从站地址占字节数
	public short _frmSANum;// 从站地址数

	/**
	 * 5、帧序列号域
	 */
	public byte[] frmSER = new byte[1];// 帧序列号域

	/**
	 * 6、数据标识域
	 */
	public byte[] frmDI = new byte[16];// 数据标识域
	public short _frmDILen;// 数据标识域所占字节数

	/**
	 * 7、数据域
	 */
	public byte[] frmDATA = new byte[256 * 128];// 数据域
	public short _frmDATALen;// 数据域所占字节数

	/**
	 * 8、校验域
	 */
	public byte[] frmCS = new byte[2];// 校验域
	public short _frmCSLen;// 校验域占字节数

	/**
	 * 9、其他信息
	 */
	public boolean IsLLCDATA;
	LLCFrameDataInfo frmLLCDATAInfo;
	public boolean FaultANS;// 从站应答状态表示，是异常应答还是正常应答
	public boolean IsWriteStatus;// 是否为写模式
	public short curFrameType; // 0：正常帧；1：帧校验和错误；2：不完全帧；
	// QString terminalAddress;
	String recvTime;// 接收时间

	/**
	 * 接收帧处理 根据接收帧内容获取数据帧长度等信息
	 * 
	 * @param curBuffer
	 * @param curBufferLength
	 * @return
	 */
	public static FrameInfo ParameterGetFrameInfoFromRecvBuffer(
			byte[] curBuffer, int curBufferLength) {
		int curBufferPos = 0;// index
		FrameInfo frameInfo = new FrameInfo();
		frameInfo.curFrameType = 0; // 正常帧
		// 帧头
		frameInfo.frmHEAD[0] = curBuffer[curBufferPos++];
		// 校验长度
		frameInfo._frmCSLen = 2;
		// frmLLCDATAInfo
		frameInfo.frmLLCDATAInfo=new LLCFrameDataInfo();

		// 第一步：获取数据帧长度信息及整帧数据长度
		curBufferPos = GetFrameLengthFromRecvBuffer(frameInfo, curBuffer,
				curBufferPos);

		// 第二步：获取数据帧CTR信息及MMADR、MA、SA信息
		frameInfo.IsLLCDATA = false;
		frameInfo.FaultANS = true;
		curBufferPos = GetFrameCTRInfoFromRecvBuffer(frameInfo, curBuffer,
				curBufferPos);

		// 第三步：获取帧序号
		frameInfo.frmSER[0] = curBuffer[curBufferPos++];

		// 第四步：根据DI获取DI长度信息
		curBufferPos = GetDIInfoFromFrameBuffer(frameInfo, curBuffer,
				curBufferPos);
		// 第五步：获取数据域长度及数据域内容
		curBufferPos = GetFrameDATAFromRecvBuffer(frameInfo, curBuffer,
				curBufferPos);

		// 第六步：获取LLC命令中的数据格式 //Add by SWW 20121018
		GetLLCFrameDATAFromFrameDATA(frameInfo);

		// 第七歩：获取帧校验码
		frameInfo.frmCS[0] = curBuffer[curBufferPos++];

		if (frameInfo._frmCSLen >= 2) {
			frameInfo.frmCS[1] = curBuffer[curBufferPos];
		}

		return frameInfo;

	}

	/**
	 * 获取数据帧长度信息及整帧数据长度
	 * 
	 * @param frameInfo
	 *            存储处理结果的FrameInfo对象
	 * @param curBuffer
	 *            帧数据byte[]数组
	 * @param curBufferPos
	 *            将要处理的index
	 * @return 返回继续处理的index
	 */
	private static int GetFrameLengthFromRecvBuffer(FrameInfo frameInfo,
			byte[] curBuffer, int curBufferPos) {
		byte curBufferiOne = 0x00;
		byte curBufferiTwo = 0x00;
		int curOffSetPos = 0;
		boolean IsExtend = false;
		boolean IsTrueCodeLength = true;// 长度域是否为反码，默认为正常不是反码
		if (IsTrueCodeLength) {
			frameInfo.frmLEN[0] = curBuffer[curBufferPos + (curOffSetPos++)];
			curBufferiOne = frameInfo.frmLEN[0];

			if ((curBufferiOne & 0x80) > 0)// 拓展的
			{
				frameInfo.frmLEN[1] = curBuffer[curBufferPos + (curOffSetPos++)];
				curBufferiTwo = frameInfo.frmLEN[1];
				IsExtend = true;
			}
		} else {
			frameInfo.frmLEN[0] = curBuffer[curBufferPos + (curOffSetPos++)];
			curBufferiOne = (byte) (~frameInfo.frmLEN[0]);

			if ((curBufferiOne & 0x80) > 0) {
				frameInfo.frmLEN[1] = curBuffer[curBufferPos + (curOffSetPos++)];
				curBufferiTwo = (byte) (~frameInfo.frmLEN[1]);
				IsExtend = true;
			}

		}
		frameInfo._frmLENLen = (byte) curOffSetPos;

		curBufferPos += curOffSetPos;

		// 确认接收帧长度
		if (IsExtend) {
			frameInfo._frmLENSend = (short) (curBufferiOne & 0x7F);
			frameInfo._frmLENSend += curBufferiTwo * 0x80;
			frameInfo._frmLENSend += frameInfo._frmCSLen; // CS长度
		} else {
			frameInfo._frmLENSend = (short) (curBufferiOne & 0x7F);
			frameInfo._frmLENSend += frameInfo._frmCSLen;// CS长度
		}

		return curBufferPos;
	}

	/**
	 * 获取数据帧CTR信息及MMADR、MA、SA信息
	 * 
	 * @param frameInfo
	 *            存储处理结果的FrameInfo对象
	 * @param curBuffer
	 *            帧数据byte[]数组
	 * @param curBufferPos
	 *            将要处理的index
	 * @return 返回继续处理的index
	 */
	private static int GetFrameCTRInfoFromRecvBuffer(FrameInfo frameInfo,
			byte[] curBuffer, int curBufferPos) {
		// 双字节CRC校验和
		// Parameter->FrmCSLen = 2;

		byte curBufferi;
		short curOffSetPos = 0;

		do {
			curBufferi = curBuffer[curBufferPos + (curOffSetPos++)];
		} while (!((curBufferi & 0x80) > 0));

		if (!frameInfo.IsLLCDATA) {
			// CTR长度确认
			frameInfo._frmCTRLen = (byte) curOffSetPos;

			for (int i = 0; i < curOffSetPos; i++) // CTR赋值
			{
				curBufferi = curBuffer[curBufferPos + i];
				frameInfo.frmCTR[i] = curBufferi;
			}
			curBufferPos += curOffSetPos;

			curBufferi = frameInfo.frmCTR[0];

			// 无扩展控制码标识 无主站地址
			if ((curBufferi & 0x80) == 1) {
				frameInfo._frmMALen = 0;
			}

			// 有无多级地址扩展
			if ((curBufferi & 0x20) > 0) {// 无多级地址扩展
				frameInfo._frmMMADRLen = 0;
				frameInfo._frmSANum = 1;
			} else {// 有多级地址扩展
				frameInfo._frmMMADRLen = 1;

				byte curMMADR = curBuffer[curBufferPos++];
				frameInfo.frmMMADR[0] = curMMADR;

				byte curMMADRH = (byte) ((curMMADR & 0x70) / 0x10);
				byte curMMADRL = (byte) (curMMADR & 0x07);

				if (curMMADRL >= curMMADRH) {
					frameInfo._frmSANum = (short) (curMMADRL - curMMADRH + 1);
				} else {
					// ShowMessage("接收帧MMADR错误！");
					frameInfo._frmSANum = 1;
				}
			}
			// 根据地址方式及多级地址码获取SA长度
			frameInfo._frmSALen = (short) (frameInfo._frmSANum * GetFrameAddrLen((byte) (curBufferi & 0x07)));

			if (frameInfo._frmCTRLen > 1) { // 主站地址长度
				curBufferi = frameInfo.frmCTR[1];
				frameInfo._frmMALen = (short) (GetFrameAddrLen((byte) (curBufferi & 0x07)));
			}

			// 主站地址
			if (frameInfo._frmMALen > 0) {
				for (int i = 0; i < frameInfo._frmMALen; i++) {
					curBufferi = curBuffer[curBufferPos++];
					frameInfo.frmMA[i] = curBufferi;
				}
			}
			// 从站地址
			if (frameInfo._frmSALen > 0) {
				for (int i = 0; i < frameInfo._frmSALen; i++) {
					curBufferi = curBuffer[curBufferPos++];
					frameInfo.frmSA[i] = curBufferi;
				}
			}

			curBufferi = frameInfo.frmCTR[0];
		} else {
			// CTR长度确认
			frameInfo.frmLLCDATAInfo._frmCTRLen = curOffSetPos;

			for (int i = 0; i < curOffSetPos; i++) // CTR赋值
			{
				curBufferi = curBuffer[curBufferPos + i];
				frameInfo.frmLLCDATAInfo.frmCTR[i] = curBufferi;
			}
			curBufferPos += curOffSetPos;

			curBufferi = frameInfo.frmLLCDATAInfo.frmCTR[0];

			// 根据地址方式及多级地址码获取SA长度
			frameInfo.frmLLCDATAInfo._frmSALen = GetFrameAddrLen((byte) (curBufferi & 0x07));

			// 从站地址
			if (frameInfo.frmLLCDATAInfo._frmSALen > 0) {
				for (int i = 0; i < frameInfo.frmLLCDATAInfo._frmSALen; i++) {
					curBufferi = curBuffer[curBufferPos++];
					frameInfo.frmLLCDATAInfo.frmSA[i] = curBufferi;
				}
			}

			curBufferi = frameInfo.frmLLCDATAInfo.frmCTR[0];
		}

		// 判断是否为正确帧
		if (frameInfo.FaultANS) { // LLC命令情况下由主命令控制正确与否
			if ((curBufferi & 0x10) == 0) {
				frameInfo.FaultANS = true;
			} else {
				frameInfo.FaultANS = false;
			}
		}

		if ((curBufferi & 0x08) > 0) {
			frameInfo.IsWriteStatus = false;
		} else {
			frameInfo.IsWriteStatus = true;
		}
		return curBufferPos;
	}

	/**
	 * 获取地址域长度
	 * 
	 * @param curCTRAddrType
	 * @return 返回地址域长度
	 */
	private static byte GetFrameAddrLen(byte curCTRAddrType) {
		byte curAddrLen;
		switch (curCTRAddrType) {
		case 0x07:
			curAddrLen = 0;
			break;
		case 0x06:
			curAddrLen = 1;
			break;
		case 0x05:
			curAddrLen = 12;
			break;
		case 0x04:
			curAddrLen = 5;
			break;
		case 0x03:
			curAddrLen = 2;
			break;
		case 0x02:
			curAddrLen = 4;
			break;
		case 0x01:
			curAddrLen = 6;
			break;
		case 0x00:
			curAddrLen = 8;
			break;
		default:
			curAddrLen = 0;
			break;
		}
		return curAddrLen;
	}

	/**
	 * 根据接收帧内容获取数据帧DI信息
	 * 
	 * @param frameInfo
	 *            存储处理结果的FrameInfo对象
	 * @param curBuffer
	 *            帧数据byte[]数组
	 * @param curBufferPos
	 *            将要处理的index
	 * @return 返回继续处理的index
	 * @return 返回地址域长度
	 */
	private static int GetDIInfoFromFrameBuffer(FrameInfo frameInfo,
			byte[] curBuffer, int curBufferPos) {
		// 根据DI获取DI长度信息
		byte curBufferi;
		int curOffSetPos = 0;
		boolean ISExtend = false;
		int curDIDataLen;
		boolean IsSpecialCmd = false;

		int curFrmDataLen = frameInfo._frmDATALen;

		curBufferi = curBuffer[curBufferPos + (curOffSetPos++)];
		switch (curBufferi & 0xC0) {
		case 0x00:// 表2-6特殊寄存器操作
			// curFrmDataLen = 2;
			IsSpecialCmd = true;
			break;
		case 0x40:// 表2-6位寄存器操作
			curFrmDataLen = 0;
			break;
		case 0x80:// 表2-6单字节寄存器操作
			curFrmDataLen = 2;
			break;
		case 0xC0:// 表2-6DI扩展标识
			ISExtend = true;
			break;
		default:
			break;
		}

		if (ISExtend) {
			curBufferi = curBuffer[curBufferPos + (curOffSetPos++)];

			switch (curBufferi & 0xF0) {
			case 0x00:
			case 0x10:
				// 表2-7特殊寄存器操作
				// curFrmDataLen = 0;
				IsSpecialCmd = true;
				break;
			case 0x20:
			case 0x30:
				// 表2-7位寄存器操作
				curFrmDataLen = 0;
				break;
			case 0x40:
			case 0x50:
				// 表2-7单字节寄存器操作
				curFrmDataLen = 2;
				break;
			case 0x60:
			case 0x70:
				// 表2-7多字节寄存器操作
				curBufferi = curBuffer[curBufferPos + (curOffSetPos++)];
				curFrmDataLen = curBufferi; // Data LEN
				break;
			case 0x80:
				// 表2-7多字节寄存器操作（扩展） 无扩展
				curBufferi = curBuffer[curBufferPos + (curOffSetPos++)];
				curDIDataLen = curBufferi & 0x7F;
				if ((curBufferi & 0x80) > 0) {
					curBufferi = curBuffer[curBufferPos + (curOffSetPos++)];
					curDIDataLen += curBufferi * 0x80;
				}
				curFrmDataLen = curDIDataLen;
				break;
			case 0x90:
				// 表2-7多字节寄存器操作（扩展） 有扩展
				do {
					curBufferi = curBuffer[curBufferPos + (curOffSetPos++)];
				} while ((curBufferi & 0x80) > 0);// AE位为1，有扩展，为0，无扩展

				curBufferi = curBuffer[curBufferPos + (curOffSetPos++)];
				curDIDataLen = curBufferi & 0x7F;
				if ((curBufferi & 0x80) > 0)// LE位为1，有扩展
				{
					curBufferi = curBuffer[curBufferPos + (curOffSetPos++)];

					curDIDataLen += curBufferi * 0x80;
				}
				// LE位为0，有扩展
				curFrmDataLen = curDIDataLen;
				break;
			case 0xA0:
				// 表2-7寄存器块操作
				curOffSetPos += 6; // 块数N、有效长度、字节数
				break;
			case 0xB0:
				do {
					curBufferi = curBuffer[curBufferPos + (curOffSetPos++)];
				} while ((curBufferi & 0x80) > 0);// AE位为1，有扩展，为0，无扩展
				curOffSetPos += 6; // 块数N、有效长度、字节数
				break;
			default:
				curOffSetPos++;
				break;
			}
		}

		if (!frameInfo.IsLLCDATA) {
			frameInfo._frmDILen = (short) curOffSetPos;

			for (int i = 0; i < curOffSetPos; i++) {
				curBufferi = curBuffer[curBufferPos + i];
				frameInfo.frmDI[i] = curBufferi;
			}

			curBufferPos += curOffSetPos;

			frameInfo._frmDATALen = (short) curFrmDataLen;
		} else {
			frameInfo.frmLLCDATAInfo._frmDILen = (short) curOffSetPos;
			frameInfo._frmDILen = (short) curOffSetPos;
			for (int i = 0; i < curOffSetPos; i++) {
				curBufferi = curBuffer[curBufferPos + i];
				frameInfo.frmLLCDATAInfo.frmDI[i] = curBufferi;

				frameInfo.frmDI[i] = curBufferi;// 透级帧，赋给外层帧

			}
			curBufferPos += curOffSetPos;

			frameInfo.frmLLCDATAInfo._frmDATALen = (short) curFrmDataLen;

		}
		return curBufferPos;
	}

	/**
	 * 获取数据域长度及数据域内容
	 * 
	 * @param frameInfo
	 *            存储处理结果的FrameInfo对象
	 * @param curBuffer
	 *            帧数据byte[]数组
	 * @param curBufferPos
	 *            将要处理的index
	 * @return 返回继续处理的index
	 */
	private static int GetFrameDATAFromRecvBuffer(FrameInfo frameInfo,
			byte[] curBuffer, int curBufferPos) {
		byte curBufferi;

		frameInfo._frmDATALen = (short) (frameInfo._frmLENSend - frameInfo._frmLENLen);
		frameInfo._frmDATALen -= frameInfo._frmCTRLen;
		frameInfo._frmDATALen -= frameInfo._frmMMADRLen;
		frameInfo._frmDATALen -= frameInfo._frmMALen;
		frameInfo._frmDATALen -= frameInfo._frmSALen;
		frameInfo._frmDATALen -= 1; // SER
		frameInfo._frmDATALen -= frameInfo._frmDILen;
		frameInfo._frmDATALen -= frameInfo._frmCSLen; // CS

		// 地址域
		if (frameInfo._frmDATALen > 0) {
			for (int i = 0; i < frameInfo._frmDATALen; i++) {
				curBufferi = curBuffer[curBufferPos++];
				frameInfo.frmDATA[i] = curBufferi;
			}
		}
		return curBufferPos;
	}

	/**
	 * 获取LLC数据格式从数据域内容
	 * 
	 * @param frameInfo
	 *            存储处理结果的FrameInfo对象
	 */
	private static void GetLLCFrameDATAFromFrameDATA(FrameInfo frameInfo) {
		byte[] curBuffer = frameInfo.frmDATA;
		int curBufferPos = 0;
		byte curBufferi;

		if (frameInfo._frmDATALen >= 2) {
			if (frameInfo.frmDI[0] == 0x3F || frameInfo.frmDI[0] == 0x3A
					|| frameInfo.frmDI[0] == 0x38) {
				if (curBuffer[curBufferPos] == 0x24
						&& curBuffer[curBufferPos + 1] == 0x00) {
					frameInfo.IsLLCDATA = true;
					frameInfo.FaultANS = true;
				} else if (curBuffer[curBufferPos] == 0x24
						&& curBuffer[curBufferPos + 1] == 0x01) {
					frameInfo.IsLLCDATA = true;
					frameInfo.FaultANS = true;
				} else if (curBuffer[curBufferPos] == 0xCC
						&& curBuffer[curBufferPos + 1] == 0x00) {
					frameInfo.IsLLCDATA = true;
					frameInfo.FaultANS = false;
				}
			}
		}

		if (frameInfo.IsLLCDATA) {
			curBufferPos += 2;

			// 获取数据帧CTR信息及MMADR、MA、SA信息
			curBufferPos = GetFrameCTRInfoFromRecvBuffer(frameInfo, curBuffer,
					curBufferPos);

			// 帧序号
			frameInfo.frmLLCDATAInfo.frmSER[0] = curBuffer[curBufferPos++];

			// 根据DI获取DI长度信息及DATA长度
			curBufferPos = GetDIInfoFromFrameBuffer(frameInfo, curBuffer,
					curBufferPos);

			// 获取数据域
			if (frameInfo._frmDATALen > curBufferPos) {
				// 数据域长度计算
				frameInfo.frmLLCDATAInfo._frmDATALen = (short) (frameInfo._frmDATALen - curBufferPos);
				for (int i = 0; i < frameInfo.frmLLCDATAInfo._frmDATALen; i++) {
					curBufferi = curBuffer[curBufferPos++];
					frameInfo.frmLLCDATAInfo.frmDATA[i] = curBufferi;

					frameInfo.frmDATA[i] = curBufferi;// 更新数据域
				}
				frameInfo._frmDATALen = frameInfo.frmLLCDATAInfo._frmDATALen;
			} else {
				frameInfo.frmLLCDATAInfo._frmDATALen = 0;
			}
		}
	}
	
	
	/**
	 * 将FramInfo中存储的数据转成Vector，方便展示在表格中
	 * @param frameInfo
	 * @return 
	 */
	public static Vector<String> frameInfoToVector(FrameInfo frameInfo)
	{
		Vector<String> vector = new Vector<String>();
		//1添加帧定界符
		String head = BitOperator.bytesAndLenToHexString(frameInfo.frmHEAD,1);
		if(head == null)
			head = "";
		head = head.toUpperCase();
		vector.add(head);
		//2添加帧长度
		String len = BitOperator.bytesAndLenToHexString(frameInfo.frmLEN,frameInfo._frmLENLen);
		if(len == null)
			len = "";
		len = len.toUpperCase();
		vector.add(len);
		//3添加控制域
		String ctr = BitOperator.bytesAndLenToHexString(frameInfo.frmCTR,frameInfo._frmCTRLen);
		if(ctr == null)
			ctr = "";
		ctr = ctr.toUpperCase();
		vector.add(ctr);
		//4添加多级地址管理域
		String mam = BitOperator.bytesAndLenToHexString(frameInfo.frmMMADR,frameInfo._frmMMADRLen);
		if(mam == null)
			mam = "";
		mam = mam.toUpperCase();
		vector.add(mam);
		//5添加地址域 ,先添加主站，再添加从站
		String ma_address = BitOperator.bytesAndLenToHexString(frameInfo.frmMA,frameInfo._frmMALen);
		if(ma_address == null)
			ma_address = "";
		ma_address = ma_address.toUpperCase();
		String sa_address = BitOperator.bytesAndLenToHexString(frameInfo.frmSA,frameInfo._frmSALen);
		if(sa_address == null)
			sa_address = "";
		sa_address = sa_address.toUpperCase();
		String address = ma_address+sa_address;
		vector.add(address);
		//6添加帧序号域
		String ser = BitOperator.bytesAndLenToHexString(frameInfo.frmSER,1);
		if(ser == null)
			ser = "";
		ser = ser.toUpperCase();
		vector.add(ser);
		//7添加数据标识域
		String di = BitOperator.bytesAndLenToHexString(frameInfo.frmDI,frameInfo._frmDILen);
		if(di == null)
			di = "";
		di = di.toUpperCase();
		vector.add(di);
		//8添加数据域
		String data = BitOperator.bytesAndLenToHexString(frameInfo.frmDATA,frameInfo._frmDATALen);
		if(data == null)
			data = "";
		data = data.toUpperCase();
		vector.add(data);
		//9添加帧校验
		String cs = BitOperator.bytesAndLenToHexString(frameInfo.frmCS,frameInfo._frmCSLen);
		if(cs == null)
			cs = "";
		cs = cs.toUpperCase();
		vector.add(cs);
		
		return vector;
		
	}

	
	
}
