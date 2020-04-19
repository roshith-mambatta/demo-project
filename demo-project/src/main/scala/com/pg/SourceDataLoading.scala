package com.pg

import com.pg.utils.Constants
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.functions._
import com.pg.utils.Reader._
import com.pg.utils.Writer._


object SourceDataLoading {
  def main(args: Array[String]): Unit = {

    try {
      val rootConfig = ConfigFactory.load("application.conf").getConfig("conf")
      val sftpConfig = rootConfig.getConfig("sftp_conf")
      val s3Config = rootConfig.getConfig("s3_conf")
      val mysqlConfig = rootConfig.getConfig("mysql_conf")

      println("Reading from SFTP: file :receipts_delta_GBR_14_10_2017.csv ...")
      val olTxnDf = readFromSftp(sftpConfig,"receipts_delta_GBR_14_10_2017.csv","csv","|")
          .withColumn("ins_ts",current_timestamp())
      println(s"Read ${olTxnDf.count()} records to DataFrame")


      val redshiftConfig = rootConfig.getConfig("redshift_conf")
      val jdbcUrl = Constants.getRedshiftJdbcUrl(redshiftConfig)
      writeDfToRedShift(s3Config,olTxnDf,jdbcUrl,"staging","stg_ol_transaction_sync","overwrite")

      println("Reading from MySQL: table :IRMSDB.TRANSACTIONSYNC ...")
      val transDF=readFromMySQL(mysqlConfig,"IRMSDB.TRANSACTIONSYNC")
        .withColumn("ins_ts",current_timestamp())
      println(s"Read ${transDF.count()} records to DataFrame")

      writeDfToRedShift(s3Config,transDF,jdbcUrl,"staging","stg_transaction_sync","overwrite")


    } catch {
      case ex: Throwable => {
        ex.printStackTrace()
      }
    }



  }
}
