package actors

import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import beans.{EnterMessage, ExitMessage, UserMessage}


class QuizActor(out: ActorRef, val name: String) extends Actor {
  //private lazy val logger = play.api.Logger(this.getClass)

  val actor: ActorSelection = context.actorSelection(context.system.child("ServerActor"))

  actor ! EnterMessage(context.self, out, name)

  override def receive: PartialFunction[Any, Unit] = {
    case message: String => actor ! UserMessage(name, message)
  }

  override def postStop(): Unit = {
    actor ! ExitMessage(name)
  }
}

object QuizActor {
  def props(out: ActorRef, name: String): Props = Props(new QuizActor(out, name))
}
