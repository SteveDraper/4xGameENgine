package server.util

import model.Span

import scalaz.syntax.std.boolean._
import scalaz.std.option._

trait SpanOps {
  val s: Span
  def size = s.to - s.from
  def clamp(d: Double) =
    Math.min(Math.max(d, s.from), s.to)
  def intersect(s2: Span): Option[Span] = {
    val min = Math.max(s.from, s2.from)
    val max = Math.min(s.to, s2.to)

    (max >= min) ? some(Span(min,max)) | None
  }

}

object SpanOps {
  implicit def toSpanOps(target: Span): SpanOps = new SpanOps {
    val s = target
  }
}
