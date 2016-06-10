package server.player

import model.map.{Point, Rectangle}
import org.http4s.Request
import server.ApiHelper._

final case class User(origin: Point,
                      visibleBounds: Rectangle)

object User {
  val testUser =
    User(
      Point(0,0),
      Rectangle(
        Point(-10.0,-10.0),
        Point(10.0,10.0)
      ))

  def getCurrentUser(req: Request): TaskFailureOr[User] = {
    successM(testUser)
  }
}