package com.example.androidthings.assistant.Youtube;

import android.os.Parcel;
import android.os.Parcelable;

public class YoutubePoster implements Parcelable {
    String TAG=YoutubePoster.class.getName();
    private String YoutubeId;
    private String Title;
    private String image_url;

    public YoutubePoster(){
        YoutubeId = "";
        Title = "";
        image_url = "";
    }

    public YoutubePoster(Parcel in) {
        TAG = in.readString();
        YoutubeId = in.readString();
        Title = in.readString();
        image_url = in.readString();
    }

    public void setYoutubeId(String YoutubeId){
        this.YoutubeId=YoutubeId;
    }

    public void setTitle(String Title){
        this.Title=Title;
    }

    public void setImage_url(String image_url){
        this.image_url=image_url;
    }

    public String getTitle(){
        return Title;
    }
    public String getYoutubeId(){
        return YoutubeId;
    }
    public String getImage_url(){
        return image_url;
    }

    public static final Creator<YoutubePoster> CREATOR = new Creator<YoutubePoster>() {
        @Override
        public YoutubePoster createFromParcel(Parcel in) {
            return new YoutubePoster(in);
        }

        @Override
        public YoutubePoster[] newArray(int size) {
            return new YoutubePoster[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(TAG);
        dest.writeString(YoutubeId);
        dest.writeString(Title);
        dest.writeString(image_url);
    }

    public String toString(){
        return "TAG="+TAG+
                "\n,Youtube Id:"+YoutubeId+
                "\n,Title:"+Title+
                "\n,image_url:"+image_url+
                "\n-------------------------------------------------------------\n";
    }
}
