package shot;

import hist.HistUtils;
import org.junit.Test;
import scala.Int;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 18-3-27.
 */
public class ShotCut {
    private int width;
    private int height;
    private String path;//图片文件夹路径
    private ArrayList<Integer> cutInfo;
    private ArrayList<GTInfo> gtInfo;
    private int frameCount;//视频帧数

    private Map<String,HistData> map;

    private static final int HISTBINS=16;
    private static final double LOCALREGIONTHRESHOLD=0.2; //主色提取范围阈值
    private static final int SLIDEWINDOWNUMBER=30;
    private static final int GTWINDOWNUMBER=25;
    private static final int THRESHOLD_N=3;
    private static final float LAMBDA_OUTFIELD=2.7f;
    private static final float LAMBDA_INFIELD=2.4f;
    private static final float DOMINATCOLORTHRESHOLD1=0.3f;
    private static final float DOMINATCOLORTHRESHOLD3=0.2f;

    private static final int CANDIDACYGRADUAL=3;
    private static final float DOMINATCOLORTHRESHOLD2=0.06f;

    public ShotCut(){
        cutInfo=new ArrayList<Integer>();
        gtInfo=new ArrayList<GTInfo>();
        map=new HashMap<String,HistData>();
        frameCount=0;
    }

    //主色提取
    //输入参数为当前图像的颜色直方图
    public CvScalar extractDomainColor(HistData curHist){
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

    @Test
    public void testMainColor(){
        //correct
        String in="75080 4019 763 1007 1512 4241 260032 16720 4828 " +
                "10452 5703 4496 7446 26582 2873 2406 74507 3476 7899 9325 " +
                "11870 24481 213749 67107 2973 2143 1466 1014 955 2185 1159 " +
                "3851 73776 1142 494 2115 2526 7548 10651 270235 7041 5198 " +
                "5065 10495 9173 11053 4168 7480";
        HistData curHist=new HistData(in);
        CvScalar res=extractDomainColor(curHist);
        System.out.println(res.value[0]);

    }

    @Test
    public void testMainColorRate() throws IOException {
        //correct
        String in="75080 4019 763 1007 1512 4241 260032 16720 4828 " +
                "10452 5703 4496 7446 26582 2873 2406 74507 3476 7899 9325 " +
                "11870 24481 213749 67107 2973 2143 1466 1014 955 2185 1159 " +
                "3851 73776 1142 494 2115 2526 7548 10651 270235 7041 5198 " +
                "5065 10495 9173 11053 4168 7480";
        HistData curHist=new HistData(in);
        BufferedImage img= ImageIO.read(new File("/root/Videos/frame00001.jpg"));
        double res=calDomainColorRate(img,curHist);
        System.out.println(res);
    }


    //计算两个直方图差的绝对值之和
    public double calFrameHistDiff(HistAllInfo prevHist,HistAllInfo curHist){
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

    //计算主色率
    public double calDomainColorRate(BufferedImage img, HistData curHist){
        CvScalar s = new CvScalar();
        CvScalar cMeanColor ;
        double distance = 0;
        double dSinHmean = 0;
        double dCosHmean = 0;
        double dSinHxy = 0, dCosHxy = 0;
        int nCount = 0;
        int height=img.getHeight();
        int width=img.getWidth();
        cMeanColor = extractDomainColor(curHist);

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
                if(distance<90){
                    nCount++;
                }
            }
        }
        //System.out.println("ncount:"+nCount);
        return (double)nCount/(height*width);
    }

    //计算主色率的差异
    public double calDomainColorDiff(BufferedImage img1,HistData hist1,BufferedImage img2,HistData hist2){
        double dRateDiff=0.0;
        CvScalar cMeanColor1 = extractDomainColor(hist1);
        CvScalar cMeanColor2 = extractDomainColor(hist2);
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
            double dRate1 = calDomainColorRate(img1, hist1);
            double dRate2 = calDomainColorRate(img2, hist2);
            dRateDiff =Math.abs(dRate1-dRate2);
        }
        else{
            dRateDiff = 0.5;
        }
        return dRateDiff;
    }

    public List<Integer> shotDetection(List<String> inputs) throws IOException {
        //h_bins代表h分量
        int h_bins = HISTBINS, s_bins = HISTBINS, v_bins = HISTBINS;
        //TODO
        //创建三维直方图
//        BufferedImage preImg=null;
//        BufferedImage pGTImg=null;
//        BufferedImage img=null;
        List<Integer> result=new ArrayList<Integer>();
        result.add(0);

        ArrayList<HistData> arrHist;
        //上一帧直方图数
        HistAllInfo preHistData=new HistAllInfo(inputs.get(0));
        //当前帧直方图数据
        HistAllInfo curHistData;
        //渐变起始帧直方图数据
        HistAllInfo histGTStart=new HistAllInfo();
        //保存滑动窗口的帧间差
        double[] slideWinDiff=new double[SLIDEWINDOWNUMBER];  //30
        //渐变检测过程中保存的帧间差
        double[] GTDiff=new double[GTWINDOWNUMBER];    //25
        //滑动窗口
        int nSlideWindow=0;
        //可能渐变标记
        boolean isGT=false;
        //渐变类型,0不包含足球场地，1包含足球场地
        int nGTType=0;
        //帧差大于最小阈值的帧数
        int nDiffCount=0;
        //渐变最大帧差 10
        int nMaxGTCount=0;
        //渐变起始帧
        int nGTStartNum=0;
        int nGTEndNum=0;
        //可能切变 如果后一帧的帧差小于最小阈值则认定为切变，否则认为是候选渐变的起始帧
        boolean isCandidateCut=false;
        //滑动窗口是否充足
        boolean isSlideEnough=false;
        //上一帧差
        double dbLastDiff=0.0;
        //是否是新一个镜头的开始 某些切变过渡帧为两帧 dbLastDiff和bIsNewCut处理这种情况
        boolean isNewCut=false;
        //ArrayList<GTInfo> m_GTInfo=new ArrayList<GTInfo>();
        String filepath="";

        //双阈值
        double dThresholdHigh1  = 0.0;
        double dThresholdLow1	= 0.0;
        double dThresholdHigh2	= 0.0;
        double dThresholdLow2	= 0.0;

        for(int i=0; i <inputs.size(); i++){
            if(i == 0){
                //初始化前一帧和渐变图像
//                width=img.getWidth();
//                height=img.getHeight();
//                //TODO
//                //复制
//                preImg=ImageIO.read(new File(path+filepath));
//                //pGTImg=ImageIO.read(new File(filepath));
                //Hist curhist=new Hist(img,iToString(i));
                //curHistData=curhist.getHist();
                //preHistData=curHistData;
                curHistData=new HistAllInfo(inputs.get(0));
                preHistData=new HistAllInfo(inputs.get(0));
            }
            else{
                //计算帧间差
                //Hist curhist=new Hist(img,iToString(i));
                curHistData=new HistAllInfo(inputs.get(i));
                double curDiff = calFrameHistDiff(preHistData, curHistData);

                ////保存帧间差
                //fresult<<i<<","<<curDiff<<","<<dThresholdHigh1<<","<<dThresholdLow1<<endl;

                //滑动窗口未填充足够，即两个镜头不会离得太近
                if(nSlideWindow < 16){
                    if(nSlideWindow==0 && !isNewCut){
                        if(curDiff < dbLastDiff/3){
                            //memmove(&vecSlideWinDiff[1],&vecSlideWinDiff[0],sizeof(vecSlideWinDiff)-sizeof(vecSlideWinDiff[0]));
                            for(int j=SLIDEWINDOWNUMBER;j>1;j--){
                                slideWinDiff[j-1]=slideWinDiff[j-2];
                            }
                            slideWinDiff[0] = curDiff;
                            nSlideWindow++;
                            //CopyHistData( prevHistData, currHistData );
                            preHistData=curHistData;
                            isSlideEnough=false;
                        }

                        isNewCut = true;
                    }
                    else{
                        //memmove( &vecSlideWinDiff[1], &vecSlideWinDiff[0], sizeof(vecSlideWinDiff) - sizeof(vecSlideWinDiff[0]) );
                        for(int j=SLIDEWINDOWNUMBER;j>1;j--){
                            slideWinDiff[j-1]=slideWinDiff[j-2];
                        }
                        slideWinDiff[0] = curDiff;
                        nSlideWindow++;
                        //CopyHistData( prevHistData, currHistData );
                        preHistData=curHistData;
                        isSlideEnough = false;
                    }
                }
                else{
                    isSlideEnough=true;
                }

                dbLastDiff = curDiff;

                //判断候选切变是否为切变，如果不是切变，则认为是候选渐变
                if(isCandidateCut && isSlideEnough){
                    if(curDiff<dThresholdLow2 * 2){
                        //帧差小于低阈值 认为候选切变是切变
                        slideWinDiff[0] = curDiff;
                        nSlideWindow = 1;
                        cutInfo.add(i-1);
                        result.add(i-1);
                        isNewCut = false;
                    }
                    else {
                        //帧差大于低阈值 认为是候选渐变
                        isGT = true;
                        nGTType = 0;
                        nDiffCount = 1;
                    }
                    isCandidateCut = false;
                }

                //如果不是可能渐变的开始，检测帧间差
                if(isGT == false && isSlideEnough){
                    //计算滑动窗口均值和标准差
                    double sum = 0, mean = 0;
                    double variance = 0, stddev = 0;

                    if(nSlideWindow == 0)
                    {
                        mean = curDiff;
                        stddev = 0;
                    }
                    else {
                        for(int nsize = 0; nsize < nSlideWindow; nsize++ ){
                            sum += slideWinDiff[nsize];
                        }
                        mean = sum/nSlideWindow;
                        for(int nsize = 0; nsize < nSlideWindow;nsize++){
                            variance += (slideWinDiff[nsize] - mean)*(slideWinDiff[nsize] - mean);
                        }
                        stddev = Math.sqrt( variance/nSlideWindow );
                    }
                    //计算高低双阈值
                    dThresholdHigh1 = (mean+THRESHOLD_N*stddev)*LAMBDA_OUTFIELD;
                    dThresholdLow1 = mean*LAMBDA_OUTFIELD;
                    dThresholdHigh2 = (mean+THRESHOLD_N*stddev)*LAMBDA_INFIELD;
                    dThresholdLow2 = mean*LAMBDA_INFIELD;

                    //fthreshold1<<i<<","<<dThresholdHigh1<<endl;
                    //fthreshold2<<i<<","<<dThresholdLow1<<endl;

                    //当前帧差大于最小低阈值
                    if(curDiff > dThresholdLow2){
                        //if( calDomainColorRate(img, curHistData)<=DOMINATCOLORTHRESHOLD1){
                        if(curHistData.rate<=DOMINATCOLORTHRESHOLD1){
                            //不包含足球场地
                            //double dDomColorDiff =  calDomainColorDiff(img, curHistData, preImg, preHistData);
                            HistData tmpCurHistData=HistUtils.convertHistAllInfo2Data(curHistData);
                            HistData tmpPreHistData=HistUtils.convertHistAllInfo2Data(preHistData);
                            double dDomColorDiff= HistUtils.calDomainColorDiff
                                    (curHistData.rate,tmpCurHistData,preHistData.rate,tmpCurHistData);

                            if((curDiff > dThresholdHigh1 * 2 && dDomColorDiff > DOMINATCOLORTHRESHOLD3) || (curDiff > dThresholdHigh1 * 3))
                            //加入dDomColorDiff > 0.3切变准确率高，渐变检测结果少些
                            {
                                //发生突变
                                nSlideWindow = 0;
                                //记录突变
                                cutInfo.add(i);
                                result.add(i);
                                isNewCut = false;
                            }
                            else if( (curDiff > dThresholdHigh1 && dDomColorDiff > DOMINATCOLORTHRESHOLD3) || (curDiff > dThresholdHigh1 * 2) )
                            {
                                //候选突变
                                isCandidateCut = true;
                                //保存渐变起始帧直方图和起始相隔帧差
                                //CopyHistData( histGTStart, prevHistData );
                                histGTStart=preHistData;
                                //cvCopyImage(prevImage, pGTImage);
//pGTImg=new BufferedImage(preImg.getWidth(),preImg.getHeight(),preImg.getType());
//pGTImg.setData(preImg.getData());
                                GTDiff[GTWINDOWNUMBER-1] = curDiff;
                                nGTStartNum = i;
                            }
                            else if(curDiff <= dThresholdHigh1 && curDiff > dThresholdLow1)
                            {
                                //可能存在渐变的起始帧
                                isGT = true;
                                nGTType = 0;
                                nDiffCount++;
                                //保存渐变起始帧直方图和起始相隔帧差
                                //CopyHistData( histGTStart, prevHistData );
                                histGTStart=preHistData;
                                //cvCopyImage(prevImage, pGTImage);
//pGTImg=new BufferedImage(preImg.getWidth(),preImg.getHeight(),preImg.getType());
//pGTImg.setData(preImg.getData());
                                GTDiff[GTWINDOWNUMBER-1] = curDiff;
                                nGTStartNum = i;
                            }
                            else
                            {//不是镜头边界，继续增大滑动窗口
                                //保存滑动窗口帧间差
                                //memmove( &vecSlideWinDiff[1], &vecSlideWinDiff[0], sizeof(vecSlideWinDiff) - sizeof(vecSlideWinDiff[0]) );
                                for(int j=SLIDEWINDOWNUMBER;j>1;j--){
                                    slideWinDiff[j-1]=slideWinDiff[j-2];
                                }
                                slideWinDiff[0] = curDiff;
                                if(nSlideWindow < SLIDEWINDOWNUMBER)
                                    nSlideWindow++;
                            }
                        }
                        else{//包含足球场地
                            //double dDomColorDiff = calDomainColorDiff(img, curHistData, preImg, preHistData);
                            HistData tmpCurHistData=HistUtils.convertHistAllInfo2Data(curHistData);
                            HistData tmpPreHistData=HistUtils.convertHistAllInfo2Data(preHistData);
                            double dDomColorDiff=HistUtils.calDomainColorDiff
                                    (curHistData.rate,tmpCurHistData,preHistData.rate,tmpPreHistData);
                            if( (curDiff > dThresholdHigh2 * 2 && dDomColorDiff > DOMINATCOLORTHRESHOLD3) || (curDiff > dThresholdHigh2 * 3)){
                                //**//**//**//*|| dDomColorDiff > 0.3*//**//**//**//* )//加入dDomColorDiff > 0.3切变准确率高，渐变检测结果少些
                                //发生突变
                                nSlideWindow = 0;
                                //记录突变
                                cutInfo.add(i);
                                result.add(i);
                                isNewCut = false;
                            }
                            else if((curDiff > dThresholdHigh2 && dDomColorDiff>DOMINATCOLORTHRESHOLD3)||(curDiff > dThresholdHigh2 * 2) ){
                                //候选突变
                                isCandidateCut = true;
                                //保存渐变起始帧直方图和起始相隔帧差
                                //CopyHistData( histGTStart, prevHistData );
                                histGTStart=preHistData;
                                //cvCopyImage(prevImage, pGTImage);
//pGTImg=new BufferedImage(preImg.getWidth(),preImg.getHeight(),preImg.getType());
//pGTImg.setData(preImg.getData());
                                GTDiff[GTWINDOWNUMBER-1] = curDiff;
                                nGTStartNum = i;
                            }
                            else if(curDiff <= dThresholdHigh2 && curDiff > dThresholdLow2){
                                //**//**//**//*&& dDomColorDiff > DOMINATCOLORTHRESHOLD2*//**//**//**//* )
                                //可能存在渐变的起始帧
                                isGT = true;
                                nGTType = 1;
                                nDiffCount++;
                                //保存渐变起始帧直方图和起始相隔帧差
                                //CopyHistData(histGTStart,prevHistData);
                                histGTStart=preHistData;
                                //cvCopyImage(prevImage, pGTImage);
//pGTImg=new BufferedImage(preImg.getWidth(),preImg.getHeight(),preImg.getType());
//pGTImg.setData(preImg.getData());
                                GTDiff[GTWINDOWNUMBER-1] = curDiff;
                                nGTStartNum = i;
                            }
                            else{
                                //不是镜头边界，继续增大滑动窗口
                                //保存滑动窗口帧间差
                                //memmove( &vecSlideWinDiff[1], &vecSlideWinDiff[0], sizeof(vecSlideWinDiff) - sizeof(vecSlideWinDiff[0]) );
                                for(int j=SLIDEWINDOWNUMBER;j>1;j--){
                                    slideWinDiff[j-1]=slideWinDiff[j-2];
                                }
                                slideWinDiff[0] = curDiff;
                                if(nSlideWindow < SLIDEWINDOWNUMBER)
                                    nSlideWindow++;
                            }
                        } //else including soccer fields
                    }
                    else{
                        //如果不是渐变检测状态则帧间差存入滑动窗口,如果是渐变在渐变检测中处理
                        //保存滑动窗口帧间差
                        //memmove( &vecSlideWinDiff[1], &vecSlideWinDiff[0], sizeof(vecSlideWinDiff) - sizeof(vecSlideWinDiff[0]) );
                        for(int j=SLIDEWINDOWNUMBER;j>1;j--){
                            slideWinDiff[j-1]=slideWinDiff[j-2];
                        }
                        slideWinDiff[0] = curDiff;
                        if( nSlideWindow < SLIDEWINDOWNUMBER )
                            nSlideWindow++;
                    }

                }
                else if(isSlideEnough){//正在渐变检测过程中
                    if(nMaxGTCount < GTWINDOWNUMBER-1){
                        nMaxGTCount++;
                        GTDiff[GTWINDOWNUMBER-nMaxGTCount-1] = curDiff;
                        if(nSlideWindow < SLIDEWINDOWNUMBER)
                            nSlideWindow++;
                        //如果当前帧差大于最大帧差的3倍，则认为是突变
                        if( curDiff > dThresholdHigh1*4){
                            //**//**//**//*(nGTType==0 ? dThresholdHigh1*3 : dThresholdHigh2*3)*//**//**//**//* )
                            //终止渐变检测，是突变
                            isGT=false;
                            nMaxGTCount = 0;
                            nDiffCount = 0;
                            nSlideWindow = 0;
                            //记录突变
                            cutInfo.add(i);
                            result.add(i);
                            isNewCut=false;
                        }
                        else if( curDiff >(nGTType==0?dThresholdLow1:dThresholdLow2)){
                            nDiffCount++;
                            nGTEndNum = i;
                        }
                        //渐变特征 前三个渐变帧中至少有两个帧间差大于最低阈值
                        //如果不满足这个特征，取消此次渐变检测，并将渐变帧差添加入滑动窗口
                        if(nMaxGTCount==CANDIDACYGRADUAL){//3
                            if(nDiffCount<2){
                                //终止渐变检测，不是候选渐变
                                isGT=false;
                                //memmove( &vecSlideWinDiff[CANDIDACYGRADUAL], &vecSlideWinDiff[0],
                                //    sizeof(vecSlideWinDiff) - sizeof(vecSlideWinDiff[0])*CANDIDACYGRADUAL );
                                for(int j=SLIDEWINDOWNUMBER;j>CANDIDACYGRADUAL;j--){
                                    slideWinDiff[j-1]=slideWinDiff[j-4];
                                }
                                //memmove( &vecSlideWinDiff[0], &vecGTDiff[GTWINDOWNUMBER-CANDIDACYGRADUAL], sizeof(vecGTDiff[0])*CANDIDACYGRADUAL );
                                for(int j=CANDIDACYGRADUAL;j>0;j--){
                                    slideWinDiff[j-1]=GTDiff[GTWINDOWNUMBER-CANDIDACYGRADUAL+j-1];
                                }
                                nMaxGTCount = 0;
                                nDiffCount = 0;
                                //判断候选渐变中是否存在切变
                                for(int k=1; k<CANDIDACYGRADUAL;k++){
                                    if( GTDiff[GTWINDOWNUMBER-k]>dThresholdHigh2 ){
                                        nSlideWindow = GTWINDOWNUMBER-k-1;
                                        //记录突变
                                        cutInfo.add(nGTStartNum+k-1);
                                        result.add(nGTStartNum+k-1);
                                        isNewCut=false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    else{
                        //相隔帧超过阈值
                        //double dBeforeDomColorDiff =  calDomainColorDiff(img,curHistData, pGTImg, histGTStart);
                        HistData tmpcurHistData=HistUtils.convertHistAllInfo2Data(curHistData);
                        HistData tmpHistGtStart=HistUtils.convertHistAllInfo2Data(histGTStart);
                        double dBeforeDomColorDiff=HistUtils.calDomainColorDiff
                                (curHistData.rate,tmpcurHistData,histGTStart.rate,tmpHistGtStart);

                        if(calFrameHistDiff(histGTStart, curHistData) > (nGTType==0 ? dThresholdHigh1 : dThresholdHigh2) &&
                                dBeforeDomColorDiff > DOMINATCOLORTHRESHOLD2 &&
                                nDiffCount > 3 ){
                            //渐变终止帧，检测出渐变
                            isGT = false;
                            GTInfo tmpInfo=new GTInfo();
                            tmpInfo.start = nGTStartNum;
                            tmpInfo.length = nGTEndNum - nGTStartNum + 1;
                            gtInfo.add(tmpInfo);
                            isNewCut=false;

                            //镜头开始，初始化滑动窗口
                            nSlideWindow = i-nGTEndNum;
                            //memmove( &vecSlideWinDiff[0], &vecGTDiff[0], sizeof(vecGTDiff[0])*nSlideWindow );
                            for(int j=0;j<nSlideWindow;j++){
                                slideWinDiff[j]=GTDiff[j];
                            }
                            nMaxGTCount = 0;
                            nDiffCount = 0;
                        }
                        else{
                            isGT = false;
                            //GTWINDOWNUMBER=25;
                            //slideWinDiff长度为30
                            for(int j=0;j<(SLIDEWINDOWNUMBER-GTWINDOWNUMBER);j++){
                                slideWinDiff[GTWINDOWNUMBER+j]=slideWinDiff[j];
                            }
                            //memmove( &vecSlideWinDiff[GTWINDOWNUMBER], &vecSlideWinDiff[0],
                            //    sizeof(vecSlideWinDiff) - sizeof(vecSlideWinDiff[0])*GTWINDOWNUMBER );
                            //memmove( &vecSlideWinDiff[0], &vecGTDiff[0], sizeof(vecGTDiff) );
                            for(int j=0;j<GTWINDOWNUMBER;j++){
                                slideWinDiff[j]=GTDiff[j];
                            }

                            nMaxGTCount = 0;
                            nDiffCount = 0;
                            //判断候选渐变中是否存在切变 20100929 11:28
                            for(int k=1;k<GTWINDOWNUMBER;k++){
                                if( GTDiff[GTWINDOWNUMBER-k]>dThresholdHigh2)
                                {
                                    //发生突变
                                    nSlideWindow = GTWINDOWNUMBER-k;
                                    //记录突变
                                    cutInfo.add(i-nSlideWindow-1);
                                    result.add(i-nSlideWindow-1);
                                    isNewCut = false;
                                    break;
                                }
                            }
                        }
                    }
                }

                //释放上一个直方图数据，重新赋值
                //CopyHistData( prevHistData, currHistData );
                preHistData=curHistData;
            }
            //cvCopyImage(pImage, prevImage);
//preImg=new BufferedImage(img.getWidth(),img.getHeight(),img.getType());
//preImg.setData(img.getData());
        }
        //最后一帧算是镜头结尾
        cutInfo.add(frameCount-1);
        result.add(inputs.size()-1);
        return result;
    }

    public ArrayList<Integer> getCutInfo(){
        return cutInfo;
    }

    public ArrayList<GTInfo> getGtInfo(){
        return gtInfo;
    }

    public int getFrameCount(){
        return frameCount;
    }

    public int setPath(String imgpath,int fCount,HashMap<String,HistData> m){
        if(imgpath==null || fCount<=0){
            return 0;
        }
        path=imgpath;
        frameCount=fCount;
        map=m;
        return 1;
    }
}
