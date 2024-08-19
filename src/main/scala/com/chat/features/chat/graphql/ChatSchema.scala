package com.chat.features.chat.graphql

import javax.inject.{Inject, Singleton}
import sangria.schema.ObjectType
import com.chat.features.chat.models.Chat
import com.chat.core.AbstractSchema
import sangria.schema._
import sangria.relay._
import com.chat.core.CurrentUserCtx

@Singleton
class ChatSchema[Ctx <: CurrentUserCtx] @Inject() () extends AbstractSchema[Ctx] {

  implicit object ChatIdentifiable extends Identifiable[Chat] {
    def id(m: Chat) = m.id.toString
  }

  lazy val ChatSchemaType: ObjectType[Ctx, Chat] = ObjectType(
    "Chat",
    interfaces[Ctx, Chat](nodeInterface),
    fields[Ctx, Chat](
      Node.globalIdField,
      Field("uid", IntType, resolve = _.value.id)
    )
  )

}
