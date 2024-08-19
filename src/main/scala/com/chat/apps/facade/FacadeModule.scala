package com.chat.apps.facade

import com.google.inject.{AbstractModule, TypeLiteral}
import com.google.inject.multibindings.Multibinder

import com.chat.core.AbstractSchema
import com.chat.features.chat.graphql.ChatSchema
import com.chat.features.user.graphql.UserSchema
import com.chat.features.auth.graphql.AuthSchema
import com.google.inject.Provides
import scala.reflect.ClassTag
import com.chat.features.user.graphql.UserOptCtx

class FacadeModule extends AbstractModule {
  override def configure(): Unit = {
    val schemaTypeLiteral = new TypeLiteral[AbstractSchema[FacadeCtx]]() {}

    val subMulti =
      Multibinder.newSetBinder(binder(), schemaTypeLiteral)
    subMulti.addBinding().to(new TypeLiteral[AuthSchema[FacadeCtx]]() {})
    subMulti.addBinding().to(new TypeLiteral[UserSchema[FacadeCtx]]() {})

    bind(classOf[FacadeAppServer]).asEagerSingleton()
  }

  @Provides
  def provideUserCtxClassTag(): ClassTag[FacadeCtx] = {
    ClassTag(classOf[UserOptCtx])
  }
}
