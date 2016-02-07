package com.google.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.GpsStatus;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sergey on 07.02.2016.
 */
public class ThumbnailDownloader<Token> extends HandlerThread {

    public static final String TAG = ThumbnailDownloader.class.getSimpleName();
    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler handler;
    private Map<Token, String> requestMap =
            Collections.synchronizedMap(new HashMap<Token, String>());

    private Handler responseHandler;
    private Listener<Token> listener;

    int cacheSize = 4 * 1024 * 1024; // 4MiB
    LruCache bitmapCache = new LruCache(cacheSize) {
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();

        }};

    public interface Listener<Token> {
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> mListener){
        listener = mListener;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD){
                    @SuppressWarnings("unchecked")
                    Token token = (Token) msg.obj;
                    Log.i(TAG, "Got a request for url: " + requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    private void handleRequest(final Token token) {
        try{
            final String url = requestMap.get(token);
            if(url == null){
                return;
            }
            final Bitmap bitmap;
            if(bitmapCache.get(url) != null){
                bitmap = (Bitmap) bitmapCache.get(url);
            } else {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory
                        .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                bitmapCache.put(url, bitmap);
            }
            Log.i(TAG, "Bitmap created");

            responseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(requestMap.get(token) != url){
                        return;
                    }
                    requestMap.remove(token);
                    listener.onThumbnailDownloaded(token, bitmap);
                }
            });

        } catch (IOException e) {
           Log.e(TAG, "Error downloading image", e);
        }
    }

    private void procBitmap(){
        //TODO load 10 photo
    }

    public ThumbnailDownloader(Handler handler) {
        super(TAG);
        responseHandler = handler;
    }

    public void queueThumbnail(Token token, String url){
        Log.e(TAG, "Got an URL: " + url);
        requestMap.put(token, url);

        handler.obtainMessage(MESSAGE_DOWNLOAD, token)
                .sendToTarget();
    }

    public void clearQueue(){
        handler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();

    }

}
