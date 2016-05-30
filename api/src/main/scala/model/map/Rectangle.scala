package model.map

import argonaut.Argonaut._
import argonaut.CodecJson

final case class Rectangle(topLeft: Point, bottomRight: Point) {
  def contains(p: Point) = {
    //  TODO - handle wrap
    (p.x >= topLeft.x &&
     p.x <= bottomRight.x &&
     p.y >= topLeft.y &&
     p.y <= bottomRight.y)
  }
}

object Rectangle {
  implicit val codec: CodecJson[Rectangle] =
    casecodec2(Rectangle.apply, Rectangle.unapply)("topLeft", "bottomRight")
}