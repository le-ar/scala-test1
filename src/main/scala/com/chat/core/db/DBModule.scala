package com.chat.core.db

import com.google.inject.AbstractModule

class DBModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[DB])
  }
}
