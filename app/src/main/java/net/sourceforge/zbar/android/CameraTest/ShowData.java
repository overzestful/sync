package net.sourceforge.zbar.android.CameraTest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ShowData extends Activity {

    private TextView output;
    private Button del;
    private Button back;
    //DB Class to perform DB related operations
    DBController controller = new DBController(this);
    //Progress Dialog Object
    ProgressDialog prgDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        super.onCreate(savedInstanceState);


        setContentView(R.layout.showdata);
        //Get User records from SQLite DB
        ArrayList<HashMap<String, String>> userList = controller.getAllUsers();
        output = (TextView) findViewById(R.id.textView1);
        del = (Button) findViewById(R.id.delete);
        back = (Button) findViewById(R.id.back);
        if (userList.size() != 0) {
            //Set the User Array list in ListView
            ListAdapter adapter = new SimpleAdapter(ShowData.this, userList, com.prgguru.example.R.layout.view_user_entry, new String[]{"userId", "userName"}, new int[]{com.prgguru.example.R.id.userId, com.prgguru.example.R.id.userName});
            ListView myList = (ListView) findViewById(android.R.id.list);
            myList.setAdapter(adapter);
            //Display Sync status of SQLite DB
            Toast.makeText(getApplicationContext(), controller.getSyncStatus(), Toast.LENGTH_LONG).show();
        }
        //Initialize Progress Dialog properties
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Synching SQLite Data with Remote MySQL DB. Please wait...");
        prgDialog.setCancelable(false);







        try {
            final String CREATE_TABLE_CONTAIN = "CREATE TABLE IF NOT EXISTS users ("  // �������� table person ���ӡ�����ҧ
                    + "userId INTEGER primary key AUTOINCREMENT,"   // ��Ŵ� _id ����� PK
                    + "userName TEXT,"
                    + "udpateStatus TEXT);";// ��Ŵ� name ��Դ Text

            CameraTestActivity.db.execSQL(CREATE_TABLE_CONTAIN);
            //  Toast.makeText(getBaseContext(), "Table person is created.",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Error," + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        showdata();


        del.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_clicked();
            }
        });

        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowData.this, CameraTestActivity.class);
                startActivity(intent);
            }
        });


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
    public void addUser(View view) {
        Intent objIntent = new Intent(getApplicationContext(), NewUser.class);
        startActivity(objIntent);
    }

    public void syncSQLiteMySQLDB(){
        //Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> userList =  controller.getAllUsers();
        if(userList.size()!=0){
            if(controller.dbSyncCount() != 0){
                prgDialog.show();
                params.put("usersJSON", controller.composeJSONfromSQLite());
                client.post("http://10.0.3.2/sqlitemysqlsync/insertuser.php",params ,new AsyncHttpResponseHandler() {
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

    public void delete_clicked() {
        String sql = "delete from users ";
        CameraTestActivity.db.execSQL(sql);
        showdata();
    }


    private void showdata() {
        output.setText("");
        SQLiteCursor cur = (SQLiteCursor) CameraTestActivity.db.rawQuery("select * from users", null);
        cur.moveToFirst();
        while (cur.isAfterLast() == false) {
            output.append(" รหัสนักศึกษา : " + cur.getString(1) + "\n");
            cur.moveToNext();
        }
        cur.close();

    }


}
