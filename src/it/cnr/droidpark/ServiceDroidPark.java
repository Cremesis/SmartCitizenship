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
	
	// Attractions ID (for preferences)
	public static final int GAME_1 = 1;
	public static final int GAME_2 = 2;
	public static final int GAME_3 = 3;
	public static final int GAME_4 = 4;

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
	
	private int numberOfNeighbors; // Neighbors that use CAMEO, not this specific application
	private Hashtable<InetAddress, UserContext> neighborsUserContext;
	private Hashtable<InetAddress, Map<Integer, Boolean>> neighbors; // Neighbors' ApplicationContext that use this application
	private InetAddress[] youngestNeighbors;
	
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
		
		neighbors = new Hashtable<InetAddress, Map<Integer,Boolean>>();
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

	private final CallbackInterface.Stub callback = new CallbackInterface.Stub() { 

		@SuppressLint("UseSparseArrays")
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void neighborApplicationContextUpdated(Map arg0, byte[] arg1)
				throws RemoteException {
			Log.d(TAG, "neighborApplicationContextUpdated()");
			try {
				InetAddress thisNeighbor = InetAddress.getByAddress(arg1);
				
				Map<Integer, Boolean> currentContext = neighbors.get(thisNeighbor);
				if(currentContext == null) { // new neighbor
					Map<Integer, Boolean> newRemoteAppContext = new HashMap<Integer, Boolean>();
					neighbors.put(thisNeighbor, newRemoteAppContext);
					currentContext = newRemoteAppContext;
					
					if(!inQueue) spreadAndWaitNeighborIn(thisNeighbor);
				}
				Set<Entry<Integer, Boolean>> remoteAppContext = (Set<Entry<Integer, Boolean>>) arg0.entrySet();
				
				for (Entry<Integer, Boolean> entry : remoteAppContext){
					
					if(entry.getValue() == null) { // Remove "null" values from the context and add the others
						currentContext.remove(entry.getKey());
						continue; // The neighbor removed this preference, go to the next one. 
					} else {
						currentContext.put(entry.getKey(), true);
					}
					
					switch(entry.getKey()) {
						case GAME_1: {
							Log.d(TAG, "Found GAME_1 preference for user " + neighborsUserContext.get(thisNeighbor).getName());
							// TODO: Send out our opinion about the "1" attraction if we have it
						}
						break;
						
						case GAME_2: {
							Log.d(TAG, "Found GAME_2 preference for user " + neighborsUserContext.get(thisNeighbor).getName());
							// TODO: Send out our opinion about the "2" attraction if we have it
						}
						break;
						
						case GAME_3: {
							Log.d(TAG, "Found GAME_3 preference for user " + neighborsUserContext.get(thisNeighbor).getName());
							// TODO: Send out our opinion about the "3" attraction if we have it 
						}
						break;
						
						case GAME_4: {
							Log.d(TAG, "Found GAME_4 preference for user " + neighborsUserContext.get(thisNeighbor).getName());
							// TODO: Send out our opinion about the "4" attraction if we have it
						}
						break;
						
						default:
							Log.e(TAG, "Context not recognized: " + entry.getKey());
					}
					
					// TODO: communicate with the activity
					//mActivity.send(msg);
				}
				
				numberOfNeighbors = neighbors.size();
				
				// Find new best Spread and Wait forwarders here because
				// addNeighbor considers even those who don't use our application
				youngestNeighbors = findYoungestForwarders();
				
				// TODO: remove debug prints when done
				printNeighborsInfo();
			} catch (UnknownHostException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		
		private void addNeighbor(UserContext remoteUserContext, byte[] address) {
			Log.d(TAG, "addNeighbor()");
			Log.d(TAG, "id: " + remoteUserContext.getName().hashCode() +
					" | name: " + remoteUserContext.getName() +
					" | age: " + remoteUserContext.getAge());
			try {
				InetAddress thisNeighbor = InetAddress.getByAddress(address);
				neighborsUserContext.put(thisNeighbor, remoteUserContext);
			} catch(UnknownHostException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		
		@Override
		public void neighborIn(UserContext arg0, byte[] arg1)
				throws RemoteException {
			Log.d(TAG, "neighborIn()");
			if(arg0.isEmpty()) return; // when it's the first time that a user enters our range and that we see her
			addNeighbor(arg0, arg1);
		}
		
		@Override
		public void neighborUserContextUpdated(UserContext remoteUserContext, byte[] arg1)
				throws RemoteException {
			Log.d(TAG, "neighborUserContextUpdated()");
			addNeighbor(remoteUserContext, arg1);
		}

		@Override
		public void neighborOut(byte[] arg0) throws RemoteException {
			Log.d(TAG, "neighborOut()");
			try {
				InetAddress thisNeighbor = InetAddress.getByAddress(arg0);
				Log.d(TAG, "name: " + neighborsUserContext.get(thisNeighbor).getName());
				
				neighborsUserContext.remove(thisNeighbor);
				neighbors.remove(thisNeighbor);
				
				numberOfNeighbors = neighbors.size();
				
				// Update youngest forwarders
				youngestNeighbors = findYoungestForwarders();
				
				// TODO: remove debug prints when done
				printNeighborsInfo();
			} catch (UnknownHostException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		
		private void printNeighborsInfo() {
			Log.d(TAG, "Number of neighbors using this app: " + numberOfNeighbors);
			if(numberOfNeighbors >= 2)
				Log.d(TAG, "Youngest neighbors: " + neighborsUserContext.get(youngestNeighbors[0]).getName() +
					" | " + neighborsUserContext.get(youngestNeighbors[1]).getName());
			else if(numberOfNeighbors == 1)
				Log.d(TAG, "Youngest neighbor: " + neighborsUserContext.get(youngestNeighbors[0]).getName());
		}

		@Override
		public void onCommunityChanged(String arg0) throws RemoteException {
			// Don't care
		}

		@Override
		public void onMessageReceived(byte[] arg0, byte[] arg1)
				throws RemoteException {
			boolean inserted;
			
			Object msg = (Object) readObject(arg0);
			
			if(msg instanceof RatingMsg) {
				RatingMsg rating = (RatingMsg) msg;
				inserted = application.insertRating(rating.getIdGame(), rating.getIdUser(), rating);
				if(inserted) {
					if(rating.getNumCopies() != 0) {
						if(inQueue)
							probAlgorithm(rating);
						else
							spreadAndWait(rating);
					}
					Message message = Message.obtain();
					Bundle data = new Bundle();
					message.what = NEW_RATING_INSERTED;
					data.putParcelable("rating", rating);
					mActivity.send(message);
				}
			} else if(msg instanceof QueueMsg) {
				QueueMsg queue = (QueueMsg) msg;
				inserted = application.insertQueue(queue.getIdGame(), queue);
				if(inserted) {
					// Start to forward the message only when numCopies != 0
					if(queue.getNumCopies() != 0) {
						if(inQueue)
							probAlgorithm(queue);
						else
							spreadAndWait(queue);
					}
					Message message = Message.obtain();
					Bundle data = new Bundle();
					message.what = NEW_QUEUE_INSERTED;
					data.putParcelable("queue", queue);
					mActivity.send(message);
				}
			} else if(msg instanceof Opinion) {
				Opinion opinion = (Opinion) msg;
				inserted = application.insertUpdateOpinion(opinion.getIdGame(), opinion.getIdUser(), opinion);
				if(inserted) {
					Message message = Message.obtain();
					Bundle data = new Bundle();
					message.what = NEW_OPINION_INSERTED;
					data.putParcelable("opinion", opinion);
					mActivity.send(message);
				}
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
			appContext.update(cameo, CAMEOAppKey); // FIXME: not sure if needed
			
			localuser = cameo.getLocalUserContext(CAMEOAppKey).getName().hashCode();
			if(mActivity!=null){
				Message msg = Message.obtain();
				msg.what = USER;
				msg.obj = localuser;
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
				}
				break;
				
				case SEND_RATING:{
					Log.d(TAG, "SEND_RATING received");
					
					RatingMsg rate = msg.getData().getParcelable("msg");
					application.insertRating(rate.getIdGame(), localuser, rate);
					if (inQueue == false)
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
				
					for (ApplicationMsg appMsg : application.getJobs())
						if(appMsg.getNumCopies()>0)						
							probAlgorithm(appMsg);
				}
				break;
					
				
				case UPDATE_PREF:{
					try {
						if(appContext.getValue(msg.arg1) != null) {
							appContext.removeValue(msg.arg1);
						} else {
							appContext.addValue(msg.arg1, true);
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
	 * Returns the two youngest neighbors
	 * 
	 * @return an array of dimension 2, where each element is the address of the
	 *         youngest among the neighbors. It may contains null values
	 */
	public InetAddress[] findYoungestForwarders() {
		int min1 = Integer.MAX_VALUE, min2 = Integer.MAX_VALUE, currentAge; // min1 <= min2
		InetAddress[] youngest = new InetAddress[2];
		for(InetAddress neighbor : neighbors.keySet()) {
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
		return youngest;
	}
	
	public void spreadAndWaitNeighborIn(InetAddress newNeighbor) {
		Log.d(TAG, "spreadAndWaitNeighborIn()");
		int newNeighborAge = neighborsUserContext.get(newNeighbor).getAge();
		if((youngestNeighbors[1] != null && newNeighborAge <= neighborsUserContext.get(youngestNeighbors[1]).getAge()) // younger than the second youngest
				|| (youngestNeighbors[0] != null && newNeighborAge <= neighborsUserContext.get(youngestNeighbors[0]).getAge()) // younger than the first youngest
				|| (youngestNeighbors[0] == null && youngestNeighbors[1] == null )) { // no other neighbor (it's a good forwarder then)
			for (ApplicationMsg appMsg : application.getJobs()) {
				ApplicationMsg copyToSend = appMsg.duplicate();
				int numCopiesToSend =(int) Math.floor(((double)copyToSend.getNumCopies())/2);
				copyToSend.setNumCopies(numCopiesToSend);
				sendMSGToPeer(copyToSend, newNeighbor);
				application.updateNumCopies(appMsg, appMsg.getNumCopies() - numCopiesToSend);
			}
		} else { // send her just a copy for herself (numCopies == 0), not a young forwarder
			for(ApplicationMsg appMsg : application.getJobs()) {
				ApplicationMsg copyToSend = appMsg.duplicate();
				copyToSend.setNumCopies(0); // FIXME: is "0" correct? Or "1"?
				sendMSGToPeer(copyToSend, newNeighbor);
				application.updateNumCopies(appMsg, appMsg.getNumCopies() - 1);
			}
		}
	}
	
	public void spreadAndWait(ApplicationMsg msg) {
		Set<InetAddress> notForwarders = new HashSet<InetAddress>(neighbors.keySet());
		notForwarders.removeAll(Arrays.asList(youngestNeighbors));
		
		for(ApplicationMsg appMsg : application.getJobs()) {
			ApplicationMsg copyToSend = appMsg.duplicate();
			int numCopiesToSend =(int) Math.floor(((double)copyToSend.getNumCopies() - numberOfNeighbors)/3);
			for(InetAddress thisNeighbor : youngestNeighbors) {
				if(thisNeighbor == null) continue;
				copyToSend.setNumCopies(numCopiesToSend);
				sendMSGToPeer(copyToSend, thisNeighbor);
				application.updateNumCopies(appMsg, appMsg.getNumCopies() - numCopiesToSend);
			}
			for(InetAddress thisNeighbor : notForwarders) {
				copyToSend.setNumCopies(0); // FIXME: is "0" correct? Or "1"?
				sendMSGToPeer(copyToSend, thisNeighbor);
				application.updateNumCopies(appMsg, appMsg.getNumCopies() - 1);
			}
		}
	}
	
	public double setProbabilityTrasmission(int n){
		if(n != 0) return  1d/n;		// probabilità decrescente con numero di utenti
		else return 0;
	}
	
	public void probAlgorithm(ApplicationMsg msg){

		int k = 0;
		if (neighbors.size()!=0){
			double p = setProbabilityTrasmission(neighbors.size());
			Set<InetAddress> addrOk = new HashSet<InetAddress>();
			Set<InetAddress> addrLoses = new HashSet<InetAddress>();
		
				for (InetAddress i : neighbors.keySet())
					if (Math.random()< p){
						k++;
						addrOk.add(i);
					}
					else addrLoses.add(i);
				
				if (msg.getNumCopies()<k){   // TODO Idea alternativa mandare le copie ai primi k destinatari
					int d = msg.getNumCopies();				
					for (InetAddress f : addrOk){
					 
						if (d!=0){												
							msg.setNumCopies(1);
							sendMSGToPeer(msg, f);
							d--;
						}
						else {
							msg.setNumCopies(0);
							sendMSGToPeer(msg, f);
						}
					}
				
				}
				else {				
					msg.setNumCopies(msg.getNumCopies()/k);  // TODO controllo al crescere di k sulle copie potenzialmente perse
					sendProbabilisticMulticastMSG(msg, addrOk);
				}
			
		msg.setNumCopies(0);
		sendProbabilisticMulticastMSG(msg, addrLoses);
		}
		application.updateNumCopies(msg, 0);
	}
	
	public void sendProbabilisticMulticastMSG(ApplicationMsg msg, Set<InetAddress> adds) {
		
		if(adds!=null) {
			for(InetAddress address : adds) {
				//mandare solo ai vicini interessati
				sendMSGToPeer(msg, address);
			}
		}
	}
	
		public void sendMSGToPeer(ApplicationMsg msg, InetAddress dest) {
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

