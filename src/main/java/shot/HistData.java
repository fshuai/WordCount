package shot;

import java.io.Serializable;

/**
 * Created by root on 17-12-27.
 */
public class HistData implements Serializable{
    //add main color rate
    public double rate=0.0;
    public int width;
    public int height;
    public static final int HISTBINS=16;
    public int[] h;
    public int[] s;
    public int[] v;

    public HistData(){
        h=new int[HISTBINS];
        s=new int[HISTBINS];
        v=new int[HISTBINS];
    }

    public HistData(String info){
        this();
        String[] fields=info.trim().split(" ");
        for (int i=0;i<16;i++){
            h[i]=Integer.parseInt(fields[i]);
        }
        for (int i=16;i<32;i++){
            s[i-16]=Integer.parseInt(fields[i]);
        }
        for (int i=32;i<48;i++){
            v[i-32]=Integer.parseInt(fields[i]);
        }
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        //String result="";
        StringBuffer result=new StringBuffer();
        for(int i=0;i<HISTBINS;i++){
            result.append(h[i]);
            result.append(" ");
        }
        for(int i=0;i<HISTBINS;i++){
            result.append(s[i]);
            result.append(" ");
        }
        for(int i=0;i<HISTBINS;i++){
            result.append(v[i]);
            result.append(" ");
        }
        return result.toString();
    }
}
