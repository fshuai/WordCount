package javautils;

import java.io.*;

/**
 * Created by root on 17-12-12.
 */
public class BeanUtils {
    private BeanUtils(){}

    /**
     * convert object to byte array
     * @param obj
     * @return
     */
    public static byte[] object2Bytes(Object obj){
        byte[] bytes=null;
        ByteArrayOutputStream bo=null;
        ObjectOutputStream oo=null;
        try {
            bo=new ByteArrayOutputStream();
            oo=new ObjectOutputStream(bo);
            oo.writeObject(obj);
            bytes=bo.toByteArray();

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if(bo!=null){
                    bo.close();
                }
                if(oo!=null){
                    oo.close();
                }

            }catch (IOException ioe){
                ioe.printStackTrace();
            }

        }
        return bytes;
    }

    /**
     * convert byte array to object
     * @param bytes
     * @return
     */
    public static Object bytes2Object(byte[] bytes){
        Object obj=null;
        ByteArrayInputStream bi=null;
        ObjectInputStream oi=null;
        try {
            bi=new ByteArrayInputStream(bytes);
            oi=new ObjectInputStream(bi);
            obj=oi.readObject();
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }finally {
            try {
                if(bi!=null){
                    bi.close();
                }
                if(oi!=null){
                    oi.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return obj;
    }
}
