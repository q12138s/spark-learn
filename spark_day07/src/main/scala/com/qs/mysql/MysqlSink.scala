package com.qs.mysql

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.spark.sql.{ForeachWriter, Row}

class MysqlSink extends ForeachWriter[Row] {
  var conn: Connection = null
  var pstm: PreparedStatement = null

  override def open(partitionId: Long, version: Long): Boolean = {
    Class.forName("com.mysql.jdbc.Driver")
    conn = DriverManager.getConnection("jdbc:mysql://hadoop01:3306/test", "root", "mysql")
    true
  }

  override def process(value: Row): Unit = {
    val sql = "replace into wc values(null,?,?)"
    pstm = conn.prepareStatement(sql)

    pstm.setString(1, value.getString(0))
    pstm.setLong(2, value.getLong(1))
//    pstm.setString(1, value.getAs[String]("value"))
//    pstm.setLong(2, value.getAs[Long]("count"))
    pstm.executeUpdate()
  }

  override def close(errorOrNull: Throwable): Unit = {
    if (conn != null) conn.close()
    if (pstm != null) pstm.close()
  }
}
