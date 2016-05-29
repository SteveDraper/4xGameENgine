package topology


trait CartesianProjection[C <: Cell] extends Projection[C,CartesianCoordinates] {
  def cellCoordinates(c: C): CartesianCoordinates
}
