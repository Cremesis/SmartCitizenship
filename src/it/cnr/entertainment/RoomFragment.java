package it.cnr.entertainment;

import it.cnr.proximity.R;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

public class RoomFragment extends ListFragment {
	/**
	 * Create a new instance of RoomFragment
	 */
	
	public ArrayAdapter<String> adapterRoom; 
	public ArrayList<String> comments;
	private String roomID;

	
//	public static RoomFragment newInstance(String room) {
//		RoomFragment f = new RoomFragment();
//		
//		// Supply index input as an argument.
//		Bundle args = new Bundle();
//		args.putString("roomID",room);
//		f.setArguments(args);
//			
//
//		return f;
//	}
	
	public RoomFragment() {
		comments = new ArrayList<String>();
	}

	public void publishMsg(String room, ArrayList<String> msgs){
		roomID = room;
		comments.clear();
		if(msgs!=null){
			comments.addAll(msgs);
		}
		if(adapterRoom!=null)
			adapterRoom.notifyDataSetChanged();
		
	}
	
	public String getRoomID (){
		return roomID;
	}
	
	public void setRoomID (String room){
		roomID = room;
	}

	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		adapterRoom = new ArrayAdapter<String>(getActivity(), R.layout.msg_list_item, R.id.textView2, comments);
		setListAdapter(adapterRoom);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_room, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	//	roomID = getArguments().getString("roomID");
		Button button = (Button) this.getActivity().findViewById(R.id.add_msg);
		button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText box = (EditText) getActivity().findViewById(R.id.edit_messages);
				String content = box.getText().toString();
				box.setText("");
				ActivityDroidPark act = (ActivityDroidPark)getActivity();
				content = act.localuser+": "+content;
				comments.add(content);
				adapterRoom.notifyDataSetChanged();
				
		//		act.publishRoomMsg(content, roomID);
			}
			
		});
	}
	
	
}