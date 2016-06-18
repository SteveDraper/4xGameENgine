package server.util

import model.Span
import model.map.{Point, Rectangle}
import SpanOps._
import monocle.Lens


trait RectangleOps {
  import RectangleOps._

  val r: Rectangle

  def xspan = Span.rectxLens.get(r)
  def yspan = Span.rectxLens.get(r)
  def intersect(other: Rectangle): Option[Rectangle] = {
    for {
      xSpan <- xspan.intersect(other.xspan)
      ySpan <- yspan.intersect(other.yspan)
    } yield Rectangle(Point(xSpan.from, ySpan.from), Point(xSpan.to, ySpan.to))
  }
}

object RectangleOps {
  implicit def toRectangleOps(target: Rectangle): RectangleOps = new RectangleOps {
    val r = target
  }
}
