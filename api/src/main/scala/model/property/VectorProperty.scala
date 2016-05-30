package model.property

import argonaut.Argonaut._
import argonaut.CodecJson

final case class VectorProperty(elementId: Int, value: Double)

object VectorProperty {
  implicit val codec: CodecJson[VectorProperty] =
    casecodec2(VectorProperty.apply, VectorProperty.unapply)("id","value")
}