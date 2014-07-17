package it.cnr.droidpark;

import it.cnr.droidpark.PopUpOptionDialog.PopUpDialogListener;
import it.cnr.droidpark.RatingFragmentDialog.NoticeDialogListener;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;




public class ActivityDroidPark extends FragmentActivity implements NoticeDialogListener, PopUpDialogListener {

	private static final String TAG = "ActivityDroidPark";
	
	private ApplicationDroidPark application;
	private static Date lastTimestamp;
	public static int lastGameId;
	public static int lastPressedGameButton;
	static boolean outOfQueueComment=false; 
	
	int cronoStarted = -1;
	
	Messenger mService;
	boolean mBound = false;
	
	final Messenger incomingMessenger = new Messenger(new IncomingHandler());
	
	private class ToggleLongListener implements OnLongClickListener{

		@Override
		public boolean onLongClick(View v) {
			switch(v.getId()){
			case R.id.toggleButton0: lastPressedGameButton = ApplicationDroidPark.GAME_1; break;
			case R.id.toggleButton1: lastPressedGameButton = ApplicationDroidPark.GAME_2; break;
			case R.id.toggleButton2: lastPressedGameButton = ApplicationDroidPark.GAME_3; break;
			case R.id.toggleButton3: lastPressedGameButton = ApplicationDroidPark.GAME_4; break;
			}
			PopUpOptionDialog pop = new PopUpOptionDialog();
			pop.show(getSupportFragmentManager(), "Pop");
			return true;
		}
		
	}

	// ChatListFragment chatListFragment;
	// RoomFragment roomFrag;
	// LinkedHashMap<Integer, String> chatList;
	// HashSet<Integer> followedRooms;
	// Hashtable<Integer, ArrayList<String>> roomMsgs; //solo una roomID e lista dei relativi messaggi
	

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler { // Handles incoming messages from the Service
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ServiceDroidPark.NEW_QUEUE_INSERTED:{
					Log.d(TAG, "NEW_QUEUE_INSERTED received");
					QueueMsg queue = msg.getData().getParcelable("queue");
					TextView tw;
					
					switch (queue.getIdGame()){
					case ApplicationDroidPark.GAME_1: 
						tw = (TextView) findViewById(R.id.textView2);
						tw.setText("Attesa: " + queue.getDuration() + " min");
						break;
					case ApplicationDroidPark.GAME_2: 
						tw = (TextView) findViewById(R.id.textView6);
						tw.setText("Attesa: " + queue.getDuration() + " min");
						break;
					case ApplicationDroidPark.GAME_3: 
						tw = (TextView) findViewById(R.id.textView4);
						tw.setText("Attesa: " + queue.getDuration() + " min");
						break;
					case ApplicationDroidPark.GAME_4: 
						tw = (TextView) findViewById(R.id.textView8);
						tw.setText("Attesa: " + queue.getDuration() + " min");
						break;
					
					}
										
										
				}
				break;
				
				case ServiceDroidPark.NEW_RATING_INSERTED:{
					Log.d(TAG, "NEW_RATING_INSERTED received");
					TextView tw;					
					switch (msg.arg1){
					case ApplicationDroidPark.GAME_1: 
						tw = (TextView) findViewById(R.id.textView3);
						tw.setText("Rating: " + application.getRatingAverage(application.GAME_1));
						break;
					case ApplicationDroidPark.GAME_2: 
						tw = (TextView) findViewById(R.id.textView7);
						tw.setText("Rating: " + application.getRatingAverage(application.GAME_2));
						break;
					case ApplicationDroidPark.GAME_3: 
						tw = (TextView) findViewById(R.id.textView5);
						tw.setText("Rating: " + application.getRatingAverage(application.GAME_3));
						break;
					case ApplicationDroidPark.GAME_4: 
						tw = (TextView) findViewById(R.id.textView9);
						tw.setText("Rating: " + application.getRatingAverage(application.GAME_4));
						break;
					
					}
														
					
				}
				break;
				
				case ServiceDroidPark.NEW_OPINION_INSERTED:{
					Log.d(TAG, "NEW_OPINION_INSERTED received");
					Opinion opinion = msg.getData().getParcelable("opinion");
					String mess="";
					switch (opinion.getIdGame()){
					case ApplicationDroidPark.GAME_1: 
						mess = getResources().getString(R.string.game_1);
						break;
					case ApplicationDroidPark.GAME_2: 
						mess = getResources().getString(R.string.game_2);
						break;
					case ApplicationDroidPark.GAME_3: 
						mess = getResources().getString(R.string.game_3);
						break;
					case ApplicationDroidPark.GAME_4: 
						mess = getResources().getString(R.string.game_4);
						break;
					
					}
					Toast toast = Toast.makeText(getApplicationContext(), "Arrivata nuova opinione per " + mess, Toast.LENGTH_LONG);
					toast.show();
					
					// TODO: decide what to do in this case...
				}
				break;
				
				case ServiceDroidPark.KILL_APP:
					finish();
				break;
				
				default:
					super.handleMessage(msg);
				}
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		
		application = (ApplicationDroidPark) getApplication();
		activateLongClick();
				
		Intent intent = new Intent(this, ServiceDroidPark.class);
		if (!mBound){
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onStart(){
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}


//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		/*int id = item.getItemId();
//		switch (id) {
//		case R.id.action_settings:
//			return true;
//		case R.id.new_room:
//			AlertDialog.Builder alert = new AlertDialog.Builder(this);
//
//			alert.setTitle("Title");
//			alert.setMessage("Message");
//
//			// Set an EditText view to get user input 
//			final EditText input = new EditText(this);
//			alert.setView(input);
//
//			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton) {
//					String value = input.getText().toString();
//					// Do something with value!
//					Message msg = Message.obtain();
//					msg.what = EntertainmentService.CREATED_LOCAL_ROOM;
//					Bundle bundle = new Bundle();
//					bundle.putString("name", value);
//					msg.setData(bundle);
//					try {
//						mService.send(msg);
//					} catch (RemoteException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					//chatList.put(value.hashCode(), value);
//					//followedRooms.add(value.hashCode());
//					//chatListFragment.addRoom(value.hashCode());
//				}
//			});
//
//			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton) {
//					// Canceled.
//				}
//			});
//
//			alert.show();
//
//			return true;
//		default:
//			break;
//		}*/
//		return super.onOptionsItemSelected(item);
//	}
	

	public void activateLongClick(){
		
		
		ToggleButton t0 = (ToggleButton) findViewById(R.id.toggleButton0);
		ToggleButton t1 = (ToggleButton) findViewById(R.id.toggleButton1);
		ToggleButton t2 = (ToggleButton) findViewById(R.id.toggleButton2);
		ToggleButton t3 = (ToggleButton) findViewById(R.id.toggleButton3);
		
		ToggleLongListener tlistener = new ToggleLongListener();
		
		t0.setOnLongClickListener(tlistener);
		t1.setOnLongClickListener(tlistener);
		t2.setOnLongClickListener(tlistener);
		t3.setOnLongClickListener(tlistener);
						
	}
	

	public Message createApplicationMsg(ApplicationMsg appMsg, int what){
		Log.d(TAG, "Create app msg");
		
		Bundle args = new Bundle();
		args.putParcelable("msg", appMsg);
		Message msg = Message.obtain();
		msg.what = what;
		msg.setData(args);
		return msg;
		}
	
	public Message createMsg(int what){
		Message msg = Message.obtain();
		msg.what = what;
		return msg;
	}
	
	private void sendMsg(Message msg) {
		try {
			mService.send(msg);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}	
	}
	
	public void sendOpinionMsg(Opinion op, int what){
		Log.d(TAG, "Sending app msg");
		
		Bundle args = new Bundle();
		args.putParcelable("msg", op);
		Message msg = Message.obtain();
		msg.what = what;
		msg.setData(args);
		
		sendMsg(msg);
	}
	
	public void sendApplicationMsg(ApplicationMsg appMsg, int what){
		Log.d(TAG, "Sending app msg");
		
		Bundle args = new Bundle();
		args.putParcelable("msg", appMsg);
		Message msg = Message.obtain();
		msg.what = what;
		msg.setData(args);
		
		sendMsg(msg);
	}
	
	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			mService = new Messenger(service);
			Message msg = createMsg(ServiceDroidPark.ACTIVITY_BIND);
			msg.replyTo = incomingMessenger;
			sendMsg(msg);
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};


	
	public void gameEvaluation(){
		RatingFragmentDialog ratingFrag = new RatingFragmentDialog();
		ratingFrag.show(getSupportFragmentManager(), "RATING");
	}
	
	public void cronoStart(Chronometer crono){
		crono.setBase(SystemClock.elapsedRealtime());
		crono.start();
		Message msg;
		msg = createMsg(ServiceDroidPark.PERFECT_FORWARDER_IN_QUEUE);
		sendMsg(msg);
	}
	
	public void cronoStop(Chronometer crono){
		crono.stop();
		long durataCodaInSec = (SystemClock.elapsedRealtime() - crono.getBase()) / 1000;
		Integer durataCodaInMinuti = (int) durataCodaInSec / 60;
		Message msg;	
		Calendar cal = Calendar.getInstance();
		lastTimestamp = cal.getTime();
		QueueMsg queue = new QueueMsg(lastGameId,lastTimestamp,durataCodaInMinuti,application.NUMBER_OF_COPIES);
		msg = createApplicationMsg(queue, ServiceDroidPark.SEND_QUEUE);
		sendMsg(msg);
						
		cronoStarted = -1;
		outOfQueueComment=true;
		gameEvaluation();
		
		TextView tw;
		
		switch (lastGameId){
		case ApplicationDroidPark.GAME_1: 
			tw = (TextView) findViewById(R.id.textView2);
			tw.setText("Attesa: " + durataCodaInMinuti.toString() + " min");
			break;
		case ApplicationDroidPark.GAME_2: 
			tw = (TextView) findViewById(R.id.textView6);
			tw.setText("Attesa: " + durataCodaInMinuti.toString() + " min");
			break;
		case ApplicationDroidPark.GAME_3: 
			tw = (TextView) findViewById(R.id.textView4);
			tw.setText("Attesa: " + durataCodaInMinuti.toString() + " min");
			break;
		case ApplicationDroidPark.GAME_4: 
			tw = (TextView) findViewById(R.id.textView8);
			tw.setText("Attesa: " + durataCodaInMinuti.toString() + " min");
			break;
		
		}
		
		
	}
		
	public void queueDuration(View v){
		Chronometer crono1 = (Chronometer) findViewById(R.id.chronometer1);
		ToggleButton t0 = (ToggleButton) findViewById(R.id.toggleButton0);
		ToggleButton t1 = (ToggleButton) findViewById(R.id.toggleButton1);
		ToggleButton t2 = (ToggleButton) findViewById(R.id.toggleButton2);
		ToggleButton t3 = (ToggleButton) findViewById(R.id.toggleButton3);
				
				
			switch (v.getId()){
			case R.id.toggleButton0:{
				
				if (t0.isChecked() && (cronoStarted == -1)) {
					cronoStart(crono1);
					cronoStarted=0;
							
				} else {
					if(cronoStarted == 0) {
						lastGameId =  ApplicationDroidPark.GAME_1;
						cronoStop(crono1);														
					} else t0.toggle();
				}
			}
			break;
			
			case R.id.toggleButton1:{
				
				if (t1.isChecked() && (cronoStarted==-1)) {
					
					cronoStart(crono1);
					cronoStarted = 1;
					
				} else {
					if (cronoStarted==1) {
						lastGameId =  ApplicationDroidPark.GAME_2;
						cronoStop(crono1);
						
					}else t1.toggle();
				}
			}
			break;
				
			case R.id.toggleButton2:{
				
				if (t2.isChecked() && (cronoStarted==-1)) {
					cronoStart(crono1);
					cronoStarted = 2;
					
				} else {
					if (cronoStarted==2) {
						lastGameId =  ApplicationDroidPark.GAME_3;
						cronoStop(crono1);
						
					}else t2.toggle();
				}
			}
			break;
			
			case R.id.toggleButton3:{
				
				if (t3.isChecked() && (cronoStarted==-1)) {
					cronoStart(crono1);
					cronoStarted = 3;
					
				} else {
					if (cronoStarted==3) {
						lastGameId =  ApplicationDroidPark.GAME_4;
						cronoStop(crono1);
					}else t3.toggle();
				}
			}
			break;
		}
	}
		
	@Override
	public void onDialogPositiveClick(DialogFragment dialog, boolean endOfQueueComment) {
		RatingBar rb = (RatingBar) dialog.getDialog().findViewById(R.id.ratingBar1);
		EditText et = (EditText) dialog.getDialog().findViewById(R.id.editText1);
		
		String comment = et.getText().toString();
		Calendar cal = Calendar.getInstance();
		
		
		if(outOfQueueComment==true){
			RatingMsg rMsg = new RatingMsg(lastGameId, application.localuser, cal.getTime(), rb.getRating(), application.NUMBER_OF_COPIES);
			sendApplicationMsg(rMsg, ServiceDroidPark.SEND_RATING);
		
			Opinion opinion = new Opinion(lastGameId, application.localuser, cal.getTime(), comment);
			sendOpinionMsg(opinion, ServiceDroidPark.SEND_OPINION);
		}
		else{
			RatingMsg rMsg = new RatingMsg(lastPressedGameButton, application.localuser, cal.getTime(), rb.getRating(), application.NUMBER_OF_COPIES);
			sendApplicationMsg(rMsg, ServiceDroidPark.SEND_RATING);
			
			Opinion opinion = new Opinion(lastPressedGameButton, application.localuser, cal.getTime(), comment);
			sendOpinionMsg(opinion, ServiceDroidPark.SEND_OPINION);
		}
		
		outOfQueueComment=false;	
		
		Toast toast = Toast.makeText(getApplicationContext(), "Hai lasciato un commento", Toast.LENGTH_SHORT);
		toast.show();
				
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog,boolean endOfQueueComment) {
		Calendar cal = Calendar.getInstance();
		RatingBar rb = (RatingBar) dialog.getDialog().findViewById(R.id.ratingBar1);	
		
		if(outOfQueueComment==true){
			RatingMsg rMsg = new RatingMsg(lastGameId, application.localuser, cal.getTime(), rb.getRating(), application.NUMBER_OF_COPIES);
			sendApplicationMsg(rMsg, ServiceDroidPark.SEND_RATING);
		}else{
			RatingMsg rMsg = new RatingMsg(lastPressedGameButton, application.localuser, cal.getTime(), rb.getRating(), application.NUMBER_OF_COPIES);
			sendApplicationMsg(rMsg, ServiceDroidPark.SEND_RATING);
						
		}
		outOfQueueComment=false;				
		Toast toast = Toast.makeText(getApplicationContext(), "Valutazione salvata", Toast.LENGTH_SHORT);
		toast.show();
				
	}

	@Override
	public void onDialogNeutralClick(DialogFragment dialog) {
		outOfQueueComment=false;
		dialog.dismiss();
	}

	@Override
	public void preferedGameUpdate() {
		Message msg = Message.obtain();
		msg.what = ServiceDroidPark.UPDATE_PREF;
		msg.arg1 = lastPressedGameButton;
		sendMsg(msg);
				
	}

	
}
