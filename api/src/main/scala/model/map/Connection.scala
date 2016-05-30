package model.map

import argonaut.Argonaut._
import argonaut.CodecJson

final case class Connection(from: Point, to: Point)

object Connection {
  implicit val codec: CodecJson[Connection] =
    casecodec2(Connection.apply, Connection.unapply)("from","to")
}