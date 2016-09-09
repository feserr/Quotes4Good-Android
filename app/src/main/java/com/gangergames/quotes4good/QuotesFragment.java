package com.gangergames.quotes4good;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Copyright by Elias Serrano [2016].
 */
public class QuotesFragment extends Fragment implements
        View.OnClickListener {
    private static final int NOTIFICATION_ID = 1234;
    private static final String ANOTHER_ACTION = "Another";
    private final String TAG = "QuotesFragment";

    private Context activityContext = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private NotificationManager notificationManager = null;

    private NotificationCompat.Builder notificationBuilder = null;
    private Notification quoteNotification = null;

    private TextView quoteView = null;
    private TextView authorView = null;

    private List listQuotes = null;
    private Random rand = null;
    private Row actualRow = null;

    public class UpdateQuote extends AsyncTask<Void, Void, Void> {
        Row row = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            row = (Row) listQuotes.get(rand.nextInt(listQuotes.size()));

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Set quote
            quoteView.setText("\"" + row.quote + "\"");

            // Set author
            authorView.setText("- " + row.author);

            changeNotification(row);

            // Notify swipeRefreshLayout that the refresh has finished
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public class switchButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(ANOTHER_ACTION.equals(action)) {
                Row row = getNewQuote();
                changeNotification(row);
            }
        }
    }

    public QuotesFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the main view
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get the main activity context
        activityContext = this.getActivity();

        // Set the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_main);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mSwipeRefreshLayout.setRefreshing(true);
                        UpdateQuote updateQuote = new UpdateQuote();
                        updateQuote.execute();
                    }
                }
        );

        notificationManager = (NotificationManager) activityContext.getSystemService(NOTIFICATION_SERVICE);

        rand = new Random();

        InputStream xmlQuotes = getResources().openRawResource(R.raw.quotes);

        try {
            listQuotes = Utility.parse(xmlQuotes);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get a randon row
        actualRow = (Row) listQuotes.get(rand.nextInt(listQuotes.size()));

        // Get quotes textviews
        quoteView = (TextView) rootView.findViewById(R.id.quote);
        authorView = (TextView) rootView.findViewById(R.id.author);

        changeMainQuote();

        makeNotification(activityContext);
        changeNotification(actualRow);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        changeMainQuote();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.destroyDrawingCache();
            mSwipeRefreshLayout.clearAnimation();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                Utility.writeLog(TAG, "Unknown button id");
                break;
        }
    }

    private Row getNewQuote() {
        // Get a randon row
        actualRow = (Row) listQuotes.get(rand.nextInt(listQuotes.size()));
        return actualRow;
    }

    private void changeMainQuote() {
        // Set quote
        quoteView.setText("\"" + actualRow.quote + "\"");

        // Set author
        authorView.setText("-" + actualRow.author);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void makeNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent anotherReceive = new Intent(context, switchButtonListener.class);
        anotherReceive.setAction(ANOTHER_ACTION);
        PendingIntent pendingIntentAnother = PendingIntent.getBroadcast(context, NOTIFICATION_ID, anotherReceive,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("Quotes4Good")
                .setContentText("Test")
                .setAutoCancel(true)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Test"))
                .addAction (R.mipmap.ic_launcher,
                        getString(R.string.get_another), pendingIntentAnother);

        quoteNotification = notificationBuilder.build();

        quoteNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(NOTIFICATION_ID, quoteNotification);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void changeNotification(Row row) {
        notificationBuilder.setContentText("\"" + row.quote + "\"\n -" + row.author);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText("\"" + row.quote + "\"\n -" + row.author));

        quoteNotification = notificationBuilder.build();
        quoteNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFICATION_ID, quoteNotification);

    }

}
