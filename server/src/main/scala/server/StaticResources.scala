package server

import java.io.File

import org.http4s.Response
import server.ApiHelper._

import scala.io.{BufferedSource, Source}
import scala.util.Try
import scalaz.concurrent.Task
import scalaz.syntax.either._
import scalaz.syntax.std.option._
import org.http4s.StaticFile
import org.http4s.Request
import org.http4s.dsl._

import scalaz.syntax.std.boolean._

trait StaticResourceProvider {
  val resourcePrefix = "/resources/"

  def get(req: Request): Task[Response]

  def validateRequest(req: Request): TaskFailureOr[String] = {
    req.pathInfo.startsWith(resourcePrefix) ?
      successM(req.pathInfo.substring(resourcePrefix.length)) |
      wrapM(internalServerError(s"Resource path '${req.pathInfo}' invalid").left)
  }
}

object EmbeddedStaticResources extends StaticResourceProvider {

  def get(req: Request): Task[Response] =  {
    val result = for {
      virtualPath <- validateRequest(req)
      maybeResponse = StaticFile.fromResource(s"/public/$virtualPath", Some(req))
      response <- wrapM(maybeResponse \/> notFound(s"Resource '${req.pathInfo}' not found"))
    } yield response

    joinResponse(result)
  }
}

final case class FilesystemStaticResources(root: String) extends StaticResourceProvider {
  def get(req: Request): Task[Response] =  {
    val result = for {
      virtualPath <- validateRequest(req)
      maybeResponse = StaticFile.fromFile(new File(s"$root/$virtualPath"), Some(req))
      response <- wrapM(maybeResponse \/> notFound(s"Resource '${req.pathInfo}' not found"))
    } yield response

    joinResponse(result)
  }
}
