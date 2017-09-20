package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger

class QuizActor(out: ActorRef) extends Actor {
  private lazy val logger = Logger(this.getClass)

  override def receive: PartialFunction[Any, Unit] = {
    case s: String =>
      out ! "hello:" + s
  }
}

object QuizActor {

  def props(out: ActorRef) = Props(new QuizActor(out))
}
