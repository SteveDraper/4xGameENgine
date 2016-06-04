package server.properties

import state.property.{PropertyId, PropertyUpdater}

final case class PropertyInfo[P](name: String,
                                 description: String,
                                 updater: PropertyUpdater[P])
