package model.map

import argonaut.Argonaut._
import argonaut.CodecJson

final case class MapMetadata(name: String,
                             description: String,
                             topology: MapTopology)

object MapMetadata {
  implicit val codec: CodecJson[MapMetadata] =
    casecodec3(MapMetadata.apply, MapMetadata.unapply)("name", "description", "topology")
}