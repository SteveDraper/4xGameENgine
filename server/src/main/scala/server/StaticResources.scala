package server

import org.http4s.Response
import server.ApiHelper._

import scala.io.{BufferedSource, Source}
import scala.util.Try
import scalaz.concurrent.Task
import scalaz.syntax.either._
import scalaz.syntax.std.option._

trait StaticResourceProvider {
  def get(name: String): Task[Response]
}

object EmbeddedStaticResources extends StaticResourceProvider {
  def get(name: String): Task[Response] =  {
    joinRaw(getResourceLines(name))
  }

  private def getResourceLines(name: String): TaskFailureOr[String] = {
    for {
      url <- wrapM(Try(getClass.getResource(s"/public/$name")).toOption.flatMap(Option(_)) \/> notFound(s"Resource '$name' not found"))
      source <- wrapM(Source.fromURL(url).right)
    } yield source.getLines mkString "\n"
  }
}

final case class FilesystemStaticResources(root: String) extends StaticResourceProvider {
  def get(name: String): Task[Response] =  {
    joinRaw(getResourceLines(name))
  }

  private def getResourceLines(name: String): TaskFailureOr[String] = {
    for {
      source <- wrapM(Try(Source.fromFile(s"$root/$name")).toOption.flatMap(Option(_)) \/> notFound(s"Resource '$name' not found"))
    } yield source.getLines mkString "\n"
  }
}
