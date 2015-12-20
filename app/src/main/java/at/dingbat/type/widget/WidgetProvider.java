package at.dingbat.type.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import at.dingbat.apiutils.ApiUtil;
import at.dingbat.type.R;
import at.dingbat.type.activity.EditorActivity;
import at.dingbat.type.activity.MainActivity;
import at.dingbat.type.model.Document;

/**
 * Created by Max on 12/19/2015.
 */
public class WidgetProvider extends AppWidgetProvider implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleClient;

    private Context context;
    private AppWidgetManager appWidgetManager;
    private int[] appWidgetIds;

    private static int[] rows_to_items = { 1, 1, 2, 4, 5 };

    private int rows = 1;

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        rows = minHeight/70;

        ComponentName t = new ComponentName(context, WidgetProvider.class);
        onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(t));

    }

    private void initGoogleApiClient() {
        googleClient = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addApi(LocationServices.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.appWidgetIds = appWidgetIds;

        if(googleClient == null) initGoogleApiClient();

        if(googleClient.isConnected()) reload();
        else googleClient.connect();

    }

    private void reload() {
        ApiUtil.getAllTypos(googleClient, new ApiUtil.SearchCallback() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult result) {
                ComponentName t = new ComponentName(context, WidgetProvider.class);
                int[] all = appWidgetManager.getAppWidgetIds(t);

                for(int id: all) {
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                    views.removeAllViews(R.id.widget_layout_root);

                    MetadataBuffer buffer = result.getMetadataBuffer();
                    for (int i = 0; i < buffer.getCount() && i < rows_to_items[rows]; i++) {
                        Metadata data = buffer.get(i);
                        RemoteViews item = new RemoteViews(context.getPackageName(), R.layout.widget_layout_file_item);
                        item.setTextViewText(R.id.widget_layout_item_title, data.getTitle());
                        item.setTextViewText(R.id.widget_layout_item_modified, Document.formatDate(context, data.getModifiedDate()));

                        Intent intent = new Intent(context, EditorActivity.class);
                        intent.putExtra("file", data.getDriveId().toString());
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        item.setOnClickPendingIntent(R.id.widget_layout_file_item, pi);

                        views.addView(R.id.widget_layout_root, item);
                    }

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    views.setOnClickPendingIntent(R.id.widget_layout_icon, pi);

                    //Intent intent = new Intent(context, WidgetProvider.class);

                    //intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

                    //PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    //views.setOnClickPendingIntent(R.id.update, pendingIntent);
                    appWidgetManager.updateAppWidget(id, views);
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        reload();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
