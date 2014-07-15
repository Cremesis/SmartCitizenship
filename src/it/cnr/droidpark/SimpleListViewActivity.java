package it.cnr.droidpark;


import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;


public class SimpleListViewActivity extends Activity {  
    
	  private ListView mainListView ;  
	  private ArrayAdapter<String> listAdapter ;  
	  private ApplicationDroidPark application;
	    
	  /** Called when the activity is first created. */  
	  @Override  
	  public void onCreate(Bundle savedInstanceState) {  
	    super.onCreate(savedInstanceState);  
	    setContentView(R.layout.comment_list);  
	      
	    // Find the ListView resource.   
	    mainListView = (ListView) findViewById( R.id.mainListView );  
	  
	    // Create and populate a List of planet names.  
	    String[] planets = new String[] { "Mercury", "Venus", "Earth", "Mars",  
	                                      "Jupiter", "Saturn", "Uranus", "Neptune"};    
	    ArrayList<String> planetList = new ArrayList<String>();  
	    planetList.addAll( Arrays.asList(planets) );  
	    
	    
	      
	    // Create ArrayAdapter using the planet list.  
	    listAdapter = new ArrayAdapter<String>(this, R.layout.simple_raw_list, planetList);  
	      
	    // Add more planets. If you passed a String[] instead of a List<String>   
	    // into the ArrayAdapter constructor, you must not add more items.   
	    // Otherwise an exception will occur.  
	    listAdapter.add( "Ceres" );  
	  
	      
	    // Set the ArrayAdapter as the ListView's adapter.  
	    mainListView.setAdapter( listAdapter );        
	  }  
	}