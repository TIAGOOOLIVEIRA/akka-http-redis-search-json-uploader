package com.example

import akka.Done
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{Multipart, StatusCodes}
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import com.example.UserRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.{ByteString, Timeout}

import java.io.File
import scala.util.{Failure, Success}

//#import-json-formats
//#user-routes-class
class UserRoutes(userRegistry: ActorRef[UserRegistry.Command])(implicit val system: ActorSystem[_]) {

  //#user-routes-class
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getUsers(): Future[Users] =
    userRegistry.ask(GetUsers)
  def getUser(name: String): Future[GetUserResponse] =
    userRegistry.ask(GetUser(name, _))
  def createUser(user: User): Future[ActionPerformed] =
    userRegistry.ask(CreateUser(user, _))
  def deleteUser(name: String): Future[ActionPerformed] =
    userRegistry.ask(DeleteUser(name, _))
  def uploadFile(content: Multipart.FormData): Future[ActionWithCorrelationPerformed] =
    userRegistry.ask(UploadFile(content, _))

  //#all-routes
  //#users-get-post
  //#users-get-delete
  val userRoutes: Route =
    pathPrefix("users") {
      concat(
        //#users-get-delete
        pathEnd {
          concat(
            get {
              complete(getUsers())
            },
            post {
              entity(as[User]) { user =>
                onSuccess(createUser(user)) { performed =>
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        //#users-get-delete
        //#users-get-post
        path(Segment) { name =>
          concat(
            get {
              //#retrieve-user-info
              rejectEmptyResponse {
                onSuccess(getUser(name)) { response =>
                  complete(response.maybeUser)
                }
              }
              //#retrieve-user-info
            },
            delete {
              //#users-delete-logic
              onSuccess(deleteUser(name)) { performed =>
                complete((StatusCodes.OK, performed))
              }
              //#users-delete-logic
            })
        }~
          (path("upload") & extractLog) { log =>
            // handle uploading files
            // multipart/form-data
            //https://github.com/rockthejvm/akka-http/blob/master/src/main/scala/part3_highlevelserver/UploadingFiles.scala
            entity(as[Multipart.FormData]) { formdata =>
              // handle file payload
              val partsSource: Source[Multipart.FormData.BodyPart, Any] = formdata.parts

              val filePartsSink: Sink[Multipart.FormData.BodyPart, Future[Done]] = Sink.foreach[Multipart.FormData.BodyPart] { bodyPart =>
                if (bodyPart.name == "myFile") {
                  // create a file
                  val filename = "src/main/resources/download/" + bodyPart.filename.getOrElse("tempFile_" + System.currentTimeMillis())
                  val file = new File(filename)

                  log.info(s"Writing to file: $filename")

                  val fileContentsSource: Source[ByteString, _] = bodyPart.entity.dataBytes
                  val fileContentsSink: Sink[ByteString, _] = FileIO.toPath(file.toPath)

                  // writing the data to the file
                  fileContentsSource.runWith(fileContentsSink)
                }
              }

              val writeOperationFuture = partsSource.runWith(filePartsSink)
              onComplete(writeOperationFuture) {
                case Success(_) => complete("File uploaded.")
                  //send message to process json in a actor JsonPipelineActor
                case Failure(ex) => complete(s"File failed to upload: $ex")
              }
            }
          }
      )
      //#users-get-delete
    }
  //#all-routes
}
