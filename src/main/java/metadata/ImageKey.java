package metadata;

import java.io.Serializable;

/**
 * Created by root on 17-12-12.
 */
public class ImageKey implements Serializable{

    private String videoId;
    private String frameId;
    public ImageKey(String videoId,String frameId){
        this.videoId=videoId;
        this.frameId=frameId;
    }

    public String getVideoId(){
        return videoId;
    }

    public String getFrameId(){
        return frameId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    @Override
    public String toString() {
        return "ImageKey{" +
                "videoId='" + videoId + '\'' +
                ", frameId='" + frameId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageKey imageKey = (ImageKey) o;

        if (!videoId.equals(imageKey.videoId)) return false;
        return frameId.equals(imageKey.frameId);
    }

    @Override
    public int hashCode() {
        int result = videoId.hashCode();
        result = 31 * result + frameId.hashCode();
        return result;
    }
}
