package newslite.com.newslite;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Comment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class DetailActivity extends ActionBarActivity {

    private static ListView listView;
    private static SwipeRefreshLayout refreshLayout;
    private static final String ACTIVITY_NAME = DetailActivity.class.getName();
    ArrayList<String> commentList = new ArrayList<>();
    ArrayList<CommentModel> commentModelList = new ArrayList<CommentModel>();
    private ArrayList<AsyncTask> cancelTask = new ArrayList<AsyncTask>();
    ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detail);
            startTransition();
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new PlaceholderFragment())
                        .commit();
            }

            commentList = getIntent().getStringArrayListExtra(ConstantManager.PASS);
        }catch(Exception e){
            Log.e(ACTIVITY_NAME,Log.getStackTraceString(e));
        }
    }

    private void startTransition() {
        try {

            overridePendingTransition(R.anim.pull_in_right,
                    R.anim.push_out_left);
        } catch (Exception e) {
            Log.e(ACTIVITY_NAME, Log.getStackTraceString(e));
        }
    }

    private void stopTransition() {
        try {

            overridePendingTransition(R.anim.pull_in_left,
                    R.anim.push_out_right);
        } catch (Exception e) {
            Log.e(ACTIVITY_NAME, Log.getStackTraceString(e));
        }
    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
            stopTransition();
        } catch (Exception e) {
            Log.e(ACTIVITY_NAME, Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onStart() {

        try {
            super.onStart();
            load();
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    load();
                    refreshLayout.setRefreshing(false);
                }
            });
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try{
                        startActivityWithExtras(position);
                    }catch(Exception e){
                        Log.e("Exception",Log.getStackTraceString(e));
                    }
                }
            });
        }catch(Exception e){
            Log.e(ACTIVITY_NAME,Log.getStackTraceString(e));
        }
    }

    private void startActivityWithExtras(int position) {

        try {
            Intent intent = new Intent(this, ReplyActivity.class);
            intent.putStringArrayListExtra(ConstantManager.PASS,commentModelList.get(position).getKids());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(ACTIVITY_NAME, Log.getStackTraceString(e));
        }
    }

    private void load(){

        try{
            if(UtilityManager.isNetworkAvailable(this)){
                getCommentList();
                showDialog();
            }else{
                Toast.makeText(this, "Please check internet connection", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Log.e(ACTIVITY_NAME,Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(cancelTask != null){
            for(AsyncTask task : cancelTask){
                try{
                    if(!task.isCancelled()){
                        task.cancel(true);
                    }
                }catch(Exception e){
                    Log.e("Exception",Log.getStackTraceString(e));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cancelTask != null){
            for(AsyncTask task : cancelTask){
                try{
                    if(!task.isCancelled()){
                        task.cancel(true);
                    }
                }catch(Exception e){
                    Log.e("Exception",Log.getStackTraceString(e));
                }
            }
        }
    }

    private void loadCommentList() {

        try {

            CommentListAdapter adapter = new CommentListAdapter(this,
                    commentModelList);
            listView.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(ACTIVITY_NAME, Log.getStackTraceString(e));
        }
    }

    private void getCommentList(){

        try{

            if(commentModelList != null){
                commentModelList.clear();
            }

            for(int i=0;i<commentList.size();i++){

                String id = commentList.get(i);
                AsyncTask asyncTask =  new LoadObject().execute(id);
                cancelTask.add(asyncTask);

                if(i==9){
                    break;
                }
            }
        }catch(Exception e){
            Log.e(ACTIVITY_NAME,Log.getStackTraceString(e));
        }
    }

    private void showDialog(){

        try {
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("loading");
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
        }catch(Exception e){
            Log.e(ACTIVITY_NAME,Log.getStackTraceString(e));
        }
    }

    private void cancelDialog(){
        if(pDialog != null){
            pDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = null;
            try {
                rootView = inflater.inflate(R.layout.fragment_detail, container, false);
                refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.comment_swipe_layout);
                listView = (ListView) rootView.findViewById(R.id.comment_list);
            }catch (Exception e){
                Log.e(ACTIVITY_NAME,Log.getStackTraceString(e));
            }
            return rootView;
        }
    }

    class LoadObject extends AsyncTask<String, Void, String> {
        CommentModel model = null;
        StringBuilder stringBuilder = new StringBuilder();
        @Override
        protected String doInBackground(String... params) {
            InputStream inputStream = null;
            String ids = "";
            try {

                String id = (String)params[0];
                String url = ConstantManager.TOP_STORIES_JSON+id+".json";

                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);

                HttpResponse responseString = httpClient.execute(httpGet);
                StatusLine statusLine = responseString.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = responseString.getEntity();
                    inputStream = entity.getContent();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                } else {
                    Log.d("JSON", "Failed to download file");
                }

                if(stringBuilder.length() > 0){
                    JSONObject response = new JSONObject(stringBuilder.toString());

                    String by = "";
                    if(response.has(ConstantManager.BY)) {
                        by = response.getString(ConstantManager.BY);
                    }

                    if(response.has(ConstantManager.ID)){
                        ids = response.getString(ConstantManager.ID);
                    }

                    ArrayList<String> kids = new ArrayList<String>();
                    if(response.has(ConstantManager.KIDS)) {
                        JSONArray kidsArray = response.getJSONArray(ConstantManager.KIDS);
                        for(int i=0;i<kidsArray.length();i++){
                            try {
                                kids.add(kidsArray.getString(i));
                            }catch(Exception e){
                                Log.e(ACTIVITY_NAME,Log.getStackTraceString(e));
                            }
                        }
                    }


                    String parent = "";
                    if(response.has(ConstantManager.PARENT)){
                        parent = response.getString(ConstantManager.PARENT);
                    }

                    String text = "";
                    if(response.has(ConstantManager.TEXT)) {
                        text = response.getString(ConstantManager.TEXT);
                    }

                    Long time = 0l;
                    if(response.has(ConstantManager.TIME)) {
                        time = Long.valueOf(response.getString(ConstantManager.TIME));
                    }


                    String type = "";
                    if(response.has(ConstantManager.TYPE)) {
                        type = response.getString(ConstantManager.TYPE);
                    }


                    model = new CommentModel();
                    model.setBy(by);
                    model.setId(ids);
                    model.setKids(kids);
                    model.setText(text);
                    model.setTime(time);
                    model.setType(type);

                    commentModelList.add(model);
                }
            } catch (Exception e) {
                Log.e(ACTIVITY_NAME, Log.getStackTraceString(e));
            }finally {
                if(inputStream != null){
                    try {
                        inputStream.close();
                    }catch (Exception e){

                    }
                }
            }
            return ids;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                super.onPostExecute(result);
                loadCommentList();
                cancelDialog();
            } catch (Exception e) {
                Log.e(ACTIVITY_NAME, Log.getStackTraceString(e));
            }
        }

    }
}
