package topology

import model.MapTopologyProvider

trait Space[T] extends MapTopologyProvider {
  def neighbourhoods: Traversable[Neighbourhood[T]]
}