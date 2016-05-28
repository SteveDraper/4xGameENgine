package topology

trait Neighbourhood[T] {
  val center: T
  def neighbours: Traversable[T]
}
