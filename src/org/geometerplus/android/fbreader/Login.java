package org.geometerplus.android.fbreader;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {
	public String user = "stu016";
	public String[] log_Username = { user };
	public String[] log_Password = { user };
    public EditText userNameEditText,passWordEditText;  
	public String UserName;
	public Boolean login_flag = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        
        final EditText userNameEditText= (EditText) findViewById(R.id.user);
        final EditText passWordEditText= (EditText) findViewById(R.id.pass); 

        final Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
                String user= userNameEditText.getText().toString(); 
                String pass= passWordEditText.getText().toString();
                if (user.length() > 0 && pass.length() > 0){
                    for (int i=0;i<log_Username.length;i++){
                    	if (user.equals(log_Username[i]) == true && pass.equals(log_Password[i]) == true){
                    		login_flag = true;
                    		UserName=log_Username[i];
                    		break;
                    	}else{
                    		login_flag = false;
                    	}
                    }
                    
                	if (login_flag == true){
                		Intent intent = new Intent();
                    	//砞﹚肚癳把计
                    	Bundle bundle = new Bundle();
                    	bundle.putString("putUsername", UserName);
                    	intent.putExtras(bundle);	//盢把计intent
                    	//startActivityForResult(intent, 0);	//㊣page2璶―肚
                		//ち传
                    	intent.setClass(Login.this, FBReader.class);
                    	startActivity(intent);
                    	Login.this.finish();
                	}else{
                		Toast.makeText(getApplicationContext(), "块岿粇",Toast.LENGTH_SHORT).show();
                	}
                }else{
                	Toast.makeText(getApplicationContext(), "叫づフ",Toast.LENGTH_SHORT).show();
                }
			}
		});
    }
    
    
}

