package javautils;

import shot.HistAllInfo;

/**
 * Created by root on 18-6-6.
 */
public class StreamingUtils {

    private static final int HISTBINS=16;

    //计算两个直方图差的绝对值之和
    static public double calFrameHistDiff(HistAllInfo prevHist, HistAllInfo curHist){
        double frameDiff=0.0;
        for(int i=0;i<HISTBINS;i++){
            frameDiff+=Math.abs(prevHist.h[i]-curHist.h[i]);
        }
        for(int i=0;i<HISTBINS;i++){
            frameDiff+=Math.abs(prevHist.s[i]-curHist.s[i]);
        }
        for(int i=0;i<HISTBINS;i++){
            frameDiff+=Math.abs(prevHist.v[i]-curHist.v[i]);
        }
        return frameDiff/(prevHist.width*prevHist.height);
        //return frameDiff;
    }
}
