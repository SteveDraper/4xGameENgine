package model.property

import argonaut.Argonaut._
import argonaut.CodecJson

final case class ScalarProperty(name: String, value: Double)

object ScalarProperty {
  implicit val codec: CodecJson[ScalarProperty] =
    casecodec2(ScalarProperty.apply, ScalarProperty.unapply)("name","value")
}