package com.gangergames.quotes4good;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright by Elias Serrano [2016].
 */
public class Utility {
    private static final String TAG = "Utility";

    private static final String ns = null;

    /**
     * Write a log message if is in Debug mode.
     *
     * @param tag String of the class
     * @param s   Message to log
     */
    public static void writeLog(String tag, String s) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, s);
        }
    }

    public static List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private static List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "root");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("row")) {
                entries.add(readRow(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }


    // Parses the contents of an Row. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private static Row readRow(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "row");
        String title = null;
        String summary = null;
        String link = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("id")) {
                title = readId(parser);
            } else if (name.equals("quote")) {
                summary = readQuote(parser);
            } else if (name.equals("author")) {
                link = readAuthor(parser);
            } else {
                skip(parser);
            }
        }
        return new Row(title, summary, link);
    }

    // Processes title tags in the feed.
    private static String readId(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "id");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "id");
        return title;
    }

    // Processes link tags in the feed.
    private static String readQuote(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "quote");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "quote");
        return summary;
    }

    // Processes summary tags in the feed.
    private static String readAuthor(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "author");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "author");
        return summary;
    }

    // For the tags title and summary, extracts their text values.
    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public static void writeToFile(Row row, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("lastquote.dat", Context.MODE_PRIVATE));
            outputStreamWriter.write(row.id);
            outputStreamWriter.write("\n");
            outputStreamWriter.write(row.quote);
            outputStreamWriter.write("\n");
            outputStreamWriter.write(row.author);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static Row readFromFile(Context context) {

        Row ret = new Row();

        try {
            InputStream inputStream = context.openFileInput("lastquote.dat");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String id = bufferedReader.readLine();
                String quote = bufferedReader.readLine();
                String author = bufferedReader.readLine();

                ret = new Row(id, quote, author);

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}