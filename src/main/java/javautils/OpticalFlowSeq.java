package javautils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 18-1-5.
 */
public class OpticalFlowSeq {
    private static final int iGFFTMAX=500;
    private MatOfPoint2f mop2fpts_cur;
    private MatOfPoint2f mop2fpts_safe;
    private MatOfPoint2f mop2fpts_prev;

    private MatOfPoint mopCorners;
    private MatOfByte mobStatus;
    private MatOfFloat mofError;

    private List<Point> cornersPre;
    private List<Point> cornersCur;

    private List<Byte> byteStatus;

    private int y;

    private Point pt;
    private Point pt2;

    public double opticalFlow(Mat mat_pre,Mat mat_cur){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        Mat mat_pre= Highgui.imread(str1,Highgui.CV_LOAD_IMAGE_GRAYSCALE);
//        Mat mat_cur= Highgui.imread(str2,Highgui.CV_LOAD_IMAGE_GRAYSCALE);

        Scalar colorRed=new Scalar(255,0,0,255);
        Scalar colorGreen=new Scalar(0,255,0,255);

        mopCorners=new MatOfPoint();
        mop2fpts_cur=new MatOfPoint2f();
        mop2fpts_safe=new MatOfPoint2f();
        mop2fpts_prev=new MatOfPoint2f();
        mobStatus=new MatOfByte();
        mofError=new MatOfFloat();
        cornersPre=new ArrayList<Point>();
        cornersCur=new ArrayList<Point>();
        byteStatus=new ArrayList<Byte>();

        if(mop2fpts_prev.rows()==0){
            Imgproc.goodFeaturesToTrack(mat_pre,mopCorners,iGFFTMAX,0.05,10);
            mop2fpts_prev.fromArray(mopCorners.toArray());
            mop2fpts_prev.copyTo(mop2fpts_safe);
        }

        Video.calcOpticalFlowPyrLK(mat_pre,mat_cur,mop2fpts_prev,mop2fpts_cur,mobStatus,mofError);

        cornersPre=mop2fpts_prev.toList();
        cornersCur=mop2fpts_cur.toList();
        byteStatus=mobStatus.toList();
        y=byteStatus.size()-1;
        int count=0;
        double sum=0.0;
        double max=0.0;

        for(int i=0;i<y;i++){
            if(byteStatus.get(i)==1){
                count++;
                pt=cornersCur.get(i);
                pt2=cornersPre.get(i);
                max=Math.max(max,getDistance(pt,pt2));
                sum+=getDistance(pt,pt2);
            }
        }
        if(y>0){
            return sum/(y*max);
        }
        else return 1.0;

////        System.out.println("count:"+count);
////        System.out.println("sum:"+sum);
//        double[] res=new double[3];
//        res[0]=y+1;
//        res[1]=max;
//        res[2]=sum;
//        return res;
    }

    private double getDistance(Point p1,Point p2){
        double x1=p1.x;
        double y1=p1.y;
        double x2=p2.x;
        double y2=p2.y;
        double distance=Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
        return distance;
    }

}
