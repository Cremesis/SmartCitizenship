package it.cnr.droidpark;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Service;
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
import android.util.Log;
import android.widget.Toast;
import cnr.Common.ApplicationContext;
import cnr.Common.CallbackInterface;
import cnr.Common.PlatformInterface;
import cnr.Common.UserContext;

public class ServiceDroidPark extends Service{
	
	private static final String TAG = "ServiceDroidPark";
	
	public static final int DROID_VERSION = 0;
	public static final int CAMEO_PORT = 33;
	
	// Messages FROM Activity TO Service
	public final static int ACTIVITY_BIND = 100; // Used to pass the messenger to use to communicate with the Activity
	public final static int PERFECT_FORWARDER_IN_QUEUE = 102; // User has multiple Spread and Wait copy to handle in queue
	public final static int UPDATE_PREF = 103; // User has changed it's preferences about what's interested in
	public final static int SEND_QUEUE = 104; // User has generated a new Queue
	public final static int SEND_OPINION = 105; // User has generated a new Opinion
	public final static int SEND_RATING = 106; // User has generated a new Rating
	
	// Messages FROM Service TO Activity
	public final static int USER = 200; // Got local CAMEO user ID
	public final static int KILL_APP = 201; // Kill Activity (and with that, the Service) if something didn't go well
	public static final int NEW_QUEUE_INSERTED = 202; // A new (or newer) queue message has been saved
	public static final int NEW_RATING_INSERTED = 203; // A new (or newer) rating message has been saved
	public static final int NEW_OPINION_INSERTED = 204; // A new (or newer) opinion message has been saved
	
	PlatformInterface cameo = null;
	boolean connectedToCameo = false;
	private long CAMEOAppKey;
	
	private ApplicationContext appContext;   // Map<Integer, Boolean>
	public Integer localuser;
	
	private final Messenger incomingMessenger = new Messenger(new IncomingHandler());
	private Messenger mActivity;
	
	private ApplicationDroidPark application;
	
	private boolean inQueue = false;
	
	private Set<InetAddress> currentNeighbors;
	private Map<InetAddress, UserContext> neighborsUserContext;
	private Map<InetAddress, Map<Integer, Boolean>> usersAppContext; // Neighbors' ApplicationContext that use this application
	private InetAddress[] youngestNeighbors = new InetAddress[2];
	
	ServiceConnection sc = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) { //gestione connessione a CAMEO che e' lui stesso un service
			cameo = PlatformInterface.Stub.asInterface(service);
			connectedToCameo = true;
			registerApp(); 
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			connectedToCameo = false;
		}

	};
	
	@Override
	public void onCreate() {
		
		application = (ApplicationDroidPark) getApplication();
		
		currentNeighbors = new HashSet<InetAddress>();
		usersAppContext = new Hashtable<InetAddress, Map<Integer,Boolean>>();
		neighborsUserContext = new Hashtable<InetAddress, UserContext>();


		if (!bindService(new Intent("cnr.CAMEO.PLATFORM"), sc, Context.BIND_AUTO_CREATE)){ // bind con il service
			Toast.makeText(this, "Can't connect to CAMEO", Toast.LENGTH_SHORT).show();
			Message msg = Message.obtain();
			msg.what = KILL_APP;
			
			try {
				mActivity.send(msg);
			} catch (RemoteException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return incomingMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		if (connectedToCameo){
			try {
				cameo.unregisterApplication(CAMEOAppKey);
			} catch (RemoteException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
			unbindService(sc);
			connectedToCameo=false;
		}
	}
	
	private void sendOpinions(InetAddress address) {
		Opinion opinion;
		for(Integer preference : usersAppContext.get(address).keySet()) {
			opinion = application.getGameOpinion(preference, localuser);
			if(opinion != null) sendMSGToPeer(opinion, address);
		}
	}

	private final CallbackInterface.Stub callback = new CallbackInterface.Stub() { 

		@SuppressLint("UseSparseArrays")
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void neighborApplicationContextUpdated(Map arg0, byte[] address)
				throws RemoteException {
			Log.d(TAG, "neighborApplicationContextUpdated()");
			
			try {
				InetAddress thisNeighbor = InetAddress.getByAddress(address);
				
				Map<Integer, Boolean> currentContext = usersAppContext.get(thisNeighbor);
				if(currentContext == null) { // new neighbor
					Map<Integer, Boolean> newRemoteAppContext = new HashMap<Integer, Boolean>();
					usersAppContext.put(thisNeighbor, newRemoteAppContext);
					currentNeighbors.add(thisNeighbor);
					currentContext = newRemoteAppContext;
					if(!inQueue) phaseN(neighborsUserContext.get(thisNeighbor), thisNeighbor);
				}
				Set<Entry<Integer, Boolean>> remoteAppContext = (Set<Entry<Integer, Boolean>>) arg0.entrySet();
				
				for (Entry<Integer, Boolean> entry : remoteAppContext){
					
					if(entry.getValue() == null) { // Remove "null" values from the context and add the others
						currentContext.remove(entry.getKey());
						continue; // The neighbor removed this preference, go to the next one. 
					} else {
						currentContext.put(entry.getKey(), true);
					}
					// TODO: communicate with the activity
					//mActivity.send(msg);
				}
				
				sendOpinions(thisNeighbor);
			} catch (UnknownHostException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		
		private boolean phase1(ApplicationMsg msg)
				throws RemoteException {
			Log.d(TAG, "phase1()");
			boolean isNew;
			
			if(msg instanceof RatingMsg) {
				RatingMsg rating = (RatingMsg) msg;
				isNew = application.insertRating(rating.getIdGame(), rating.getIdUser(), rating);
			} else {
				QueueMsg queue = (QueueMsg) msg;
				isNew = application.insertQueue(queue.getIdGame(), queue);
			}
			if(isNew) {
				Message message = Message.obtain();
				Bundle data = new Bundle();
				message.what = msg instanceof RatingMsg?NEW_RATING_INSERTED:NEW_QUEUE_INSERTED;
				data.putParcelable(msg instanceof RatingMsg?"rating":"queue", msg);
				mActivity.send(message);
			}
			return isNew;
		}
		
		private void phase2(ApplicationMsg appMsg) {
			Log.d(TAG, "phase2()");
			if(appMsg.getNumCopies() != 0) {
				if(inQueue)
					probAlgorithm(appMsg);
				else
					spreadAndWait(appMsg);
			}
		}
		
		private void phaseN(UserContext remoteUserContext, InetAddress thisNeighbor) {
			Log.d(TAG, "phaseN()");
			spreadAndWaitNeighborIn(thisNeighbor);
		}
		
		@Override
		public void neighborIn(UserContext userContext, byte[] address)
				throws RemoteException {
			Log.d(TAG, "neighborIn()");
			if(userContext.isEmpty()) return; // when it's the first time that a user enters our range and that we see her
			Log.d(TAG, "id: " + userContext.getName().hashCode() +
					" | name: " + userContext.getName() +
					" | age: " + userContext.getAge());
			try {
				InetAddress ipAddress = InetAddress.getByAddress(address);
				neighborsUserContext.put(InetAddress.getByAddress(address), userContext);
				if(usersAppContext.get(ipAddress) != null) { // If she has my application
					currentNeighbors.add(ipAddress);
					if(!inQueue)
						phaseN(userContext, ipAddress);
					sendOpinions(ipAddress);
				}
				printNeighborsInfo();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void neighborUserContextUpdated(UserContext remoteUserContext, byte[] address)
				throws RemoteException {
			Log.d(TAG, "neighborUserContextUpdated()");
			Log.d(TAG, "id: " + remoteUserContext.getName().hashCode() +
					" | name: " + remoteUserContext.getName() +
					" | age: " + remoteUserContext.getAge());
			try {
				neighborsUserContext.put(InetAddress.getByAddress(address), remoteUserContext);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void neighborOut(byte[] arg0) throws RemoteException {
			Log.d(TAG, "neighborOut()");
			try {
				InetAddress thisNeighbor = InetAddress.getByAddress(arg0);
				Log.d(TAG, "name: " + neighborsUserContext.get(thisNeighbor).getName());
				
				neighborsUserContext.remove(thisNeighbor);
				currentNeighbors.remove(thisNeighbor);
				
				// TODO: remove debug prints when done
				printNeighborsInfo();
			} catch (UnknownHostException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		
		// TODO: remove debug prints when done
		private void printNeighborsInfo() {
			Log.d(TAG, "Number of neighbors using this app: " + currentNeighbors.size());
			if(currentNeighbors.size() >= 2)
				Log.d(TAG, "Youngest neighbors: " + neighborsUserContext.get(youngestNeighbors[0]).getName() +
					" | " + neighborsUserContext.get(youngestNeighbors[1]).getName());
			else if(currentNeighbors.size() == 1)
				Log.d(TAG, "Youngest neighbor: " + neighborsUserContext.get(youngestNeighbors[0]).getName());
		}

		@Override
		public void onCommunityChanged(String arg0) throws RemoteException {
			// Don't care
		}

		@Override
		public void onMessageReceived(byte[] msgReceived, byte[] address)
				throws RemoteException {
			
			Object msg = readObject(msgReceived);
			
			if(msg instanceof Opinion) {
				Opinion opinion = (Opinion) msg;
				boolean inserted = application.insertUpdateOpinion(opinion.getIdGame(), opinion.getIdUser(), opinion);
				if(inserted) {
					Message message = Message.obtain();
					Bundle data = new Bundle();
					message.what = NEW_OPINION_INSERTED;
					data.putParcelable("opinion", opinion);
					mActivity.send(message);
				}
			} else {
				ApplicationMsg appMsg = (ApplicationMsg) msg;
				boolean isNew = phase1(appMsg);
				if(isNew) phase2(appMsg);
			}
		}
	};

	private void registerApp(){
		//successfully connected to CAMEO
		try {
			if(!cameo.isUserDefined()){
				cameo.startUserProfileActivity();
				Message msg = Message.obtain();
				msg.what = KILL_APP;
				mActivity.send(msg);
			}
			cameo.startCAMEO();
			if ((CAMEOAppKey = cameo.registerApplication(CAMEO_PORT, callback)) == -1){   //chiamata std x registrare app a cameo
				Toast.makeText(this, "Can't register application with CAMEO", Toast.LENGTH_SHORT).show();
				//die
				Message msg = Message.obtain();
				msg.what = KILL_APP;
				mActivity.send(msg);
			}
			Toast.makeText(this, "Registered with CAMEO", Toast.LENGTH_SHORT).show();
			//create new application context
			appContext = new ApplicationContext();
			
			appContext.addValue(DROID_VERSION, true);
			appContext.update(cameo, CAMEOAppKey); // FIXME: not sure if needed
			
			localuser = cameo.getLocalUserContext(CAMEOAppKey).getName().hashCode();
			if(mActivity!=null){
				Message msg = Message.obtain();
				msg.what = USER;
				msg.arg1 = localuser;
				try {
					mActivity.send(msg);
				} catch (RemoteException e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
			}

		} catch (RemoteException e) {
			Toast.makeText(this, "Can't register application with CAMEO", Toast.LENGTH_SHORT).show();
			// Die
			Message msg = Message.obtain();
			msg.what = KILL_APP;
			try {
				mActivity.send(msg);
			} catch (RemoteException e1) {
				Log.e(TAG, Log.getStackTraceString(e1));
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	private class IncomingHandler extends Handler { // Handles incoming messages from the Activity
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ACTIVITY_BIND:{
					mActivity = msg.replyTo;
				}
				break;
				
				case SEND_OPINION:{
					Log.d(TAG, "SEND_OPINION received");
					
					Opinion opinion = msg.getData().getParcelable("msg");
					application.insertUpdateOpinion(opinion.getIdGame(), opinion.getIdUser(), opinion);
					for(InetAddress neighbor : currentNeighbors) {
						sendOpinions(neighbor);
					}
				}
				break;
				
				case SEND_RATING:{
					Log.d(TAG, "SEND_RATING received");
					
					RatingMsg rate = msg.getData().getParcelable("msg");
					application.insertRating(rate.getIdGame(), localuser, rate);
					if (!inQueue)
						spreadAndWait(rate);
					else
						probAlgorithm(rate);
				}
				break;
				
				case SEND_QUEUE: {
					Log.d(TAG, "SEND_QUEUE received");
					
					inQueue = false;
					
					QueueMsg queue = msg.getData().getParcelable("msg");
					application.insertQueue(queue.getIdGame(), queue);
					spreadAndWait(queue); // use Spread and Wait because you exited the queue
				}
				break;
				
				case PERFECT_FORWARDER_IN_QUEUE:{  
					inQueue = true;
				
//					for (ApplicationMsg appMsg : application.getJobs())
//						probAlgorithm(appMsg);
				}
				break;
					
				
				case UPDATE_PREF:{
					try {
						Log.d(TAG,"PREFERENCE UPDATE "+ msg.arg1);
						if(appContext.getValue(msg.arg1) != null) {
							appContext.removeValue(msg.arg1);
							application.getAllGameOpinions(msg.arg1).clear();
							Toast.makeText(getApplicationContext(), "Preferenza rimossa", Toast.LENGTH_SHORT).show();
						} else {
							appContext.addValue(msg.arg1, true);
							Toast.makeText(getApplicationContext(), "Preferenza aggiunta", Toast.LENGTH_SHORT).show();
						}
						appContext.update(cameo, CAMEOAppKey);						
						
					} catch(RemoteException e) {
						e.printStackTrace();
					}
				}
				break;
				
				default:
					super.handleMessage(msg);
				}
		}
	}
	
	/**
	 * Calculate the youngest forwarders
	 * 
	 */
	public void updateYoungestForwarders() {
		int min1 = Integer.MAX_VALUE, min2 = Integer.MAX_VALUE, currentAge; // min1 <= min2
		InetAddress[] youngest = new InetAddress[2];
		for(InetAddress neighbor : currentNeighbors) {
			currentAge = neighborsUserContext.get(neighbor).getAge();
			if(currentAge <= min1) { // the "=" part is needed to get the youngest ones, even if they have the same age
				min2 = min1;
				min1 = currentAge;
				
				youngest[1] = youngest[0];
				youngest[0] = neighbor;
			} else if(currentAge > min1 && currentAge < min2) {
				min2 = currentAge;
				
				youngest[1] = neighbor;
			}
		}
		youngestNeighbors =  youngest;
	}
	
	public void spreadAndWaitNeighborIn(InetAddress newNeighbor) {
		Log.d(TAG, "spreadAndWaitNeighborIn()");
		updateYoungestForwarders();
		int newNeighborAge = neighborsUserContext.get(newNeighbor).getAge();
		if((youngestNeighbors[1] != null && newNeighborAge <= neighborsUserContext.get(youngestNeighbors[1]).getAge()) // younger than the second youngest
				|| newNeighborAge <= neighborsUserContext.get(youngestNeighbors[0]).getAge()) { // younger than the first youngest
			for (ApplicationMsg appMsg : application.getJobs()) {
				ApplicationMsg copyToSend = appMsg.duplicate();
				int numCopiesToSend =(int) Math.floor(((double)copyToSend.getNumCopies())/2);
				copyToSend.setNumCopies(numCopiesToSend);
				sendMSGToPeer(copyToSend, newNeighbor);
				application.updateNumCopies(appMsg, appMsg.getNumCopies() - numCopiesToSend);
			}
		} else { // send her just a copy for herself (numCopies == 1), not a young forwarder
			for(ApplicationMsg appMsg : application.getJobs()) {
				ApplicationMsg copyToSend = appMsg.duplicate();
				copyToSend.setNumCopies(1);
				sendMSGToPeer(copyToSend, newNeighbor);
				application.updateNumCopies(appMsg, appMsg.getNumCopies() - 1);
			}
		}
	}
	
	public void spreadAndWait(ApplicationMsg msg) {
		Log.d(TAG, "spreadAndWait()");
		Set<InetAddress> notForwarders = new HashSet<InetAddress>(currentNeighbors);
		updateYoungestForwarders();
		notForwarders.removeAll(Arrays.asList(youngestNeighbors));
		
		ApplicationMsg copyToSend = msg.duplicate();
		int numCopiesToSend =(int) Math.floor(((double)copyToSend.getNumCopies() - notForwarders.size())/3);
		for(InetAddress thisNeighbor : youngestNeighbors) {
			if(thisNeighbor == null) continue;
			copyToSend.setNumCopies(numCopiesToSend);
			sendMSGToPeer(copyToSend, thisNeighbor);
			application.updateNumCopies(msg, msg.getNumCopies() - numCopiesToSend);
		}
		for(InetAddress thisNeighbor : notForwarders) {
			copyToSend.setNumCopies(1);
			sendMSGToPeer(copyToSend, thisNeighbor);
			application.updateNumCopies(msg, msg.getNumCopies() - 1);
		}
	}
	
	public double setProbabilityTrasmission(int n){
		if(n != 0) return  1d/n;		// probabilità decrescente con numero di utenti
		else return 0;
	}
	
	public void probAlgorithm(ApplicationMsg msg){
		Log.d(TAG, "probAlgorithm()");
		if (currentNeighbors.size()!=0){
			double p = setProbabilityTrasmission(currentNeighbors.size());
			Set<InetAddress> addrOk = new HashSet<InetAddress>();
			Set<InetAddress> addrLoses = new HashSet<InetAddress>();
		
			for (InetAddress i : currentNeighbors)
				if (Math.random()< p && msg.getNumCopies() >= addrOk.size()) {
					addrOk.add(i);
					Log.d(TAG, "forw: " + neighborsUserContext.get(i).getName());
				} else {
					addrLoses.add(i);
					Log.d(TAG, "NO forw: " + neighborsUserContext.get(i).getName());
				}
			ApplicationMsg copyToSend = msg.duplicate();
			if(addrOk.size() != 0) {
				int numCopiesToSend = copyToSend.getNumCopies()/addrOk.size();
				int copiesKept = copyToSend.getNumCopies()%addrOk.size();
				copyToSend.setNumCopies(numCopiesToSend);  // TODO controllo al crescere di k sulle copie potenzialmente perse
				sendToMultipleRecipients(copyToSend, addrOk);
				application.updateNumCopies(msg, copiesKept);
			} else {
				Log.d(TAG, "No forwarders");
			}
			copyToSend.setNumCopies(0);
			sendToMultipleRecipients(copyToSend, addrLoses);
		}
	}
	
	public void sendToMultipleRecipients(ApplicationMsg msg, Set<InetAddress> adds) {
		if(adds!=null) {
			for(InetAddress address : adds) {
				sendMSGToPeer(msg, address);
			}
		}
	}
	
	public void sendMSGToPeer(Object msg, InetAddress dest) {
		try {
			boolean result=cameo.sendMessage(writeObject(msg),
					dest.getAddress(), false, CAMEOAppKey);
			if(!result)
				Log.e("TAG", "error with CAMEO");
		} catch (RemoteException re) {
			Log.e("TAG", "Got exception while sending message to peer: " +
					Log.getStackTraceString(re));
		}
	}
	
	private static byte[] writeObject(Object obj) {
		byte[] yourBytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);   
			out.writeObject(obj);
			yourBytes = bos.toByteArray();
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				// ignore close exception
				Log.e(TAG, Log.getStackTraceString(e));
			}
			try {
				bos.close();
			} catch (IOException e) {
				// ignore close exception
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		return yourBytes;
	}

	private static Object readObject(byte[] bytes) {
		Object o = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			o = in.readObject(); 
		} catch (StreamCorruptedException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		} catch (ClassNotFoundException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		} finally {
			try {
				bis.close();
			} catch (IOException e) {
				// ignore close exception
				Log.e(TAG, Log.getStackTraceString(e));
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				// ignore close exception
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		return o;
	}

	
}

