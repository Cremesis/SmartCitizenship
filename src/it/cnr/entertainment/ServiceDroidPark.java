package it.cnr.entertainment;

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
	//public final static int CREATED_LOCAL_ROOM = 101; // input dall'utente
	//public final static int CREATED_REMOTE_ROOM = 102; // msg derivato dall'evento di CAMEO
	//public final static int REMOVED_LOCAL_ROOM = 103; // input dall'utente
	//public final static int REMOVED_REMOTE_ROOM = 104; // dovuto a un cambio del contesto
	//public final static int FOLLOW_REMOTE_ROOM = 105; // input dall'utente
	
	//public final static int ROOM_MSG = 106;// contenuto della room chat creato localmente
	//public final static int DISPLAY_CHAT_MSGS = 107;
	
	private ApplicationContext appContext;   // Map<Integer,List<Boolean>> Integer indica il gioco se si � in coda o -1 altrimenti
												// La lista indica gli interessi ai giochi in base alla posizione. Controllo dim MAp ==1
	private long CAMEOAppKey;
	
	private Set<InetAddress> notInQueue;
	private Hashtable<InetAddress, Hashtable<ContextKey, Boolean>> neighbors;// Contesto applicativo dei vicini
	private Hashtable<InetAddress, UserContext> neighborsUserContext;
	
	//private Hashtable<Integer, ArrayList<InetAddress>> activeChats;
	//private Hashtable<Integer, ArrayList<String>> roomMsg;
	//private Hashtable<Integer, ArrayList<ChatMsg>> localMessages; // struttura dati locale per mantenere i messaggi delle varie room 
	
	private final Messenger incomingMessenger = new Messenger(new IncomingHandler());
	private Messenger mActivity;
	
	private ApplicationDroidPark application;

	PlatformInterface cameo = null;
	boolean connectedToCameo = false;
	
	private static int numberOfNeighbors;

	public Integer localuser;
	
	ServiceConnection sc = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) { //gestione connessione a CAMEO che e' lui stesso un service
			cameo = PlatformInterface.Stub.asInterface(service);
			connectedToCameo = true;
			registerApp(); //registro la mia app
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			connectedToCameo = false;
		}

	};
	
	@Override
	public void onCreate() {
		
		application = (ApplicationDroidPark) getApplication();
		
		notInQueue = new HashSet<InetAddress>();
		neighbors = new Hashtable<InetAddress, Hashtable<ContextKey,Boolean>>();
		neighborsUserContext = new Hashtable<InetAddress, UserContext>();

		if (!bindService(new Intent("cnr.CAMEO.PLATFORM"), sc, Context.BIND_AUTO_CREATE)){ // bind con il service
			Toast.makeText(this, "Can't connect to CAMEO", Toast.LENGTH_SHORT).show();
			Message msg = Message.obtain();
			msg.what = KILL_APP;
			
			try {
				mActivity.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
		
		}
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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
				Hashtable<ContextKey, Boolean> currentContext = neighbors.get(thisNeighbor);
				
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
						}
						break;
						
						case WMH: {
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
				e.printStackTrace();
			}
		}
		
		@Override
		public void neighborIn(UserContext arg0, byte[] arg1)
				throws RemoteException {
			try {
				InetAddress thisNeighbor = InetAddress.getByAddress(arg1);
				neighborsUserContext.put(thisNeighbor, arg0);
				
				// TODO: check and eventually save this neighbor if it is the youngest (keep maximum two)
				
				Hashtable<ContextKey, Boolean> remoteContext = (Hashtable<ContextKey, Boolean>) cameo.getRemoteApplicationContext(arg1, CAMEOAppKey);
				neighbors.put(thisNeighbor, remoteContext);
				
				if(remoteContext.get(ContextKey.WMH))
					notInQueue.add(thisNeighbor);
				
				numberOfNeighbors = neighbors.size();
				Log.d(TAG, "NeighborIn: id - " + arg0.hashCode() + " - age: " + arg0.getAge());
				Log.d(TAG, "Number of Neighbors: " + numberOfNeighbors);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void neighborOut(byte[] arg0) throws RemoteException {
			try {
				InetAddress thisNeighbor = InetAddress.getByAddress(arg0);
				
				// TODO: check and eventually delete this neighbor from the youngest,
				// and then add the third more young (if exists) in its place
				
				neighborsUserContext.remove(thisNeighbor);
				notInQueue.remove(thisNeighbor);
				neighbors.remove(thisNeighbor);
				
				numberOfNeighbors = neighbors.size();
				Log.d(TAG, "NeighborOut: id - " + arg0.hashCode());
				Log.d(TAG, "Number of Neighbors: " + numberOfNeighbors);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void neighborUserContextUpdated(UserContext remoteUserContext, byte[] arg1)
				throws RemoteException {
			try {
				InetAddress thisNeighbor = InetAddress.getByAddress(arg1);
				
				neighborsUserContext.remove(thisNeighbor);
				neighborsUserContext.put(thisNeighbor, remoteUserContext);
				
				// TODO: need to check if it is the youngest now...

				Log.d(TAG, "neighborUserContextUpdated: id - " + remoteUserContext.hashCode());
			} catch (UnknownHostException e) {
				e.printStackTrace();
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
			// TODO Gestione messaggio in arrivo, servir� una condizione su tipo di messaggio e sulla presenza in coda per switchare tra un algoritmo in coda e l'altro
	//		InetAddress remoteAdd;
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
			localuser= (cameo.getLocalUserContext(CAMEOAppKey)).hashCode();
			if(mActivity!=null){
				Log.d(TAG, "Local user id: " + localuser);
				Message msg = Message.obtain();
				msg.what = USER;
				msg.obj = localuser;
				try {
					mActivity.send(msg);
				} catch (RemoteException e1) {
					e1.printStackTrace();
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
				e1.printStackTrace();
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
				/*
				case CREATED_LOCAL_ROOM:{ //msg ricevuto dall'app quando crea una nuova stanza
					String roomName = msg.getData().getString("name");
					appContext.addValue(roomName.hashCode(), roomName); //aggiungo la stanza al contesto dell'applicazione
					try {
						appContext.update(cameo, CAMEOAppKey);
						Log.e("FRANCA", "APP context updated with a new room");
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
				}
				break;
				
				case FOLLOW_REMOTE_ROOM:{ //messaggio inviato dall'activity quando spunto una room remota per segnalare il mio interesse
					String roomName = msg.obj.toString();
					appContext.addValue(roomName.hashCode(), roomName);
					try {
						appContext.update(cameo, CAMEOAppKey);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				break;
				
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

		public void sendMulticastMSG(ChatMsg msg, Integer roomID) {
			/*
			ArrayList<InetAddress> adds = activeChats.get(roomID);

			if(adds!=null) { //qui ho cambiato l'utilizzo dell'iteratore
				Iterator<InetAddress> it=adds.iterator();
				while (it.hasNext()) //qui c'è nullpointerexception perché activechats non ha roomid
				{
					InetAddress address=it.next();
					//mandare solo ai vicini interessati
					sendMSGToPeer(msg, address);
				}
			}*/
		}

		public void sendMSGToPeer(ChatMsg msg, InetAddress dest) {
			/*try {
				boolean result=cameo.sendMessage(writeObject(msg),
						dest.getAddress(), false, CAMEOAppKey);
				if(!result)
					Log.e("ProximityChat", "error with CAMEO");
			} catch (
					RemoteException re) {
				Log.e("ProximityChat", "Got exception while sending message to peer: "+
						Log.getStackTraceString(re));
			}*/
			
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
				e.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException ex) {
					// ignore close exception
					ex.printStackTrace();
				}
				try {
					bos.close();
				} catch (IOException ex) {
					// ignore close exception
					ex.printStackTrace();
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
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					bis.close();
				} catch (IOException ex) {
					// ignore close exception
					ex.printStackTrace();
				}
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
					// ignore close exception
					ex.printStackTrace();
				}
			}
			return o;
		}


	}

