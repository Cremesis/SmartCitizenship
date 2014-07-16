package it.cnr.droidpark;


import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class SimpleListViewActivity extends Activity {  
    
	  private ListView mainListView ;  
	  private ArrayAdapter<String> listAdapter ;  
	  private ApplicationDroidPark application;
	  
	  
	    
	  /** Called when the activity is first created. */  
	  @Override  
	  public void onCreate(Bundle savedInstanceState) {  
	    super.onCreate(savedInstanceState);  
	    setContentView(R.layout.comment_list); 
	    
	    application = (ApplicationDroidPark) getApplication();
	      
	    // Find the ListView resource.   
	    mainListView = (ListView) findViewById( R.id.mainListView );  

	    ArrayList<String> comments = new ArrayList<String>();
	    
	    Collection<Opinion> collection = application.getAllGameOpinions(ActivityDroidPark.lastPressedGameButton).values();
	    
	    if(collection.size()!=0){
	    	Collection<Opinion> opinions = (Collection<Opinion>) application.getAllGameOpinions(ActivityDroidPark.lastPressedGameButton).values();
	    		for (Opinion i : opinions){
	    				comments.add(i.getMsg());
	    		}
	    }else {
	    	comments.add("Nessun Commento presente per questo gioco");
	    }

 
	    listAdapter = new ArrayAdapter<String>(this, R.layout.simple_raw_list, comments);  
  
	    mainListView.setAdapter( listAdapter ); 
	  }  
	}