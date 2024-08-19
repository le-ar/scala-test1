package com.chat.core

import javax.inject.Inject
import sangria.schema._
import sangria.relay._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class ViewerSchema[Ctx](schemas: Seq[AbstractSchema[Ctx]])(implicit ec: ExecutionContext) extends AbstractSchema[Ctx] {
  val allSchemas = Seq(this) ++ schemas

  def viewerFromCtx(ctx: Context[Ctx, Unit]): Future[Viewer] = Future.successful(Viewer(""))

  lazy val ViewerType: ObjectType[Ctx, ViewerNode] =
    ObjectType(
      "Viewer",
      interfaces[Ctx, ViewerNode](nodeInterface),
      () => fields[Ctx, ViewerNode](Node.globalIdField) ++ allSchemas.flatMap(_.viewerFields)
    )

  private val nodesMap = allSchemas.flatMap(_.nodes.map(n => n.name -> n.resolve)).toMap
  private val nodeTypes = allSchemas.flatMap(_.nodes.map(_.nodeType))
  private val allRootFields = allSchemas.flatMap(_.rootFields).toList

  implicit val allIdentifiables: Seq[sangria.relay.Identifiable[_ <: sangria.relay.Node]] =
    allSchemas.flatMap(_.nodes.map(_.identifiable))

  override val NodeDefinition(nodeInterface, nodeField, nodesField) =
    Node.definition(
      (globalId: GlobalId, ctx: Context[Ctx, Unit]) =>
        nodesMap.get(globalId.typeName).fold(Future.successful[Option[Node]](None))(_(globalId, ctx)),
      Node.possibleNodeTypes[Ctx, Node](nodeTypes: _*)
    )

  val QueryType: ObjectType[Ctx, Unit] = ObjectType(
    "Query",
    fields[Ctx, Unit](
      Field("viewer", ViewerType, resolve = ctx => viewerFromCtx(ctx): Future[ViewerNode])
    )
      ++ allRootFields
      ++ fields[Ctx, Unit](nodeField, nodesField)
  )

  val allMutations =
    allSchemas
      .flatMap(_.mutations(this))
      .asInstanceOf[Seq[Field[Ctx, Unit]]]

  val MutationType: Option[ObjectType[Ctx, Unit]] =
    Option(allMutations)
      .filter(_.nonEmpty)
      .map(mutations => ObjectType("Mutation", fields[Ctx, Unit](mutations: _*)))

  lazy val schema = Schema(QueryType, MutationType)
}
