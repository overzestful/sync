package net.sourceforge.zbar.android.CameraTest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Created by overzestful on 8/5/2558.
 */
public class SelectMode  extends Activity {

    private Toast toast;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_select_mode);



        /*tv_footer=(TextView)findViewById(R.id.tv_footer);
        //tv_footer.setText("ยินดีต้อนรับเข้าสู่ Activity Recording System with QR Code");
        tv_footer.setSelected(true);
*/


       ImageButton ButtonOffline = (ImageButton)findViewById(R.id.offline);
       ImageButton ButtonOnline = (ImageButton)findViewById(R.id.online);



    }


    //ปุ่ม menu home

    public boolean onCreateOptionsMenu ( Menu menu )
    {
        MenuItem item1 = menu.add ( 0, MainWebserv.HOME, Menu.NONE, "Home" );

        item1.setIcon ( R.drawable.contentmenu_home );


        MenuItem item2 = menu.add ( 0, MainWebserv.ABOUTUS, Menu.NONE, "" );

        item2.setIcon ( R.drawable.contentmenu_aboutus );


        return super.onCreateOptionsMenu ( menu );
    }

    public boolean onOptionsItemSelected ( MenuItem item )
    {
        int itemID = item.getItemId ( );


        if ( itemID == MainWebserv.HOME )
        {
            Intent intent = new Intent(SelectMode.this, SelectMode.class);
            //  setContentView(R.layout.main_book);
            startActivity(intent);


        }
        else if ( itemID == MainWebserv.ABOUTUS )
        {
            Intent intent = new Intent(SelectMode.this, AddActivityWebserv.class);
            //  setContentView(R.layout.main_book);
            startActivity(intent);
        }


        return super.onOptionsItemSelected ( item );
    }




    public void offline(View v) {
        Intent intent = new Intent(SelectMode.this, Main.class);
        //  setContentView(R.layout.main_book);
        startActivity(intent);


    }

    public void online(View v){
        Intent intent = new Intent(SelectMode.this, MainWebserv.class);
        // setContentView(R.layout.activity_viewall);
        startActivity(intent);


    }
    private long lastBackPressTime = 0;
    @Override
    public void onBackPressed(){
        if(this.lastBackPressTime < System.currentTimeMillis() - 2000){
            toast = Toast.makeText(this, "กดปุ่ม Back อีกครั้งเพื่อปิดแอพลิเคชั่น", 2000);
            toast.show();
            this.lastBackPressTime = System.currentTimeMillis();
        }else{
            if(toast != null){
                toast.cancel();
            }
            super.onBackPressed();
        }
    }






}


