package com.chat.core

case class CurrentUser(id: Int)

trait CurrentUserCtx extends CommonCtx {
  val currentUser: CurrentUser
}
