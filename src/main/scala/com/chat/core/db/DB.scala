package com.chat.core.db

import javax.inject.{Singleton, Inject}
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto._
import scala.concurrent.Future
import com.chat.features.user.models.User

final case class AuthUser(id: Int, login: String, password: String)
final case class AuthUserResponse(id: Int, token: String)

final case class AuthUserToken(id: Int, token: String)

@Singleton
class DB @Inject() () {
  val dbAuth = Seq(AuthUser(1, "admin", "admin"), AuthUser(2, "user", "user"))
  val dbAuthToken = Seq(AuthUserToken(1, "tokenadmin"), AuthUserToken(2, "tokenuser"))
  val dbUser = Seq(User(1, "admin"), User(2, "user"))

  def authByPassword(
      login: String,
      password: String
  ): Option[AuthUserResponse] =
    dbAuth
      .find(a => a.login == login && a.password == password)
      .map(a => AuthUserResponse(a.id, dbAuthToken.find(_.id == a.id).get.token))

  def getAllUsers(): Future[Seq[User]] = Future.successful(dbUser)
  def getUserById(id: Int): Future[Option[User]] = Future.successful(dbUser.find(_.id == id))
  def getUserByToken(token: String): Future[Option[User]] = Future.successful(dbAuthToken.find(_.token == token).flatMap(t => dbUser.find(_.id == t.id)))
}

object DB {
  implicit val authUserResponseDecoder: Decoder[AuthUserResponse] = deriveDecoder[AuthUserResponse]
  implicit val authUserResponseEncoder: Encoder[AuthUserResponse] = deriveEncoder[AuthUserResponse]
}
