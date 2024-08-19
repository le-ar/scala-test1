package com.chat.core

import com.chat.core.CommonCtx

final case class UserCtx(userId: Int) extends CurrentUserCtx {
  val currentUser: CurrentUser = CurrentUser(userId)
}
