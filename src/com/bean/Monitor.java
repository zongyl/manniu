package com.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.basic.SoftDecoder;
import com.views.analog.camera.encode.DecoderDebugger;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Monitor implements Serializable
{
	private static final long serialVersionUID = -8818620490723629797L;

	/** 设备ID */
	public int devID = 0;
	/** 通道ID */
	public int chnID = 0;
	/** 登录状态 1:登录成功 <0:登录失败 0:未登录 */
	public int login_state = 0;
	/** 辅连接状态 1:连接成功 <0:连接失败 0:未连接 */
	public int sub_state = 0;
	/** 子连接状态 1:连接成功 <0:连接失败 0:未连接 */
	public int video_state = 0;
	/** 设备状态 */
	public int status = 0;
	/** 播放状态 1:正在播放 0:暂停播放 -1:未加载 */
	public int play_status = -1;
	/** 是否能切换通道 */
	public boolean isCanChangeChn = true;
	/** 是否在录像状态 */
	public boolean isRecord = false;
	/** 主辅码流 */
	public int isSubStream = 0;
	/** 当前流量 */
	public long videoDataFlow = 0;
	/** 当前通道的总流量 */
	public long curChnDataFlow = 0;
	/** 前6秒的数据流量 */
	public List<Long> dataFlowArray = null;
	/** 每秒当前通道的流量 */
	public long dataFlowEachSecond = 0;
	/** 通道类型 */
	public int isP2P = 0;
	/** 是否代理 */
	public int isProxy = 0;
	/** 带宽类型 */
	public int bandWidth = 0;
	/** 设备播放信息 */
	public DevCart devCart = null;
	/** 绘图视图 */
	public SurfaceView tileView = null;
	/** 绘图视图尺寸 */
	public Rect drawRect;
	/** 绘图源数据*/
	public Bitmap imageBitmap = null;
	/**是否需要截图*/
	public boolean isPicture = false;
	/** 解码器句柄 */
	public long decoder = -1;
	/**解码器互斥锁*/
	public Lock decoder_lock = null;
	/** 解码完成后的图片 */
	public byte[] imageData = null;
	/**是否加载过通道视频：1，加载；-1，未加载*/
	private int load_status;
	/**主辅码流切换按钮显示状态*/
	private int displayStream=1;
	/**硬解码器*/
	private DecoderDebugger avdecoder;
	/**h265宽*/
	private int codecwidth=0;
	/**h265高*/
	private int codecheight=0;
	/**设备类型 12:h265,2:h264*/
	private int codectype=0;
	/**是否已经初始化*/
	private int codecinit=0;
	/**是否已停止*/
	private boolean isStop=false;
	/**记录H265解码通道*/
	private int decPort=-1;	
	/**支持截图*/
	private boolean canShot = true;
	/**支持语音对讲*/
	private boolean canTalk = true;
	/**支持云台*/
	private boolean canPTZ = true;
	/**支持录像*/
	private boolean canRecord = true;	
	/**支持切换码流*/
	private boolean canSwitchStream = true;
	/**软解处理,暂时只用于MN的H265支持*/
	private SoftDecoder softDecoder;
	
	public void setSoftDecoder(SoftDecoder softDecoder){
		this.softDecoder=softDecoder;
		this.softDecoder.SetMonitor(this);
	}

	public void setSoftDecoderSureFaceHolder(SurfaceHolder sf) {
		if (this.softDecoder != null) {
			this.softDecoder.SetSurfaceHolder(sf);
		}
	}

	public SoftDecoder getSoftDecoder() {
		if (softDecoder == null) {
			softDecoder = new SoftDecoder();
			//softDecoder.StartDecoder();
			softDecoder.SetMonitor(this);
		}
		return softDecoder;
	}
	
	public void clearSoftDecoder(){
		softDecoder = null;
	}
	
	public boolean getCanShot()
	{
		return canShot;
	}
	public boolean getCanTalk()
	{
		return canTalk;
	}
	public boolean getCanPTZ()
	{
		return canPTZ;
	}
	public boolean getCanRecord()
	{
		return canRecord;
	}
	public boolean getCanSwitchStream()
	{
		return canSwitchStream;
	}
	
	
	public void setCanShot(boolean blnCanShot)
	{
		canShot = blnCanShot;
	}
	public void setCanTalk(boolean blnCanTalk)
	{
		canTalk = blnCanTalk;
	}
	public void setCanPTZ(boolean blnCanPTZ)
	{
		canPTZ = blnCanPTZ;
	}
	public void setCanRecord(boolean blnCanRecord)
	{
		canRecord = blnCanRecord;
	}
	public void setCanSwitchStream(boolean blnCanSwitchStream)
	{
		canSwitchStream = blnCanSwitchStream;
	}
	
	public boolean isStop() {
		return isStop;
	}
	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}
	public int getDisplayStream(){
		return displayStream;
	}
	public void setDisplayStream(int displayStream){
		this.displayStream=displayStream;
	}
	
	public void setAVDecoder(DecoderDebugger avdecoder){
		this.avdecoder=avdecoder;
	}
	public DecoderDebugger getAVDecoder(){
		return avdecoder;
	}

	public void stopSoftDecoder() {
		if (softDecoder != null) {
			softDecoder.StopDecoder();
		}
	}
	public int getDevID()
	{
		return devID;
	}

	public void setDevID(int devID)
	{
		this.devID = devID;
	}

	public int getChnID()
	{
		return chnID;
	}

	public void setChnID(int chnID)
	{
		this.chnID = chnID;
	}

	public int getLogin_state()
	{
		return login_state;
	}

	public void setLogin_state(int login_state)
	{
		this.login_state = login_state;
	}

	public int getSub_state()
	{
		return sub_state;
	}

	public void setSub_state(int sub_state)
	{
		this.sub_state = sub_state;
	}

	public int getVideo_state()
	{
		return video_state;
	}

	public void setVideo_state(int video_state)
	{
		this.video_state = video_state;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public int getPlay_status()
	{
		return play_status;
	}

	public void setPlay_status(int play_status)
	{
		this.play_status = play_status;
	}

	public boolean isCanChangeChn()
	{
		return isCanChangeChn;
	}

	public void setCanChangeChn(boolean isCanChangeChn)
	{
		this.isCanChangeChn = isCanChangeChn;
	}

	public boolean isRecord()
	{
		return isRecord;
	}

	public void setRecord(boolean isRecord)
	{
		this.isRecord = isRecord;
	}

	public int getIsSubStream()
	{
		return isSubStream;
	}

	public void setIsSubStream(int isSubStream)
	{
		this.isSubStream = isSubStream;
	}

	public long getVideoDataFlow()
	{
		return videoDataFlow;
	}

	public void setVideoDataFlow(long videoDataFlow)
	{
		this.videoDataFlow = videoDataFlow;
	}

	public long getCurChnDataFlow()
	{
		return curChnDataFlow;
	}

	public void setCurChnDataFlow(long curChnDataFlow)
	{
		this.curChnDataFlow = curChnDataFlow;
	}

	public List<Long> getDataFlowArray()
	{
		return dataFlowArray;
	}

	public void setDataFlowArray(List<Long> dataFlowArray)
	{
		this.dataFlowArray = dataFlowArray;
	}

	public long getDataFlowEachSecond()
	{
		return dataFlowEachSecond;
	}

	public void setDataFlowEachSecond(long dataFlowEachSecond)
	{
		this.dataFlowEachSecond = dataFlowEachSecond;
	}

	public int getIsP2P()
	{
		return isP2P;
	}

	public void setIsP2P(int isP2P)
	{
		this.isP2P = isP2P;
	}

	public int getIsProxy()
	{
		return isProxy;
	}

	public void setIsProxy(int isProxy)
	{
		this.isProxy = isProxy;
	}

	public int getBandWidth()
	{
		return bandWidth;
	}

	public void setBandWidth(int bandWidth)
	{
		this.bandWidth = bandWidth;
	}

	public DevCart getDevCart()
	{
		return devCart;
	}

	public void setDevCart(DevCart devCart)
	{
		this.devCart = devCart;
	}

	public SurfaceView getTileView()
	{
		return tileView;
	}

	public void setTileView(SurfaceView tileView)
	{
		this.tileView = tileView;
	}

	public Rect getDrawRect()
	{
		return drawRect;
	}

	public void setDrawRect(Rect drawRect)
	{
		this.drawRect = drawRect;
	}

	public Bitmap getImageBitmap()
	{
		return imageBitmap;
	}

	public void setImageBitmap(Bitmap imageBitmap)
	{
		this.imageBitmap = imageBitmap;
	}

	public boolean isPicture()
	{
		return isPicture;
	}

	public void setPicture(boolean isPicture)
	{
		this.isPicture = isPicture;
	}

	public long getDecoder()
	{
		return decoder;
	}

	public void setDecoder(long decoder)
	{
		this.decoder = decoder;
	}

	public Lock getDecoder_lock()
	{
		return decoder_lock;
	}

	public void setDecoder_lock(Lock decoder_lock)
	{
		this.decoder_lock = decoder_lock;
	}

	public byte[] getImageData()
	{
		return imageData;
	}

	public void setImageData(byte[] imageData)
	{
		this.imageData = imageData;
	}

	public Monitor()
	{
		devID = 0;
		chnID = 0;
		login_state = 0;
		sub_state = 0;
		video_state = 0;
		status = 0;
		play_status = -1;
		isCanChangeChn = true;
		isRecord = false;
		isSubStream = 0;
		videoDataFlow = 0;
		curChnDataFlow = 0;
		dataFlowArray = new ArrayList<Long>();
		dataFlowEachSecond = 0;
		isP2P = 0;
		isProxy = 0;
		bandWidth = 0;
		devCart = null;
		tileView = null;
		imageBitmap = null;
		isPicture = false;
		decoder = -1;
		decoder_lock = new ReentrantLock();
		imageData = null;
		load_status=-1;
		canShot = true;
		canTalk = true;
		canPTZ = true;
		canRecord = true;
		canSwitchStream = true;
	}

	public Monitor(int devID, int chnID, int login_state, int sub_state, int video_state, int status, int play_status, boolean isCanChangeChn, boolean isRecord, int isSubStream, long videoDataFlow,
			long curChnDataFlow, List<Long> dataFlowArray, long dataFlowEachSecond, int isP2P, int isProxy, int bandWidth, DevCart devCart, SurfaceView tileView, Rect drawRect, Bitmap imageBitmap,
			boolean isPicture, long decoder, Lock decoder_lock, byte[] imageData,boolean blnCanShot,boolean blnCanTalk,boolean blnCanPTZ,boolean blnCanRecord,boolean blnCanSwitchStream)
	{
		super();
		this.devID = devID;
		this.chnID = chnID;
		this.login_state = login_state;
		this.sub_state = sub_state;
		this.video_state = video_state;
		this.status = status;
		this.play_status = play_status;
		this.isCanChangeChn = isCanChangeChn;
		this.isRecord = isRecord;
		this.isSubStream = isSubStream;
		this.videoDataFlow = videoDataFlow;
		this.curChnDataFlow = curChnDataFlow;
		this.dataFlowArray = dataFlowArray;
		this.dataFlowEachSecond = dataFlowEachSecond;
		this.isP2P = isP2P;
		this.isProxy = isProxy;
		this.bandWidth = bandWidth;
		this.devCart = devCart;
		this.tileView = tileView;
		this.drawRect = drawRect;
		this.imageBitmap = imageBitmap;
		this.isPicture = isPicture;
		this.decoder = decoder;
		this.decoder_lock = decoder_lock;
		this.imageData = imageData;
		this.canShot = blnCanShot;
		this.canTalk = blnCanTalk;
		this.canPTZ = blnCanPTZ;
		this.canRecord = blnCanRecord;
		this.canSwitchStream = blnCanSwitchStream;
	}

	public void resetMonitor()
	{
		devID = 0;
		chnID = 0;
		login_state = 0;
		sub_state = 0;
		video_state = 0;
		status = 0;
		play_status = -1;
		isCanChangeChn = true;
		isRecord = false;
		videoDataFlow = 0;
		curChnDataFlow = 0;
		dataFlowArray = new ArrayList<Long>();
		dataFlowEachSecond = 0;
		isP2P = 0;
		isProxy = 0;
		bandWidth = 0;
		devCart = null;
		isPicture = false;
		decoder = -1;
		decoder_lock =new ReentrantLock();;
		canShot = true;
		canTalk = true;
		canPTZ = true;
		canRecord = true;
		canSwitchStream = true;
	}
	public void setLoad_status(int load_status){
		this.load_status=load_status;
	}
	public int getLoad_status(){
		return load_status;
	}
	public int getCodecwidth() {
		return codecwidth;
	}
	public int getCodecheight() {
		return codecheight;
	}
	public int getCodectype() {
		return codectype;
	}
	public int getCodecinit() {
		return codecinit;
	}
	public int getDecPort() {
		return decPort;
	}
	public void setCodecwidth(int codecwidth) {
		this.codecwidth = codecwidth;
	}
	public void setCodecheight(int codecheight) {
		this.codecheight = codecheight;
	}
	public void setCodectype(int codectype) {
		this.codectype = codectype;
	}
	public void setCodecinit(int codecinit) {
		this.codecinit = codecinit;
	}
	public void setDecPort(int decPort) {
		this.decPort = decPort;
	}
	
}
