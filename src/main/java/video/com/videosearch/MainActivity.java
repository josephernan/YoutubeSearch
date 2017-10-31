package video.com.videosearch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HttpDateGenerator;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity implements View.OnClickListener {

    private String[] itemTitle = new String[25];
    private String[] itemTime = new String[25];
    private String[] itemImage = new String[25];

    private ListView lst_main;
    private EditText edt_search;
    private Button btn_close;
    private Button btn_search;

    private String strSearchKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        edt_search = findViewById(R.id.edt_search);
        btn_close = findViewById(R.id.btn_close);
        btn_search = findViewById(R.id.btn_search);

        btn_close.setOnClickListener(this);
        btn_search.setOnClickListener(this);

        lst_main = findViewById(R.id.lst_main);
        //lst_main.setAdapter(new MyAdapter(this, itemTitle, itemTime, itemImage));

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_close) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            strSearchKey = edt_search.getText().toString();
        }
        else if (v.getId() == R.id.btn_search) {

            for ( int i = 0; i < 25;i ++ ) {
                itemTitle[i] = "";
                itemTime[i] = "";
                itemImage[i] = "";
            }

            getResultByKeyword();
            lst_main.setAdapter(new MyAdapter(this, itemTitle, itemTime, itemImage));
        }
    }

    private class MyAdapter extends ArrayAdapter<String> {

        private final Activity context;
        private final String[] itemTitle;
        private final String[] itemTime;
        private final String[] itemUrl;

        public MyAdapter(Activity context, String[] itemTitle, String[] itemTime, String[] itemUrl) {
            super(context, R.layout.search_result_view, itemTitle);

            this.context = context;
            this.itemTitle = itemTitle;
            this.itemTime = itemTime;
            this.itemUrl = itemUrl;
        }

        public View getView(int position,View view,ViewGroup parent) {
            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.search_result_view, null,true);

            TextView txtTitle = rowView.findViewById(R.id.txt_title);
            TextView txtTime = rowView.findViewById(R.id.txt_time);
            WebView imgThumb = rowView.findViewById(R.id.img_thumbView);

            txtTitle.setText(itemTitle[position]);
            txtTime.setText(itemTime[position]);

            try {
                imgThumb.loadUrl(itemUrl[position]);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (itemTitle[position] == "")
                return null;
            else
                return rowView;
        }
    }

    public Object getResultByKeyword() {

        strSearchKey = edt_search.getText().toString();

        if (strSearchKey.isEmpty())
            return null;

        String stringUrl = "https://www.googleapis.com/youtube/v3/search/?q=" + strSearchKey + "&maxResults=25&part=snippet&key=AIzaSyAQ7hg6ai_CZH2f5qvuyGcXwt9ruO4qgMo&videoDuration=any";

        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpGet request = new HttpGet(stringUrl);

        try {
            HttpResponse response = httpclient.execute(request);
            HttpEntity resEntity = response.getEntity();
            String _response = EntityUtils.toString(resEntity);

            JSONObject responseJSON = new JSONObject(_response);

            try {
                JSONArray items = responseJSON.getJSONArray("items");

                for(int i = 0; i < items.length(); i ++ ){
                    itemTitle[i] = items.getJSONObject(i).getJSONObject("snippet").get("title").toString();
                    itemTime[i] =  getDuration(items.getJSONObject(i).getJSONObject("id").get("videoId").toString());
                    itemImage[i] = items.getJSONObject(i).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").get("url").toString();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private String getDuration(String strIdVideo){
        String strRet = "";

        String watchUrl = "https://www.googleapis.com/youtube/v3/videos?id=" + strIdVideo + "&part=contentDetails&key=AIzaSyAQ7hg6ai_CZH2f5qvuyGcXwt9ruO4qgMo";

        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpGet request = new HttpGet(watchUrl);

        try {
            HttpResponse response = httpclient.execute(request);
            HttpEntity resEntity = response.getEntity();
            String _response = EntityUtils.toString(resEntity);

            JSONObject responseJSON = new JSONObject(_response);

            try {

                JSONArray items = responseJSON.getJSONArray("items");
                String strDuration = items.getJSONObject(0).getJSONObject("contentDetails").get("duration").toString();

                String strArrTMS[] = strDuration.split("T");
                String strTime = strArrTMS[0];
                String strMS[] = strArrTMS[1].split("M");
                String strMin = strMS[0];
                String strSec = strMS[1].substring(0, strMS[1].length() - 1);

                try{
                    Integer.parseInt(strTime);
                    strRet = strTime + " : ";
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    strRet = strRet;
                    try{
                        Integer.parseInt(strMin);
                        strRet = strRet + strMin + " : ";
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    finally {
                        try{
                            Integer.parseInt(strSec);
                            strRet = strRet + strSec;
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        finally {
                            return strRet;
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return strRet;
    }
}
