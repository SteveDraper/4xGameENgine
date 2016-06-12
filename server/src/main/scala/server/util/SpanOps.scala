package server.util

import model.Span

trait SpanOps {
  val s: Span
  def size = s.to - s.from
  def clamp(d: Double) =
    Math.min(Math.max(d, s.from), s.to)
}

object SpanOps {
  implicit def toSpanOps(target: Span): SpanOps = new SpanOps {
    val s = target
  }
}
