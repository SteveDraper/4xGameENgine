package model.map

import argonaut.Argonaut._
import argonaut.CodecJson
import model.property.{ScalarProperty, VectorProperty}

final case class CellInfo(location: Point,
                          scalarProperties: List[ScalarProperty],
                          vectorProperties: List[VectorProperty])

object CellInfo {
  implicit val codec: CodecJson[CellInfo] =
    casecodec3(CellInfo.apply, CellInfo.unapply)("location", "scalars", "vectors")
}
