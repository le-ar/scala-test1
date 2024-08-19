package com.chat.features.auth.graphql

import javax.inject.{Singleton, Inject}
import sangria.schema._

import com.chat.core.{AbstractSchema, UserCtx}
import com.chat.core.CommonCtx
import com.chat.core.ViewerSchema
import com.chat.features.auth.graphql.mutations.AuthByLoginPasswordMutation

@Singleton
class AuthSchema[Ctx <: CommonCtx] @Inject() (
    authByLoginPasswordMutation: AuthByLoginPasswordMutation[Ctx]
) extends AbstractSchema[Ctx] {
  override def mutations(
      schema: ViewerSchema[Ctx]
  ): List[Field[Ctx, Unit]] = fields[Ctx, Unit](
    authByLoginPasswordMutation.apply(schema)
  )
}
