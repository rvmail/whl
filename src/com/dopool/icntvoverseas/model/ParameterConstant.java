package com.dopool.icntvoverseas.model;

/**
 * @class 参数常量类
 * @author Alisa
 * 
 */
public class ParameterConstant {
	public static final String SERISID = "seriesId";
	public static final String CATGITEMID = "catgItemId";
	public static final String ACTIONURL = "actionUrl";
	public static final String OBJECT_SERIALIZABLE = "serializable";
	public static final String COUNT_SERIES = "count";
	public static final String TYPE = "type";

	public static final String TITLE = "title";

	// 推荐位配置
	// 推荐位box长宽比
	public static final float RECOM_BOX_ASPECT_RATIO = 1.2f;
	// 推荐位box放大因数
	public static final float RECOM_BOX_ENLARGE_FACTOR = 1.1f;

	// 点播栏目列表的requestId
	public static final String CATGITEMID_VOD = "217953"; 
//			"216209";

	// 首页推荐位的requestId
	public static final int REQUESTID_RECOMMEND = 583;

	// log sdk init constant
	public static final String DATASOURCE = "4";
	// 7代表海外闪动
	public static final String FSOURCE = "7";
	// log sdk upload type
	public static final int HOME = 0;
	public static final int COLUMN = 1;
	public static final int SEARCH = 2;
	public static final int DETAIL = 3;
	public static final int COLLECT = 5;
	public static final int ONOROFF = 88;

	public static final int COLOMN_DEFAULT_OPERATE = 0;
	public static final int COLOMN_USER_OPERATE = 1;

	public static final int COLLECT_YES = 0;
	public static final int COLLECT_NO = 1;

	public static final int HOME_ENTER = 0;
	public static final int HOME_ENTER_OTHERS_FROM_RECOMMEND = 1;

	public static final int ONOROFF_ON = 0;
	public static final int ONOROFF_OFF = 1;
	// 点播播放页需要上传的type info
	public static final int PLAYER_ACTIVITY = 4;
	public static final int PLAY_START = 0; // 点播开始
	public static final int SEEK_COMPLETE = 1; // seek结束
	public static final int PAUSE_COMPLETE = 2; // 点播暂停结束
	public static final int PLAY_COMPLETE = 3; // 点播结束
	// 结束原因：0表示播放完毕，自动结束；1表示用户中断；2表示播放异常
	public static final int ENDTYPE_NORMAL = 0;
	public static final int ENDTYPE_BY_USER = 1;
	public static final int ENDTYPE_PLAY_ERROR = 2;

	public static final int NOT_SMOOTH_COMPLETE = 4; // 卡顿结束
	// public static final int SHOW_DETAIL_PAGE = 5; // 点播调出详情页
	public static final int SHOW_SERIES_LAYER = 6; // 点播调出选集页
	public static final int PLAY_NORMALLY_START = 7; // 开始正常播放视频
	public static final int SEEK_START = 11; // seek开始
	public static final int PAUSE_START = 12; // pause开始
	public static final int NOT_SMOOTH_START = 13; // 卡顿开始
	// public static final int HIDE_DETAIL_PAGE = 14; // 隐藏详情页
	public static final int HIDE_SERIES_LAYER = 15; // 隐藏选集页
	public static final int CHARGE_TYPE_FREE = 0; // 0：免费， 1：收费
	public static final int RESOLUTION_HD = 0; // 高清
	public static final int RESOLUTION_SD = 1; // 标清
	// Login info
	public static final String LOGIN_STATE = "loginState";
	public static final String USER_ID = "userId";
	public static final String DEVICE_ID = "deviceId";
	public static final String TEMPLATE_ID = "templateID";
	public static final String PLATFORM_ID = "platformId";
	public static final String TOKEN = "token";
	public static final String EPG_SERVER = "epgServer";
	public static final String SEARCH_SERVER = "searchServer";
	public static final String LOG_SERVER = "logServer";
	public static final String SNS_SERVER = "snsServer";
	public static final String DEVICE_UPDATE_SERVER = "deviceUpdateServer";
	public static final String VERSION = "version";

	// Login failed error code
	/** 终端设备未报备、服务端错误未找到该设备报备信息、报备信息被删除 */
	public static final String ERROR_CODE_000 = "000";

	/** 认证服务器崩溃 */
	public static final String ERROR_CODE_999 = "999";

	/** 设备id对应的设备被播控系统停用 */
	public static final String ERROR_CODE_250 = "250";

	/** mac地址号无效，终端没有带mac地址，或者播控系统中，这个设备id对应的mac地址和终端上报的不符 */
	public static final String ERROR_CODE_257 = "257";

	/** token服务异常，可能是MAC被盗用 */
	public static final String ERROR_CODE_260 = "260";

	/** 连接认证服务器进行激活失败，可能是DNS解析失败、用户网络异常等原因 */
	public static final String ERROR_CODE_765 = "765";

	/** 连接认证服务器进行认证失败，可能是DNS解析失败、用户网络异常等原因 */
	public static final String ERROR_CODE_766 = "766";

	/** 表示下载激活xml不完整，解析出错 */
	public static final String ERROR_CODE_776 = "776";

	/** 网络特别不好，认证时下载了一些数据，但是解析Xml无法成功 */
	public static final String ERROR_CODE_777 = "777";

	/** 表示连接EPG服务器失败 */
	public static final String ERROR_CODE_788 = "788";

	/** MAC读取失败 */
	public static final String ERROR_CODE_755 = "755";

	/** 激活时后台兑换出DeviceID为空，可能是没报备、服务端异常、域名劫持 */
	public static final String ERROR_CODE_775 = "775";

	/** ip被限制访问 */
	public static final String ERROR_CODE_255 = "255";

	// 从播放页面返回到详情页时，获取当前播放剧集数的name字符串
	public static final String NAME_POSTION_CURRENT = "postion_current_record";

}
