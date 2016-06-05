package server.map

import model.map.{Point, Rectangle}

trait Area[R] {
  def contains(r: R, p: Point): Boolean
}

trait AreaOps[R] {
  def typeClassInstance: Area[R]
  def self: R
  def contains(p: Point) = typeClassInstance.contains(self,p)
}

object AreaOps {
  implicit def toAreaOps[R](target: R)(implicit tc: Area[R]): AreaOps[R] = new AreaOps[R] {
    val self = target
    val typeClassInstance = tc
  }
}

trait AreaInstances {
  implicit val rectangleAreaOps = new Area[Rectangle] {
    def contains(r: Rectangle, p: Point): Boolean = {
      (p.x >= r.topLeft.x &&
        p.x <= r.bottomRight.x &&
        p.y >= r.topLeft.y &&
        p.y <= r.bottomRight.y)
    }
  }
}

object Area extends AreaInstances