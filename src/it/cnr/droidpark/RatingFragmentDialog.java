package it.cnr.droidpark;

import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;


public class RatingFragmentDialog extends DialogFragment{
	
	public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, boolean b);
        public void onDialogNegativeClick(DialogFragment dialog, boolean b);
        public void onDialogNeutralClick(DialogFragment dialog);
    }

	NoticeDialogListener mListener;
	 
		
	public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        			
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
           
        builder.setTitle("Lascia il tuo giudizio")
        
               .setPositiveButton("Valuta e Commenta", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   mListener.onDialogPositiveClick(RatingFragmentDialog.this,false);
                   }
               })
        
               .setNegativeButton("Valuta", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
               	   mListener.onDialogNegativeClick(RatingFragmentDialog.this,false);
               	   
                   }
               })
        		
               .setNeutralButton("Non adesso", new DialogInterface.OnClickListener() {			
					public void onClick(DialogInterface dialog, int which) {
						mListener.onDialogNeutralClick(RatingFragmentDialog.this);
			
					}
               });
				
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.rating_fragment_dialog, null);
                builder.setView(view);
                
                float previousRating;
                String previousOpinion;
                if(ActivityDroidPark.outOfQueueComment){
                	previousRating = getPreviousRating(ActivityDroidPark.lastGameId);
                	previousOpinion = getPreivousOpinion(ActivityDroidPark.lastGameId);
                } else {
                	previousRating = getPreviousRating(ActivityDroidPark.lastPressedGameButton);
                	previousOpinion = getPreivousOpinion(ActivityDroidPark.lastPressedGameButton);
                }
                ((RatingBar)view.findViewById(R.id.ratingBar1)).setRating(previousRating);
                ((EditText)view.findViewById(R.id.editText1)).setText(previousOpinion);
               
        return builder.create();
        
    }
	
	private String getPreivousOpinion(int gameID) {
		ApplicationDroidPark application = (ApplicationDroidPark) getActivity().getApplication();
		Opinion previousOpinion = application.getGameOpinion(gameID, application.localuser);
		if(previousOpinion != null)
			return previousOpinion.getMsg();
		else
			return "";
	}
	
	private float getPreviousRating(int gameID) {
		ApplicationDroidPark application = (ApplicationDroidPark) getActivity().getApplication();
		Map<Integer, RatingMsg> map = application.getRatingMsg(gameID);
    	if(map!=null){
    		RatingMsg rating = map.get(application.localuser);
    		if(rating != null) {
    			return rating.getEval();
    		}
    	}
    	return 0f;
	}

}
