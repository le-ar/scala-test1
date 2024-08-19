package com.chat.core

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
import sangria.http.akka.circe.CirceHttpSupport
import sangria.schema.Schema
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import sangria.introspection.introspectionQuery
import sangria.marshalling.circe._
import io.circe.syntax._
import scala.concurrent.Future

trait AppServer[Ctx] extends CorsSupport with CirceHttpSupport {
  implicit val system: ActorSystem = ActorSystem("sangria-server")
  import system.dispatcher
  import AppServer._

  val INTERFACE = "0.0.0.0"
  lazy val PORT = 8080
  lazy val appName: String = ""
  val routePath = Option(appName).filter(_.nonEmpty) match {
    case Some(appName) => s"$appName/graphql"
    case _             => "graphql"
  }
  val schemaSDLPath = s"/$routePath/sdl"
  val schemaJsonPath = s"/$routePath/json"

  val schema: Schema[Ctx, Unit]
  def createCtx(isDev: Boolean = false)(implicit request: RequestContext): Future[Ctx]

  val pathSegments = routePath.split("/").toList
  val graphqlPath: PathMatcher[Unit] =
    pathSegments.map(segment => segmentMatcher(segment)).reduceLeft(_ / _)

  val isDev = true

  val route: Route =
    pathPrefix(graphqlPath) {
      (if (isDev) { // Проверка на значение isDev
         path("schema.graphql") {
           (get | post) {
             complete(
               HttpEntity(
                 ContentTypes.`text/plain(UTF-8)`,
                 schema.renderPretty
               )
             )
           }
         } ~
           path("schema.json") {
             extractRequestContext { implicit ctx =>
               onSuccess(createCtx(isDev)) { ctx =>
                 (get | post) {
                   val introspectionResult = Executor.execute(
                     schema,
                     introspectionQuery,
                     ctx
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
               }
             }
           } ~
           pathEndOrSingleSlash {
             explicitlyAccepts(`text/html`) {
               getFromResource("graphiql.html")
             }
           }
       } else {
         reject // Если isDev == false, маршруты будут отвергнуты
       }) ~
        pathEndOrSingleSlash {
          extractRequestContext { implicit ctx =>
            onSuccess(createCtx()) { ctx =>
              optionalHeaderValueByName("X-Apollo-Tracing") { tracing =>
                prepareGraphQLRequest {
                  case Success(req) =>
                    val middleware =
                      if (tracing.isDefined) SlowLog.apolloTracing :: Nil else Nil
                    val graphQLResponse = Executor
                      .execute(
                        schema,
                        req.query,
                        ctx,
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
        }
    }

  Http().newServerAt(INTERFACE, PORT).bindFlow(corsHandler(route))
}

object AppServer {
  def segmentMatcher(segment: String): PathMatcher0 = segment
}
