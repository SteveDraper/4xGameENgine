package model.map

import argonaut.Argonaut._
import argonaut.CodecJson

final case class Point(x: Double, y: Double)

object Point {
  implicit val codec: CodecJson[Point] =
    casecodec2(Point.apply, Point.unapply)("x","y")
}