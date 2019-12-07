package com.qs.kafka010

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.kafka010.OffsetRange

object OffsetUtil {
  //从mysql中读取offset
  def getOffsetFromMysql(topics:Set[String],groupId:String): Map[TopicPartition,Long] ={
    //构建返回值 返回为空
    var topicPartitions:Map[TopicPartition,Long] = Map.empty
    //读取偏移量，放到map集合
    var conn:Connection = null
    var pstm: PreparedStatement = null
    val topicName = topics
        .map(topic=>{
          s"\'$topic\'"
        })
      .mkString(",")
    try {
      Class.forName("com.mysql.jdbc.Driver")
      conn = DriverManager.getConnection("jdbc:mysql://hadoop01:3306/test", "root", "mysql")
      val sql = s"select * from tb_offset where groupid =? and topic in($topicName)"
      pstm = conn.prepareStatement(sql)
      pstm.setString(1, groupId)
      val rsSet: ResultSet = pstm.executeQuery()
      while (rsSet.next()) {
        val topic: String = rsSet.getString("topic")
        val partition: Int = rsSet.getInt("partitions")
        val groupId: String = rsSet.getString("groupid")
        val offset: Long = rsSet.getLong("offset")
        val topicPartition = new TopicPartition(topic, partition)
        topicPartitions += topicPartition -> offset
      }
    }catch {
      case e:Exception=>e.printStackTrace()
    }finally {
      if (null !=pstm)pstm.close()
      if (null !=conn)conn.close()
    }
    topicPartitions
  }


  def setOffsetToMysql(offsetRanges: Array[OffsetRange],groupId:String): Unit ={
    //读取偏移量，放到map集合
    var conn:Connection = null
    var pstm: PreparedStatement = null

    try {
      Class.forName("com.mysql.jdbc.Driver")
      conn = DriverManager.getConnection("jdbc:mysql://hadoop01:3306/test", "root", "mysql")
      val sql = "replace into tb_offset (`topic`, `partitions`, `groupid`, `offset`) values(?, ?, ?, ?)"
      pstm = conn.prepareStatement(sql)
      offsetRanges.foreach(offsetRange=>{
        val topic = offsetRange.topic
        val partition: Int = offsetRange.partition
        val startOffset: Long = offsetRange.fromOffset
        val endOffset: Long = offsetRange.untilOffset
        pstm.setString(1,topic)
        pstm.setInt(2,partition)
        pstm.setString(3,groupId)
        pstm.setLong(4,endOffset)
        pstm.addBatch()
      })
      pstm.executeBatch()
    }catch {
      case e:Exception=>e.printStackTrace()
    }finally {
      if (null !=pstm)pstm.close()
      if (null !=conn)conn.close()
    }


  }


  def main(args: Array[String]): Unit = {

//    val map: Map[TopicPartition, Long] = getOffsetFromMysql(Set("test","test2"),"bg")
//    map
//      .foreach(println)
    setOffsetToMysql(
      Array(
        OffsetRange("xx-tp", 0, 11L, 50L),
        OffsetRange("xx-tp", 1, 11L, 50L),
        OffsetRange("xx-tp", 2, 11L, 50L),
        OffsetRange("xx-tp", 3, 11L, 50L)
      ),
      "group_id_00001"
    )
  }
}
