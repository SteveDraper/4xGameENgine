package model

import argonaut._
import Argonaut._

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
                             xWrap: Boolean,
                             yWrap: Boolean,
                             cellSpacing: Double)

object MapTopology {
  implicit val codec: CodecJson[MapTopology] =
    casecodec4(MapTopology.apply, MapTopology.unapply)("baseType","xWrap","yWrap","cellSpacing")
}

trait MapTopologyProvider {
  def getMapTopology: MapTopology
}