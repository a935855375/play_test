package modules

import javax.inject.Inject

import actors.ServerActor
import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.AbstractModule

trait ApplicationActors

class Actors @Inject()(system: ActorSystem) extends ApplicationActors {
  val reference: ActorRef = system.actorOf(ServerActor.props, name = "ServerActor")
}

class ActorsModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ApplicationActors]).to(classOf[Actors]).asEagerSingleton()
  }
}