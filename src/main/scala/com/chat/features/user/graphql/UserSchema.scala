package com.chat.features.user.graphql

import javax.inject.{Inject, Singleton}
import com.chat.core.{AbstractSchema, CommonCtx}
import com.chat.features.user.models.User
import sangria.schema._
import sangria.relay._
import com.chat.core.NodeSchema
import com.chat.core.db.DB
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import cats.Functor
import cats.instances.future._
import cats.instances.option._
import cats.syntax.functor._
import com.chat.core.{GQNode, GQNodeObject}
import com.chat.core.ViewerNode
import scala.reflect.ClassTag

final case class UserNode(user: User) extends GQNode[User] {
  override val entity: User = user
  override val id: String = user.id.toString
}
object UserNode extends GQNodeObject[User, UserNode] {
  override def createNode(entity: User): UserNode = UserNode(entity)
}

@Singleton
class UserSchema[Ctx <: UserOptCtx] @Inject() (
  db: DB
)(implicit ec: ExecutionContext, ct: ClassTag[Ctx])
    extends AbstractSchema[Ctx] {

  lazy val UserType: ObjectType[Ctx, UserNode] =
    ObjectType(
      "User",
      interfaces[Ctx, UserNode](nodeInterface),
      () =>
        fields[Ctx, UserNode](
          Node.globalIdField,
          Field("login", StringType, resolve = _.value.login)
        )
    )

  val ConnectionDefinition(_, userConnection) =
    Connection.definition[Ctx, Connection, UserNode](
      "User",
      UserType
    )

  override lazy val nodes = Seq(
    NodeSchema[Ctx, UserNode](
      "User",
      (gid: GlobalId, ctx: Context[Ctx, Unit]) => db.getUserById(gid.id.toInt),
      UserType,
      UserNode.IdentifiableInstance
    )
  )

  override lazy val rootFields: List[Field[Ctx, Unit]] = fields(
    Field("allUsers", ListType(UserType), resolve = ctx => db.getAllUsers(): Future[Seq[UserNode]]),
    Field(
      "users",
      userConnection,
      arguments = Connection.Args.All,
      resolve =
        ctx => db.getAllUsers().map(users => Connection.connectionFromSeq(users: Seq[UserNode], ConnectionArgs(ctx)))
    )
  )

  override lazy val viewerFields: List[Field[Ctx, ViewerNode]] =
    ct.runtimeClass match {
      case userCtx if userCtx == classOf[UserCtx] =>
        fields[Ctx, ViewerNode](
          Field(
            "user",
            UserType,
            resolve = ctx => db.getUserById(ctx.ctx.asInstanceOf[UserCtx].currentUser.id).map(_.get): Future[UserNode]
          )
        )
      case userCtx if userCtx == classOf[UserOptCtx] =>
        fields[Ctx, ViewerNode](
          Field(
            "user",
            OptionType(UserType),
            resolve = ctx =>
              ctx.ctx.currentUserOpt.fold(Future.successful(None): Future[Option[UserNode]])(u => db.getUserById(u.id))
          )
        )
      case _ =>
        fields[Ctx, ViewerNode]()
    }

  // override lazy val viewerFields: List[Field[Ctx, ViewerNode]] = fields(
  //   Field(
  //     "user",
  //     OptionType(UserType),
  //     resolve =
  //       ctx => ctx.ctx.currentUserOpt.fold(Future.successful(None): Future[Option[UserNode]])(u => db.getUserById(u.id))
  //   )
  // )
}
