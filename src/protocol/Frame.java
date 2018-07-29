package protocol;

public class Frame {
	public final int MAXFRAMELEN = 200;
	public int frameLen;// 帧长度
	public byte data[];// 帧数据

	public Frame() {
		super();
		this.frameLen = 0;
		this.data = new byte[MAXFRAMELEN];
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		/*打印crc成帧*/
		String dataString ="";
		for (int i = 0; i < frameLen; i++) {
			String hex= Integer.toHexString(data[i]& 0xFF); 
			hex =hex.toUpperCase();
		     if (hex.length() == 1) {    
		       hex = '0' + hex;    
		     }  
		     dataString +=hex;
		}
		dataString +="\n";
		
		return dataString;
	} 


}
