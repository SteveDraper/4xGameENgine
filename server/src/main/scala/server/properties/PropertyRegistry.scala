package server.properties

import state.property.PropertyId


trait PropertyRegistry[SP,VP] {
  def scalarProperties: Map[PropertyId, PropertyInfo[SP]]
  def vectorProperties: Map[PropertyId, PropertyInfo[VP]]
}
