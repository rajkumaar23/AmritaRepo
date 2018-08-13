package in.co.rajkumaar.amritarepo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SemesterActivity extends AppCompatActivity {

    String semUrl;
    String externLink;
    int statusCode;
    List<String> sems=new ArrayList<>();
    List<String> links=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new clearCache().clear();
        String protocol=getString(R.string.protocol);
        String cloudSpace=getString(R.string.clouDspace);
        String amrita=getString(R.string.amrita);
        String port=getString(R.string.port);
        externLink=protocol+cloudSpace+amrita+port;
        String xmlUi=getString(R.string.xmlUi);
        String numbers=getString(R.string.numbers);
        semUrl=externLink+xmlUi+numbers;
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        Bundle b=getIntent().getExtras();
        int course=Integer.parseInt(""+b.get("course"));
        this.setTitle(""+b.get("pageTitle"));
        setContentView(R.layout.list_view);
        TextView textView=findViewById(R.id.empty_view);
        textView.setVisibility(View.GONE);

        TextView wifiwarning=findViewById(R.id.wifiwarning);
        wifiwarning.setVisibility(View.GONE);
        ImageView imageView=findViewById(R.id.empty_imageview);
        imageView.setVisibility(View.GONE);
        switch (course)
        {
            case 1 : semUrl+="150"; break;
            case 2 : semUrl+="893"; break;
            case 3 : semUrl+="894"; break;
            case 4 : semUrl+="903"; break;
            case 5 : semUrl+="331"; break;
            case 6 : semUrl+="393"; break;
            case 7 : semUrl+="279"; break;

        }


        new Load().execute();

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(R.color.colorAccent);
        final ListView listView=findViewById(R.id.list);
        listView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                swipeLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                SemesterActivity.this.recreate();

            }



        });




    }

    private class Load extends AsyncTask<Void,Void,Void> {
        String proxy=getString(R.string.proxyurl);
        Document document=null;
        @Override
        protected Void doInBackground(Void... voids) {
            sems.clear();
            links.clear();

            try {

                // Connect to the web site
                statusCode = Jsoup.connect(semUrl).execute().statusCode();
                document = Jsoup.connect(semUrl).get();
            }
            catch (IOException e1)
            {
                try{
                    document=Jsoup.connect(proxy).method(Connection.Method.POST).data("data", semUrl).execute().parse();
                    statusCode=Jsoup.connect(proxy).method(Connection.Method.POST).data("data", semUrl).execute().statusCode();
                }catch (IOException e2)
                {

                    e2.printStackTrace();
                }
                e1.printStackTrace();}
            finally {
                if(document!=null){
                Elements elements = document.select("div[id=aspect_artifactbrowser_CommunityViewer_div_community-view]").select("ul[xmlns:i18n=http://apache.org/cocoon/i18n/2.1]").get(0).select("a[href]");
                for (int i = 0; i < elements.size(); ++i) {
                    sems.add(elements.get(i).text());
                    links.add(elements.get(i).attr("href"));
                }}
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(document==null)
                SemesterActivity.this.recreate();
            ProgressBar progressBar=findViewById(R.id.loading_indicator);
            progressBar.setVisibility(View.GONE);
            if(statusCode!=200)
            {
                TextView emptyView=findViewById(R.id.empty_view);
                emptyView.setVisibility(View.VISIBLE);
                ImageView imageView=findViewById(R.id.empty_imageview);
                imageView.setVisibility(View.VISIBLE);

                TextView wifiwarning=findViewById(R.id.wifiwarning);
                wifiwarning.setVisibility(View.VISIBLE);
            }
            else{

                ImageView imageView=findViewById(R.id.empty_imageview);
                imageView.setVisibility(View.GONE);
            TextView textView=findViewById(R.id.empty_view);
            textView.setVisibility(View.GONE);

                TextView wifiwarning=findViewById(R.id.wifiwarning);
                wifiwarning.setVisibility(View.GONE);
            ListView listView=findViewById(R.id.list);
            ArrayAdapter<String> semsAdapter=new ArrayAdapter<String>(SemesterActivity.this,android.R.layout.simple_list_item_1,sems);
            listView.setAdapter(semsAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent=new Intent(SemesterActivity.this,AssessmentsActivity.class);
                    intent.putExtra("href",externLink+links.get(i));
                    intent.putExtra("pageTitle",sems.get(i));
                    startActivity(intent);
                }
            });

            listView.setVisibility(View.VISIBLE);}
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}