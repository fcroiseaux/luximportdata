package com.intech.luxdataimport

import scala.xml._

import org.xml.sax.{SAXNotRecognizedException, SAXNotSupportedException}
import com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl
import javax.xml.parsers.ParserConfigurationException
import au.com.bytecode.opencsv.CSVReader
import scala.collection.JavaConversions._
import java.io._



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

  def resultUrl(year: String, town: String) = "http://www.elections.public.lu/fr/elections-legislatives/" + year + "/resultats/communes/" + town + "/index.html"

  def main(args: Array[String]) {
    //Change default parser in order not to validate DTD
    System.setProperty("javax.xml.parsers.SAXParserFactory", "com.intech.luxdataimport.NoDTDXMLParserFactory")

    val in = getClass.getResourceAsStream("/towns.csv")
    val out = new java.io.PrintWriter(new java.io.FileWriter("./result.csv"))
    out.println("annee,canton,commune,parti,candidat,suffrages")
    val towns = new CSVReader(new InputStreamReader(in), ',')
    for (row <- towns.readAll) {
      val canton = row(0)
      val town = row(1)
      val townResults = parseTown("2009", canton, town)
      townResults.map(line => out.println(line.mkString(",")))
      println(town + "-> OK " + townResults.size + " records parsed")
    }
    out.flush
  }

  def getTableTitle(table: NodeSeq) = (table \\ "thead" \\ "a")(0).text

  def loadPage(pageUrl: String) = XML.load(new java.net.URL(pageUrl))


  /**
   *
   * @param town
   * @return Tha html result page for the given town, parsed with scala XML utility
   */
  def loadTown(year: String, town: String) = loadPage(resultUrl(year, town))

  /**
   *
   * @param htmlTownPage
   * @return The list of htmml tables included in the page
   */
  def resultTables(htmlTownPage: Elem) = htmlTownPage \\ "table"

  def parseTown(year: String, canton: String, town: String) = {
    val tables = resultTables(loadTown(year, town.toLowerCase))
    val results = (2 to 8).map(tables(_))
    results.map{ table =>
      val party = getTableTitle(table).substring(4)
      //
      (table \\ "tr") // Retrieve lines
        .map(_ \\ "td") // Retrieves cells
        .filter(_.size > 1) // Skip if line is empty
        .drop(2) // Skip the 2 first lines because they arre consolidated data
        .map(ns => List(year, canton, town, party, ns(0).text, ns(1).text)) // Create the result
    }.flatten
  }
}

