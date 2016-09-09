package com.gangergames.quotes4good;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright by Reisor [2016].
 */
public class Row {
    public final String id;
    public final String quote;
    public final String author;

    public Row(String id, String quote, String author) {
        this.id = id;
        this.quote = quote;
        this.author = author;
    }
}