package server.map


trait Region[R] extends Area[R] {
  def combine(r1: R, r2:R): R
  def intersect(r1: R, r2: R): R
}

trait RegionOps[R] {
  def typeClassInstance: Region[R]
  def self: R
  def combine(other: R): R = typeClassInstance.combine(self, other)
  def intersect(other: R): R = typeClassInstance.intersect(self, other)
}

object RegionOps {
  implicit def toRegionOps[R](target: R)(implicit tc: Region[R]): RegionOps[R] = new RegionOps[R] {
    val self = target
    val typeClassInstance = tc
  }
}
