package actors

import akka.actor.{Actor, ActorLogging, Props}

class BackActor extends Actor with ActorLogging {
  override def receive: PartialFunction[Any, Unit] = {
    case _ => "hi"
  }
}

object BackActor {
  def props: Props = Props[BackActor]
}
