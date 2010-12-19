package at.barcamp.cleancity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import at.barcamp.cleancity.R;


public class GUITools {
	/**
	 * Zeigt den übergebenen Text in einem Dialog an, welcher durch OK zu bestätigen ist.
	 * @author stefan
	 * @param Activity
	 * @param Text
	 */
	public static void showOKDialog(Activity a, int msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(a);
	    	builder.setMessage(msg)
	    	       .setCancelable(true)
	    	       .setNeutralButton(R.string.label_ok, new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
    	                //Dialog1.this.finish();
	    	           }
	    	       });
	    	@SuppressWarnings("unused")
			AlertDialog alert = builder.create();
	    	builder.show();
	}
	
	/**
	 * Zeigt den übergebenen Text in einem Dialog an, welcher durch OK zu bestätigen ist.
	 * @author stefan
	 * @param Activity
	 * @param Text
	 */
	public static void showOKDialog(Activity a, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(a);
	    	builder.setMessage(msg)
	    	       .setCancelable(true)
	    	       .setNeutralButton(R.string.label_ok, new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
    	                //Dialog1.this.finish();
	    	           }
	    	       });
	    	@SuppressWarnings("unused")
			AlertDialog alert = builder.create();
	    	builder.show();
	}
}
