package com.chat

import com.google.inject.AbstractModule
import com.chat.apps.facade.FacadeModule
import com.chat.core.db.DBModule
import scala.concurrent.ExecutionContext
import com.chat.apps.client.ClientModule

class MainModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ExecutionContext]).toInstance(scala.concurrent.ExecutionContext.global)

    install(new DBModule())
    install(new FacadeModule())
    install(new ClientModule())
  }
}
