package topology

sealed trait ProjectionCoordinates

final case class CartesianCoordinates(x: Double, y: Double) extends ProjectionCoordinates