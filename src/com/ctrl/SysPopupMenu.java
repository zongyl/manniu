package com.ctrl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.basic.APP;
import com.basic.XMSG;
import com.manniu.manniu.R;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.views.analog.camera.encode.AnalogvideoActivity;

import P2P.SDK;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * 主界面下拉菜单  重定义 popupMenu
 */
@SuppressWarnings("unused")
public class SysPopupMenu {

    private Context mContext;
    private LayoutInflater mInflater;
    private WindowManager mWindowManager;
    private PopupWindow mPopupWindow;
	private View mContentView;
    private ListView mItemsView;
    private TextView mHeaderTitleView;
    private OnClickListener mListener;

    private List<MenuItem> mItems;
    private int mWidth = 240;
    private float mScale;

    public SysPopupMenu(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScale = metrics.scaledDensity;

        mItems = new ArrayList<MenuItem>();

        mPopupWindow = new PopupWindow(context);
        mPopupWindow.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mPopupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });

        setContentView(mInflater.inflate(R.layout.analog_popup_menu, null));
    }

    /**
     * Sets the popup's content.
     * @param contentView
     */
    private void setContentView(View contentView) {
        mContentView = contentView;
        mItemsView = (ListView) contentView.findViewById(R.id.items);
        mHeaderTitleView = (TextView) contentView.findViewById(R.id.header_title);
        mPopupWindow.setContentView(contentView);
    }

    /**
     * Add menu item.
     * @param itemId
     * @param titleRes
     * @param iconRes
     * @return item
     */
    public MenuItem add(int itemId, int titleRes) {
        MenuItem item = new MenuItem();
        item.setItemId(itemId);
        item.setTitle(mContext.getString(titleRes));
        mItems.add(item);

        return item;
    }

    /**
     * Show popup menu.
     */
    public void show() {
        show(null);
    }

    /**
     * Show popup menu.
     * @param view  anchor
     * 显示菜单并注册点击事件
     */
    public void show(View anchor) {
        if (mItems.size() == 0) {
            throw new IllegalStateException("PopupMenu#add was not called with a menu item to display.");
        }
        preShow();
        MenuItemAdapter adapter = new MenuItemAdapter(mContext, mItems);
        mItemsView.setAdapter(adapter);
        mItemsView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
                if (mListener != null) {
                	if(mItems.get(position).getItemId() == 0){
                		//如果正在采集提示 并不跳转
                		if(SDK._sessionIdContext != 0){
                			AnalogvideoActivity.instance.stopEncode();
                			SDK._sessionIdContext = 0; //清空连接数
                			AnalogvideoActivity.instance.finish();
                		}else{
                			APP.ShowToast(mContext.getString(R.string.exit_monitor_alert));
                		}
                	}
                }
                mPopupWindow.dismiss();
            }
        });
        if (anchor == null) {
            View parent = ((Activity)mContext).getWindow().getDecorView();
            mPopupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
            return;
        }
       // mPopupWindow.showAsDropDown(anchor);//在控制下方
        
        int xPos, yPos;
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        Rect anchorRect = new Rect(location[0], location[1],
                location[0] + anchor.getWidth(),
                location[0] + anchor.getHeight());
        mContentView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mContentView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int rootHeight = mContentView.getMeasuredHeight();
        int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
        xPos = anchorRect.centerX() - mPopupWindow.getWidth() / 2;
        int dyTop = anchorRect.top;
        int dyBottom = screenHeight + rootHeight;
        boolean onTop = dyTop > dyBottom;
        if (onTop) {
            yPos = anchorRect.top - rootHeight;
        } else {
            if (anchorRect.bottom > dyTop) {
                yPos = anchorRect.bottom - 20;
            } else {
                yPos = anchorRect.top - anchorRect.bottom + 50;
            }
        }
        //mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
//        int[] location2 = new int[2];  
//        anchor.getLocationOnScreen(location2);   //location[1]-mPopupWindow.getHeight()
        mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, location[0], location[1]-100);  //在控制上方
           
    }

    private void preShow() {
        int width = (int) (mWidth * mScale);
        mPopupWindow.setWidth(width);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        Resources resources = mContext.getResources();  
        Drawable d = resources.getDrawable(R.color.bk_menu);
        mPopupWindow.setBackgroundDrawable(d);
        //背景图片
//        mPopupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bk_menu));
    }

    /**
     * Dismiss the popup menu.
     */
    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    /**
     * Sets the popup menu header's title.
     *
     * @param title
     */
    public void setHeaderTitle(CharSequence title) {
        mHeaderTitleView.setText(title);
        mHeaderTitleView.setVisibility(View.VISIBLE);
        mHeaderTitleView.requestFocus();
    }

    /**
     * Change the popup's width.
     *
     * @param width
     */
    public void setWidth(int width) {
        mWidth = width;
    }

    /**
     * Register a callback to be invoked when an item in this PopupMenu has
     * been selected.
     *
     * @param onClickListener
     */
    public void setOnItemSelectedListener(OnClickListener onClickListener) {
        mListener = onClickListener;
    }
    

    /**
     * Interface definition for a callback to be invoked when
     * an item in this PopupMenu has been selected.
     */
    public interface OnItemSelectedListener {
        public void onItemSelected(MenuItem item);
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
    }

    private class MenuItemAdapter extends ArrayAdapter<MenuItem> {

        public MenuItemAdapter(Context context, List<MenuItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.menu_list_item, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MenuItem item = getItem(position);
            if (item.getIcon() != null) {
                holder.icon.setImageDrawable(item.getIcon());
                holder.icon.setVisibility(View.VISIBLE);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
            holder.title.setText(item.getTitle());

            return convertView;
        }
    }
}
