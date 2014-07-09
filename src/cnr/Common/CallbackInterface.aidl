package cnr.Common;

import cnr.Common.UserContext;

/**
 * The AIDL interface that the applications must implement in order to correctly receive
 * messages and events coming from CAMEO
 *
 * @author Giovanni Minutiello (g.minutiello@iit.cnr.it)
 * @author Valerio Arnaboldi (v.arnaboldi@iit.cnr.it)
 */

interface CallbackInterface {
	
	/**
	 * Notifies the application that a new node has entered the 1-hop neighborhood 
	 * 
	 * @param remoteUserContext The user context of the new neighbor, if CAMEO already owns it, or null otherwise
	 * @param remoteAddress The ip address of the new neighbor
	 */
	void neighborIn(in UserContext remoteUserContext, in byte[] remoteAddress);
	
	/**
	 * Notifies the application that a new node has quit the 1-hop neighborhood 
	 * 
	 * @param remoteAddress The ip address of the neighbor
	 */
	void neighborOut(in byte[] remoteAddress);
	
	/**
	 * Notifies the application that a neighbor has updated its user context
	 * 
	 * @param remoteUserContext The neighbor user context
	 * @param remoteAddress The ip address of the neighbor
	 */
	void neighborUserContextUpdated(in UserContext remoteUserContext, in byte[] remoteAddress);
	
	/**
	 * Notifies the application that a neighbor has updated his application context
	 * 
	 * @param remoteAppContextChanges Changes to the neighbor application context
	 * @param remoteAddress The ip address of the neighbor
	 */
	void neighborApplicationContextUpdated(in Map remoteAppContextChanges, in byte[] remoteAddress);
	
	/**
	 * A new message has been received from a remote node
	 * 
	 * @param packet The message received from the network
	 * @param source The IP address of the sender
	 *
	 */
	void onMessageReceived(in byte[] packet, in byte[] source);
	
	/**
	 * Notifies the application that the community has changed
	 * 
	 * @param newCommunityName The name of the new community
	 *
	 */
	void onCommunityChanged(in String newCommunityName);
}