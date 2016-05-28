package topology


case class StaticNeighbourhood[T](center: T, neighbours: Traversable[T]) extends Neighbourhood[T]

