package com.chat.core

import com.google.inject.AbstractModule
import com.chat.core.AbstractSchema
import com.chat.features.auth.graphql.AuthSchema
import com.chat.features.user.graphql.UserSchema

class UserModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[AbstractSchema[UserCtx]]).to(classOf[AuthSchema[UserCtx]]).asEagerSingleton()
    // bind(classOf[AbstractSchema[UserCtx]]).to(classOf[UserSchema[UserCtx]]).asEagerSingleton()
  }
}
