import scala.util.{Failure, Success}
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.slowlog.SlowLog
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import sangria.marshalling.circe._
import sangria.http.akka.Util.explicitlyAccepts
import akka.http.scaladsl.model.MediaTypes.`text/html`
import com.chat.core.CorsSupport
import sangria.introspection.introspectionQuery

// This is the trait that makes `graphQLPlayground and prepareGraphQLRequest` available
import sangria.http.akka.circe.CirceHttpSupport
import Data.FactionRepo
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import sangria.renderer.SchemaRenderer
import sangria.marshalling.circe._
import io.circe.syntax._

object Server extends App with CorsSupport with CirceHttpSupport {
  implicit val system: ActorSystem = ActorSystem("sangria-server")
  import system.dispatcher

  val repo = new FactionRepo

  val isDev = true

  val route: Route =
    optionalHeaderValueByName("X-Apollo-Tracing") { tracing =>
      pathPrefix("graphql") {
        (if (isDev) { // Проверка на значение isDev
           path("schema.graphql") {
             get {
               complete(
                 HttpEntity(
                   ContentTypes.`text/plain(UTF-8)`,
                   SchemaDefinition.schema.renderPretty
                 )
               )
             }
           } ~
             path("schema.json") {
               get {
                 val introspectionResult = Executor.execute(
                   SchemaDefinition.schema,
                   introspectionQuery,
                   userContext = repo
                 )

                 onComplete(introspectionResult) {
                   case Success(result) =>
                     complete(
                       HttpEntity(
                         ContentTypes.`application/json`,
                         result.asJson.spaces2
                       )
                     )
                   case Failure(ex) =>
                     complete(
                       InternalServerError,
                       s"An error occurred: ${ex.getMessage}"
                     )
                 }
               }
             } ~
             explicitlyAccepts(`text/html`) {
               getFromResource("graphiql.html")
             }
         } else {
           reject // Если isDev == false, маршруты будут отвергнуты
         }) ~
          pathEndOrSingleSlash {
            prepareGraphQLRequest {
              case Success(req) =>
                val middleware =
                  if (tracing.isDefined) SlowLog.apolloTracing :: Nil else Nil
                val graphQLResponse = Executor
                  .execute(
                    SchemaDefinition.schema,
                    req.query,
                    repo,
                    operationName = req.operationName,
                    variables = req.variables,
                    maxQueryDepth = Some(10),
                    middleware = middleware
                  )
                  .map(OK -> _)
                  .recover {
                    case error: QueryAnalysisError =>
                      BadRequest -> error.resolveError
                    case error: ErrorWithResolver =>
                      InternalServerError -> error.resolveError
                  }
                complete(graphQLResponse)
              case Failure(preparationError) =>
                complete(BadRequest, formatError(preparationError))
            }
          }
      }
    }

  val PORT = sys.props.get("http.port").fold(8080)(_.toInt)
  val INTERFACE = "0.0.0.0"
  Http().newServerAt(INTERFACE, PORT).bindFlow(corsHandler(route))
  Http().newServerAt(INTERFACE, 8081).bindFlow(corsHandler(route))
}
