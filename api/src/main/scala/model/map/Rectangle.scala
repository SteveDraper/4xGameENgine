package model.map

import argonaut.Argonaut._
import argonaut.CodecJson

final case class Rectangle(topLeft: Point, bottomRight: Point)

object Rectangle {
  implicit val codec: CodecJson[Rectangle] =
    casecodec2(Rectangle.apply, Rectangle.unapply)("topLeft", "bottomRight")
}