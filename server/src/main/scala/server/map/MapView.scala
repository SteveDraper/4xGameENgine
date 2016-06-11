package server.map

import model.map.{Point, Rectangle}

final case class MapView(origin: Point,
                         visibleBounds: Rectangle)