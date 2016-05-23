package com.nulleye.yaaa.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nulleye.yaaa.data.YaaaContract.AlarmEntry;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.SentenceBuilder;


/**
 * Yaaa content provider
 *
 * Created by Cristian Alvarez on 26/4/16.
 */
public class YaaaProvider extends ContentProvider {

    static String TAG = YaaaProvider.class.getSimpleName();

    //Types of URIs
    static final int ALARM = 100;
    static final int ALARMS = 200;

    //Uri matcher: URI to Type of URI
    private static UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = YaaaContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority, YaaaContract.PATH_ALARM, ALARMS);
        uriMatcher.addURI(authority, YaaaContract.PATH_ALARM + "/#", ALARM);
    }

    private YaaaDbHelper dbHelper;


    @Override
    public boolean onCreate() {
        dbHelper = new YaaaDbHelper(getContext());
        return true;
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SentenceBuilder where = new SentenceBuilder();
        final int match = uriMatcher.match(uri);
        switch (match) {
            case ALARMS:
                break;
            case ALARM:
                final Long id = AlarmEntry.getAlarmIdFromUri(uri);
                if (id != null) {
                    where.addExpr(AlarmEntry._ID, SentenceBuilder.EQ, id);
                    break;
                }
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(AlarmEntry.TABLE_NAME);
        if (where.length() > 0) qb.appendWhere(where.build());
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final Cursor ret = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (ret != null) notifyChange(uri, ret);
        return ret;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALARM:
                return AlarmEntry.CONTENT_ITEM_TYPE;
            case ALARMS:
                return AlarmEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) == ALARMS)
            try {
                final Uri returnUri = insert(dbHelper.getWritableDatabase(), values);
                notifyChange(uri, null);
                return returnUri;
            } catch (Exception e) {
                throw new android.database.SQLException("Failed to insert row into: " + uri);
            }
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int count;
        switch (uriMatcher.match(uri)) {
            case ALARMS:
                count = db.delete(AlarmEntry.TABLE_NAME, selection, selectionArgs);
                notifyChange(uri, null);
                break;
            case ALARM:
                final Long id = AlarmEntry.getAlarmIdFromUri(uri);
                if (id != null) {
                    SentenceBuilder select = new SentenceBuilder(AlarmEntry._ID, SentenceBuilder.EQ, id);
                    if (!FnUtil.isVoid(selection)) select.add(SentenceBuilder.AND).addLP().add(selection).addRP();
                    count = db.delete(AlarmEntry.TABLE_NAME, select.build(), selectionArgs);
                    notifyChange(uri, null);
                    break;
                }
            default:
                throw new IllegalArgumentException("Cannot delete from: " + uri);
        }
        return count;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) == ALARM) {
            final SQLiteDatabase db = dbHelper.getWritableDatabase();
            final Long id = AlarmEntry.getAlarmIdFromUri(uri);
            if (id != null) {
                final int count = db.update(AlarmEntry.TABLE_NAME, values,
                        new SentenceBuilder(AlarmEntry._ID, SentenceBuilder.EQ, id).build(), null);
                notifyChange(uri, null);
                return count;
            }
        }
        throw new UnsupportedOperationException("Cannot update uri: " + uri);
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (uriMatcher.match(uri) == ALARMS) {
            int returnCount = 0;
            db.beginTransaction();
            try {
                for (ContentValues value : values)
                    try {
                        insert(db, value);
                        returnCount++;
                    } catch(Exception ignore) {}
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            if (returnCount > 0) notifyChange(uri, null);
            return returnCount;
        } else return super.bulkInsert(uri, values);
    }


    private Uri insert(final SQLiteDatabase db, final ContentValues values) {
//        normalizeData(values);
        final long id = db.insert(AlarmEntry.TABLE_NAME, null, values);
        if (id < 0) throw new SQLException("Failed to insert row: " + values);
        return AlarmEntry.buildAlarmUri(id);
    }


//    private void normalizeData(final ContentValues values) {
//        // normalize time value
//        if (values.containsKey(AlarmEntry.COLUMN_TIME))
//            values.put(AlarmEntry.COLUMN_TIME,
//                    YaaaContract.normalizeTime(values.getAsLong(AlarmEntry.COLUMN_TIME)));
//        // normalize date value
//        if (values.containsKey(AlarmEntry.COLUMN_DATE))
//            values.put(AlarmEntry.COLUMN_DATE,
//                    YaaaContract.normalizeDate(values.getAsLong(AlarmEntry.COLUMN_DATE)));
//        // normalize boolean value
//        if (values.containsKey(AlarmEntry.COLUMN_VIBRATE))
//            values.put(AlarmEntry.COLUMN_VIBRATE,
//                    YaaaContract.normalizeBoolean(values.getAsBoolean(AlarmEntry.COLUMN_VIBRATE)));
//        // normalize boolean value
//        if (values.containsKey(AlarmEntry.COLUMN_DELETE))
//            values.put(AlarmEntry.COLUMN_DELETE_DATE,
//                    YaaaContract.normalizeBoolean(values.getAsBoolean(AlarmEntry.COLUMN_DELETE)));
//        // normalize date value
//        if (values.containsKey(AlarmEntry.COLUMN_DELETE_DATE))
//            values.put(AlarmEntry.COLUMN_DELETE_DATE,
//                    YaaaContract.normalizeDate(values.getAsLong(AlarmEntry.COLUMN_DELETE_DATE)));
//        // normalize boolean value
//        if (values.containsKey(AlarmEntry.COLUMN_IGNORE_VACATION))
//            values.put(AlarmEntry.COLUMN_IGNORE_VACATION,
//                    YaaaContract.normalizeBoolean(values.getAsBoolean(AlarmEntry.COLUMN_IGNORE_VACATION)));
//        // normalize boolean value
//        if (values.containsKey(AlarmEntry.COLUMN_ENABLED))
//            values.put(AlarmEntry.COLUMN_ENABLED,
//                    YaaaContract.normalizeBoolean(values.getAsBoolean(AlarmEntry.COLUMN_ENABLED)));
//    }


    private void notifyChange(final Uri uri, final Cursor cursor) {
        final Context ctxt = getContext();
        if (ctxt != null) {
            final ContentResolver cr = ctxt.getContentResolver();
            if (cr != null) {
                if (cursor != null) cursor.setNotificationUri(cr, uri);
                else cr.notifyChange(uri, null);
                return;
            }
        }
        Log.e(TAG, "Couldn't notify of a change: " + uri);
    }


    @Override
    @TargetApi(11)
    public void shutdown() {
        dbHelper.close();
        super.shutdown();
    }


}
