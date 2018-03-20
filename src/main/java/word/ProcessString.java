package word;

/**
 * Created by root on 17-12-15.
 */
public class ProcessString {

    public static String processString(String str){
        StringBuffer sb=new StringBuffer();
        sb.append(str);
        sb.append("j");
        return sb.toString();
    }
}
