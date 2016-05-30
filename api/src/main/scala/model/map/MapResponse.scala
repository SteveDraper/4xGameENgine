package model.map

import argonaut.Argonaut._
import argonaut.CodecJson

final case class MapResponse(topology: MapTopology,
                             extraConnections: List[Connection],
                             cells: List[CellInfo])

object MapResponse {
  implicit val codec: CodecJson[MapResponse] =
    casecodec3(MapResponse.apply, MapResponse.unapply)("topology","extraConnections","cells")
}