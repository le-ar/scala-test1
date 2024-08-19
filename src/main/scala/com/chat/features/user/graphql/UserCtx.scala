package com.chat.features.user.graphql

import com.chat.core.CommonCtx
import com.chat.core.CurrentUser

trait UserOptCtx extends CommonCtx {
  val currentUserOpt: Option[CurrentUser]
}

trait UserCtx extends UserOptCtx {
  val currentUser: CurrentUser
}
