package net.sourceforge.zbar.android.CameraTest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends Activity {


	//DB Class to perform DB related operations
	DBController controller = new DBController(this);
	//Progress Dialog Object
	ProgressDialog prgDialog;


    private static final int TAKE_PICTURE = 100;
    private static final int TAKE_PICTURE_SAVE = 101;
    private File imageFile;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.prgguru.example.R.layout.activity_main);
		//Get User records from SQLite DB

		ArrayList<HashMap<String, String>> userList =  controller.getAllUsers();
		//
		if(userList.size()!=0){
			//Set the User Array list in ListView
			ListAdapter adapter = new SimpleAdapter( MainActivity.this,userList, com.prgguru.example.R.layout.view_user_entry, new String[] { "userId","userName"}, new int[] {com.prgguru.example.R.id.userId, com.prgguru.example.R.id.userName});
			ListView myList=(ListView)findViewById(android.R.id.list);
			myList.setAdapter(adapter);
			//Display Sync status of SQLite DB
			Toast.makeText(getApplicationContext(), controller.getSyncStatus(), Toast.LENGTH_LONG).show();
		}
		//Initialize Progress Dialog properties
		prgDialog = new ProgressDialog(this);
		prgDialog.setMessage("Synching SQLite Data with Remote MySQL DB. Please wait...");
		prgDialog.setCancelable(false);


        Button btnCaptureSave = (Button) findViewById(R.id.Pic);
        btnCaptureSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                imageFile = new File(Environment.getExternalStorageDirectory(), "my_image.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(intent, TAKE_PICTURE_SAVE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView image = (ImageView) findViewById(R.id.image);

        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            Bitmap capturedImage = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(capturedImage);
        } else if (requestCode == TAKE_PICTURE_SAVE && resultCode == RESULT_OK) {
            try {
                FileInputStream fis = new FileInputStream(imageFile);
                //Bitmap fullSizeImage = BitmapFactory.decodeStream(fis);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap fullSizeImage = BitmapFactory.decodeStream(fis, null, options);

                image.setImageBitmap(fullSizeImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(com.prgguru.example.R.menu.main_sync, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//When Sync action button is clicked
		if (id == com.prgguru.example.R.id.refresh) {
			//Sync SQLite DB data to remote MySQL DB
			syncSQLiteMySQLDB();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	//Add User method getting called on clicking (+) button



/*	public void takePic(View view) {
		Intent objIntent = new Intent(getApplicationContext(), CaptureCamera.class);
		startActivity(objIntent);
	}*/
	
	public void syncSQLiteMySQLDB(){
		//Create AsycHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		ArrayList<HashMap<String, String>> userList =  controller.getAllUsers();
		if(userList.size()!=0){
			if(controller.dbSyncCount() != 0){
				prgDialog.show();
				params.put("usersJSON", controller.composeJSONfromSQLite());
				client.post("http://qrcodeactivity.seniorproject-te.com/sqlitemysqlsync/insertuser.php",params ,new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						System.out.println(response);
						prgDialog.hide();
						try {
							JSONArray arr = new JSONArray(response);
							System.out.println(arr.length());
							for(int i=0; i<arr.length();i++){
								JSONObject obj = (JSONObject)arr.get(i);
								System.out.println(obj.get("id"));
								System.out.println(obj.get("status"));
								controller.updateSyncStatus(obj.get("id").toString(),obj.get("status").toString());
							}
							Toast.makeText(getApplicationContext(), "DB Sync completed!", Toast.LENGTH_LONG).show();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
							e.printStackTrace();
						}
					}
		    
					@Override
					public void onFailure(int statusCode, Throwable error,
						String content) {
						// TODO Auto-generated method stub
						prgDialog.hide();
						if(statusCode == 404){
							Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
						}else if(statusCode == 500){
							Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
						}
					}
				});
			}else{
				Toast.makeText(getApplicationContext(), "SQLite and Remote MySQL DBs are in Sync!", Toast.LENGTH_LONG).show();
			}
		}else{
				Toast.makeText(getApplicationContext(), "No data in SQLite DB, please do enter User name to perform Sync action", Toast.LENGTH_LONG).show();
		}
	}


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MainActivity.this, CameraTestActivityWebserv.class);
        //  setContentView(R.layout.main_book);
        startActivity(intent);

    }


}

