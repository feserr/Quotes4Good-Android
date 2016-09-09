package com.gangergames.quotes4good;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright by Elias Serrano [2016].
 */
public class MainActivity extends AppCompatActivity
{
    private static final String ns = null;

    public static class Row {
        public final String id;
        public final String quote;
        public final String author;

        private Row(String id, String quote, String author) {
            this.id = id;
            this.quote = quote;
            this.author = author;
        }
    }

    public List parse(InputStream in) throws XmlPullParserException, IOException {
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

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
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
    private Row readRow(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, ns, "row");
        String title = null;
        String summary = null;
        String link = null;
        while(parser.next() != XmlPullParser.END_TAG)
        {
            if(parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            if(name.equals("id"))
            {
                title = readId(parser);
            }
            else if(name.equals("quote"))
            {
                summary = readQuote(parser);
            }
            else if(name.equals("author"))
            {
                link = readAuthor(parser);
            }
            else
            {
                skip(parser);
            }
        }
        return new Row(title, summary, link);
    }

    // Processes title tags in the feed.
    private String readId(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        parser.require(XmlPullParser.START_TAG, ns, "id");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "id");
        return title;
    }

    // Processes link tags in the feed.
    private String readQuote(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        parser.require(XmlPullParser.START_TAG, ns, "quote");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "quote");
        return summary;
    }

    // Processes summary tags in the feed.
    private String readAuthor(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        parser.require(XmlPullParser.START_TAG, ns, "author");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "author");
        return summary;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        String result = "";
        if(parser.next() == XmlPullParser.TEXT)
        {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        if(parser.getEventType() != XmlPullParser.START_TAG)
        {
            throw new IllegalStateException();
        }
        int depth = 1;
        while(depth != 0)
        {
            switch(parser.next())
            {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputStream xmlQuotes = getResources().openRawResource(R.raw.quotes);

        List listQuotes = null;
        try
        {
            listQuotes = parse(xmlQuotes);
        }
        catch(XmlPullParserException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        listQuotes.get(2);
    }


}
