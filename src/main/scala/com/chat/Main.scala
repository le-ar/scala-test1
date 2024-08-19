package com.chat

import com.google.inject.{Guice, Injector}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

import com.typesafe.scalalogging.LazyLogging

object Main extends App with LazyLogging {
  Guice.createInjector(new MainModule())
}
