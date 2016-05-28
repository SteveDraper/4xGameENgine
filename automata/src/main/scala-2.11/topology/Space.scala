package topology

trait Space[T] {
  def neighbourhoods: Traversable[Neighbourhood[T]]
}