package com.chat.apps.client

import com.chat.core.AppServer
import sangria.schema.Schema
import scala.concurrent.Future
import javax.inject.Inject
import com.chat.core.db.DB
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.Directives._
import com.chat.core.CurrentUser
import com.chat.features.user.models.User
import scala.concurrent.ExecutionContext

class ClientAppServer @Inject() (
  clientSchema: ClientSchema,
  db: DB
)(implicit ec: ExecutionContext)
    extends AppServer[ClientCtx] {
  override lazy val appName: String = "client"
  override lazy val PORT = 8081

  override val schema: Schema[ClientCtx, Unit] = clientSchema.schema

  override def createCtx(isDev: Boolean)(implicit request: RequestContext): Future[ClientCtx] =
    if (isDev) {
      Future.successful(ClientCtx(CurrentUser(1)))
    } else {
      val userToken = request.request.headers.find(_.name == "UserToken").map(_.value).get

      for {
        userByToken <- db.getUserByToken(userToken).map(_.get)
      } yield ClientCtx(CurrentUser(userByToken.id))
    }
}
