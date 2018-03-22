package hist;

import shot.CvScalar;
import shot.HistData;

import java.awt.image.BufferedImage;

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
            if(curHist.s[i] < maxValueS*LOCALREGIONTHRESHOLD){
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

    //计算主色率
    public static double calDomainColorRate(BufferedImage img, HistData curHist){
        CvScalar s = new CvScalar();
        CvScalar cMeanColor ;
        double distance = 0;
        double dSinHmean = 0;
        double dCosHmean = 0;
        double dSinHxy = 0, dCosHxy = 0;
        int nCount = 0;
        int height=img.getHeight();
        int width=img.getWidth();
        cMeanColor = extractDoMainColor(curHist);

        dSinHmean = Math.sin( cMeanColor.value[0]*Math.PI/360.0 );
        dCosHmean = Math.cos( cMeanColor.value[0]*Math.PI/360.0 );

        int[] pix=img.getRGB(0,0,width,height,null,0,width);

        for(int i=0; i<height; i++){
            for(int j=0; j<width; j++){
                //获取（i,j）像素点
                int c=pix[j + i * width];
                int R = (c >> 16) & 0xFF;
                int G = (c >> 8) & 0xFF;
                int B = (c >> 0) & 0xFF;
                //float[] value= Color.RGBtoHSB(R,G,B,null);
                s.value[0]=B;
                s.value[1]=G;
                s.value[2]=R;
                //原来是180
                dSinHxy=Math.sin(s.value[0]*Math.PI/180.0);
                dCosHxy=Math.cos(s.value[0]*Math.PI/180.0);

                //计算距离
                distance = Math.sqrt((s.value[2] - cMeanColor.value[2]) * (s.value[2] - cMeanColor.value[2])
                        + (s.value[1] * dCosHxy - cMeanColor.value[1] * dCosHmean) * (s.value[1] * dCosHxy - cMeanColor.value[1] * dCosHmean)
                        + (s.value[1] * dSinHxy - cMeanColor.value[1] * dSinHmean) * (s.value[1] * dSinHxy - cMeanColor.value[1] * dSinHmean));

                //85 经验阈值
                if( distance < 90 ){
                    nCount++;
                }
            }
        }
        //System.out.println("ncount:"+nCount);
        return (double)nCount/(height*width);
    }

    //计算主色率的差异
    public double calDomainColorDiff(double dRate1,HistData hist1,double dRate2,HistData hist2){
        double dRateDiff=0.0;
        CvScalar cMeanColor1 = extractDoMainColor(hist1);
        CvScalar cMeanColor2 = extractDoMainColor(hist2);
        double dSinHmean1 = Math.sin(cMeanColor1.value[0]*Math.PI/360.0);
        double dCosHmean1 = Math.cos(cMeanColor1.value[0]*Math.PI/360.0);
        double dSinHmean2 = Math.sin(cMeanColor2.value[0]*Math.PI/360.0);
        double dCosHmean2 = Math.cos(cMeanColor2.value[0]*Math.PI/360.0);

        //计算主色距离
        double distance = Math.sqrt((cMeanColor1.value[2] - cMeanColor2.value[2]) * (cMeanColor1.value[2] - cMeanColor2.value[2])
                + (cMeanColor1.value[1] * dCosHmean1 - cMeanColor2.value[1] * dCosHmean2) * (cMeanColor1.value[1] * dCosHmean1 - cMeanColor2.value[1] * dCosHmean2)
                + (cMeanColor1.value[1] * dSinHmean1 - cMeanColor2.value[1] * dSinHmean2) * (cMeanColor1.value[1] * dSinHmean1 - cMeanColor2.value[1] * dSinHmean2) );

        //先判断主色颜色差别，再判断主色率差别
        //如果主色颜色差别比较大，就没必要判断主色率
        if(distance<25){
            dRateDiff =Math.abs(dRate1 - dRate2);
        }
        else{
            dRateDiff = 0.5;
        }
        return dRateDiff;
    }

}
