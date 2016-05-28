package server

import java.net.InetSocketAddress

import org.http4s.server.blaze.BlazeBuilder


object ServerApp extends App {
  val builder = BlazeBuilder.mountService(AscentService.service)
  val server =
    builder
      .bindSocketAddress(new InetSocketAddress(9600))
      .run

  server.awaitShutdown()
}
