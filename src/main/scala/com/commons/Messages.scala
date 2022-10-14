package com.commons

case class LoadJsonToRedis(pathFile: String)
case class ResponseLoadJsonToRedis(hash: String, records: Int, timeElaspsed: Int)
case class ExtractHash(pathFile: String)
case class ResponseHashFile(hash: String)