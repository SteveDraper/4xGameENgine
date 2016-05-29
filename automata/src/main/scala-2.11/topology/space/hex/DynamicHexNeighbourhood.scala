package topology.space.hex
import topology.Neighbourhood
import topology.space.CartesianCell

trait DynamicHexNeighbourhood extends Neighbourhood[CartesianCell]

case class GenericDynamicHexNeighbourhood(center: CartesianCell, space: HexSpace) extends DynamicHexNeighbourhood {
  def neighbours = new Traversable[CartesianCell] {
    val allowUp = ( center.y > 0 || space.wrapY )
    val allowDown = ( center.y < space.height-1 || space.wrapY )
    val allowLeft = ( center.x > 0 || space.wrapX )
    val allowRight = ( center.x < space.width-1 || space.wrapX )

    def foreach[U](f: (CartesianCell) => U) = {
      if ( allowUp ) {
        val upY = (center.y+space.height-1)%space.height
        if ( allowLeft ) f(space.cellAt((center.x+space.width-1)%space.width, upY))
        f(space.cellAt(center.x,upY))
        if ( allowRight ) f(space.cellAt((center.x+space.width+1)%space.width, upY))
      }
      if ( allowDown ) {
        val downY = (center.y+space.height+1)%space.height
        if ( allowLeft ) f(space.cellAt((center.x+space.width-1)%space.width, downY))
        f(space.cellAt(center.x,downY))
        if ( allowRight ) f(space.cellAt((center.x+space.width+1)%space.width, downY))
      }
    }
  }
}

case class CentralDynamicHexNeighbourhood(center: CartesianCell, space: HexSpace) extends DynamicHexNeighbourhood {
  def neighbours = new Traversable[CartesianCell] {
    def foreach[U](f: (CartesianCell) => U) = {
      val upY = center.y-1
      val downY = center.y+1
      val xEven = center.x%2 == 0

      if ( xEven ) {
        f(space.cellAt(center.x-1, center.y))
        f(space.cellAt(center.x,upY))
        f(space.cellAt(center.x+1, center.y))
        f(space.cellAt(center.x-1, downY))
        f(space.cellAt(center.x,downY))
        f(space.cellAt(center.x+1, downY))
      }
      else {
        f(space.cellAt(center.x - 1, upY))
        f(space.cellAt(center.x, upY))
        f(space.cellAt(center.x + 1, upY))
        f(space.cellAt(center.x - 1,center.y))
        f(space.cellAt(center.x, downY))
        f(space.cellAt(center.x + 1, center.y))
      }
    }
  }
}

object DynamicHexNeighbourhood {
  def apply(center: CartesianCell, space: HexSpace) = {
    val allowUp = ( center.y > 0 )
    val allowDown = ( center.y < space.height-1 )
    val allowLeft = ( center.x > 0 )
    val allowRight = ( center.x < space.width-1 )

    if ( allowUp && allowDown && allowLeft && allowRight )
      new CentralDynamicHexNeighbourhood(center, space)
    else
      new GenericDynamicHexNeighbourhood(center, space)
  }
}