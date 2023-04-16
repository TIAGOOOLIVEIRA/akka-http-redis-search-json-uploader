import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._

case class MyMessage(data: String)

object Main extends IOApp {

  val actorSystemName = "MyCluster"
  implicit val actorSystem = ActorSystem(actorSystemName)

  val sharding = ClusterSharding(actorSystem)
  val shardRegion: ActorRef = sharding.start(
    typeName = "MyActor",
    entityProps = MyActor.props(),
    settings = ClusterShardingSettings(actorSystem),
    extractEntityId = MyActor.extractEntityId,
    extractShardId = MyActor.extractShardId
  )

  val httpApp = HttpRoutes.of[IO] {
    case req @ POST -> Root / "send-message" =>
      for {
        message <- req.as[MyMessage]
        _ <- IO {
          shardRegion ! MyActor.SendMessage(message.data)
        }
        response <- Ok("Message sent")
      } yield response
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] = {
    val server = for {
      _ <- BlazeServerBuilder[IO]
        .bindHttp(8080, "localhost")
        .withHttpApp(httpApp)
        .serve
    } yield ()

    server.as(ExitCode.Success)
  }
}

object MyApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = Main.run(args)
}
