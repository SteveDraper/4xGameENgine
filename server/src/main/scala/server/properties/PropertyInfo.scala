package server.properties

import model.Span
import state.property.PropertyUpdater

final case class PropertyInfo[P](name: String,
                                 description: String,
                                 valueRange: Option[Span],
                                 updater: PropertyUpdater[P])
