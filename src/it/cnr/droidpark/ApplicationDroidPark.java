package it.cnr.droidpark;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Application;
import android.util.Log;

public class ApplicationDroidPark extends Application {
	
	// TODO: persistence of the data missing
	
    private static final String TAG = "ApplicationDroidPark";
	public final Integer NUMBER_OF_GAMES = 4;
	public final Integer NUMBER_OF_COPIES = 1000;
	
	// Attractions ID (for preferences)
	public static final int GAME_1 = 1;
	public static final int GAME_2 = 2;
	public static final int GAME_3 = 3;
	public static final int GAME_4 = 4;

	private Set<ApplicationMsg> jobs;
	public int localuser;

	private Map<Integer, QueueMsg> queueList; // < IDgioco, QueueMsg > 
	private Map<Integer, Map<Integer, RatingMsg>> ratingList; // < IDgioco, <IDutente,RatingMsg> >
	private Map<Integer, Map<Integer, Opinion>> opinionList; // < IDgioco, <IDutente,Opinion> >
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		jobs = new HashSet<ApplicationMsg>();
		queueList = new Hashtable<Integer, QueueMsg>(NUMBER_OF_GAMES);
		ratingList = new Hashtable<Integer, Map<Integer, RatingMsg>>(NUMBER_OF_GAMES);
		opinionList = new Hashtable<Integer, Map<Integer, Opinion>>(NUMBER_OF_GAMES);
	}
	
	public float getRatingAverage(Integer gameID) {
		double average = 0f;
		int numGameRatings = 0;
		
		Set<Entry<Integer, RatingMsg>> currentGameRatings = ratingList.get(gameID).entrySet();
		for(Entry<Integer, RatingMsg> entry : currentGameRatings) {
			average += entry.getValue().getEval();
			numGameRatings++;
		}
		
		return (float) (average / numGameRatings);
	}
	
	public Map<Integer, RatingMsg> getRatingMsg(Integer gameID) {
		return ratingList.get(gameID);
	}
	
	public QueueMsg getQueueMsg(Integer gameID) {
		return queueList.get(gameID);
	}
	
	public Opinion getGameOpinion(Integer gameID, Integer userID) {
		Map<Integer, Opinion> gameOpinions = opinionList.get(gameID);
		if(gameOpinions != null)
			return gameOpinions.get(userID);
		else
			return null;
	}
	
	public Map<Integer, Opinion> getAllGameOpinions(Integer gameID) {
		Map<Integer, Opinion> map = opinionList.get(gameID);
		if(map != null)
			return opinionList.get(gameID);
		else 
			return new Hashtable<Integer, Opinion>();
	}
	
	public Set<ApplicationMsg> getJobs(){
		return new HashSet<ApplicationMsg>(jobs);
	}
	
	/**
	 * Change the numCopies of an ApplicationMsg. Use this function to update
	 * correctly the job list, don't do it by yourself! If you want to insert a
	 * new msg that you have produced or received, use <code>insertRating</code>
	 * or <code>insertQueue</code>. This function is used only to manage
	 * previous messages, got from <code>getJobs</code>
	 * 
	 * @param msg
	 * @param newNumCopies
	 */
	public void updateNumCopies(ApplicationMsg msg, int newNumCopies) {
		msg.setNumCopies(newNumCopies);
		if(newNumCopies <= 0) {
			jobs.remove(msg);
		}
	}
	
	/**
	 * Insert the opinion. If there is an opinion of the same user about the
	 * same game, keep the most recent one only
	 * 
	 * @param gameID
	 * @param userID
	 * @param opinion
	 * @return true if the opinion was inserted/updated (because more recent or new),
	 *         false otherwise
	 */
	public boolean insertUpdateOpinion(Integer gameID, Integer userID, Opinion opinion) {
		boolean rv = true;
		Map<Integer, Opinion> gameOpinions = opinionList.get(gameID);
		if(gameOpinions != null) {
			Opinion currentUserOpinion = gameOpinions.get(userID);
			// false when don't have that opinion or mine is more recent
			rv = currentUserOpinion != null && currentUserOpinion.getTimestamp().compareTo(opinion.getTimestamp()) >= 0;
		} else {
			Log.d(TAG, "inserted/updated opinion");
			
			// Insert new opinion
			gameOpinions = new Hashtable<Integer, Opinion>();
			opinionList.put(gameID, gameOpinions);
		}
		gameOpinions.put(userID, opinion);
		return rv;
	}
	
	/**
	 * Insert the rating. If there is a rating of the same user about the same
	 * game, keep the most recent one only. If the user, game and timestamp are
	 * the same, sums the copies
	 * 
	 * @param gameID
	 * @param userID
	 * @param rating
	 * @return true if the rating is new
	 * 
	 */
	public boolean insertRating(Integer gameID, Integer userID, RatingMsg rating) {
		boolean rv = true;
		Map<Integer, RatingMsg> gameRatings = ratingList.get(gameID);
		RatingMsg currentUserRating = null;
		int ratingCopies = rating.getNumCopies();
		if(gameRatings != null) {
			currentUserRating = gameRatings.get(userID);
			if(currentUserRating != null) {
				int compare = currentUserRating.getTimestamp().compareTo(rating.getTimestamp());
				if(compare > 0) { // The local rating is newer than the "new" one. Don't do anything
					Log.d(TAG, "\"new\" rating is older than present");
					rating = currentUserRating;
					rv = false;
				} else if(compare == 0) { // The local rating is the same of the "new" one. Sum the copies.
					ratingCopies += currentUserRating.getNumCopies();
					Log.d(TAG, "added copies in rating");
					rv = false;
				} else {
					Log.d(TAG, "inserting new rating message");
					if(ratingCopies > 0) 
						ratingCopies--;
				}
			} else if(ratingCopies > 0) 
					ratingCopies--;
		} else {
			gameRatings = new Hashtable<Integer, RatingMsg>();
			ratingList.put(gameID, gameRatings);
			if(ratingCopies > 0) 
				ratingCopies--;
		}
		
		gameRatings.put(userID, rating);

		if(currentUserRating != null)
			jobs.remove(currentUserRating);
		rating.setNumCopies(ratingCopies);
		if(ratingCopies > 0)
			jobs.add(rating);
		
		return rv;
	}
	
	/**
	 * Insert the queue. If there is a queue of the same game, keep the most
	 * recent one only. If the game and timestamp are the same, sums the
	 * copies
	 * 
	 * @param gameID
	 * @param queue
	 * @return true if the queue is new
	 * 
	 */
	public boolean insertQueue(Integer gameID, QueueMsg queue) {
		boolean rv = true;
		QueueMsg currentQueue = queueList.get(gameID);
		int queueCopies = queue.getNumCopies();
		if(currentQueue != null) {
			int compare = currentQueue.getTimestamp().compareTo(queue.getTimestamp());
			if(compare > 0) { // The local queue is newer than the "new" one. Don't do anything
				Log.d(TAG, "\"new\" queue is older than present");
				queue = currentQueue;
				rv = false;
			} else if(compare == 0) { // The local queue is the same of the "new" one. Sum the copies.
				queueCopies += currentQueue.getNumCopies();
				Log.d(TAG, "added copies in queue");
				rv = false;
			} else {
				Log.d(TAG, "inserting new queue message");
				if(queueCopies > 0) 
					queueCopies--;
			}
		} else if(queueCopies > 0) 
			queueCopies--;

		queueList.put(gameID, queue);
		if(currentQueue != null)
			jobs.remove(currentQueue);
		queue.setNumCopies(queueCopies);
		if(queueCopies > 0)
			jobs.add(queue);
		
		return rv;
	}
}
