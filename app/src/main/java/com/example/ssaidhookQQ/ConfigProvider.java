package com.example.ssaidhookQQ;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class ConfigProvider extends ContentProvider {
    public static final String AUTHORITY = "com.example.ssaidhookQQ.config";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/config");
    public static final String COL_ID = "android_id";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{COL_ID});
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(App.PREF_NAME, 0);
            String id = prefs.getString(App.KEY_ID, null);
            cursor.addRow(new Object[]{id});
        }
        return cursor;
    }

    @Override public String getType(Uri uri) { return "vnd.android.cursor.item/vnd.qqssaid.config"; }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException("read only"); }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { throw new UnsupportedOperationException("read only"); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { throw new UnsupportedOperationException("read only"); }
}
