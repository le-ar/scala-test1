package com.chat.apps.client

import com.chat.core.CurrentUserCtx
import com.chat.core.CurrentUser
import com.chat.features.user.graphql.{UserCtx, UserOptCtx}

final case class ClientCtx(user: CurrentUser) extends UserCtx {
  val currentUserOpt = Some(user)
  val currentUser = user
}
