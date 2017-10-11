package actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import beans.{EnterMessage, ExitMessage, UserMessage}

import scala.collection.mutable

class ServerActor extends Actor with ActorLogging {
  private lazy val logger = play.api.Logger(this.getClass)

  val map: mutable.HashMap[String, (ActorRef, ActorRef)] =
    scala.collection.mutable.HashMap[String, (ActorRef, ActorRef)]()

  override def receive: PartialFunction[Any, Unit] = {
    case EnterMessage(in, out, name) =>
      if (map.contains(name)) {
        out ! "You have connected the server, don't attempt repeatedly!"
        out ! "If you want to receive message in this window, you have to close previous page"
        sender() ! PoisonPill
      }
      else {
        sender() ! true
        map += ((name, (in, out)))
        out ! "Welcome to the boring chatroom"
      }
    case ExitMessage(name) => map -= name
    case UserMessage(name, message) => if (!message.equals("ping-pong"))
      map.foreach(x => x._2._2 ! name + "说:" + message)
    case s => logger.error(s.toString)
  }
}

object ServerActor {
  def props: Props = Props[ServerActor]
}
