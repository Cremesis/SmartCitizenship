package cnr.Common;

import cnr.Common.CallbackInterface;
import cnr.Common.ACChanges;
import cnr.Common.CAMEOOffering;
import cnr.Common.CAMEOObservation;
import cnr.Common.CAMEOObservationRequest;
import cnr.Common.CAMEOSensor;
import cnr.Common.UserContext;

/**
 * This is the main interface for applications to access CAMEO functionality 
 *
 * @author Claudio Scandura (cl.scandura@gmail.com)
 * @author Giovanni Minutiello (g.minutiello@iit.cnr.it)
 * @author Valerio Arnaboldi (v.arnaboldi@iit.cnr.it)
 */

interface PlatformInterface {

	/**
	 * Check if the user details are defined
	 *
	 * @return a boolean indicating whether user details are defined or not
	 */
	boolean isUserDefined();
	
	/**
	 * Ask CAMEO to start an activity for the management of user's profile data
	 *
	 */
	void startUserProfileActivity();
	
	/**
	 * Start CAMEO
	 *
	 * @return a boolean indicating if the platform started correctly
	 */
	boolean startCAMEO();
	
	/**
	 * Register an application on CAMEO
	 * 
	 * @param port The port that the application wants to use for communicating with CAMEO. Valid ports are integers between 0 and 1023, excluded 1001, that is used by CAMEO internal services
	 * @param callback The callback interface that the application wants to use to receive asynchronous messages/events from CAMEO
	 *
	 * @return a security key to be used by the application to access CAMEO functionality. This avoids applications without the key 
	 * to be able to send and receive data using an already used port. The key is equal to -1 if the registration has failed, for example
	 * if the port was already in use  
	 */
	long registerApplication(in int port, in CallbackInterface callback);
	
	/**
	 * Unregister the application and close the connection with CAMEO
	 * 
	 * @param securityKey The security key returned by CAMEO during registration
	 */
	void unregisterApplication(long securityKey);
	
	/**
	 * Retrieve the local user context from CAMEO
	 * 
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return the local user context
	 */
	UserContext getLocalUserContext(long securityKey);
	
	/**
	 * Retrieve the local application context from CAMEO
	 *
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return a map containing the local application context
	 */
	Map getLocalApplicationContext(long securityKey);
	
	/**
	 * Retrieve the remote application context of a specified node from CAMEO
	 *
	 * @param remoteIPAddress The ip address of the remote node
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return a map containing the remote application context
	 */
	Map getRemoteApplicationContext(in byte[] remoteIPAddress, long securityKey);
	
	/**
	 * Update local application context
	 *
	 * @param changes The changes made to the application context
	 * @param securityKey The security key returned by CAMEO during registration
	 */
	void updateApplicationContext(in ACChanges changes, long securityKey);
	
	/**
	 * Create a connection to a SOS server and perform a getCapabilities, saving the result into CAMEO data structures
	 *
	 * @param sosUrl The url of the SOS server
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return true if the connection has been established, false otherwise 
	 */
	boolean initializeSOSServerManager(String sosUrl, long securityKey);
	
	/**
	 * Retrieve the list of sensors from a SOS server
	 *
	 * @param sosUrl The url of the SOS server
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return The list of names of the sensors
	 */
	List<String> getAllSensorsNamesFromSOS(String sosUrl, long securityKey);
	
	/**
	 * Retrieve the list of offerings from a SOS server
	 *
	 * @param sosUrl The url of the SOS server
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return The list of offerings of the sensors
	 */
	List<CAMEOOffering> getOfferingsFromSOS(String sosUrl, long securityKey);
	
	/**
	 * Retrieve the list of observations from a SOS server
	 *
	 * @param sosUrl The url of the SOS server
	 * @param request The request containing the parameters for the query
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return The list of observations of the sensors
	 */
	List<CAMEOObservation> getObservationsFromSOS(String sosUrl, in CAMEOObservationRequest request, long securityKey);
	
	/**
	 * Retrieve the list of descriptions of a sensor from a SOS server
	 *
	 * @param sosUrl The url of the SOS server
	 * @param sensorName The name of the sensor
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return The list of descriptions of the sensor
	 */
	List<CAMEOSensor> getSensorFromSOS(String sosUrl, String sensorName, long securityKey);
	
	/**
	 * Register a new sensor on a SOS server
	 *
	 * @param sosUrl The url of the SOS server
	 * @param sensor The sensor to be registered on the server
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return The id assigned to the sensor by the SOS server
	 */
	String registerSensorOnSOS(String sosUrl, in CAMEOSensor sensor, long securityKey);
	
	/**
	 * Insert a new observation on a SOS server 
	 *
	 * @param sosUrl The url of the SOS server
	 * @param observation The observation to be inserted
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return true if the observation has been inserted, false otherwise
	 */
	boolean insertObservationOnSOS(String sosUrl, in CAMEOObservation observation, long securityKey);
	
	
	/**
	 * Sends a message over the network
	 * 
	 * @param packet The message that has to be sent
	 * @param dest The IP address of the destination
	 * @param broadcast The flag which indicates if the message has to be sent as a broadcast message (unreliable)
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return true if the request has been successfully accepted, false otherwise
	 */
	boolean sendMessage(in byte[] packet, in byte[] dest, boolean broadcast, long securityKey);
	
	/**
	 * Evaluate the utility function for one or more contents
	 * 
	 * @param ids The list of contents ids to evaluate
	 * @param securityKey The security key returned by CAMEO during registration
	 *
	 * @return a Bundle containing a map with utilities value for each content (key = MAP)
	 */
	 Bundle evaluateUtility(in List ids, long securityKey);
	 
}