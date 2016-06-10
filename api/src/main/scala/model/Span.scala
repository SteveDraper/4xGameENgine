package model

import argonaut.Argonaut._
import argonaut.CodecJson
import model.map.{Point, Rectangle}
import monocle.Lens

final case class Span(from: Double, to: Double)

object Span {
  implicit val rectxLens =
    Lens[Rectangle,Span]((r: Rectangle) => Span(r.topLeft.x, r.bottomRight.x))(
                         (s: Span) => (r: Rectangle) => Rectangle(Point(s.from,r.topLeft.y), Point(s.to, r.bottomRight.y)))

  implicit val rectyLens =
    Lens[Rectangle,Span]((r: Rectangle) => Span(r.topLeft.y, r.bottomRight.y))(
                         (s: Span) => (r: Rectangle) => Rectangle(Point(r.topLeft.x, s.from), Point(r.bottomRight.x,s.to)))

  implicit val codec: CodecJson[Span] =
    casecodec2(Span.apply, Span.unapply)("min", "max")
}