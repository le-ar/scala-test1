package com.chat.apps.facade

import com.chat.core.CurrentUserCtx
import com.chat.core.CurrentUser
import com.chat.features.user.graphql.UserOptCtx

final case class FacadeCtx(user: Option[CurrentUser] = None) extends UserOptCtx {
  val currentUserOpt = user
}
