package com.tophelf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.tophelf.R;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Created by FKRT on 20.04.2016.
 */
public class ListRowAdapter extends ArrayAdapter<String> {

    Intent intent;

    Context context;
    int images;
    String[] names;
    String[] ids;
    String[] places;
    String[] tags;
    String[] comments;
    String[] ratings;
    String[] relationTimes;
    String[] emails;
    String[] relation_ids;
    String[] ranks;
    boolean[] minus;
    boolean[] plus;
    double[] sorted;

    ListRowAdapter(Context context, int images, String[] names, String[] ids, String[] places, String[] tags, String[] comments,
                                        String[] ratings, String[] relationTimes, String[] emails, String[] relation_ids, String[] ranks) {
        super(context, R.layout.single_row, R.id.place, places);
        this.context = context;
        this.images = images;
        this.names = names;
        this.ids = ids;
        this.places = places;
        this.tags = tags;
        this.comments = comments;
        this.ratings = ratings;
        this.relationTimes = relationTimes;
        this.emails = emails;
        this.relation_ids = relation_ids;
        this.ranks = ranks;
        minus = new boolean[names.length];
        plus = new boolean[names.length];
        sorted = new double[relationTimes.length];

        sort();
    }

    public void sort(){
        int j;
        double key;
        String keyN, keyId, keyT,keyP,keyC,keyR,keyRe,keyE,keyRid;
        int i;
        for(i = 0; i < relationTimes.length; i++){
            sorted[i] = Double.parseDouble(relationTimes[i].substring(0, 4) + relationTimes[i].substring(5, 7) + relationTimes[i].substring(8, 10) +
                    relationTimes[i].substring(11, 13) + relationTimes[i].substring(14, 16) + relationTimes[i].substring(17, 19));
        }

        for (j = 1; j < sorted.length; j++) {
            key = sorted[j];
            keyN = names[j];
            keyId = ids[j];
            keyP = places[j];
            keyT = tags[j];
            keyC = comments[j];
            keyR = ratings[j];
            keyRe = relationTimes[j];
            keyE = emails[j];
            keyRid = relation_ids[j];
            for(i = j - 1; (i >= 0) && (sorted[i] < key); i--) {
                sorted[i+1] = sorted[i];
                names[i+1] = names[i];
                ids[i+1] = ids[i];
                places[i+1] = places[i];
                tags[i+1] = tags[i];
                comments[i+1] = comments[i];
                ratings[i+1] = ratings[i];
                relationTimes[i+1] = relationTimes[i];
                emails[i+1] = emails[i];
                relation_ids[i+1] = relation_ids[i];
            }
            sorted[i+1] = key;
            names[i+1] = keyN;
            ids[i+1] = keyId;
            places[i+1] = keyP;
            tags[i+1] = keyT;
            comments[i+1] = keyC;
            ratings[i+1] = keyR;
            relationTimes[i+1] = keyRe;
            emails[i+1] = keyE;
            relation_ids[i+1] = keyRid;

        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.single_row, parent, false);

        ProfilePictureView myImage = (ProfilePictureView) row.findViewById(R.id.image);
        TextView myName = (TextView) row.findViewById(R.id.name);
        final TextView myPlace = (TextView) row.findViewById(R.id.place);
        TextView myTag = (TextView) row.findViewById(R.id.tag);
        TextView myComment = (TextView) row.findViewById(R.id.comment);
        TextView myRating = (TextView) row.findViewById(R.id.rating);
        TextView myTime = (TextView) row.findViewById(R.id.time);
        final Button myMinus = (Button) row.findViewById(R.id.minus);
        final Button myPlus = (Button) row.findViewById(R.id.plus);

        if( !emails[position].contains("@") )
            myImage.setProfileId(emails[position]);


        if( comments[position] != null ) {
            myComment.setVisibility(View.VISIBLE);
            myComment.setText(comments[position]);
        }

        if(ranks != null) {
            if(ranks[0].equals("profile_activity")) {
                myPlus.setVisibility(View.INVISIBLE);
                myMinus.setVisibility(View.INVISIBLE);
            } else {
                for (int i = 0; i < ranks.length; i+=2){
                    if (relation_ids[position].equals(ranks[i])) {
                        if (ranks[i+1].equals("+")) {
                            plus[position] = true;
                            myPlus.setBackgroundResource(R.drawable.plusf);
                            myMinus.setBackgroundResource(R.drawable.minuse);
                            break;
                        }
                        if (ranks[i+1].equals("-")) {
                            minus[position] = true;
                            myMinus.setBackgroundResource(R.drawable.minusf);
                            myPlus.setBackgroundResource(R.drawable.pluse);
                            break;
                        }
                    }
                }
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

        if(currentDateandTime.substring(0,4).equals(relationTimes[position].substring(0,4))) {
            if(currentDateandTime.substring(5,7).equals(currentDateandTime.substring(5,7))) {
                if(currentDateandTime.substring(8,10).equals(relationTimes[position].substring(8,10))) {
                    if(currentDateandTime.substring(11,13).equals(relationTimes[position].substring(11,13))) {
                        if(currentDateandTime.substring(14,16).equals(relationTimes[position].substring(14,16))) {
                            if(currentDateandTime.substring(17,19).equals(relationTimes[position].substring(17,19))) {
                                myTime.setText("0s");
                            } else {
                                myTime.setText((Integer.parseInt(currentDateandTime.substring(17,19))-Integer.parseInt(relationTimes[position].substring(17,19)))+"s");
                            }
                        } else {
                            myTime.setText((Integer.parseInt(currentDateandTime.substring(14,16))-Integer.parseInt(relationTimes[position].substring(14, 16)))+"m");
                        }
                    } else {
                        myTime.setText((Integer.parseInt(currentDateandTime.substring(11,13))-Integer.parseInt(relationTimes[position].substring(11, 13))-3)+"h");
                    }
                } else {
                    myTime.setText((Integer.parseInt(currentDateandTime.substring(8,10))-Integer.parseInt(relationTimes[position].substring(8, 10)))+"d");
                }
            } else {
                myTime.setText((Integer.parseInt(currentDateandTime.substring(5,7))-Integer.parseInt(relationTimes[position].substring(5, 7)))+"mo");
            }

        } else {
            myTime.setText((Integer.parseInt(currentDateandTime.substring(0,4))-Integer.parseInt(relationTimes[position].substring(0, 4)))+"y");
        }

        myName.setText(names[position]);
        myPlace.setText(places[position]);
        myTag.setText(tags[position]);
        myRating.setText(ratings[position] + " ");

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final String u_id = sharedPref.getString("u_id", "N/A");

        myName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(u_id != ids[position]) {
                    intent = new Intent(context, FriendActivity.class);
                    intent.putExtra("friend_id", ids[position]);
                } else {
                    intent = new Intent(context, ProfileActivity.class);
                }
                context.startActivity(intent);

            }
        });

        myImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!u_id.equals(ids[position])) {
                    intent = new Intent(context, FriendActivity.class);
                    intent.putExtra("friend_id", ids[position]);
                } else {
                    intent = new Intent(context, ProfileActivity.class);
                }
                context.startActivity(intent);
            }
        });

        myPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nn = names[position];
                String pp = places[position];
                String tt = tags[position];
                String rr = ratings[position];
                String p_id = null, p_info = null, p_loc = null;

                String placeIDinfoArray[] = null;
                try {
                    placeIDinfoArray = new GetPlaceIDinfo().execute(pp).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                p_id = placeIDinfoArray[0];
                p_info = placeIDinfoArray[1];
                p_loc = placeIDinfoArray[2];

                if(p_id != null) {
                    intent = new Intent(context, TagForPlaceActivity.class);
                    intent.putExtra("place", pp);
                    intent.putExtra("placeID", p_id);
                    intent.putExtra("placeInfo", p_info);
                    intent.putExtra("placeLoc", p_loc);
                    context.startActivity(intent);
                }
            }
        });

        myTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tt = tags[position];
                intent = new Intent(context, PlaceForTagActivity.class);
                intent.putExtra("tag", tt);
                context.startActivity(intent);
            }
        });

        myMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean b = new MakeRankingConn().execute(u_id,relation_ids[position],"-").get();
                    if(plus[position]) {
                        b = new CalculateRankConn().execute(ids[position],"-2").get();
                        myMinus.setBackgroundResource(R.drawable.minusf);
                        myPlus.setBackgroundResource(R.drawable.pluse);
                        minus[position] = true;
                        plus[position] = false;
                    } else if (minus[position]) {
                        b = new CalculateRankConn().execute(ids[position],"1").get();
                        myMinus.setBackgroundResource(R.drawable.minuse);
                        myPlus.setBackgroundResource(R.drawable.pluse);
                        minus[position] = false;
                        plus[position] = false;
                    } else {
                        b = new CalculateRankConn().execute(ids[position],"-1").get();
                        myMinus.setBackgroundResource(R.drawable.minusf);
                        myPlus.setBackgroundResource(R.drawable.pluse);
                        minus[position] = true;
                        plus[position] = false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                myMinus.setBackgroundResource(R.drawable.minusf);
                myPlus.setBackgroundResource(R.drawable.pluse);
                minus[position] = true;
                plus[position] = false;
            }
        });

        myPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean b = new MakeRankingConn().execute(u_id,relation_ids[position],"+").get();
                    if(minus[position]) {
                        b = new CalculateRankConn().execute(ids[position],"2").get();
                        myPlus.setBackgroundResource(R.drawable.plusf);
                        myMinus.setBackgroundResource(R.drawable.minuse);
                        minus[position] = false;
                        plus[position] = true;
                    } else if (plus[position]) {
                        b = new CalculateRankConn().execute(ids[position],"-1").get();
                        myMinus.setBackgroundResource(R.drawable.minuse);
                        myPlus.setBackgroundResource(R.drawable.pluse);
                        minus[position] = false;
                        plus[position] = false;
                    } else {
                        b = new CalculateRankConn().execute(ids[position],"1").get();
                        myPlus.setBackgroundResource(R.drawable.plusf);
                        myMinus.setBackgroundResource(R.drawable.minuse);
                        minus[position] = false;
                        plus[position] = true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        });

        return row;
    }

    //  Server connectıon
    class GetPlaceIDinfo extends AsyncTask<String, Void, String[]>
    {
        ArrayList<Relation> relation = new ArrayList<Relation>();
        String returns[] = new String[3];

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String... params) {
            String p_name = params[0];

            try {
                URL url = new URL("http://"+context.getString(R.string.ip)+":3000/"); // 192.168.1.24 --- 10.0.2.2 --- 139.179.211.68
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("type", "GetPlace");
                jsonParam.put("placename", p_name);

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
                    responseString =responseString.substring(1, response.length() - 1);

                    jsonParam = new JSONObject(responseString);
                    returns[0] = jsonParam.getString("p_id");
                    returns[1] = jsonParam.getString("info");
                    returns[2] = jsonParam.getString("location");
                    conn.disconnect();

                    return returns;
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
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
        }
    }

    //  Server connectıon
    class MakeRankingConn extends AsyncTask<String, Void, Boolean>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String user_id = params[0];
            String r_id = params[1];
            String rank = params[2];

            try {
                URL url = new URL("http://"+context.getString(R.string.ip)+":3000/"); // 192.168.1.24 --- 10.0.2.2 --- 139.179.211.68
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("type", "MakeRankings");
                jsonParam.put("user_id", user_id);
                jsonParam.put("r_id",r_id);
                jsonParam.put("rank",rank);

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
                    }

                    return true;
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
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }

    //  Server connectıon
    class CalculateRankConn extends AsyncTask<String, Void, Boolean>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String user_id = params[0];
            String vote = params[1];

            try {
                URL url = new URL("http://"+context.getString(R.string.ip)+":3000/"); // 192.168.1.24 --- 10.0.2.2 --- 139.179.211.68
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("type", "CalcRankings");
                jsonParam.put("user_id", user_id);
                jsonParam.put("vote",vote);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString()); // URLEncoder.encode(jsonParam.toString(), "UTF-8")
                writer.flush();
                writer.close();
                os.close();

                int statusCode = conn.getResponseCode();

                if (statusCode >= 200 && statusCode < 400) {
                    // Create an InputStream in order to extract the response object
                    conn.disconnect();
                    return true;
                }
                else {
                    conn.disconnect();
                    return false;
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}
