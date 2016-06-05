package model

import argonaut.Argonaut._
import argonaut.CodecJson
import model.map.{Point, Rectangle}

import scalaz.{Lens, Store}

final case class Span(from: Double, to: Double)

object Span {
  implicit val rectxLens =
    Lens((r: Rectangle) => Store(
      (s:Span) => Rectangle(Point(s.from,r.topLeft.y), Point(s.to, r.bottomRight.y)),
      Span(r.topLeft.x, r.bottomRight.x)
    ))

  implicit val rectyLens =
    Lens((r: Rectangle) => Store(
      (s:Span) => Rectangle(Point(r.topLeft.x,s.from), Point(r.bottomRight.x,s.to)),
      Span(r.topLeft.y, r.bottomRight.y)
    ))

  implicit val codec: CodecJson[Span] =
    casecodec2(Span.apply, Span.unapply)("min", "max")
}