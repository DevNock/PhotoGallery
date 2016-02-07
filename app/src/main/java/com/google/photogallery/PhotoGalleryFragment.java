package com.google.photogallery;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Sergey on 05.02.2016.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG =   PhotoGalleryFragment.class.getSimpleName();

    private ArrayList<GalleryItem> items;
    private GridView gridView;
    private ThumbnailDownloader<ImageView> thumbnailThread;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.INTERNET);
        new FetchItemsTask().execute();

        thumbnailThread = new ThumbnailDownloader(new Handler());
        thumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if(isVisible()){
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        thumbnailThread.start();
        thumbnailThread.getLooper();
        Log.e(TAG, "Background thread started");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        gridView = (GridView) view.findViewById(R.id.gridView);
        setupAdapter();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thumbnailThread.quit();
        Log.e(TAG, "Background thread destroyed");
    }

    private void setupAdapter() {
        if(getActivity() == null || gridView == null){
            return;
        }
        if(items != null){
            gridView.setAdapter(new GalleryItemAdapter(items));
        } else{
            gridView.setAdapter(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailThread.clearQueue();
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> galleryItems) {
            items = galleryItems;
            setupAdapter();
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>{

        public GalleryItemAdapter(ArrayList<GalleryItem> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.gallery_item, parent, false);
            }
            ImageView imageView = (ImageView)convertView.
                    findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.brian_up_close);
            GalleryItem item = getItem(position);
            thumbnailThread.queueThumbnail(imageView, item.getUrl());
            return convertView;
        }
    }
}
