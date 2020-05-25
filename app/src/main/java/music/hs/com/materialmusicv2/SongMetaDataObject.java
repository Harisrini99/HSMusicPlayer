package music.hs.com.materialmusicv2;

public class SongMetaDataObject
{
    public String trackName;
    public String albumName;
    public String artistName;
    public String url;

    public SongMetaDataObject(String t,String al,String ar,String url)
    {
        this.trackName = t;
        this.albumName = al;
        this.artistName = ar;
        this.url = url;

    }

    public String getTrackName()
    {
        return trackName;
    }

    public String getAlbumName()
    {
        return albumName;
    }

    public String getArtistName()
    {
        return artistName;
    }

    public String getUrl()
    {
        return url;
    }


}
