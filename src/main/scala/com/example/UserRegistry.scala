package com.example

//#user-registry-actor
import akka.actor.TypedActor.dispatcher
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.{HttpResponse, Multipart, StatusCodes}
import akka.stream.Materializer
import akka.util.ByteString

import scala.collection.immutable
import java.io.FileOutputStream
import java.util.UUID
import scala.util.Failure

//#user-case-classes
final case class User(name: String, age: Int, countryOfResidence: String)
final case class Users(users: immutable.Seq[User])
//#user-case-classes

object UserRegistry {

  // actor protocol
  sealed trait Command
  final case class GetUsers(replyTo: ActorRef[Users]) extends Command
  final case class CreateUser(user: User, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetUser(name: String, replyTo: ActorRef[GetUserResponse]) extends Command
  final case class DeleteUser(name: String, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class UploadFile(content: Multipart.FormData, replyTo: ActorRef[ActionWithCorrelationPerformed]) extends Command


  final case class GetUserResponse(maybeUser: Option[User])
  final case class ActionPerformed(description: String)
  final case class ActionWithCorrelationPerformed(description: String, id: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(users: Set[User]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! Users(users.toSeq)
        Behaviors.same
      case CreateUser(user, replyTo) =>
        replyTo ! ActionPerformed(s"User ${user.name} created.")
        registry(users + user)
      case GetUser(name, replyTo) =>
        replyTo ! GetUserResponse(users.find(_.name == name))
        Behaviors.same
      case DeleteUser(name, replyTo) =>
        replyTo ! ActionPerformed(s"User $name deleted.")
        registry(users.filterNot(_.name == name))
      case UploadFile(content, replyTo) =>
        val uuid = UUID.randomUUID().toString
        replyTo ! ActionWithCorrelationPerformed(s"File uploading...", uuid)
        val temp = System.getProperty("java.io.tmpdir")
        val filePath = temp + "/" + uuid
        processFile(filePath, content).map {fileSize => println(s"filesize $fileSize")}.onComplete{
          case Failure(t) => println("An error has occurred: " + t.getMessage)
        }

        //generate hash id over doc, return to caller
        //pipelining two tasks:
          //1. save file to disk
          //2. send a message to redis uploader actor
        Behaviors.same
    }

  private def processFile(filePath: String, fileData: Multipart.FormData)(implicit materializer: Materializer) = {
    val fileOutput = new FileOutputStream(filePath)
    fileData.parts.mapAsync(1) { bodyPart ⇒
      def writeFileOnLocal(array: Array[Byte], byteString: ByteString): Array[Byte] = {
        val byteArray: Array[Byte] = byteString.toArray
        fileOutput.write(byteArray)
        array ++ byteArray
      }
      bodyPart.entity.dataBytes.runFold(Array[Byte]())(writeFileOnLocal)
    }.runFold(0)(_ + _.length)
  }
}
//#user-registry-actor
