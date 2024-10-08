import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

import scala.concurrent.Future
import sangria.relay._
import Data._
import sangria.util.tag.@@
import sangria.marshalling.FromInput

/** Defines a GraphQL schema for the current project
  */
object SchemaDefinition {
  /** We get the node interface and field from the relay library.
    *
    * The first method is the way we resolve an ID to its object. The second is
    * the way we resolve an object that implements node to its type.
    */
  val NodeDefinition(nodeInterface, nodeField, nodesField) =
    Node.definition(
      (globalId: GlobalId, ctx: Context[FactionRepo, Unit]) => {
        if (globalId.typeName == "Faction")
          ctx.ctx.getFaction(globalId.id)
        else if (globalId.typeName == "Ship")
          ctx.ctx.getShip(globalId.id)
        else
          None
      },
      Node.possibleNodeTypes[FactionRepo, Node](ShipType)
    )

  def idFields[Ctx, T: Identifiable]: List[Field[Ctx, T]] = fields[Ctx, T](
    Node.globalIdField,
    Field(
      "uid",
      StringType,
      resolve = ctx => implicitly[Identifiable[T]].id(ctx.value)
    )
  )

  /** We define our basic ship type.
    *
    * This implements the following type system shorthand: type Ship : Node {
    * id: String! name: String }
    */
  val ShipType: ObjectType[Unit, Ship] = ObjectType(
    "Ship",
    "A ship in the Star Wars saga",
    interfaces[Unit, Ship](nodeInterface),
    idFields[Unit, Ship] ++
      fields[Unit, Ship](
        Field(
          "name",
          OptionType(StringType),
          Some("The name of the ship."),
          resolve = _.value.name
        )
      )
  )

  /** We define a connection between a faction and its ships.
    *
    * connectionType implements the following type system shorthand: type
    * ShipConnection { edges: [ShipEdge] pageInfo: PageInfo! }
    *
    * connectionType has an edges field - a list of edgeTypes that implement the
    * following type system shorthand: type ShipEdge { cursor: String! node:
    * Ship }
    */
  val ConnectionDefinition(_, shipConnection) =
    Connection.definition[FactionRepo, Connection, Option[Ship]](
      "Ship",
      OptionType(ShipType)
    )

  /** We define our faction type, which implements the node interface.
    *
    * This implements the following type system shorthand: type Faction : Node {
    * id: String! name: String ships: ShipConnection }
    */
  val FactionType: ObjectType[FactionRepo, Faction] = ObjectType(
    "Faction",
    "A faction in the Star Wars saga",
    interfaces[FactionRepo, Faction](nodeInterface),
    idFields[FactionRepo, Faction] ++
      fields[FactionRepo, Faction](
        Field(
          "name",
          OptionType(StringType),
          Some("The name of the faction."),
          resolve = _.value.name
        ),
        Field(
          "ships",
          OptionType(shipConnection),
          arguments = Connection.Args.All,
          resolve = ctx =>
            Connection.connectionFromSeq(
              ctx.value.ships map ctx.ctx.getShip,
              ConnectionArgs(ctx)
            )
        )
      )
  )

  /** This is the type that will be the root of our query, and the entry point
    * into our schema.
    *
    * This implements the following type system shorthand: type Query {
    * factions(names: [String!]!): [Faction]! rebels: Faction empire: Faction
    * node(id: String!): Node }
    */

  val namesArgument: Argument[Seq[String @@ FromInput.CoercedScalaResult]] =
    Argument("names", ListInputType(StringType))

  val QueryType: ObjectType[FactionRepo, Unit] = ObjectType(
    "Query",
    fields[FactionRepo, Unit](
      Field(
        "factions",
        ListType(OptionType(FactionType)),
        arguments = namesArgument :: Nil,
        resolve = ctx => ctx.ctx.getFactions(ctx.arg(namesArgument))
      ),
      Field("rebels", OptionType(FactionType), resolve = _.ctx.getRebels),
      Field("empire", OptionType(FactionType), resolve = _.ctx.getEmpire),
      nodeField,
      nodesField
    )
  )

  case class ShipMutationPayload(
      clientMutationId: Option[String],
      shipId: String,
      factionId: String
  ) extends Mutation

  /** This will return a `Field` for our ship mutation.
    *
    * It creates these two types implicitly: input IntroduceShipInput {
    * clientMutationId: string! shipName: string! factionId: ID! }
    *
    * input IntroduceShipPayload { clientMutationId: string! ship: Ship faction:
    * Faction }
    */
  val shipMutation: Field[FactionRepo, Unit] =
    Mutation.fieldWithClientMutationId[
      FactionRepo,
      Unit,
      ShipMutationPayload,
      InputObjectType.DefaultInput
    ](
      fieldName = "introduceShip",
      typeName = "IntroduceShip",
      inputFields = List(
        InputField("shipName", StringType),
        InputField("factionId", IDType)
      ),
      outputFields = fields(
        Field(
          "ship",
          OptionType(ShipType),
          resolve = ctx => ctx.ctx.getShip(ctx.value.shipId)
        ),
        Field(
          "faction",
          OptionType(FactionType),
          resolve = ctx => ctx.ctx.getFaction(ctx.value.factionId)
        )
      ),
      mutateAndGetPayload = (input, ctx) => {
        val mutationId = input
          .get(Mutation.ClientMutationIdFieldName)
          .asInstanceOf[Option[Option[String]]]
          .flatten
        val shipName = input("shipName").asInstanceOf[String]
        val factionId = input("factionId").asInstanceOf[String]

        val newShip = ctx.ctx.createShip(shipName, factionId)

        ShipMutationPayload(mutationId, newShip.id, factionId)
      }
    )

  /** This is the type that will be the root of our mutations, and the entry
    * point into performing writes in our schema.
    *
    * This implements the following type system shorthand: type Mutation {
    * introduceShip(input IntroduceShipInput!): IntroduceShipPayload }
    */
  val MutationType: ObjectType[FactionRepo, Unit] =
    ObjectType("Mutation", fields[FactionRepo, Unit](shipMutation))

  val schema: Schema[FactionRepo, Unit] = Schema(QueryType, Some(MutationType))
}
