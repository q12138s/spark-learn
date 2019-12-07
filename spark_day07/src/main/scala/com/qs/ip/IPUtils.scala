package com.qs.ip

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.util.Properties

object IPUtils {

  def binarySezrch(ipLong:Long,ipAddr: Array[(Long, Long, String, String)]): Int ={
    //起始下标
    var startIndex = 0
    //结尾下标
    var endIndex = ipAddr.length-1
    while (startIndex<=endIndex){
      //中间下标
      var middleIndex = startIndex+(endIndex-startIndex)/2
      val (startIP, endIP, _, _) = ipAddr(middleIndex)
      if (ipLong<=endIP&&ipLong>=startIP){
        return middleIndex
      }else if(ipLong<startIP){
          endIndex=middleIndex-1
      }else if(ipLong>endIP){
        startIndex=middleIndex+1
      }
    }
    -1
  }

  def ipToLong(ip:String): Long ={
    val fragments = ip.split("[.]")
    var ipNum = 0L
    for (i <- 0 until fragments.length){
      ipNum =  fragments(i).toLong | ipNum << 8L
    }
    // 返回
    ipNum
  }


  def saveToMysql(part:Iterator[((String,String),Int)]): Unit ={
    //写JDBC
    //TODO-1：申请Driver类
    Class.forName("com.mysql.jdbc.Driver")
    //申明连接配置
    var conn:Connection = null
    var prep:PreparedStatement = null
    val url = "jdbc:mysql://hadoop01:3306/test"
    val prop = new Properties()
    prop.put("user","root")
    prop.put("password","mysql")
    val sql = "insert into tb_ip_location(longitude,latitude,total_count) values(?,?,?)"
    try{
      conn = DriverManager.getConnection(url,prop)
      prep = conn.prepareStatement(sql)
      //迭代分区数据，赋值
      part.foreach{ case ((longitude,latitude),number)=> {
        //将经度赋值
        prep.setString(1,longitude)
        //将纬度赋值
        prep.setString(2,latitude)
        //将个数赋值
        prep.setInt(3,number)
        //添加到批处理
        prep.addBatch()
      }}
      //执行批处理
      prep.executeBatch()
    }catch {
      case e:Exception => e.printStackTrace()
    }finally {
      if(prep != null) prep.close()
      if(conn != null) conn.close()
    }
  }

}
