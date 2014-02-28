package sapphire.appexamples.jeffsdocs.glue;

import com.example.jeffsdocs.R;

import sapphire.app.AndroidSapphireActivity;
import sapphire.app.SapphireActivity;
import sapphire.app.SapphireObject;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import sapphire.appexamples.jeffsdocs.app.*;
import sapphire.common.AppObjectStub;
import sapphire.oms.OMSServer;
import sapphire.runtime.SapphireActivityStarter;

public class MainActivity extends AndroidSapphireActivity {
    public static final String EXTRA_MESSAGE = "";
    public static MainActivity mainAct;
    
    public static UserManager um;
    public static Doc curDoc;
    public static User curUser;
    
    public static boolean updating = false;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mainAct = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		um = (UserManager)DoCall.doNow(this, Action.getAppEntry);
		if(um == null)
			throw new NullPointerException();
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); //TODO: don't do this preferably
		StrictMode.setThreadPolicy(policy); 
		
		this.Login();
		
		//set up document selection
        final Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        final List<String> list = new ArrayList<String>();
        final Map<String,Doc> docStore = new HashMap<String,Doc>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,list);
        spinner.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(curUser == null) {
					list.clear();
					adapter.notifyDataSetChanged();
					docStore.clear();
				} else {
					list.clear();
					docStore.clear();
					Set<Data> offered = (Set<Data>) DoCall.doNow(curUser,Action.getOfferedData);
					for(Data d : offered)
						DoCall.doNow(curUser,Action.acceptData,d);
					Set<Data> docs = (Set<Data>) DoCall.doNow(curUser,Action.getData);
					for(Data d : docs) {
						String name = (String)DoCall.doNow(d,Action.toString);
						list.add(name);
						docStore.put(name, (Doc)d);
					}
					adapter.notifyDataSetChanged();
				}
				return false;
			}
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				curDoc = (Doc)docStore.get(parent.getSelectedItem());
				mainAct.setTitle("JeffsDocs: " + DoCall.doNow(curUser,Action.toString) +" - " + DoCall.doNow(curDoc,Action.toString));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				curDoc = null;
			}
		});
        spinner.setAdapter(adapter);
        
        //New button
        final Button newBut = (Button) findViewById(R.id.button1);
        newBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(mainAct);
				dialog.setTitle("New document with name... ");
				dialog.setContentView(R.layout.share);
				final EditText text = (EditText)dialog.findViewById(R.id.dialogText1);
				text.setText("");
				text.setHint("Document Name");

				Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
				dialogButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(curUser != null)
							curDoc = (Doc)DoCall.doNow(curUser,Action.newDoc,text.getText().toString());
						dialog.dismiss();
					}
				});
				Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
				dialogButtonCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
        
        //share button
        final Button shareBut = (Button) findViewById(R.id.button2);
        shareBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(curDoc == null)
					return;
				final Dialog dialog = new Dialog(mainAct);
				dialog.setTitle("Share " + DoCall.doNow(curDoc,Action.toString) + " with...");
				dialog.setContentView(R.layout.share);
				final EditText text = (EditText)dialog.findViewById(R.id.dialogText1);
				text.setText("");
				text.setHint("Username");

				Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
				dialogButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(um != null)
							DoCall.start(um,Action.shareData,text.getText().toString(),curUser,curDoc, Data.Permissions.SHARE);
						dialog.dismiss();
					}
				});
				Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
				dialogButtonCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
        
        //login button
        final Button logoutBut = (Button) findViewById(R.id.button3);
        logoutBut.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		list.clear();
        		adapter.notifyDataSetChanged();
        		docStore.clear();
				mainAct.Login();
			}
		});
        
        //main text area
        final JeffsEditText text = (JeffsEditText) findViewById(R.id.textView1);
        text.setVerticalScrollBarEnabled(true);
        text.addTextChangedListener(new TextWatcher() {
        	String lastEdit = "";
			@Override
			public void afterTextChanged(Editable arg0) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				if(curDoc == null || curUser == null) {
					//mainAct.setTitle("NO USER OR DOC");
				} else {
					mainAct.setTitle(updating +" "+s.toString());
					if(!updating) {
						DoCall.doNow(curDoc,Action.highlight,curUser,0,text.getText().toString().length());
						DoCall.doNow(curDoc,Action.insert,curUser,s.toString());
					}
				}
			}
        });
        /*text.setOnEditorActionListener(new OnEditorActionListener(){
        	private String buffer = "";
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(curDoc == null || curUser == null) {
					mainAct.setTitle("NO USER OR DOC");
				} else {
					//DoCall.doNow(curDoc,Action.insert,curUser,"");
					mainAct.setTitle(Character.toString((char)event.getUnicodeChar()));
					buffer += Character.toString((char)event.getUnicodeChar());
					if(buffer.length() > 3) {
						DoCall.doNow(curDoc,Action.insert,curUser,buffer);
						buffer = "";
					}
				}
				return true; //say we don't handle it, to act optimistic
			}
        });*/
        
        final Handler handler = new Handler(); 
        final Runnable updater = new Runnable() { 
            public void run() {
                text.update();
                handler.postDelayed(this, 100);
           }
        };
        handler.postDelayed(updater, 10);
	}
	
	public void Login() {
		final MainActivity mainAct = this;
		final Dialog dialog = new Dialog(this);
		dialog.setCanceledOnTouchOutside(false); 
		dialog.setCancelable(false);
		dialog.setTitle("Login");
		dialog.setContentView(R.layout.login);
		final EditText name = (EditText)dialog.findViewById(R.id.userNameText);
		final EditText pass = (EditText)dialog.findViewById(R.id.passWordText);
		
		Button dialogLogin = (Button) dialog.findViewById(R.id.dialogButtonLogin);
		dialogLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(um == null) {
					return;
				}
				User user = (User)DoCall.doNow(um,Action.login,name.getText().toString(), pass.getText().toString());
				if(user != null) {
					MainActivity.curUser = user;
					dialog.dismiss();
					mainAct.setTitle("JeffsDocs: " + DoCall.doNow(curUser,Action.toString));
				} 
			}
		});
		Button dialogCreate = (Button) dialog.findViewById(R.id.dialogButtonCreate);
		dialogCreate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(um == null) {
					name.setText("NO UM");
					return;
				}
				User user = (User)DoCall.doNow(um,Action.createUser,name.getText().toString(), pass.getText().toString());
				if(user != null) {
					MainActivity.curUser = user;
					dialog.dismiss();
					mainAct.setTitle("JeffsDocs: " + DoCall.doNow(curUser,Action.toString));
				 }else {
					name.setText("FAILURE");
				}
			}
		});
		dialog.show();
	}

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/
    
    public static class JeffsEditText extends EditText {
    	
    	private Map<String,Doc.Position> cursors = new HashMap<String,Doc.Position>();
    	private float height;
    	private Paint p = new Paint();
    	
    	public void update() {
    		if(MainActivity.curDoc == null || MainActivity.curUser == null) {
    			this.setText("No Document");
    			this.setEnabled(false);
    		} else {
    			this.setEnabled(true);
    			updating = true;
    			
    			DoCall callerText = DoCall.start(MainActivity.curDoc,Action.read,MainActivity.curUser);
    			DoCall callerCursor = DoCall.start(MainActivity.curDoc,Action.getCursors,MainActivity.curUser);
    			DoCall callerUser = DoCall.start(MainActivity.curUser,Action.toString);
    			
    			try {
    			this.setText((String)callerText.get());
    			cursors = (Map<String,Doc.Position>)callerCursor.get();
    			Doc.Position pos = cursors.get(callerUser.get());
    			this.setSelection(pos.start, pos.end);
    			} catch(Exception e) {}
    			
    			
    			updating = false;
    		}
    	}
    	
    	@Override
    	protected void onSelectionChanged(int selStart, int selEnd) {
    		super.onSelectionChanged(selStart, selEnd);
    		if(MainActivity.curDoc == null || MainActivity.curUser == null) {
    			
    		} else {
    			if(!updating)
    				DoCall.doNow(MainActivity.curDoc,Action.highlight,MainActivity.curUser, selStart, selEnd);
    		}
    	}
    	@Override
    	protected void onDraw(Canvas canvas) {
    		//TODO: doesn't support showing highlight of others (just shows as if cursor at start)
    		super.onDraw(canvas);
            for(String u : cursors.keySet()) {
            	p.setARGB(128, 255, 0, 0); //TODO: all users should get some personal color
            	Doc.Position pos = cursors.get(u);
            	float x = getCursorX(pos.start);
                float y = getCursorY(pos.start);
                //canvas.drawRect(x, y+height,x+1, y, p);
                canvas.drawLine(x, y, x, y+height, p);
            }
    	}
    	
		public float getCursorX(int pos){
			Layout layout = this.getLayout();
			float x = layout.getPrimaryHorizontal(pos);
			return x + this.getPaddingLeft();
		}
		
		public float getCursorY(int pos) {
			Layout layout = this.getLayout();
			int line = layout.getLineForOffset(pos);
			height = layout.getLineAscent(line);
			float y = layout.getLineBaseline(line) + this.getPaddingTop();
			return y;
		}
    	
    	public JeffsEditText(Context context) {super(context);}
    	public JeffsEditText(Context context, AttributeSet attrs) {super(context, attrs);}
    	public JeffsEditText(Context context, AttributeSet attrs, int defStyle) {super(context, attrs, defStyle);}

    }
    
    public enum Action {
    	//user
    	getOfferedData, acceptData, getData, newDoc, 
    	//um
    	shareData, login, createUser,
    	//doc
    	insert, highlight, read, getCursors,
    	//general
    	toString,
    	getAppEntry
    }
    
    public static class DoCall extends AsyncTask<Object, Void, Object> {
    	
    	public static Object doNow(Object... args) {
    		try {
				return new DoCall().execute(args).get();
			} catch (Exception e) {
				Log.v("JeffsDocs", "had exception: " + e);
				return null;
			}
    	}
    	
    	public static DoCall start(Object... args) {
    		try {
				DoCall out = new DoCall();
				out.execute(args);
				return out;
			} catch (Exception e) {
				//TODO: shouldn't do this
				Log.v("JeffsDocs", "had exception: " + e);
				return null;
			}
    	}

		@Override
		protected Object doInBackground(Object... args) {
			switch((Action)args[1]) {//determine method
		    	//user
				case getOfferedData: return ((User)args[0]).getOfferedData();
				case acceptData: ((User)args[0]).acceptData((Data)args[2]); return null;
				case getData: return ((User)args[0]).getData();
				case newDoc: return ((User)args[0]).newDoc((String)args[2]);
		    	//um
				case shareData: ((UserManager)args[0]).shareData((String)args[2], (User)args[3], (Data)args[4], (Data.Permissions)args[5]); return null;
				case login: return ((UserManager)args[0]).login((String)args[2], (String)args[3]);
				case createUser: return ((UserManager)args[0]).createUser((String)args[2], (String)args[3]);
		    	//doc
				case insert: ((Doc)args[0]).insert((User)args[2], (String)args[3]); return null;
				case highlight: ((Doc)args[0]).highlight((User)args[2], (Integer)args[3], (Integer)args[4]); return null;
				case read: return ((Doc)args[0]).read((User)args[2]);
				case getCursors: return ((Doc)args[0]).getCursors((User)args[2]);
		    	//general
				case toString: return args[0].toString();
				case getAppEntry: return ((MainActivity)args[0]).getAppEntryPoint();
			}
			return null;
		}
    	
    }

    
}
