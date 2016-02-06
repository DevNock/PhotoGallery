package com.google.photogallery;

import android.Manifest;
import android.app.Activity;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Sergey on 05.02.2016.
 */
public class FlickrFetchr {

    public static final String TAG = FlickrFetchr.class.getSimpleName();

    public static final String ENDPOINT = "https://api.flickr.com/services/rest/";
    public static final String API_KEY = "0e198ecc1a8ee65d58da5657d8c7ce02";
    public static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    public static final String FORMAT_REST = "rest";
    public static final String EXTRA_SMALL_URL = "url_s";

    public static final String XML_PHOTO = "photo";

    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                return null;
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrl(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public ArrayList<GalleryItem> fetchItems(){
        ArrayList<GalleryItem> items = new ArrayList<>();
        try{
            String url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD_GET_RECENT)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("extras", EXTRA_SMALL_URL)
                    .appendQueryParameter("format", FORMAT_REST)
                    .build().toString();
            String xmlString = getUrl(url);
            Log.i(TAG, "Receive xml: " + xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));

            parseItems(items, parser);
        } catch (IOException e){
            Log.e(TAG, "Failed to fetch items " + e.getMessage());
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Failed to parse items", e);
        }
        return items;
    }

    public void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws IOException, XmlPullParserException {
        int eventType = parser.next();

        while(eventType != XmlPullParser.END_DOCUMENT){
            if(eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())){
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);

                GalleryItem item = new GalleryItem();
                item.setId(id);
                item.setCaption(caption);
                item.setUrl(smallUrl);
                items.add(item);
            }
            eventType = parser.next();
        }

    }
}
