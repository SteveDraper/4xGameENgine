package server

import org.http4s.HttpService
import org.http4s.dsl._


object AscentService {
  val service = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
  }
}
