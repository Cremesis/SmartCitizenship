package it.cnr.droidpark;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import cnr.Common.ApplicationContext;
import cnr.Common.CallbackInterface;
import cnr.Common.PlatformInterface;
import cnr.Common.UserContext;

public class ServiceDroidPark extends Service{
	
	private static final String TAG = "ServiceDroidPark";

	public static final int CAMEO_PORT = 33;
	public final static int ACTIVITY_BIND = 1; // msg che deve processare il service quando riceve il messenger dall'activity
	public final static int KILL_APP = 2; //per kill activity che killa anche il service perche' connessa con una bind
	public final static int USER = 3;
	public final static int SEND_QUEUE_MSG = 101;
	public final static int PERFECT_FORWARDER_CHECK = 102;
	public final static int ADD_CONTEXT_VALUE = 103;
	//public final static int CREATED_REMOTE_ROOM = 102; // msg derivato dall'evento di CAMEO
	//public final static int REMOVED_LOCAL_ROOM = 103; // input dall'utente
	//public final static int REMOVED_REMOTE_ROOM = 104; // dovuto a un cambio del contesto
	//public final static int FOLLOW_REMOTE_ROOM = 105; // input dall'utente
	
	//public final static int ROOM_MSG = 106;// contenuto della room chat creato localmente
	//public final static int DISPLAY_CHAT_MSGS = 107;
	
	private ApplicationContext appContext;   // Map<ContextKey, Boolean>
	
	private long CAMEOAppKey;
	
	private Set<InetAddress> notInQueue;
	private Set<InetAddress> inQueue;
	
	private Hashtable<InetAddress, HashMap<ContextKey, Boolean>> neighbors; // Neighbors ApplicationContext that use this application

	//private Hashtable<Integer, ArrayList<InetAddress>> activeChats;
	//private Hashtable<Integer, ArrayList<String>> roomMsg;
	//private Hashtable<Integer, ArrayList<ChatMsg>> localMessages; // struttura dati locale per mantenere i messaggi delle varie room 
	
	private final Messenger incomingMessenger = new Messenger(new IncomingHandler());
	private Messenger mActivity;
	
	private ApplicationDroidPark application;

	PlatformInterface cameo = null;
	boolean connectedToCameo = false;
	
	private int numberOfNeighbors; // Neighbors that use CAMEO, not this specific application
	private Hashtable<InetAddress, UserContext> neighborsUserContext;

	public Integer localuser;
	
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
		
		inQueue = new HashSet<InetAddress>();
		notInQueue = new HashSet<InetAddress>();
		neighbors = new Hashtable<InetAddress, HashMap<ContextKey,Boolean>>();
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
	
	/**
	 * Return in which queue is the local user
	 * @return the queue, or null if she is not in queue
	 */
	public Attraction whichQueue() {
		if(appContext.getValue(ContextKey.QUEUE_1.ordinal()) != null) return Attraction.GAME_1;
		if(appContext.getValue(ContextKey.QUEUE_2.ordinal()) != null) return Attraction.GAME_2;
		if(appContext.getValue(ContextKey.QUEUE_3.ordinal()) != null) return Attraction.GAME_3;
		if(appContext.getValue(ContextKey.QUEUE_4.ordinal()) != null) return Attraction.GAME_4;
		return null;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return incomingMessenger.getBinder();
	}

	@Override
	public void onDestroy() { // deregistro app da cameo e un binding
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

	private final CallbackInterface.Stub callback = new CallbackInterface.Stub() { //da passare a CAMEO Stub della Callback interface 

		@Override
		public void neighborApplicationContextUpdated(Map arg0, byte[] arg1)//cambiamento al contesto applicativo di un nodo
				throws RemoteException {
			Log.d(TAG, "neighborApplicationContextUpdated()");
			try {
				InetAddress thisNeighbor = InetAddress.getByAddress(arg1);
				
				// Assumed that the neighbor context is already present locally
				HashMap<ContextKey, Boolean> currentContext = neighbors.get(thisNeighbor);
				
				Set<Entry<ContextKey, Boolean>> updatedSet = (Set<Entry<ContextKey, Boolean>>) arg0.entrySet();
				for (Entry<ContextKey, Boolean> entry : updatedSet){ // sulle mappe non ci sono gli iterator quindi devo trasformarli in array
					
					Message msg = Message.obtain();//messaggio vuoto del messenger per poi comunicare con l'activity
					
					if(entry.getValue() == null) { // Remove "null" values from the context and add the others
						currentContext.remove(entry.getKey());
					} else {
						currentContext.put(entry.getKey(), true);
					}
					
					switch(entry.getKey()) {
						case QUEUE_1:
						case QUEUE_2:
						case QUEUE_3:
						case QUEUE_4: {
							// TODO: We know in which attraction the user is in queue. What do we do with this?
						}
						break;
						
						case PREF_1: {
							// TODO: Send out opinion about the "1" attraction if we have it
						}
						break;
						
						case PREF_2: {
							// TODO: Send out opinion about the "2" attraction 
						}
						break;
						
						case PREF_3: {
							// TODO: Send out opinion about the "3" attraction 
						}
						break;
						
						case PREF_4: {
							// TODO: Send out opinion about the "4" attraction 
						}
						break;
						
						case SAW: {
							notInQueue.add(thisNeighbor);
							inQueue.remove(thisNeighbor);
						}
						break;
						
						case WMH: {
							inQueue.add(thisNeighbor);
							notInQueue.remove(thisNeighbor);
						}
						break;
						
						default:
							Log.e(TAG, "Context not recognized: " + entry.getKey());
					}
					
					/*msg.arg1 = entry.getKey(); //roomID
					ArrayList<InetAddress> list = activeChats.get(msg.arg1);
					if(list==null){
						list=new ArrayList<InetAddress>();
						activeChats.put(msg.arg1, list);
					}
						
					Log.e("FRANCA", "entry:"+entry.getKey()+" "+entry.getValue());
					if (entry.getValue() == null){//nodo remoto ha cancellato quella stanza
						currentContext.remove(entry.getKey()); //rimozione dalla struttura dati locale
						list.remove(InetAddress.getByAddress(arg1));
						activeChats.put(msg.arg1, list);
						msg.what = REMOVED_REMOTE_ROOM; // messaggio per rimuovere la stanza dall'interfaccia grafica
					}
					else{
						currentContext.put(entry.getKey(), entry.getValue());
						list.add(InetAddress.getByAddress(arg1));
						activeChats.put(msg.arg1, list);
						msg.what = CREATED_REMOTE_ROOM;
						msg.obj = entry.getValue();
						Log.e("FRANCA", "service send CREATE_REMOTE_ROOM");
					}
					mActivity.send(msg);*/
				}
				
			} catch (UnknownHostException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		
		@Override
		public void neighborIn(UserContext arg0, byte[] arg1)
				throws RemoteException {
			Log.d(TAG, "neighborIn");
			try {
				if(arg0.isEmpty()) return; // when it's the first time that a user enters our range and that we see her
				Log.d(TAG, "id: " + arg0.hashCode() + " - name: " + arg0.getName() + " - age: " + arg0.getAge());
				InetAddress thisNeighbor = InetAddress.getByAddress(arg1);
				neighborsUserContext.put(thisNeighbor, arg0);
				
				// TODO: check and eventually save this neighbor if it is the youngest (keep maximum two)

//				if(remoteContext != null && remoteContext.get(ContextKey.WMH)) {
//					notInQueue.add(thisNeighbor);
//				} else {
//					inQueue.add(thisNeighbor);
//				}
				
				numberOfNeighbors = neighborsUserContext.size();
				
				Log.d(TAG, "Number of Neighbors: " + numberOfNeighbors);
			} catch (UnknownHostException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}

		@Override
		public void neighborOut(byte[] arg0) throws RemoteException {
			Log.d(TAG, "neighborOut");
			try {
				InetAddress thisNeighbor = InetAddress.getByAddress(arg0);
				Log.d(TAG, "name: " + neighborsUserContext.get(thisNeighbor).getName());
				
				// TODO: check and eventually delete this neighbor from the youngest,
				// and then add the third more young (if exists) in its place
				
				neighborsUserContext.remove(thisNeighbor);
				notInQueue.remove(thisNeighbor);
				inQueue.remove(thisNeighbor);
				neighbors.remove(thisNeighbor);
				
				numberOfNeighbors = neighborsUserContext.size();
				Log.d(TAG, "Number of Neighbors: " + numberOfNeighbors);
			} catch (UnknownHostException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}

		/**
		 * I can't figure out how to say to do the same thing that the neighborIn does. So I replicated its code.
		 */
		@Override
		public void neighborUserContextUpdated(UserContext remoteUserContext, byte[] arg1)
				throws RemoteException {
			Log.d(TAG, "neighborUserContextUpdated");
			try {
				InetAddress thisNeighbor = InetAddress.getByAddress(arg1);
				neighborsUserContext.put(thisNeighbor, remoteUserContext);
				
				// TODO: check and eventually save this neighbor if it is the youngest (keep maximum two)
				
				numberOfNeighbors = neighborsUserContext.size();
				Log.d(TAG, "id: " + remoteUserContext.hashCode() + " - name: " + remoteUserContext.getName() + " - age: " + remoteUserContext.getAge());
				Log.d(TAG, "Number of Neighbors: " + numberOfNeighbors);
			} catch(UnknownHostException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}

		@Override
		public void onCommunityChanged(String arg0) throws RemoteException {
			// TODO Auto-generated method stub
			// Don't care
		}
		

		@Override
		public void onMessageReceived(byte[] arg0, byte[] arg1)
				throws RemoteException {
			try {
			// condizione su tipo di algoritmo
			// ipotesi Probabilistic Transmission
			// Evito di ritrasmetterlo a lui
			
			InetAddress remoteAddr = InetAddress.getByAddress(arg1);
			QueueMsg qm = (QueueMsg) readObject(arg0);
			probAlgorithm(qm);
			// TODO Gestione messaggio in arrivo, servir� una condizione su tipo di messaggio e sulla presenza in coda per switchare tra un algoritmo in coda e l'altro
				
			/*ChatMsg chatmsg = (ChatMsg) readObject(arg0);
			ArrayList<String> messages;
			synchronized(roomMsg){
				if(!roomMsg.containsKey((chatmsg.getRoomName()).hashCode())){
					messages = new ArrayList<String>();
				}
				else
					messages = roomMsg.get((chatmsg.getRoomName()).hashCode());
			
				messages.add((String)chatmsg.getContent());
				roomMsg.put((chatmsg.getRoomName()).hashCode(), messages);
			}
			Message msg = Message.obtain();
			msg.obj = messages;
			msg.arg1 = (chatmsg.getRoomName()).hashCode();
			msg.what = DISPLAY_CHAT_MSGS;
			mActivity.send(msg);
			*/
			} catch(UnknownHostException e) {
				Log.e(TAG, Log.getStackTraceString(e));
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
			
			// FIXME: temporary context
			appContext.addValue(ContextKey.PREF_1.ordinal(), true);
			appContext.update(cameo, CAMEOAppKey);
			
			Log.d(TAG, "appContext updated");
			
			localuser= (cameo.getLocalUserContext(CAMEOAppKey)).hashCode();
			if(mActivity!=null){
				Log.d(TAG, "Local user id: " + localuser);
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
			//die
			Message msg = Message.obtain();
			msg.what = KILL_APP;
			try {
				mActivity.send(msg);
			} catch (RemoteException e1) {
				Log.e(TAG, Log.getStackTraceString(e1));
			}
		}
	}
	
	private class IncomingHandler extends Handler { //gestisce la comunicazione tra i messenger di actvity e service (dall'activity al service)
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ACTIVITY_BIND:{
					mActivity = msg.replyTo; //msg di servizio tra activity e service per permettere callback
				}
				break;
				
				case SEND_QUEUE_MSG:{ // QueueMsg generated by the user. You have to send it to everyone.
					Log.d(TAG, "SEND_QUEUE_MSG received");
					QueueMsg queue = msg.getData().getParcelable("queue");
					appContext.removeValue(ContextKey.QUEUE_1.ordinal());
					appContext.removeValue(ContextKey.QUEUE_2.ordinal());
					appContext.removeValue(ContextKey.QUEUE_3.ordinal());
					appContext.removeValue(ContextKey.QUEUE_4.ordinal());
					appContext.removeValue(ContextKey.WMH.ordinal());
					appContext.addValue(ContextKey.SAW.ordinal(), true);
					try {
						appContext.update(cameo, CAMEOAppKey);
						Log.e(TAG, "Queue updated");
					} catch (RemoteException e) {
						Log.e(TAG, Log.getStackTraceString(e));
					}
					
					sendMulticastMSG(queue);
				}
				break;
				
				case PERFECT_FORWARDER_CHECK:{ // messaggio ricevuto dall'app quando entro in coda e voglio cedere le copie dei messaggi
					Log.d(TAG, "PERFECT_FORWARDER_CHECK received");
					
					if (appContext.getValuesMap().containsKey(ContextKey.SAW.ordinal())){ //&& {  // TODO condizione sul perfect forwarder
						//cercare un nuovo perfect forwarder tra i miei vicini NON IN CODA e passargli le copie
					if(notInQueue.size() != 0){
						int age_min = 1000;
						InetAddress addr_age_min;
						for(InetAddress i : notInQueue){
							UserContext uc = neighborsUserContext.get(i);
								if (uc.getAge() < age_min) {
									age_min = uc.getAge();
									addr_age_min = i;
								}
														
						}
								//passo le copie al pi� giovane funz(addr_age_min) 
					}
	
					else {
								// vedere se il messaggio riguarda la coda in cui sto entrando se s� lo elimino 
								  //              altrimenti lo invio secondo l'algoritmo probabilistico
					 	}
					}
				}
				
				case ADD_CONTEXT_VALUE:{ //messaggio inviato dall'activity quando spunto una room remota per segnalare il mio interesse
					// TODO
					/*
					String roomName = msg.obj.toString();
					appContext.addValue(roomName.hashCode(), roomName);
					try {
						appContext.update(cameo, CAMEOAppKey);
					} catch (RemoteException e) {
						Log.e(TAG, Log.getStackTraceString(e));
					}
					*/
				}
				break;
				/*
				case ROOM_MSG:{
					Bundle b = msg.getData();
					String room = b.getString("room");
					String content = b.getString("content");
	
					ChatMsg message = new ChatMsg(room, content);
					
					ArrayList<String> texts = new ArrayList<String>();
					synchronized(roomMsg){
					if(!roomMsg.containsKey(room.hashCode())){
						roomMsg.put(room.hashCode(), texts);
						
					}
					texts = roomMsg.get(room.hashCode());
					texts.add(content);
					roomMsg.put(room.hashCode(), texts);
					}
					sendMulticastMSG(message, room.hashCode());
				}
				break;
				*/
				default:
					super.handleMessage(msg);
				}
		}
	}
	
public double setProbabilityTrasmission(int n){
		return  Math.exp(1-n);		// probabilità esponenziale decrescente con numero di utenti
	}
	
	public void probAlgorithm(ApplicationMsg msg){
		// TODO: @Filippo: non capisco perché tu valuti solo il numero
		// di vicini in coda. Teoricamente, da quello che avevo capito, dovresti
		// considerare tutti i vicini a prescindere da dove sono per stimare la
		// probabilità di trasmettere. Inoltre anche chi non è in coda potrebbe
		// essere interessato a ricevere i pacchetti che mandi dalla coda. No?
		// Infatti ho avuto un dubbio quando mi hai detto che avevi bisogno
		// dell'insieme dei vicini "inQueue". Se nello Spread and Wait ha senso
		// che io scelga il miglior forwarder tra i miei vicini NON in coda
		// (perché così gestiscono le copie multiple), nel tuo caso penso che
		// non ci siano differenze.
		double p = setProbabilityTrasmission(inQueue.size());
		if (inQueue.size()!=0){
			for (InetAddress i : inQueue)
				if (Math.random()< p){
					sendMSGToPeer(msg, i);
				}
		}
	}

		public void sendMulticastMSG(ApplicationMsg msg) {
			Set<InetAddress> adds = neighborsUserContext.keySet();

			if(adds!=null) { //qui ho cambiato l'utilizzo dell'iteratore
				for(InetAddress address : adds) {//qui c'è nullpointerexception perché activechats non ha roomid
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

