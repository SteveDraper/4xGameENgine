package model.property

import argonaut.Argonaut._
import argonaut.CodecJson

final case class PropertiesResponse(scalarProperties: List[PropertyMetadata],
                                    vectorProperties: List[PropertyMetadata])

object PropertiesResponse {
  implicit val codec: CodecJson[PropertiesResponse] =
    casecodec2(PropertiesResponse.apply, PropertiesResponse.unapply)("scalars","vectors")
}