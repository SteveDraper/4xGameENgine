package topology


trait Projection[C <: Cell, P <: ProjectionCoordinates] {
  def cellCoordinates(c: C): P
}