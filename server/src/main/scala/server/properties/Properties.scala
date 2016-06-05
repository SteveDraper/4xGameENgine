package server.properties

import model.property.{PropertiesResponse, PropertyMetadata}
import org.http4s.{Request, Response}
import server.ApiHelper._

import scalaz.concurrent.Task

object Properties {
  def getProperties(req: Request): Task[Response] = {
    join {
      successM(
        PropertiesResponse(
          GamePropertyRegistry.scalarProperties.values.toList.map(toPropertyMetadata),
          GamePropertyRegistry.vectorProperties.values.toList.map(toPropertyMetadata))
      )
    }
  }

  private def toPropertyMetadata(p: PropertyInfo[_]) = {
    PropertyMetadata(p.name, p.description, p.valueRange)
  }
}
