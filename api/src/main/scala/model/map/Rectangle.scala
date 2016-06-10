package model.map

import argonaut.Argonaut._
import argonaut.CodecJson

final case class Rectangle(topLeft: Point, bottomRight: Point) {
  def +(origin: Point) =
    Rectangle(topLeft + origin, bottomRight + origin)
}

object Rectangle {
  implicit val codec: CodecJson[Rectangle] =
    casecodec2(Rectangle.apply, Rectangle.unapply)("topLeft", "bottomRight")
}