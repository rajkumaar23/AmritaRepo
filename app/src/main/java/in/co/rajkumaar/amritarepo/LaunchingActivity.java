package in.co.rajkumaar.amritarepo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
public class LaunchingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(LaunchingActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LaunchingActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
        new clearCache().clear();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        setContentView(R.layout.activity_launching);


        // Spinner element
        final Spinner spinner = (Spinner) findViewById(R.id.semspinner);

        // Spinner Drop down elements
        final List<String> categories = new ArrayList<String>();
        categories.add("[Choose your course]");
        categories.add("B.Tech");
        categories.add("BA Communication");
        categories.add("MA Communication");
        categories.add("Integrated MSc & MA");
        categories.add("MCA");
        categories.add("MSW");
        categories.add("M.Tech");


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item1, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_home));
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);

//        Button linkDownloads=findViewById(R.id.linkDownloads);
//        linkDownloads.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Intent(LaunchingActivity.this,DownloadsActivity.class);
//            }
//        });
        Button button=findViewById(R.id.sembutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos=spinner.getSelectedItemPosition();
                if(isNetworkAvailable()){
                        if(pos>0)
                        {
                            Intent intent=new Intent(LaunchingActivity.this,SemesterActivity.class);
                            intent.putExtra("course",pos);
                            intent.putExtra("pageTitle",categories.get(pos));
                            startActivity(intent);
                        }
                        else {
                            Snackbar.make(view, "You haven't selected any course.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        }
                }
                else{
                    Snackbar.make(view,"Device not connected to Internet.",Snackbar.LENGTH_SHORT).show();
                }

            }
        });





    }

    @Override
    protected void onDestroy() {
        new clearCache().clear();
        super.onDestroy();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void showSnackbar(String message) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar
                .make(parentLayout, message, Snackbar.LENGTH_SHORT);
        //snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray));
        //((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        snackbar.show();
    }
    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.nav_home);
            } else {
            super.onBackPressed();}
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        showSnackbar("Please click BACK again to exit");
        //Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                doubleBackToExitPressedOnce=false;
            }
        }, 2000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sem_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent it = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","rajkumaar2304@gmail.com", null));
            it.putExtra(Intent.EXTRA_SUBJECT, "Regarding Bug in Amrita Repository App");
            it.putExtra(Intent.EXTRA_EMAIL, new String[] {"rajkumaar2304@gmail.com"});
            //it.setType("text/plain");
            if(it.resolveActivity(getPackageManager())!=null)
            startActivity(it);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_downloads) {

            startActivity(new Intent(this, DownloadsActivity.class));
        }

        else if(id==R.id.nav_timetable)
            {
                startActivity(new Intent(this,TimetableActivity.class));
            }
            else if(id==R.id.nav_facultytimetable){
            startActivity(new Intent(this,FacultyTimetable.class));
        }
        else if(id==R.id.nav_campuswifi)
            if(isNetworkAvailable())
            startActivity(new Intent(this,WifiStatus.class));
        else
            showSnackbar("Device not connected to internet");


         else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Find all question papers under one roof. Download Amrita Repository, the must-have app for exam preparation " +
                            " : https://play.google.com/store/apps/details?id="+getPackageName());
            sendIntent.setType("text/plain");
            if(sendIntent.resolveActivity(getPackageManager())!=null)
            {
                startActivity(sendIntent);
            }
            else{
                Toast.makeText(this,"Error.",Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this,AboutActivity.class));

        }
        else if(id==R.id.nav_review){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName()));
            if(intent.resolveActivity(getPackageManager())!=null)
            {
                startActivity(intent);
            }
            else{
                Toast.makeText(this,"Error Opening Play Store.",Toast.LENGTH_SHORT).show();
            }


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}