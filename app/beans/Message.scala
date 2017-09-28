package beans

import akka.actor.ActorRef

trait Message

case class EnterMessage(in: ActorRef, out: ActorRef, name: String) extends Message

case class ExitMessage(name: String) extends Message

case class UserMessage(name: String, message: String) extends Message