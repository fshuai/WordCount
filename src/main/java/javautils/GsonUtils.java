package javautils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hist.Hist;
import org.apache.commons.codec.binary.Base64;

/**
 * Created by root on 18-3-13.
 */
public class GsonUtils {

    public static String str2Hist(String json){

        JsonParser parser=new JsonParser();
        JsonObject obj=parser.parse(json).getAsJsonObject();
        String dataString=obj.get("data").getAsString();
        byte[] data=Base64.decodeBase64(dataString.getBytes());
        Hist h=new Hist(data);
        return h.getHist().toString();

    }
}
