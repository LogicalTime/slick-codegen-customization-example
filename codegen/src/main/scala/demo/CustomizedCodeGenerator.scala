package demo

import java.io.File
import java.net.URL

import com.typesafe.config.ConfigFactory
import slick.driver.H2Driver

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

/**
  * This customizes the Slick code generator. We only do simple name mappings.
  * For a more advanced example see https://github.com/cvogt/slick-presentation/tree/scala-exchange-2013
  */
object CustomizedCodeGenerator {
  def main(args: Array[String]) = {
    val cgs: SlickCodegenSettings = {
      def inputfileConfig = ConfigFactory.parseURL(new URL(args(0)))
      def fullConfig = ConfigFactory.load().withFallback(inputfileConfig).resolve()

      SlickCodegenSettings(fullConfig)
    }

    println(cgs)
    val makeDir = (new File(cgs.destDir)).mkdir()
    println(s"makeDir=$makeDir")

    Await.ready(
      codegen(cgs).map(_.writeToFile(
        cgs.databaseConfig.driver.toString,
        args(0),
        cgs.destPkg,
        cgs.destContainer,
        cgs.destFilenameWithExtension
      )),
      20.seconds
    )
  }


  def codegen(cgs: SlickCodegenSettings) = {
    cgs.databaseConfig.db.run {
      H2Driver.defaultTables.map(_.filter(t => cgs.tableNames.get contains t.name.name)).flatMap(H2Driver.createModelBuilder(_, ignoreInvalidDefaults = false).buildModel)
    }.map { model =>
      new slick.codegen.SourceCodeGenerator(model) {
        // customize Scala entity name (case class, etc.)
        override def entityName = {
          case "COFFEES"       => "Coffee"
          case "SUPPLIERS"     => "Supplier"
          case "COF_INVENTORY" => "CoffeeInventoryItem"
          case dbTableName     => super.entityName(dbTableName)
        }
        // customize Scala table name (table class, table values, ...)
        override def tableName = {
          case "COF_INVENTORY" => "CoffeeInventory"
          case dbTableName     => super.tableName(dbTableName)
        }
        // override generator responsible for tables
        override def Table = new Table(_) {
          table =>
          // customize table value (TableQuery) name (uses tableName as a basis)
          override def TableValue = new TableValue {
            override def rawName = super.rawName.uncapitalize
          }
          // override generator responsible for columns
          override def Column = new Column(_) {
            // customize Scala column names
            override def rawName = (table.model.name.table, this.model.name) match {
              case ("COFFEES", "COF_NAME")       => "name"
              case ("COFFEES", "SUP_ID")         => "supplierId"
              case ("SUPPLIERS", "SUP_ID")       => "id"
              case ("SUPPLIERS", "SUP_NAME")     => "name"
              case ("COF_INVENTORY", "QUAN")     => "quantity"
              case ("COF_INVENTORY", "COF_NAME") => "coffeeName"
              case _                             => super.rawName
            }
          }
        }
      }
    }
  }


}
