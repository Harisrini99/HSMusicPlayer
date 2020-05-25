package music.hs.com.materialmusicv2;

import java.sql.Time;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import music.hs.com.materialmusicv2.utils.conversionutils.ConversionUtils;

public class ShowDatabaseObject
{
    public  String Name;
    public  String Artist;
    public  String Duration;
    public  String Lyrics;


    public ShowDatabaseObject(String Name,String Artist,String Duration,String Lyrics)
    {
        this.Name = Name;
        this.Artist  = Artist;
        this.Duration = Duration;
        this.Lyrics = Lyrics;
    }

    public String getName()
    {
        return this.Name;
    }

    public String getArtist()
    {
        return Artist;
    }

    public String getDuration()
    {
        long d = Long.valueOf(Duration);
        int s = Integer.valueOf(Duration);
        int ms = s%100;
        s = s/100;
        int sec = s%100;
        return "[ "+ ConversionUtils.covertMilisToTimeString(d) +" ]";
    }

    public String getLyrics()
    {
        return getReadableLyrics(Lyrics);
    }

    protected TreeMap<Integer,String> convertToTreeMap(String text){
        TreeMap<Integer,String> data = new TreeMap<>();
        String[] split = text.split(",");
        for ( int i=0; i <split.length; i++ )
        {
            String t[] = split[i].split("=");
            t[0] = t[0].replaceAll("\\s+","");

            if( t.length > 1)
                data.put(Integer.parseInt(t[0]),t[1]);
        }
        return data;
    }

    public  String getOrgLyrics()
    {
        return Lyrics;
    }

    private String getReadableLyrics(String lyricsString)
    {
        TreeMap<Integer,String> temp = convertToTreeMap(lyricsString);
        String t = "";
        if(temp != null)
        {
            Iterator<HashMap.Entry<Integer, String>> itr = temp.entrySet().iterator();
            int i=0;
            while(itr.hasNext())
            {
                HashMap.Entry<Integer, String> entry = itr.next();
                t = t + entry.getValue() + "\n";
            }

        }
        String t1 = "";
        if(t1.equals(t) || t.equals(""))
        {
            return "No lyrics";
        }

        return t;

    }


}
