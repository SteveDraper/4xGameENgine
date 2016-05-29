package server

import java.io.File
import java.net.InetSocketAddress

import org.http4s.server.blaze.BlazeBuilder
import knobs._
import scalaz.std.option._
import scalaz.syntax.std.boolean._


object ServerApp extends App {
  val specifiedConfigFile = (args.length > 0) ? some(args(0)) | None
  val config =
    loadImmutable(
      Required(
        specifiedConfigFile
          .fold(
            ClassPathResource("default.cfg"))(
            filename => FileResource(new File(filename))))
        :: Nil
    )

  runServer

  private def runServer = {
    val serverTask = for {
      conf <- config
      server <- createServer(conf)
    } yield server.awaitShutdown

    val result =
      serverTask
        .attemptRun
        .fold(t => s"Service failed to start: ${t.toString}",_=>"Service completed successfully")

    println(result)
  }

  private def createServer(conf: Config) = {
    val port = conf.require[Int]("port")
    val resourceProvider =
      conf
        .lookup[String]("resources")
        .fold(EmbeddedStaticResources: StaticResourceProvider)(FilesystemStaticResources)

    val builder = BlazeBuilder.mountService(AscentService.service(resourceProvider))

    println(s"Creating Ascent server on port $port")
    builder
      .bindSocketAddress(new InetSocketAddress(port))
      .start
  }
}
