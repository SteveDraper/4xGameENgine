package topology

import model.map.MapTopologyProvider

trait Space[T] extends MapTopologyProvider {
  def neighbourhoods: Traversable[Neighbourhood[T]]
}