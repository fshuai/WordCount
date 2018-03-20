package utility

import javautils.{FrameActivity, OpticalFlowSeq}

import org.apache.hadoop.io.{BytesWritable, Text}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * Created by root on 18-1-8.
  */
object ScalaFrameActivity {

  val opticalFlow=new OpticalFlowSeq

  def getFrameActivity(list: List[(Text,BytesWritable)]):ArrayBuffer[(String,Double)]={
    var res:ArrayBuffer[(String,Double)]=ArrayBuffer[(String,Double)]()
    //var result:Double=0.0
    val input:ArrayBuffer[(String,BytesWritable)]=ArrayBuffer[(String,BytesWritable)]()
    var r=new Random()
    for(i <- 0 until list.length){
      //result=opticalFlow.opticalFlow(FrameActivity.bytesWritable2Mat(list(i)._2),
       // FrameActivity.bytesWritable2Mat(list(i+1)._2))
      res.append((list(i)._1.toString,r.nextDouble()))
    }
    res
  }

}
