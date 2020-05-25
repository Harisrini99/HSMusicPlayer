package music.hs.com.materialmusicv2.lyrics;

import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class flip {
    /** Regex of:[mm:ss.ms], lyric time tag. */
    final private static String TimeTagRegex = "[0-9]+:[0-6]?[0-9]\\.[0-9][0-9]";
    /** Regex of:[mm:ss] */
    final private static String TimeRegex = "[0-9]+:[0-6]?[0-9]";

    /**
     * Parse a string, format like:[mm:ss].
     * @since 0.1
     * @return <MSms> A time data.
     */
    public static MSms timeParser(String TimeStr){
        String[] StrParsed = TimeStr.split(":");
        return new MSms(Integer.parseInt(StrParsed[0]),
                Integer.parseInt(StrParsed[1]) );
    }

    /**
     * Parse a string, format like:[mm:ss].
     * @since 0.1
     * @return <MSms> A time data.
     * @throws Exception Format not right.
     */
    public static MSms timeParser_s(String TimeStr) throws Exception{
        if (TimeStr.matches(TimeRegex) == false) throw new Exception("Format not right.");
        return timeParser(TimeStr);
    }

    /**
     * Parse a string, format like:[mm:ss.ms].
     * @since 0.1
     * @return <MSms> A time data.
     */
    public static MSms timeTagParser(String TimeStr){
        String[] StrParsed = TimeStr.split(":|\\.");
        return new MSms(Integer.parseInt(StrParsed[0]),
                Integer.parseInt(StrParsed[1]),
                Integer.parseInt(StrParsed[2]) );
    }

    /**
     * Parse a string, format like:[mm:ss.ms]. With check the format.
     * @since 0.1
     * @return <MSms> A time data.
     */
    public static MSms timeTagParser_s(String TimeStr) throws Exception{
        if (TimeStr.matches(TimeTagRegex) == false) throw new Exception("Format not right.");
        return timeTagParser(TimeStr);
    }

    /**
     * Parse a line of LRC.
     * @since 0.1
     * @return <TreeMap> A treeMap for parsed result: keys are the time of line, values are the string of line.
     */
    public static TreeMap<Integer, String> lineStrParser(String lineStr) {

        ArrayList<String> timeTags = new ArrayList<>();

        while (lineStr.matches("(\\[" + TimeTagRegex + "\\])+.*") == true){
            String[] lineStrParsed = lineStr.split("\\[|\\]",3);
            timeTags.add(lineStrParsed[1]);
            lineStr = lineStrParsed[2];
        }

        TreeMap<Integer, String> lineMap = new TreeMap<>();
        for (String timeTag : timeTags){
            String t = "\""+lineStr.trim()+"\"";
            t = t.replaceAll(",",".");
            lineMap.put(timeTagParser(timeTag).TOms(), t);
        }
        return lineMap;
    }

    /**
     * Parse Tag of LRC.
     * @since 0.1
     * @return <String> Value of tag.
     */
    public static String infoTagParser(String id, String line){
        String[] lineParsed = line.split("\\[" + id + ":|\\]",3);
        return lineParsed[1].trim();
    }

    /**
     * Parse lines of LRC.
     * @since 0.1
     * @return <LRC> A LRC data.
     */
    public static LRC LinesParser(ArrayList<String> lines){
        LRC lrc = new LRC();
        TreeMap<Integer,String> l = new TreeMap<>();
        for (String line : lines){
            if (line.matches("(\\[" + TimeTagRegex + "\\])+.*") == true) {
                TreeMap<Integer,String> m = lineStrParser(line);
                Iterator<HashMap.Entry<Integer, String>> itr = m.entrySet().iterator();
                while(itr.hasNext())
                {
                    HashMap.Entry<Integer, String> entry = itr.next();
                    l.put(entry.getKey(),entry.getValue());
                }


            } else if (line.matches("(\\["+ "offset" +":.*\\])+.*") == true) {
                lrc.offset = Integer.valueOf(infoTagParser("offset", line));
            } else if (line.matches("(\\["+ "length" +":(\\s)*" + TimeRegex + "(\\s)*\\])+.*") == true) {
                lrc.length = timeParser(infoTagParser("length", line));
            } else if (line.matches("(\\[.+:.*\\])+.*") == true) {
                for (int i = 0; i< LRC.metasName.length; i++){
                    if (line.matches("(\\["+ LRC.metasName[i] +":.*\\])+.*") == true) {
                        lrc.metas[i] = infoTagParser(LRC.metasName[i], line);
                        break;
                    }
                }
            } else {}
        }
        Log.d("$$$$$$$$$$$$$$$$$$$$$$$$$$flip",l.toString());
        LRC ll = new LRC(l);
        return ll;
    }

    /**
     * Parse file of LRC.
     * @since 0.1
     * @return <LRC> A LRC data.
     * @throws IOException File open fail.
     */
    public static LRC Parse(File file) throws IOException{
        try{
            return LinesParser(fileOperation.fileToLines(file));
        }catch(IOException e){throw e;}
    }
}

