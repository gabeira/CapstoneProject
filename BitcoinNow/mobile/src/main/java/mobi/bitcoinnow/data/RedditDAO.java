package mobi.bitcoinnow.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import mobi.bitcoinnow.model.Reddit;

/**
 * Created by gabrielbernardopereira on 11/4/16.
 */
public class RedditDAO implements BaseColumns {

    public static final String LOG_TAG = RedditDAO.class.getSimpleName().toString();

    public static final String TABLE_NAME = "reddits";

    public static final String COLUMN_CREATED = "created";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_NUM_COMMENTS = "comments";
    public static final String COLUMN_THUMBNAIL = "thumbnail";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_SELF_TEXT = "selftext";

    protected SQLiteDatabase db;

    public RedditDAO(Context ctx) {
        db = ctx.openOrCreateDatabase(RedditDbHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);
        dbHelper = new RedditDbHelper(ctx);
    }

    protected RedditDAO() {
    }

    private RedditDbHelper dbHelper;

//    public RedditDAO(Context context) {
//        dbHelper = new RedditDbHelper(context);
//    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    public void insert(Reddit reddit) {
        try {
            ContentValues redditValues = new ContentValues();
            redditValues.put(RedditDAO.COLUMN_ID, reddit.getId());
            redditValues.put(RedditDAO.COLUMN_TITLE, reddit.getTitle());
            redditValues.put(RedditDAO.COLUMN_AUTHOR, reddit.getAuthor());
            redditValues.put(RedditDAO.COLUMN_CREATED, reddit.getCreated());
            redditValues.put(RedditDAO.COLUMN_NUM_COMMENTS, reddit.getNumberOfComments());
            redditValues.put(RedditDAO.COLUMN_THUMBNAIL, reddit.getThumbnail());
            redditValues.put(RedditDAO.COLUMN_URL, reddit.getUrl());
            redditValues.put(RedditDAO.COLUMN_SELF_TEXT, reddit.getSelfText());

            db.insert(TABLE_NAME, "", redditValues);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getCursor() {
        try {
//            return db.query(TABLE_NAME, null, null, null, null, null, null, null);
            return dbHelper.getReadableDatabase().rawQuery("select * from " + TABLE_NAME, null);

        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error: " + e.toString());
            return null;
        }
    }

    public List<Reddit> getRedditList() {
        Cursor c = getCursor();
        List<Reddit> reddits = new ArrayList<>();
        if (c.moveToFirst()) {
            int idxId = c.getColumnIndex(COLUMN_ID);
            int idxTitle = c.getColumnIndex(COLUMN_TITLE);
            int idxAuthor = c.getColumnIndex(COLUMN_AUTHOR);
            int idxCreated = c.getColumnIndex(COLUMN_CREATED);
            int idxNumComments = c.getColumnIndex(COLUMN_NUM_COMMENTS);
            int idxThumb = c.getColumnIndex(COLUMN_THUMBNAIL);
            int idxUrl = c.getColumnIndex(COLUMN_URL);
            int idxSelfText = c.getColumnIndex(COLUMN_SELF_TEXT);
            do {
                Reddit r = new Reddit();
                r.setId(c.getString(idxId));
                r.setTitle(c.getString(idxTitle));
                r.setAuthor(c.getString(idxAuthor));
                r.setCreated(c.getLong(idxCreated));
                r.setNumberOfComments(c.getInt(idxNumComments));
                r.setThumbnail(c.getString(idxThumb));
                r.setUrl(c.getString(idxUrl));
                r.setSelfText(c.getString(idxSelfText));
                reddits.add(r);
            } while (c.moveToNext());
        }
        return reddits;
    }

    public void setRedditList(List<Reddit> reddits) {
        for (Reddit r : reddits) {
            insert(r);
        }
        Log.d(LOG_TAG, "Inseriu: " + reddits.size());

    }

    public void deleteAllReddits() {
        try {
            System.out.println(db.delete(TABLE_NAME, null, null) + " Reddits deleted");

        } catch (Exception e) {
        }
    }

    public Cursor query(SQLiteQueryBuilder queryBuilder, String[] projection, String selection, String[] selectionArgs,
                        String groupBy, String having, String orderBy) {
        Cursor c = queryBuilder.query(this.db, projection, selection, selectionArgs, groupBy, having, orderBy);
        return c;
    }

}