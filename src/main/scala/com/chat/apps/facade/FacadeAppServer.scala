package com.chat.apps.facade

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

class FacadeAppServer @Inject() (
  facadeSchema: FacadeSchema,
  db: DB
)(implicit ec: ExecutionContext) extends AppServer[FacadeCtx] {
  override lazy val appName: String = "facade"

  override val schema: Schema[FacadeCtx, Unit] = facadeSchema.schema

  override def createCtx(isDev: Boolean)(implicit request: RequestContext): Future[FacadeCtx] = {
    val userToken = request.request.headers.find(_.name == "UserToken").map(_.value)

    for {
      userByToken <- userToken.fold(Future.successful(Option.empty[User]))(token => db.getUserByToken(token))
      currentUser = userByToken.map(u => CurrentUser(u.id))
    } yield FacadeCtx(currentUser)
  }
}
