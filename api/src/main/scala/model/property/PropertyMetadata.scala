package model.property

import argonaut.{DecodeJson, EncodeJson, Json}
import argonaut.Argonaut._
import model.Span

final case class PropertyMetadata(name: String,
                                  description: String,
                                  valueRange: Option[Span])

object PropertyMetadata {
  val nameField = "name"
  val descriptionField = "description"
  val valueRangeField = "valueRange"

  implicit val encode: EncodeJson[PropertyMetadata] =
    EncodeJson[PropertyMetadata]((p: PropertyMetadata) => {
      (nameField := p.name) ->:
      (descriptionField := p.description) ->:
      (valueRangeField :?= p.valueRange) ->?:
      Json.jEmptyObject
    })

  implicit val decode: DecodeJson[PropertyMetadata] =
    DecodeJson(c => for {
      name <- (c --\ nameField).as[String]
      description <- (c --\ descriptionField).as[String]
      valueRange <- (c --\ valueRangeField).as[Option[Span]]
    } yield PropertyMetadata(name, description, valueRange))
}