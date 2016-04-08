package com.menu;

import java.util.ArrayList;
import java.util.List;

import com.manniu.manniu.R;
/**
 * 实时视频菜单列表设置
 * @author jianhua
 *
 */
public class MenuUtils {
	public static final int MENU_AUDIO=1;
	public static final int MENU_TALK=2;
	public static final int MENU_HELP=3;
	public static final int MENU_PTZ=4;
	public static final int MENU_EXIT=5;
	
	/*private static List<MenuInfo> initMenu(){
		List<MenuInfo> list=new ArrayList<MenuInfo>();
		list.add(new MenuInfo(MENU_AUDIO,"伴音",R.drawable.btn_audio0,false));
		list.add(new MenuInfo(MENU_TALK,"语音对讲",R.drawable.talk1,false));
		list.add(new MenuInfo(MENU_PTZ,"云台控制",R.drawable.btn_ptz,false));
		return list;
	}*/
	
	/**
	 * 获取当前菜单列表
	 * @param audioState 伴音、对讲   状态：  1 启用
	 * @param talkState 对讲状态
	 * @return
	 */
	public static List<MenuInfo> getMenuList(int audioState,int talkState){
		//List<MenuInfo> list=initMenu();	
		List<MenuInfo> list=new ArrayList<MenuInfo>();
		int au_state = R.drawable.btn_audio2;
		if(audioState == 1)
			au_state = R.drawable.btn_audio1;
		
		int talk_state = R.drawable.talk2;
		if(talkState == 1)
			talk_state = R.drawable.talk3;
		list.add(new MenuInfo(MENU_AUDIO,"伴音",au_state,false));
		list.add(new MenuInfo(MENU_TALK,"语音对讲",talk_state,false));
		list.add(new MenuInfo(MENU_PTZ,"云台控制",R.drawable.btn_ptz1,false));
		return list;
	}
	
}
