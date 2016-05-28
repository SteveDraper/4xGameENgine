package server

import org.http4s.Response
import server.ApiHelper._

import scala.io.{BufferedSource, Source}
import scala.util.Try
import scalaz.concurrent.Task
import scalaz.syntax.either._
import scalaz.syntax.std.option._


object StaticResources {
  def get(name: String): Task[Response] =  {
    joinRaw(getResourceLines(name))
  }

  private def getResourceLines(name: String): TaskFailureOr[String] = {
    for {
      url <- wrapM(Try(getClass.getResource(s"/$name")).toOption.flatMap(Option(_)) \/> notFound(s"Resource '$name' not found"))
      source <- wrapM(Source.fromURL(url).right)
    } yield source.getLines mkString "\n"
  }
}
