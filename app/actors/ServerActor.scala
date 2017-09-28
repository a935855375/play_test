package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import beans.{EnterMessage, ExitMessage, UserMessage}

import scala.collection.mutable

class ServerActor extends Actor with ActorLogging {
  private lazy val logger = play.api.Logger(this.getClass)

  val map: mutable.HashMap[String, (ActorRef, ActorRef)] =
    scala.collection.mutable.HashMap[String, (ActorRef, ActorRef)]()

  override def receive: PartialFunction[Any, Unit] = {
    case EnterMessage(in, out, name) => map += ((name, (in, out)))
    case ExitMessage(name) => map -= name
    case UserMessage(name, message) => if (!message.equals("ping-pong"))
      map.foreach(x => x._2._2 ! name + "说:" + message)
    case s => logger.error(s.toString)
  }
}

object ServerActor {
  def props: Props = Props[ServerActor]
}
