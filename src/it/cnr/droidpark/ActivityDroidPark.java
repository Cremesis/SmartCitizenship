package it.cnr.droidpark;

import it.cnr.droidpark.RatingFragmentDialog.NoticeDialogListener;

import java.util.Calendar;
import java.util.Date;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ActivityDroidPark extends FragmentActivity implements NoticeDialogListener{

	private static final String TAG = "ActivityDroidPark";
	
	private ApplicationDroidPark application;
	private static Date lastTimestamp;
	private static int lastGameId;
	
	int cronoStarted = -1;
	
	Messenger mService;
	boolean mBound = false;
	
	int localuser;

	final Messenger incomingMessenger = new Messenger(new IncomingHandler());

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
				case ServiceDroidPark.USER:{
					localuser = (Integer) msg.obj;
					Log.d(TAG, "Local user id: " + localuser);
				}
				break;
				
				case ServiceDroidPark.NEW_QUEUE_INSERTED:{
					Log.d(TAG, "NEW_QUEUE_INSERTED received");
					//QueueMsg queue = msg.getData().getParcelable("queue");
					// TODO: show new time queue.getDuration() for game queue.getIdGame()
				}
				break;
				
				case ServiceDroidPark.NEW_RATING_INSERTED:{
					Log.d(TAG, "NEW_RATING_INSERTED received");
					//RatingMsg rating = msg.getData().getParcelable("rating");
					// TODO: show new average rating application.getRatingAverage(rating.getIdGame()) for game rating.getIdGame()
				}
				break;
				
				case ServiceDroidPark.NEW_OPINION_INSERTED:{
					Log.d(TAG, "NEW_OPINION_INSERTED received");
					//Opinion opinion = msg.getData().getParcelable("opinion");
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


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		/*int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			return true;
		case R.id.new_room:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Title");
			alert.setMessage("Message");

			// Set an EditText view to get user input 
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = input.getText().toString();
					// Do something with value!
					Message msg = Message.obtain();
					msg.what = EntertainmentService.CREATED_LOCAL_ROOM;
					Bundle bundle = new Bundle();
					bundle.putString("name", value);
					msg.setData(bundle);
					try {
						mService.send(msg);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//chatList.put(value.hashCode(), value);
					//followedRooms.add(value.hashCode());
					//chatListFragment.addRoom(value.hashCode());
				}
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Canceled.
				}
			});

			alert.show();

			return true;
		default:
			break;
		}*/
		return super.onOptionsItemSelected(item);
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
	
	public void sendApplicationMsg(ApplicationMsg appMsg, int what){
		Log.d(TAG, "Sending app msg");
		
		Bundle args = new Bundle();
		args.putParcelable("msg", appMsg);
		Message msg = Message.obtain();
		msg.what = what;
		msg.setData(args);
		
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
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
	
	public void queueDuration(View v){
		Chronometer crono1 = (Chronometer) findViewById(R.id.chronometer1);
		ToggleButton t0 = (ToggleButton) findViewById(R.id.toggleButton0);
		ToggleButton t1 = (ToggleButton) findViewById(R.id.toggleButton1);
		ToggleButton t2 = (ToggleButton) findViewById(R.id.toggleButton2);
		ToggleButton t3 = (ToggleButton) findViewById(R.id.toggleButton3);
		
		// devo cambiare contesto: tipo algoritmo,coda 
		Message msg;
		switch (v.getId()){
			case R.id.toggleButton0:{
				
				if (t0.isChecked() && (cronoStarted == -1)) {
					crono1.setBase(SystemClock.elapsedRealtime());
					crono1.start();
					cronoStarted=0;
					msg = createMsg(ServiceDroidPark.PERFECT_FORWARDER_IN_QUEUE);
					sendMsg(msg);			
				} else {
					if(cronoStarted == 0) {
						crono1.stop();
						long durataCodaInSec = (SystemClock.elapsedRealtime() - crono1.getBase()) / 1000;
						Integer durataCodaInMinuti = (int) durataCodaInSec / 60;
										
						Calendar cal = Calendar.getInstance();
						lastTimestamp = cal.getTime();
						lastGameId =  ServiceDroidPark.GAME_1;
						QueueMsg queue = new QueueMsg(lastGameId,lastTimestamp,durataCodaInMinuti,application.NUMBER_OF_COPIES);
						msg = createApplicationMsg(queue, ServiceDroidPark.SEND_QUEUE);
						sendMsg(msg);
										
						cronoStarted = -1;
						gameEvaluation();
										
						// Chiamare Spread and Wait
					}
				}
			}
			break;
			
			case R.id.toggleButton1:{
				
				if (t1.isChecked() && (cronoStarted==-1)) {
					crono1.setBase(SystemClock.elapsedRealtime());
					crono1.start();
					cronoStarted = 1;
					msg = createMsg(ServiceDroidPark.PERFECT_FORWARDER_IN_QUEUE);
					sendMsg(msg);
				} else {
					if (cronoStarted==1) {
						crono1.stop();
						cronoStarted = -1;
						long durataCodaInSec = (SystemClock.elapsedRealtime() - crono1.getBase()) / 1000;
						Integer durataCodaInMinuti = (int) durataCodaInSec / 60;
									
						Calendar cal = Calendar.getInstance();
						lastTimestamp = cal.getTime();
						lastGameId =  ServiceDroidPark.GAME_2;
						QueueMsg queue = new QueueMsg(lastGameId,lastTimestamp,durataCodaInMinuti,application.NUMBER_OF_COPIES);
						msg = createApplicationMsg(queue, ServiceDroidPark.SEND_QUEUE);
						sendMsg(msg);
						
						gameEvaluation();
					}
				}
			}
			break;
				
			case R.id.toggleButton2:{
				
				if (t2.isChecked() && (cronoStarted==-1)) {
					crono1.setBase(SystemClock.elapsedRealtime());
					crono1.start();
					cronoStarted = 2;
					msg = createMsg(ServiceDroidPark.PERFECT_FORWARDER_IN_QUEUE);
					sendMsg(msg);
				} else {
					if (cronoStarted==2) {
						crono1.stop();
						cronoStarted = -1;
						long durataCodaInSec = (SystemClock.elapsedRealtime() - crono1.getBase()) / 1000;
						Integer durataCodaInMinuti = (int) durataCodaInSec / 60;
						
						Calendar cal = Calendar.getInstance();
						lastTimestamp = cal.getTime();
						lastGameId =  ServiceDroidPark.GAME_3;
						QueueMsg queue = new QueueMsg(lastGameId,lastTimestamp,durataCodaInMinuti,application.NUMBER_OF_COPIES);
						msg = createApplicationMsg(queue, ServiceDroidPark.SEND_QUEUE);
						sendMsg(msg);
						
						gameEvaluation();
					}
				}
			}
			break;
			
			case R.id.toggleButton3:{
				
				if (t3.isChecked() && (cronoStarted==-1)) {
					crono1.setBase(SystemClock.elapsedRealtime());
					crono1.start();
					cronoStarted = 3;
					msg = createMsg(ServiceDroidPark.PERFECT_FORWARDER_IN_QUEUE);
					sendMsg(msg);
				} else {
					if (cronoStarted==3) {
						crono1.stop();
						cronoStarted = -1;
						long durataCodaInSec = (SystemClock.elapsedRealtime() - crono1.getBase()) / 1000;
						Integer durataCodaInMinuti = (int) durataCodaInSec / 60;
						
						Calendar cal = Calendar.getInstance();
						lastTimestamp = cal.getTime();
						lastGameId =  ServiceDroidPark.GAME_4;
						QueueMsg queue = new QueueMsg(lastGameId,lastTimestamp,durataCodaInMinuti,application.NUMBER_OF_COPIES);
						msg = createApplicationMsg(queue, ServiceDroidPark.SEND_QUEUE);
						sendMsg(msg);
						
						gameEvaluation();
					}
				}
			}
			break;
		}
	}
		
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		RatingBar rb = (RatingBar) dialog.getDialog().findViewById(R.id.ratingBar1);
		float rate = rb.getRating();
		EditText et = (EditText) dialog.getDialog().findViewById(R.id.editText1);
		String comment = et.getText().toString();
		Calendar cal = Calendar.getInstance();
		lastTimestamp = cal.getTime();
				
		application.insertUpdateOpinion(lastGameId, localuser, new Opinion(lastGameId,localuser,lastTimestamp, comment));
		application.insertRating(lastGameId, localuser, new RatingMsg(lastGameId, localuser, lastTimestamp, rate, application.NUMBER_OF_COPIES));
		
		Toast toast = Toast.makeText(getApplicationContext(), "Hai lasciato un commento", Toast.LENGTH_SHORT);
		toast.show();
				
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		Calendar cal = Calendar.getInstance();
		lastTimestamp = cal.getTime();
		RatingBar rb = (RatingBar) dialog.getDialog().findViewById(R.id.ratingBar1);	
		
		RatingMsg rMsg = new RatingMsg(lastGameId, localuser, lastTimestamp, rb.getRating(), application.NUMBER_OF_COPIES);
		sendApplicationMsg(rMsg, ServiceDroidPark.SEND_RATING);
		
		/*try {
			mService.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		Toast toast = Toast.makeText(getApplicationContext(), "Valutazione salvata", Toast.LENGTH_SHORT);
		toast.show();
		
		
	}

	@Override
	public void onDialogNeutralClick(DialogFragment dialog) {
		dialog.dismiss();
	}
	
	
}
