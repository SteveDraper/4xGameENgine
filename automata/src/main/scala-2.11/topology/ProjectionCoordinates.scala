package topology

import model.map.Point

sealed trait ProjectionCoordinates

final case class CartesianCoordinates(x: Double, y: Double) extends ProjectionCoordinates {
  def toPoint = Point(x,y)
}