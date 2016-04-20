package com.example.fkrt.tophelf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class TagForPlace extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Intent intent;
    Bundle bundle;

    private RelativeLayout inner0;
    private SearchView searchView;
    private TextView place, rating, placeInfoV;
    private Button placeInfo, comments, map;
    private ListView commentsV, searchList, votes;
    private ImageView mapV;
    SharedPreferences sharedPref;

    ArrayList<Relation> relations;

    private String p_name, p_id, p_info, p_loc;

    private String[] names, places, tags, commentsList, ratings, relationTimes, emails;

    String[] temp = {"#ankara", "#antalya", "#adana", "#bursa", "#istanbul", "#izmir", "#mersin", "#malatya", "#rize", "#erzurum"};
    int images = R.drawable.logo64;

    ArrayAdapter<String> arrayAdapter;

    private boolean isFB;
    private String user_name, u_id, fbID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isFB = sharedPref.getBoolean("isFB", false);
        user_name = sharedPref.getString("name", "N/A");
        u_id = sharedPref.getString("u_id", "N/A");
        fbID = sharedPref.getString("fbID","N/A");

        bundle = getIntent().getExtras();
        String nn = bundle.getString("name");
        p_name = bundle.getString("place");
        p_id = bundle.getString("placeID");
        p_info = bundle.getString("placeInfo");
        p_loc = bundle.getString("placeLoc");
        String tt = bundle.getString("tag");
        String rr = bundle.getString("rating");
        setTitle(nn);

        try {
            relations = new GetTimelineConn().execute(p_id).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        names = new String[relations.size()];
        places = new String[relations.size()];
        tags = new String[relations.size()];
        commentsList = new String[relations.size()];
        ratings = new String[relations.size()];
        relationTimes = new String[relations.size()];
        emails = new String[relations.size()];

        double overallRating = 0;

        for(int i = 0; i < relations.size(); i++) {
            names[i] = relations.get(i).getUsername();
            places[i] = relations.get(i).getP_id();
            tags[i] = relations.get(i).getT_id();
            commentsList[i] = relations.get(i).getC_id();
            ratings[i] = relations.get(i).getRating();
            relationTimes[i] = relations.get(i).getRelationTime();
            emails[i] = relations.get(i).getEmail();

            overallRating += Double.parseDouble(ratings[i]);
        }
        overallRating /= ratings.length;

        setContentView(R.layout.activity_tag_for_place);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(view.getContext(), VoteActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        inner0 = (RelativeLayout) findViewById(R.id.inner0);

        place = (TextView) findViewById(R.id.place);
        place.setText(p_name);
        rating = (TextView) findViewById(R.id.rating);
        rating.setText("Overall : " + overallRating);

        placeInfo = (Button) findViewById(R.id.placeinfo);
        comments = (Button) findViewById(R.id.comments);
        comments.setTextColor(Color.parseColor("#2D96C4"));
        comments.setTypeface(null, Typeface.BOLD);
        map = (Button) findViewById(R.id.map);

        placeInfoV = (TextView) findViewById(R.id.placeinfoV);
        placeInfoV.setText(p_info);
        commentsV = (ListView) findViewById(R.id.commentsV);
        ListRowAdapter listRowAdapter = new ListRowAdapter(this, images, names, places, tags, commentsList, ratings, relationTimes,emails);
        commentsV.setAdapter(listRowAdapter);
        mapV = (ImageView) findViewById(R.id.mapV);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, temp);
        searchList = (ListView) findViewById(R.id.searchlist);
        searchView = (SearchView) findViewById(R.id.searchbox);
        searchList.setAdapter(arrayAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchList.setVisibility(View.INVISIBLE);
                inner0.setVisibility(View.VISIBLE);

                if (query.charAt(0) == '@') {
                    String f_id = null;
                    try {
                        f_id = new GetFriendIdConn().execute(query.substring(1)).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    if(f_id.equals("-1")) {
                        intent = new Intent(getApplicationContext(), FriendActivity.class);
                        intent.putExtra("friend_id", f_id);
                        startActivity(intent);
                    }
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText != null) {
                    searchList.setVisibility(View.VISIBLE);
                    inner0.setVisibility(View.INVISIBLE);
                } else {
                    searchList.setVisibility(View.INVISIBLE);
                    inner0.setVisibility(View.VISIBLE);
                }
                arrayAdapter.getFilter().filter(newText);
                return false;
            }
        });

        View hView =  navigationView.getHeaderView(0);
        TextView name = (TextView)hView.findViewById(R.id.name);
        name.setText(user_name);

        if(isFB){
            ProfilePictureView imgvw = (ProfilePictureView)hView.findViewById(R.id.profilePicture);
            imgvw.setProfileId(fbID);

        }

    }

    // Place Info
    public void onClick(View v) {
        placeInfoV.setVisibility(View.VISIBLE);
        placeInfo.setEnabled(false);
        commentsV.setVisibility(View.INVISIBLE);
        comments.setEnabled(true);
        mapV.setVisibility(View.INVISIBLE);
        map.setEnabled(true);

        placeInfo.setTextColor(Color.parseColor("#2D96C4"));
        placeInfo.setTypeface(null, Typeface.BOLD);
        comments.setTextColor(Color.parseColor("#7cc3e1"));
        comments.setTypeface(null, Typeface.NORMAL);
        map.setTextColor(Color.parseColor("#7cc3e1"));
        map.setTypeface(null, Typeface.NORMAL);
    }

    // Comments
    public void onClick2(View v) {
        placeInfoV.setVisibility(View.INVISIBLE);
        placeInfo.setEnabled(true);
        commentsV.setVisibility(View.VISIBLE);
        comments.setEnabled(false);
        mapV.setVisibility(View.INVISIBLE);
        map.setEnabled(true);

        placeInfo.setTextColor(Color.parseColor("#7cc3e1"));
        placeInfo.setTypeface(null, Typeface.NORMAL);
        comments.setTextColor(Color.parseColor("#2D96C4"));
        comments.setTypeface(null, Typeface.BOLD);
        map.setTextColor(Color.parseColor("#7cc3e1"));
        map.setTypeface(null, Typeface.NORMAL);

    }
    // Map
    public void onClick3(View v) {

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("place_location", p_loc);
        editor.putString("place_name", p_name);
        editor.commit();

        intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tag_for_place, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            intent = new Intent(this, ProfileActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.nav_friends) {

        } else if (id == R.id.nav_votesComments) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_helpfeedback) {

        } else if (id == R.id.nav_logout) {
            if (isFB) {
                LoginManager.getInstance().logOut();
            } else {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("isLogin", false);
                editor.commit();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //  Server connectıon
    class GetFriendIdConn extends AsyncTask<String, Void, String> {
        int f_id = -1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String friend_name = params[0];

            try {
                URL url = new URL("http://" + getResources().getString(R.string.ip) + ":3000/"); // 192.168.1.24 --- 10.0.2.2 --- 139.179.211.68
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("type", "GetUserId");
                jsonParam.put("username", friend_name);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString()); // URLEncoder.encode(jsonParam.toString(), "UTF-8")
                writer.flush();
                writer.close();
                os.close();

                int statusCode = conn.getResponseCode();
                InputStream is = null;

                if (statusCode >= 200 && statusCode < 400) {
                    is = conn.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line, responseString;
                    StringBuffer response = new StringBuffer();
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                    }
                    rd.close();
                    responseString = response.toString();
                    responseString = responseString.substring(1, response.length() - 1);

                    jsonParam = new JSONObject(responseString);
                    f_id = Integer.parseInt(jsonParam.getString("u_id"));

                } else {
                    is = conn.getErrorStream();
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return Integer.toString(f_id);
        }

        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
        }
    }

    //  Server connectıon
    class GetTimelineConn extends AsyncTask<String, Void, ArrayList<Relation>>
    {
        ArrayList<Relation> relation = new ArrayList<Relation>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Relation> doInBackground(String... params) {
            String p_id = params[0];

            try {
                URL url = new URL("http://"+getResources().getString(R.string.ip)+":3000/"); // 192.168.1.24 --- 10.0.2.2 --- 139.179.211.68
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("type", "GetPlaceRelations");
                jsonParam.put("place_id", p_id);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString()); // URLEncoder.encode(jsonParam.toString(), "UTF-8")
                writer.flush();
                writer.close();
                os.close();

                int statusCode = conn.getResponseCode();
                InputStream is = null;

                if (statusCode >= 200 && statusCode < 400) {
                    // Create an InputStream in order to extract the response object
                    is = conn.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line, responseString;
                    StringBuffer response = new StringBuffer();
                    while((line = rd.readLine()) != null) {
                        response.append(line);
                    }
                    rd.close();
                    responseString = response.toString();
                    //responseString =responseString.substring(1, response.length() - 1);

                    JSONArray jsonarray = new JSONArray(responseString);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        jsonParam = jsonarray.getJSONObject(i);
                        relation.add(new Relation(jsonParam.getString("username"), jsonParam.getString("placename"), jsonParam.getString("tagname"),
                                jsonParam.getString("content"), jsonParam.getString("rating"), jsonParam.getString("relationtime"), jsonParam.getString("email")));
                    }

                    return relation;
                }
                else {
                    is = conn.getErrorStream();
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return relation;
        }

        @Override
        protected void onPostExecute(ArrayList<Relation> relation) {
            super.onPostExecute(relation);
        }
    }
}
