package model.map

import argonaut.Argonaut._
import argonaut._

sealed trait TopologyFamily {
  val encodingString: String
}
case object Hex extends TopologyFamily {
  val encodingString = "Hex"
}
case object RectWithDiagonals extends TopologyFamily {
  val encodingString = "RectWithDiagonals"
}

object TopologyFamily {
  implicit val encode: EncodeJson[TopologyFamily] =
    new EncodeJson[TopologyFamily] {
      def encode(a: TopologyFamily): Json = a.encodingString.asJson
    }

  implicit val decode: DecodeJson[TopologyFamily] =
    DecodeJson(c => for {
      value <- c.as[String]
      result <- value match {
        case Hex.encodingString => DecodeResult.ok(Hex:TopologyFamily)
        case RectWithDiagonals.encodingString => DecodeResult.ok(RectWithDiagonals:TopologyFamily)
        case _ => DecodeResult.fail[TopologyFamily](s"'$value' is not a valid topology type", c.history)
      }
    } yield result)
}

final case class MapTopology(baseType: TopologyFamily,
                             xWrap: Option[Double],
                             yWrap: Option[Double],
                             cellSpacing: Double)

object MapTopology {
  val baseTypeField = "baseType"
  val xWrapField = "xWrap"
  val yWrapField = "yxWrap"
  val cellSpacingField = "cellSpacing"

  implicit val encode: EncodeJson[MapTopology] =
    EncodeJson[MapTopology]((p: MapTopology) => {
      (baseTypeField := p.baseType) ->:
      (cellSpacingField := p.cellSpacing) ->:
      (xWrapField :?= p.xWrap) ->?:
      (yWrapField :?= p.yWrap) ->?:
      Json.jEmptyObject
    })

  implicit val decode: DecodeJson[MapTopology] =
    DecodeJson(c => for {
      baseType <- (c --\ baseTypeField).as[TopologyFamily]
      cellSpacing <- (c --\ cellSpacingField).as[Double]
      xWrap <- (c --\ xWrapField).as[Option[Double]]
      yWrap <- (c --\ yWrapField).as[Option[Double]]
    } yield MapTopology(baseType, xWrap, yWrap, cellSpacing))
}

trait MapTopologyProvider {
  def projectionWidth: Double
  def projectionHeight: Double
  def getMapTopology: MapTopology
}