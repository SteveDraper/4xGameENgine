package model.property

import argonaut.Argonaut._
import argonaut.CodecJson

final case class VectorPropertyElement(elementId: Int, value: Double)

final case class VectorProperty(name: String, elements: List[VectorPropertyElement])

object VectorPropertyElement {
  implicit val codec: CodecJson[VectorPropertyElement] =
    casecodec2(VectorPropertyElement.apply, VectorPropertyElement.unapply)("name","elements")
}

object VectorProperty {
  implicit val codec: CodecJson[VectorProperty] =
    casecodec2(VectorProperty.apply, VectorProperty.unapply)("id","value")
}