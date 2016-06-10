package model.map

import argonaut.Argonaut._
import argonaut.CodecJson

final case class Point(x: Double, y: Double) {
  def +(other: Point) = Point(x + other.x, y + other.y)
  def -(other: Point) = Point(x - other.x, y - other.y)
}

object Point {
  implicit val codec: CodecJson[Point] =
    casecodec2(Point.apply, Point.unapply)("x","y")
}