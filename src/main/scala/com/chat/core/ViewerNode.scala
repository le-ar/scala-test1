package com.chat.core

import cats.Traverse
import cats.instances.list._
import cats.Functor
import cats.syntax.functor._
import sangria.relay._

trait GQNode[T] extends Node {
  val entity: T
}

trait GQNodeObject[T, N <: GQNode[T]] {
  implicit object IdentifiableInstance extends Identifiable[N] {
    def id(node: N): String = node.id
  }

  implicit def entityToNode(entity: T): N = createNode(entity)
  implicit def nodeToEntity(node: N): T = node.entity

  implicit def functorEntityToNode[F[_]: Functor](fe: F[T]): F[N] =
    Functor[F].map(fe)(createNode)

  implicit def nestedFunctorEntityToNode[F[_]: Functor, G[_]: Functor](fge: F[G[T]]): F[G[N]] =
    fge.map(_.map(createNode))

  def createNode(entity: T): N
}
