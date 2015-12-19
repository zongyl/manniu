package com.views.bovine;
/**
 * @author: li_jianhua Date: 2015-6-13 上午10:25:58
 * To change this template use File | Settings | File Templates.
 * Description：本地视频
 */

import java.io.File;

import com.basic.APP;
import com.localmedia.Fun_RecordplayActivity_MediaPlayer;
import com.localmedia.XListViewRewrite;
import com.localmedia.XVideoAdapter;
import com.localmedia.XListViewRewrite.DelButtonClickListener;
import com.manniu.manniu.R;
import com.views.Fun_InitMedia;
import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.app.Fragment;  
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;  
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
  
@SuppressLint("NewApi")
public class Fra_VideoActivity extends Fragment implements TextWatcher,DelButtonClickListener{  
  
	private XListViewRewrite mListView;
	public XVideoAdapter _adapter;
	EditText _etQuery;					// 查询条件输入框
	ImageButton _btnQuery;	// 查询清除，查询按钮
	Context context = null;
	 
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
    	
    	View view = inflater.inflate(R.layout.media_tab_video, null);
    	context = this.getActivity().getApplicationContext();
    	_etQuery = (EditText) view.findViewById(R.id.etQuery);
		_btnQuery = (ImageButton) view.findViewById(R.id.btnQuery);
		_btnQuery.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("static-access")
			@Override
			public void onClick(View v) {
				((InputMethodManager) APP.GetMainActivity().getSystemService(APP.GetMainActivity().INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(_etQuery.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				initData();
			}
		});
//		((EditText) view.findViewById(R.id.etQuery)).addTextChangedListener(this);
		mListView = (XListViewRewrite) view.findViewById(R.id.listView1);
		_adapter = new XVideoAdapter(this.getActivity());
		mListView.setAdapter(_adapter);
		initData();
		mListView.setDelButtonClickListener(this);
		mListView.setCacheColorHint(0);// 拖动时避免出现黑条 
		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				System.out.println(position+"--"+id);
			}
	    });
		Fun_InitMedia.currentTab = 0;
		
		return view;
    }  
    
    
    public void initData(){
		String query = _etQuery.getText().toString();
		boolean bQuery = !query.equals("");
		String dir = Fun_AnalogVideo.RecordPath;
		String fileType = ".mp4";
		_adapter.UpdateList(dir, fileType,query,bQuery);
	}
    
    


	@Override
	public void clickHappend(int position, Object obj) {
		try {
			final String path = (String) obj;
			new AlertDialog.Builder(Fra_VideoActivity.this.getActivity()).setTitle(APP.GetString(R.string.tip_title)).setMessage(APP.GetString(R.string.del_chose_ask)).setIcon(R.drawable.help)
			.setPositiveButton(APP.GetString(R.string.confirm), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
						_adapter.deleteSeletect(path);
						initData();
				}
			}).setNegativeButton(APP.GetString(R.string.cancel),null).show();
		} catch (Exception e) {
			System.out.println(1111);
			//LogUtil.e("Fra_videoActivity", ExceptionsOperator.getExceptionInfo(e));
		}
		
	}
	
	@Override
    public void onDestroy(){
		super.onDestroy();
		XListViewRewrite.dismissPopWindow();
    }
	
	
	@Override
	public void playHappend(int position, Object obj) {
		try {
			final String path = (String) obj;
			File file = new File(path);
			if(file.exists()){
				long fsize = file.length();
				if(fsize < 102400){
					APP.ShowToast(getString(R.string.smalfile_tip));
					return;
				}
			}
			Intent intent = new Intent(this.getActivity(), Fun_RecordplayActivity_MediaPlayer.class);
			intent.putExtra("fileName", path);
			this.getActivity().startActivity(intent);
		} catch (Exception e) {
		}
	}

	@Override
	public void afterTextChanged(Editable arg0) {
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
	}
    
    
  
}  
