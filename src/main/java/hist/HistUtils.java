package hist;

import shot.CvScalar;
import shot.HistData;

/**
 * Created by root on 18-3-22.
 */
public class HistUtils {

    private static final int HISTBINS=16;
    private static final double LOCALREGIONTHRESHOLD=0.2;

    public static CvScalar extractDoMainColor(HistData curHist){
        int maxPosH = 0, maxPosS = 0, maxPosV = 0;
        int maxValueH = 0, maxValueS = 0, maxValueV = 0;
        int leftPosH = 0, rightPosH = 0;
        int leftPosS = 0, rightPosS = 0;
        int leftPosV = 0, rightPosV = 0;
        double meanPosH = 0, meanPosS = 0, meanPosV = 0;
        double meanValueH = 0, meanValueS = 0, meanValueV = 0;
        CvScalar domainMean=new CvScalar();
        domainMean.value[0] = 0;
        domainMean.value[1] = 0;
        domainMean.value[2] = 0;
        domainMean.value[3] = 0;

        //查找峰值
        for(int i = 0; i < HISTBINS; i++){
            if( curHist.h[i] > maxValueH ){
                maxValueH = curHist.h[i];
                maxPosH = i;
            }
            if( curHist.s[i] > maxValueS ){
                maxValueS = curHist.s[i];
                maxPosS = i;
            }
            if( curHist.v[i] > maxValueV ){
                maxValueV = curHist.v[i];
                maxPosV = i;
            }
        }

        //区间范围H
        leftPosH = rightPosH = maxPosH;
        for(int i = maxPosH-1; i>=0; i--){
            if( curHist.h[i] < maxValueH*LOCALREGIONTHRESHOLD){
                break;
            }
            else{
                leftPosH = i;
            }
        }
        for(int i = maxPosH + 1; i < HISTBINS; i++){
            if( curHist.h[i] < maxValueH*LOCALREGIONTHRESHOLD){
                break;
            }
            else{
                rightPosH = i;
            }
        }
        //区间范围S
        leftPosS = rightPosS = maxPosS;
        for(int i = maxPosS-1; i>=0; i--){
            if( curHist.s[i] < maxValueS*LOCALREGIONTHRESHOLD){
                break;
            }
            else{
                leftPosS = i;
            }
        }
        for(int i= maxPosS+1; i< HISTBINS; i++){
            if( curHist.s[i] < maxValueS*LOCALREGIONTHRESHOLD){
                break;
            }
            else{
                rightPosS = i;
            }
        }
        //区间范围V
        leftPosV = rightPosV = maxPosV;
        for(int i = maxPosV-1; i>=0; i--){
            if( curHist.v[i] < maxValueV*LOCALREGIONTHRESHOLD){
                break;
            }
            else{
                leftPosV = i;
            }
        }
        for(int i= maxPosV+1; i< HISTBINS; i++){
            if( curHist.v[i] < maxValueV*LOCALREGIONTHRESHOLD){
                break;
            }
            else{
                rightPosV = i;
            }
        }

        //计算区间范围内的均值
        float nTotal = 0;
        float nTotalMult = 0;
        for(int i = leftPosH; i<= rightPosH; i++)
        {
            nTotal += curHist.h[i];
            nTotalMult += curHist.h[i] * i;
        }
        meanPosH = nTotalMult / nTotal;
        //TODO
        //原来是*180
        meanValueH = meanPosH * 360 / HISTBINS;

        nTotal = 0;
        nTotalMult = 0;
        for(int i = leftPosS; i<= rightPosS; i++) {
            nTotal += curHist.s[i];
            nTotalMult += curHist.s[i] * i;
        }
        meanPosS = nTotalMult / nTotal;
        meanValueS = meanPosS * 255 / HISTBINS;

        nTotal = 0;
        nTotalMult = 0;
        for(int i = leftPosV; i<= rightPosV; i++){
            nTotal += curHist.v[i];
            nTotalMult += curHist.v[i] * i;
        }
        meanPosV= nTotalMult / nTotal;
        meanValueV = meanPosV * 255 / HISTBINS;

        //主色结构
        domainMean.value[0] = meanValueH;
        domainMean.value[1] = meanValueS;
        domainMean.value[2] = meanValueV;
        return domainMean;
    }

}
