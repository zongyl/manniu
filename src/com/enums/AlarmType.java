package com.enums;

//报警类型
public enum AlarmType {
//	#define ALARM_ALL								0		/*!< 全部 */
//	#define ALARM_VIDEO_BEGIN						1		/*!< 视频分析报警起始值 */
//	#define ALARM_VIDEO_LOST						2		/*!< 视频丢失 */
//	#define ALARM_MOTION_DETECT						3		/*!< 移动侦测 */
//	#define ALARM_VIDEO_SHELTER						4		/*!< 视频遮挡 */
//	#define ALARM_INTELLECT_BEGIN					100		/*!< 视频智能分析报警起始 */
//	#define ALARM_ENVIOROMENT_ANALOG				1000    /*!< 动力环境报警 */
//	#define ALARM_SWTICH_ALARM						2000	/*!< 开关量报警 */
//	#define ALARM_DOOR_BEGIN						3000	/*!< 门禁报警起始值 */
//	#define ALARM_DEVICE_EXCEPTION					99999	/*!< 设备故障 */
//	#define ALARM_LOGIC_BEGIN						100000	/*!< 智能报警起始值,此后的生成报警类型在此基础上递增 */
	
	TYPE_0(0,"全部"),
	TYPE_1(1,"视频分析报警"),
	TYPE_2(2,"视频丢失"),
	TYPE_3(3,"移动侦测"),
	TYPE_4(4,"视频遮挡"),
	TYPE_100(100,"视频智能分析报警"),
	TYPE_1000(1000,"动力环境报警"),
	TYPE_2000(2000,"开关量报警"),
	TYPE_3000(3000,"门禁报警"),
	TYPE_99999(99999,"设备故障"),
	TYPE_100000(100000,"智能报警");
	private int _type;
	private String _desc;
	/**
	* <p>Title: alarmType</p>
	* <p>Description: 私有构造函数</p>
	* @param type  	int  类型值
	* @param desc  String 说明
	*/
	private AlarmType(int type,String desc){
		_type = type;
		_desc = desc;
	}
	public int get_type() {
		return _type;
	}
	public String get_desc() {
		return _desc;
	}
	
	
	

}
