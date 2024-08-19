package com.chat.core

final case class Viewer(id: String)

final case class ViewerNode(viewer: Viewer) extends GQNode[Viewer] {
  override val entity: Viewer = viewer
  override val id: String = viewer.id
}

object ViewerNode extends GQNodeObject[Viewer, ViewerNode] {
  override def createNode(entity: Viewer): ViewerNode = ViewerNode(entity)
}