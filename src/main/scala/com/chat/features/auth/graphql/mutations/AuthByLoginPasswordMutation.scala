package com.chat.features.auth.graphql.mutations

import javax.inject.{Singleton, Inject}
import sangria.relay._
import sangria.schema._
import sangria.marshalling._
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto._
import sangria.marshalling.{FromInput, ToInput}
import sangria.marshalling.circe._
import io.circe.generic.extras.Configuration

import com.chat.core.ViewerSchema
import com.chat.core.db.AuthUserResponse
import com.chat.core.db.DB

@Singleton
class AuthByLoginPasswordMutation[Ctx] @Inject() (
    db: DB
) {

  private case class Input(
      clientMutationId: Option[String],
      login: String,
      password: String
  )
  private case class Payload(
      clientMutationId: Option[String],
      response: Option[AuthUserResponse]
  ) extends Mutation

  private implicit val inputDecoder: Decoder[Input] = deriveDecoder[Input]
  private implicit val inputEncoder: Encoder[Input] = deriveEncoder[Input]

  private implicit val fromInput: FromInput[Input] =
    circeDecoderFromInput[Input]
  private implicit val toInput: ToInput[Input, Json] =
    circeEncoderToInput[Input]

  val AuthUserResponseType = ObjectType(
    "AuthUserResponse",
    fields[Unit, AuthUserResponse](
      Field("id", IntType, resolve = _.value.id),
      Field("token", StringType, resolve = _.value.token)
    )
  )

  def apply(schema: ViewerSchema[Ctx]) =
    Mutation.fieldWithClientMutationId[Ctx, Unit, Payload, Input](
      fieldName = "authByLoginPassword",
      typeName = "AuthByLoginPassword",
      inputFields = List(
        InputField("login", StringType),
        InputField("password", StringType)
      ),
      outputFields = fields(
        Field(
          "result",
          OptionType(AuthUserResponseType),
          resolve = ctx => ctx.value.response
        )
      ),
      mutateAndGetPayload = (input, ctx) => {
        val authResponse = db.authByPassword(input.login, input.password)
        Payload(input.clientMutationId, authResponse)
      }
    )
}
