package mobi.bitcoinnow.sync;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mobi.bitcoinnow.model.Reddit;


/**
 * Created by gabrielbernardopereira on 10/4/16.
 */
public class RedditDataSyncTask extends AsyncTask<String, Void, List<Reddit>> {

    private final String LOG_TAG = RedditDataSyncTask.class.getSimpleName();
    private OnRedditDataSyncFinishListener listener;


    public static List<Reddit> parseReddit(String response) {
        List<Reddit> redditEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");

            for (int i = 0; i < children.length(); i++) {
                try {
                    JSONObject dataObj = children.getJSONObject(i).getJSONObject("data");
                    Reddit entry = new Reddit();
                    entry.setId(dataObj.getString("id"));
                    entry.setTitle(dataObj.getString("title"));
                    entry.setAuthor(dataObj.getString("author"));
                    entry.setThumbnail(dataObj.getString("thumbnail"));
                    entry.setCreated(dataObj.getLong("created_utc"));
                    entry.setNumberOfComments(dataObj.getInt("num_comments"));
                    entry.setUrl(dataObj.getString("url"));
                    entry.setSelfText(dataObj.getString("selftext"));
                    redditEntries.add(entry);
                } catch (Exception e) {
                    Log.e("REDDIT", "Error => " + e.getLocalizedMessage());
                }
            }
        } catch (JSONException e) {
            Log.e("REDDIT", "Error => " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return redditEntries;
    }

    public void setListener(OnRedditDataSyncFinishListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<Reddit> doInBackground(String... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String after = "";
        if (null != params && params.length >= 1 && params[0] != null) {
            after = "&after=" + params[0];
        }

        try {
            URL url = new URL("http://www.reddit.com/r/Bitcoin/new/.json?sort=new" + after + "&count=30&limit=30");
            // Create the request to Reddit, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            return parseReddit(buffer.toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    @Override
    protected void onPostExecute(List<Reddit> reddits) {
        super.onPostExecute(reddits);
        if (null != reddits && reddits.size() > 0) {
            if (listener != null) {
                listener.onRedditDataSyncFinish(reddits);
            }

        }
    }

    public interface OnRedditDataSyncFinishListener {
        void onRedditDataSyncFinish(List<Reddit> page);
    }
}
