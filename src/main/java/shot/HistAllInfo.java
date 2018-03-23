package shot;


/**
 * Created by root on 18-3-23.
 */
public class HistAllInfo {
    //put all the info
    //including hist,domainColorRate,width,height
    public static final int HISTBINS=16;
    public int[] h;
    public int[] s;
    public int[] v;
    public double rate;
    public int width;
    public int height;

    public HistAllInfo(){
        h=new int[HISTBINS];
        s=new int[HISTBINS];
        v=new int[HISTBINS];
    }

    public HistAllInfo(String input){
        this();
        String[] fields=input.trim().split(" ");
        for (int i=0;i<16;i++){
            h[i]=Integer.parseInt(fields[i]);
        }
        for (int i=16;i<32;i++){
            s[i-16]=Integer.parseInt(fields[i]);
        }
        for (int i=32;i<48;i++){
            v[i-32]=Integer.parseInt(fields[i]);
        }
        rate=Double.parseDouble(fields[48]);
        width=Integer.parseInt(fields[49]);
        height=Integer.parseInt(fields[50]);
    }

    @Override
    public String toString(){
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
        result.append(rate);
        result.append(" ");
        result.append(width);
        result.append(" ");
        result.append(height);
        return result.toString();
    }

//correct
//    public static void main(String[] args){
//        String in="75080 4019 763 1007 1512 4241 260032 16720 " +
//                "4828 10452 5703 4496 7446 26582 2873 2406 74507 " +
//                "3476 7899 9325 11870 24481 213749 67107 2973 2143 " +
//                "1466 1014 955 2185 1159 3851 73776 1142 494 2115 " +
//                "2526 7548 10651 270235 7041 5198 5065 10495 9173 " +
//                "11053 4168 7480 0.6590526905829597 960 446";
//        HistAllInfo a=new HistAllInfo(in);
//        System.out.println(a.v[15]);
//        System.out.println(a.rate);
//        System.out.println(a.height);
//    }

}
