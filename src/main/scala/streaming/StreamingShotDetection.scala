package streaming

import javautils.StreamingUtils

import hist.HistUtils
import kafka.serializer.StringDecoder
import org.apache.spark.{HashPartitioner, SparkConf}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.KafkaUtils
import shot.HistAllInfo

import scala.collection.immutable.HashSet
import scala.collection.mutable.ArrayBuffer
import scalautils.LoggerLevels

/**
  * Created by root on 18-3-28.
  */
object StreamingShotDetection {


  val updateFunc:(Seq[String],Option[ShotState])=>Option[ShotState] = (hists:Seq[String],state:Option[ShotState]) => {
    val lists=hists.toList
    var tmp=state
    lists match {
      case h::t => {
        state match {
          case None =>{
            val slideWindiff=new ArrayBuffer[Double]
            var j=0
            while (j<30){
              slideWindiff+=0.0
              j+=1
            }
            Some(ShotState("frame00001.jpg",slideWindiff,1,false,false,h,false,0.0,0.0,0.0,0.0))
          }
          case Some(st) => updateFunc(t,Some(update(h,st)))
        }
      }
      case Nil => state
    }

  }

  val update = (h:String,state:ShotState) => {
    //frame00001.jpg 75080 4019 763 1007 1512 4241 260032 16720 4828 10452 5703 4496 7446
    // 26582 2873 2406 74507 3476 7899 9325 11870 24481 213749 67107 2973 2143 1466 1014 955
    // 2185 1159 3851 73776 1142 494 2115 2526 7548 10651 270235 7041 5198 5065 10495 9173 11053
    // 4168 7480 0.6590526905829597 960 446

    val frameId=h.substring(0,14)

    var newdThresholdHigh1=state.dThresholdHigh1
    var newdThresholdLow1=state.dThresholdLow1
    var newdThresholdHigh2=state.dThresholdHigh2
    var newdThresholdLow2=state.dThresholdLow2

    var curHistData=new HistAllInfo(h.substring(15))
    var preHistData=new HistAllInfo(state.preHist.substring(15))

    val curDiff=StreamingUtils.calFrameHistDiff(preHistData,curHistData)
    var newisSlideEnough=false
    var newnSlideWindow=state.nSlideWindow
    var newisCandidateCut=state.isCandidateCut

    var newisBoundary=false

    if(newnSlideWindow<16){
      var j=30
      while(j>1){
        state.slideWinDiff(j-1)=state.slideWinDiff(j-2)
        j-=1
      }
      newnSlideWindow+=1
      state.slideWinDiff(0)=curDiff
      preHistData=curHistData
    }
    else {
      newisSlideEnough=true
    }

    if(newisCandidateCut && newisSlideEnough){
      var sum=0d
      var mean=0d
      var variance=0d
      var stddev=0d
      if(newnSlideWindow==0){
        mean=curDiff
        stddev=0
      }
      else {
        for(i <- state.slideWinDiff){
          sum+=i
        }
        mean=sum/newnSlideWindow
        for(i<-0 until newnSlideWindow){
          variance += (state.slideWinDiff(i)-mean)*(state.slideWinDiff(i)-mean)
        }
        stddev=math.sqrt(variance/newnSlideWindow)
      }
      newdThresholdHigh1=(mean+3*stddev)*2.7
      newdThresholdLow1=mean*2.7
      newdThresholdHigh2=(mean+3*stddev)*2.4
      newdThresholdLow2=mean*2.4
      if(curDiff>newdThresholdLow2){
        if(curHistData.rate<=0.3){
          val tmpCurHistData=HistUtils.convertHistAllInfo2Data(curHistData)
          val tmpPreHistData=HistUtils.convertHistAllInfo2Data(preHistData)
          val dDomColorDiff=HistUtils.calDomainColorDiff(curHistData.rate,tmpCurHistData,preHistData.rate,tmpCurHistData)
          if((curDiff>newdThresholdHigh1 * 2 && dDomColorDiff > 0.2) || curDiff>newdThresholdHigh1 *3){
            newnSlideWindow=0
            newisBoundary=true
          }
          else if((curDiff>newdThresholdHigh1 && dDomColorDiff > 0.2) || (curDiff > newdThresholdHigh1 *2)){
            newisCandidateCut=true
          }
          else {
            var j=30
            while (j>1){
              state.slideWinDiff(j-1)=state.slideWinDiff(j-2)
              j-=1
            }
            state.slideWinDiff(0)=curDiff
            if(newnSlideWindow<30){
              newnSlideWindow+=1
            }
          }
        }
        else {
          val tmpCurHistData=HistUtils.convertHistAllInfo2Data(curHistData)
          val tmpPreHistData=HistUtils.convertHistAllInfo2Data(preHistData)
          val dDomColorDiff=HistUtils.calDomainColorDiff(curHistData.rate,tmpCurHistData,preHistData.rate,tmpPreHistData)
          if((curDiff>newdThresholdHigh2 * 2 && dDomColorDiff > 0.2) || (curDiff>newdThresholdHigh2 * 3)){
            newnSlideWindow=0
            newisBoundary=true
          }
          else if((curDiff>newdThresholdHigh2 && dDomColorDiff>0.2) || (curDiff>newdThresholdHigh2 * 2)){
            newisCandidateCut=true
          }
          else {
            var j=30
            while(j>1){
              state.slideWinDiff(j-1)=state.slideWinDiff(j-2)
              j-=1
            }
            state.slideWinDiff(0)=curDiff
            if(newnSlideWindow<30){
              newnSlideWindow+=1
            }
          }
        }
      }
      if(curDiff<newdThresholdLow2 * 2){
        state.slideWinDiff(0)=curDiff
        newnSlideWindow=1
        newisBoundary=true
      }
      else {
        var j=30
        while(j>1){
          state.slideWinDiff(j-1)=state.slideWinDiff(j-2)
          j-=1
        }
        state.slideWinDiff(0)=curDiff
        if(newnSlideWindow<30){
          newnSlideWindow+=1
        }
      }
    }
    val tmpCurHistData=HistUtils.convertHistAllInfo2Data(curHistData)
    val tmpPreHistData=HistUtils.convertHistAllInfo2Data(preHistData)
    val domaindiff=HistUtils.calDomainColorDiff(curHistData.rate,tmpCurHistData,preHistData.rate,tmpPreHistData)
    if(domaindiff>0.2)
      newisBoundary=true
    else newisBoundary=false
    ShotState(frameId,state.slideWinDiff,newnSlideWindow,newisCandidateCut,
      newisSlideEnough,h,newisBoundary,newdThresholdHigh1,newdThresholdLow1,newdThresholdHigh2,newdThresholdLow2)
  }

  def main(args: Array[String]): Unit = {
    LoggerLevels.setStreamingLogLevels()
    //val Array(brokers,topics,groupId)=args
    val brokers="master:9092,slave01:9092"
    val topics="frames72"
    val groupId="test0"

    //create context with 2 seconds batch interval
    val sparkconf=new SparkConf().setAppName("streamingshot")
    //sparkconf.setMaster("spark://master:7077")
    sparkconf.setMaster("local[2]")
    sparkconf.set("spark.streaming.kafka.maxRatePerPartition","5")
    sparkconf.set("spark.serializer","org.apache.spark.serializer.KryoSerializer")

    val ssc=new StreamingContext(sparkconf,Seconds(2))
    //set checkpoint
    ssc.checkpoint("/root/check")

    //create direct kafka stream with brokers and topics
    val topicSet=HashSet(topics)
    val kafkaParams=Map[String,String]("metadata.broker.list"->brokers)
    val messages=KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc,kafkaParams,topicSet)
    //messages.print()
    //get each frame hist
    //val histedDstream=messages.map(x=>(x._1.toString,getHist(Base64.decodeBase64(x._2))))
    //val resultRdd=messages.reduceByKeyAndWindow({(x,y) => x+y},Seconds(10))

    val resultRdd=messages.updateStateByKey(updateFunc)
    resultRdd.map(b=>(b._2.frameId,b._2.isBoundary)).print()
    //resultRdd.filter(a => a._2.isBoundary).map(b=>(b._2.frameId,b._2.isBoundary)).print()
    ssc.start()
    ssc.awaitTermination()
  }

}

case class ShotState(frameId:String,slideWinDiff:ArrayBuffer[Double],nSlideWindow:Int,
                     isCandidateCut:Boolean,isSlideEnough:Boolean,preHist:String,
                     isBoundary:Boolean,dThresholdHigh1:Double,dThresholdLow1:Double,
                     dThresholdHigh2:Double,dThresholdLow2:Double)


