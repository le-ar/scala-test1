package com.chat.core

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

import sangria.execution.UserFacingError
import sangria.relay.{GlobalId, Node, NodeDefinition}
import sangria.schema.{fields, Context => GQContext, Field}
import sangria.relay.{Node, PossibleNodeObject}
import sangria.relay.Identifiable

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import cats.Functor

final case class NodeSchema[Ctx, N <: Node](
  name: String,
  resolve: (GlobalId, GQContext[Ctx, Unit]) => Future[Option[N]],
  nodeType: PossibleNodeObject[Ctx, Node],
  identifiable: Identifiable[N]
)

trait AbstractSchema[Ctx] {
  protected case class TypeNotFoundException(msg: String) extends Exception(msg) with UserFacingError

  protected val NodeDefinition(nodeInterface, nodeField, nodesField) =
    Node.definition[Ctx, Unit, Node](
      (id: GlobalId, _: GQContext[Ctx, Unit]) => Future.successful(None),
      Node.possibleNodeTypes[Ctx, Node]()
    )

  lazy val nodes: Seq[NodeSchema[Ctx, _ <: Node]] = Seq()

  lazy val rootFields: List[Field[Ctx, Unit]] = fields()

  lazy val viewerFields: List[Field[Ctx, ViewerNode]] = fields()

  def mutations(schema: ViewerSchema[Ctx]): List[Field[Ctx, Unit]] =
    fields[Ctx, Unit]()
}

object AbstractSchema {}
