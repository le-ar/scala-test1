package com.chat.apps.client

import com.google.inject.{AbstractModule, TypeLiteral}
import com.google.inject.multibindings.Multibinder

import com.chat.core.AbstractSchema
import com.chat.features.chat.graphql.ChatSchema
import com.chat.features.user.graphql.UserSchema
import com.chat.features.auth.graphql.AuthSchema
import com.google.inject.Provides
import scala.reflect.ClassTag
import com.chat.features.user.graphql.UserOptCtx
import com.chat.features.user.graphql.UserCtx

class ClientModule extends AbstractModule {
  override def configure(): Unit = {
    val schemaTypeLiteral = new TypeLiteral[AbstractSchema[ClientCtx]]() {}

    val subMulti =
      Multibinder.newSetBinder(binder(), schemaTypeLiteral)
    subMulti.addBinding().to(new TypeLiteral[UserSchema[ClientCtx]]() {})

    bind(classOf[ClientAppServer]).asEagerSingleton()
  }

  @Provides
  def provideUserCtxClassTag(): ClassTag[ClientCtx] = {
    ClassTag(classOf[UserCtx])
  }
}
