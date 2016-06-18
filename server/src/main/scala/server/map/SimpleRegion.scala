package server.map

import model.map.{Point, Rectangle}
import server.util.RectangleOps

import scalaz.{Lens, Monoid}

final case class SimpleRegion(areas: List[Rectangle])

object SimpleRegion {
  implicit val simpleRegionAreaOps = new Area[SimpleRegion] {
    import AreaOps._

    def contains(r: SimpleRegion, p: Point): Boolean = {
      r.areas.exists(_.contains(p))
    }
  }

  implicit val simpleRegionRegionOps = new Region[SimpleRegion] {
    import AreaOps._
    import RectangleOps._

    def combine(r1: SimpleRegion, r2: SimpleRegion): SimpleRegion = {
      SimpleRegion(r1.areas ++ r2.areas)
    }

    def intersect(r1: SimpleRegion, r2: SimpleRegion): SimpleRegion = {
      SimpleRegion(
        (for {
          rect1 <- r1.areas
          rect2 <- r2.areas
        } yield rect1.intersect(rect2)).flatten)
    }

    def contains(r: SimpleRegion, p: Point): Boolean = r.contains(p)
  }

  implicit val simpleRegionMonoid = new Monoid[SimpleRegion] {
    import RegionOps._

    def zero: SimpleRegion = SimpleRegion(Nil)

    def append(f1: SimpleRegion, f2: => SimpleRegion): SimpleRegion =
      f1.combine(f2)
  }
}