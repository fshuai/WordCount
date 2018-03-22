package hist;

import shot.CvScalar;
import shot.HistData;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by root on 17-12-27.
 */
public class Hist {
    transient private BufferedImage srcImg;
    private String filename;
    //private ArrayList<HSV> hsvlist;

    public Hist(BufferedImage img,String name){
        srcImg=img;
        filename=name;
        //hsvlist=new ArrayList<HSV>();
    }
    public Hist(BufferedImage img){
        srcImg=img;
        filename="default";
    }

    public Hist(byte[] bytes){
        ImageIcon imageIcon=new ImageIcon(bytes);
        srcImg=new BufferedImage(imageIcon.getIconWidth(),imageIcon.getIconHeight(),BufferedImage.TYPE_INT_RGB);
        Graphics2D gs=(Graphics2D)srcImg.getGraphics();
        gs.drawImage(imageIcon.getImage(),0,0,imageIcon.getImageObserver());
    }

    public String getName(){
        return filename;
    }

    public ArrayList<Hsv> RGB2HSV(){
        if(srcImg==null){
            System.out.println("srcImg is null");
        }
        int width=srcImg.getWidth();
        int height=srcImg.getHeight();
        ArrayList<Hsv> result=new ArrayList<Hsv>();
        int[] pix=srcImg.getRGB(0,0,width,height,null,0,width);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = pix[x + y * width];
                int r = (c >> 16) & 0xFF;
                int g = (c >> 8) & 0xFF;
                int b = (c >> 0) & 0xFF;
                float[] value= Color.RGBtoHSB(r,g,b,null);
                Hsv hsv=new Hsv(value[0]*360,value[1]*255,value[2]*255);
                result.add(hsv);
            }
        }
        return result;
    }
    public HistData getHist(){
        HistData result=new HistData();
        ArrayList<Hsv> re=RGB2HSV();
        int len=re.size();
        for(int i=0;i<len;i++){
            result.h[(int)(re.get(i).h/22.5)]++;
            result.s[(int)(re.get(i).s/15.93751)]++;
            result.v[(int)(re.get(i).v/15.93751)]++;
        }
        return result;
    }

    /**
     * return histinfo , mainColorRate , width , height
     * @return
     */
    public String getAllHistInfo(){
        StringBuilder res=new StringBuilder();
        HistData hist=getHist();
        res.append(hist.toString());
        double rate=HistUtils.calDomainColorRate(srcImg,hist);
        res.append(rate);
        res.append(srcImg.getWidth());
        res.append(srcImg.getHeight());
        return res.toString();
    }

    public CvScalar HSV2Scalar(ArrayList<Hsv> in){
        return null;
    }
    //deal with null pointer exception
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageIO.write(srcImg, "jpg", out);
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        srcImg= ImageIO.read(in);
    }
}
