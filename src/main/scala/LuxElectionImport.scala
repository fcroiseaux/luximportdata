package com.intech.luxdataimport

import scala.xml._

import org.xml.sax.{SAXNotRecognizedException, SAXNotSupportedException}
import com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl
import javax.xml.parsers.ParserConfigurationException


//Used to speed html page parsing by not validating DTD

@throws(classOf[SAXNotRecognizedException])
@throws(classOf[SAXNotSupportedException])
@throws(classOf[ParserConfigurationException])
class NoDTDXMLParserFactory extends SAXParserFactoryImpl() {
  super.setFeature("http://xml.org/sax/features/validation", false)
  super.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
  super.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
  super.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
}


object LuxElectionImport {

  val electionSiteUrl = "http://www.elections.public.lu/fr/elections-legislatives/2009/resultats/communes/"

  def resultUrl(town: String) = electionSiteUrl + town + "/index.html"

  def main(args: Array[String]) {
    System.setProperty("javax.xml.parsers.SAXParserFactory", "com.intech.luxdataimport.NoDTDXMLParserFactory")
    val tables = resultTables(loadTown("luxembourg"))
    val results = (2 to 8).map(tables(_))
    println(getContent(results(3)))
  }

  def getTableTitle(table: NodeSeq) = (table \\ "thead" \\ "a")(0).text

  def getContent(table: NodeSeq) = (table \\ "tr").map(_ \\ "td").filter(_.size > 1).map(ns => ns(0).text -> ns(1).text)

  def loadPage(pageUrl: String) = {
    XML.load(new java.net.URL(pageUrl))
  }

  def loadTown(town: String) = loadPage(resultUrl(town))

  def resultTables(htmlTownPage: Elem) = htmlTownPage \\ "table"
}

