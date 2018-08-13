package in.co.rajkumaar.amritarepo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class FacultyTimetable extends AppCompatActivity {
    static ArrayList<String> res=new ArrayList<>();
    static String search=null;
    ProgressDialog dialog;
    String url;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty);
        new clearCache().clear();
        dialog= new ProgressDialog(FacultyTimetable.this);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        final Spinner year=findViewById(R.id.faculty_year);
        final Spinner sem=findViewById(R.id.faculty_sem);
        RelativeLayout facultypage=findViewById(R.id.faculty_page);
        facultypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
            }
        });
        year.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard();
                return false;
            }
        });
        sem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard();
                return false;
            }
        });
        ArrayList<String> years=new ArrayList<>();
        years.add("[Choose year]");
        years.add("2018_19");
        years.add("2019_20");
        years.add("2020_21");
        years.add("2021_22");
        final ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item1, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year.setAdapter(yearAdapter);


        ArrayList<String> sems=new ArrayList<>();
        sems.add("[Choose sem]");
        sems.add("Odd");
        sems.add("Even");
        ArrayAdapter<String> semAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item1, sems);
        semAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sem.setAdapter(semAdapter);
        final AutoCompleteTextView actv =  (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView1);
        actv.addTextChangedListener(new addListenerOnTextChange(this,actv));



        Button facultysubmit= findViewById(R.id.faculty_button);
        Button reset=findViewById(R.id.reset_button);
        facultysubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if(!isNetworkAvailable())
                    showSnackbar("Device not connected to internet");
                    //Toast.makeText(FacultyTimetable.this,"Device not connected to internet.",Toast.LENGTH_SHORT).show();
                else {
                    String name = null, acadsem;
                    int acadyear;
                    acadyear = year.getSelectedItemPosition();
                    name = actv.getText().toString();
                    if(actv.getText().toString().isEmpty() || acadyear==0 || sem.getSelectedItemPosition()==0)
                        showSnackbar(getString(R.string.incompletefield));
                        //Toast.makeText(FacultyTimetable.this,"Please complete all fields",Toast.LENGTH_SHORT).show();
                    else{
                    switch (sem.getSelectedItemPosition()) {
                        case 1:
                            acadsem = "O";
                            break;
                        case 2:
                            acadsem = "E";
                            break;
                        default:
                            acadsem = "O";
                    }
                        dialog.setMessage("Loading..");
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    new PostRequest().execute(name, yearAdapter.getItem(acadyear), acadsem,"1");
                        }
                }

            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actv.setText("");
            }
        });
        Button download=findViewById(R.id.faculty_download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                String name = null, acadsem;
                int acadyear;
                acadyear = year.getSelectedItemPosition();
                name = actv.getText().toString();
                switch (sem.getSelectedItemPosition()) {
                    case 1:
                        acadsem = "O";
                        break;
                    case 2:
                        acadsem = "E";
                        break;
                    default:
                        acadsem = "O";
                }
                if (!(actv.getText().toString().isEmpty() || acadyear == 0 || sem.getSelectedItemPosition() == 0) ){
                    if (ContextCompat.checkSelfPermission(FacultyTimetable.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(FacultyTimetable.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                1);
                    } else {
                        if (isNetworkAvailable()) {
                            Log.e("Download url",getString(R.string.facultyurl)+url);
                            new PostRequest().execute(name, yearAdapter.getItem(acadyear), acadsem,"2");


                                //Toast.makeText(FacultyTimetable.this,"Resource not found",Toast.LENGTH_SHORT).show();

                        } else {
                            Snackbar.make(view, "Device not connected to Internet.", Snackbar.LENGTH_SHORT).show();
                        }


                    }
                } else {
                    showSnackbar("Please input all fields");
                }
            }
        });



    }
    private void showSnackbar(String message) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar
                .make(parentLayout, message, Snackbar.LENGTH_SHORT);
        //snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray));
        //((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        snackbar.show();
    }
    public void hideKeyboard(){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onDestroy();
    }

    private class PostRequest extends AsyncTask<String,Void,Void>{

        Document doc1=null;
        int choice=0;

        @Override
        protected void onPostExecute(Void aVoid) {

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            Elements ele=doc1.select("iframe[src]");
            url=ele.attr("src");
            Log.e("URL",url);
            if(choice==1){
                if(url!=null && !url.isEmpty())
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.facultyurl)+url)));

                else
                    showSnackbar("Resource not found");
            }
            else if(choice==2){
                new DownloadTask(FacultyTimetable.this, getString(R.string.facultyurl)+url, 0);
            }

            //startActivity(new Intent(MainActivity.this,POSTResponse.class).putExtras(doc1));
        }

        @Override
        protected Void doInBackground(String... strings) {
            choice=Integer.parseInt(strings[3]);


            try {
                Connection.Response login = Jsoup.connect("http://intranet.cb.amrita.edu/TimeTable/Faculty/index.php")
                        .method(Connection.Method.POST)
                        .data("year", strings[1])
                        .data("sem", strings[2])
                        .data("Nyear", strings[1])
                        .data("Nsem", strings[2])
                        .data("NAMEshwbutton", "Show Details")
                        .data("faculty", strings[0].trim())
                        .execute();

            doc1 = login.parse();
        }catch(IOException e)

        {
            e.printStackTrace();
        }
                Log.e("POST RESULT", strings[0]+" "+strings[1]+" "+strings[2]);
            return null;
        }
    }

    private class Load extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... searching) {
            BufferedReader br ;
            try {
                URL url = new URL("http://intranet.cb.amrita.edu/TimeTable/Faculty/get_staff_list.php?q="+searching[0]);
                br = new BufferedReader(new InputStreamReader(url.openStream()));

                res.clear();
                String line;
                while ((line = br.readLine()) != null) {
                    res.add(line);
                }
                Log.e("Size of result ",String.valueOf(res.size()));
                int i=0;
                while (!res.isEmpty() && i<res.size()){
                    Log.e("Elements ",String.valueOf(res.get(i)));
                i++;}


                //for(int i=0;i<res.size();++i)

                //names[i]=result.get(i).text();


             //Log.e("GET RESULT", names[1]);

//
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AutoCompleteTextView actv =  (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView1);
            if(!res.isEmpty() && res!=null)
            {adapter= new ArrayAdapter<String>
                    (FacultyTimetable.this,android.R.layout.simple_spinner_dropdown_item,res);
            actv.setThreshold(1);
            actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
            actv.setTextColor(Color.BLACK);
            actv.showDropDown();}
            else{
                actv.dismissDropDown();
            }
        }
    }
    public class addListenerOnTextChange implements TextWatcher {
        private Context mContext;
        AutoCompleteTextView mEdittextview;

        public addListenerOnTextChange(Context context, AutoCompleteTextView edittextview) {
            super();
            this.mContext = context;
            this.mEdittextview= edittextview;
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            res.clear();
            mEdittextview.dismissDropDown();
            search=mEdittextview.getText().toString();
            new Load().execute(search);




        }
    }
}