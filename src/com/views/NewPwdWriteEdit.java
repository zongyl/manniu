package com.views;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.basic.APP;
import com.manniu.manniu.R;

public class NewPwdWriteEdit extends Activity implements OnClickListener{
	String _newPwd = "";
	String _oldPwd = "";
	ArrayList<String> msgs = new ArrayList<String>();
	protected void onCreate(Bundle savedInstanceState) {
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_pwd_write_edit);
        //监听文本输入状态
        EditText et = (EditText) findViewById(R.id.pwd_write);
        
        
        et.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() == 0){
						findViewById(R.id.del_edit).setVisibility(View.GONE);
						//Log.v("隐藏删除键", ""+findViewById(R.id.del_edit).getVisibility());
					}else{
						findViewById(R.id.del_edit).setVisibility(View.VISIBLE);
					}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
        findViewById(R.id.cancel_edit).setOnClickListener(this);
        findViewById(R.id.next_edit).setOnClickListener(this);
        findViewById(R.id.del_edit).setOnClickListener(this);
        _oldPwd = this.getIntent().getStringExtra("oldPwd");
        msgs.add(_oldPwd);
		
	}
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.del_edit:
			((EditText)findViewById(R.id.pwd_write)).setText("");
			break;
		case R.id.next_edit:
			_newPwd = ((EditText) findViewById(R.id.pwd_write)).getText().toString();
			Log.v("输入的新密码",_newPwd );
			if("".equals(_newPwd)){
				APP.ShowToast(getString(R.string.pwd_newEmpty));
			}else if(_newPwd.equals(_oldPwd)){
				APP.ShowToast(getString(R.string.pwd_newidentical));
			}else{
				Log.v("-转向确认密码", _newPwd);
				msgs.add(_newPwd);
				forward(msgs,NewPwdConfirmEdit.class);
				finish();
			}
			break;
			
		case R.id.cancel_edit:
			finish();
			break;
		}
	}
	private void forward(ArrayList<String> name,Class<?> target) {
		Intent intent =new Intent(this,target);
		Bundle b = new Bundle();
		b.putStringArrayList("pwd", name);
		intent.putExtras(b);
		startActivity(intent);
	}
	
	public void onBackPressed(){
		finish();
		super.onBackPressed();
	}
}
