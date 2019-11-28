package com.example.androidthings.assistant.Youtube.Search;

import android.os.AsyncTask;
import android.util.Log;

import com.example.androidthings.assistant.Tool.Utils;
import com.example.androidthings.assistant.Youtube.Auth;
import com.example.androidthings.assistant.Youtube.YoutubeConstants;
import com.example.androidthings.assistant.Youtube.YoutubePoster;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class SearchYoutube extends AsyncTask<Void, Void, List<SearchResult>> {
    String TAG =SearchYoutube.class.getName();

    private final long NUMBER_OF_VIDEOS_RETURNED = 50;// [default: 5] [minimum: 0] [maximum: 50]
    private YouTube youtube = null;

    List<YoutubePoster> youtubePosters = new ArrayList<>();

    String keyWord="ayasa";
    String nexttoken;
    public SearchYoutube(String keyWord){
        Log.d(TAG,"SearchYoutube start");
        this.keyWord=keyWord;
        nexttoken=null;
    }
    public SearchYoutube(String keyWord, String nexttoken){
        Log.d(TAG,"SearchYoutube Next");
        this.keyWord=keyWord;
        this.nexttoken=nexttoken;
    }

    @Override
    protected List<SearchResult> doInBackground(Void... voids) {
        init();
        return null;
    }


    private void init(){
        try {
            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {

                }
            }).setApplicationName("youtube-cmdline-search-sample").build();

            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");


            // Set your developer key from the {{ Google Cloud Console }} for
            // non-authenticated requests. See:
            // {{ https://cloud.google.com/console }}
            search.setKey(YoutubeConstants.API_KEY);//API_KEY
            search.setQ(keyWord);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            if(nexttoken!=null){
                search.setPageToken(nexttoken);
            }

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("kind,etag,nextPageToken,prevPageToken,regionCode,items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            //search.setForMine(true);//代表取得自己的撥放列表


            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            Log.d(TAG,"searchBtn searchResponse:"+searchResponse);
            List<SearchResult> searchResultList = searchResponse.getItems();
            Log.d(TAG,"searchBtn searchResultList:"+searchResultList);

            getNextPageToken(searchResponse.getNextPageToken());
            getPrevPageToken(searchResponse.getPrevPageToken());

            Log.d(TAG,"searchBtn NextPageToken:"+searchResponse.getNextPageToken());

            if (searchResultList != null) {
                prettyPrint(searchResultList.iterator(), keyWord);
            }
        } catch (GoogleJsonResponseException e) {
            Log.e(TAG,"GoogleJsonResponseException:"+ Utils.FormatStackTrace(e));
        } catch (IOException e) {
            Log.e(TAG,"IOException:"+Utils.FormatStackTrace(e));
        } catch (Throwable t) {
            Log.e(TAG,"Throwable:"+Utils.FormatStackTrace(t));
        }
    }


    /*
     * Prints out all results in the Iterator. For each result, print the
     * title, video ID, and thumbnail.
     *
     * @param iteratorSearchResults Iterator of SearchResults to print
     *
     * @param query Search query (String)
     */
    private void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {
        String TAG2 =TAG+" prettyPrint";
        Log.d(TAG2,"\n=============================================================");
        Log.d(TAG2,
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        Log.d(TAG2,"=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            Log.d(TAG2," There aren't any results for your query.");
        }

        youtubePosters = new ArrayList<>();

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();

                Log.d(TAG2," Video Id:" + rId.getVideoId());
                Log.d(TAG2," Title: " + singleVideo.getSnippet().getTitle());
                Log.d(TAG2," Thumbnail: " + thumbnail.getUrl());
                Log.d(TAG2,"\n-------------------------------------------------------------\n");
                YoutubePoster youtubePoster = new YoutubePoster();
                youtubePoster.setYoutubeId(rId.getVideoId());
                youtubePoster.setTitle(singleVideo.getSnippet().getTitle());
                youtubePoster.setImage_url(thumbnail.getUrl());
                youtubePosters.add(youtubePoster);
            }
        }
        YoutubePosters(youtubePosters);
    }

    public abstract void YoutubePosters(List<YoutubePoster> youtubePosters);

    public abstract void getNextPageToken(String NextPageToken);

    public abstract void getPrevPageToken(String PextPageToken);

}

