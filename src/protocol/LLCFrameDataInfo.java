package protocol;

public class LLCFrameDataInfo {
	public byte[]  frmCTR =new byte[16];
    public short  _frmCTRLen;
    public byte[]  frmSA = new byte[16];
    public short   _frmSALen;
    public byte[]  frmSER =  new byte[1];
    public byte[]  frmDI	= new byte[16];
    public short  _frmDILen;
    public byte[]  frmDATA =new byte[256*128];
    public short  _frmDATALen;
}
