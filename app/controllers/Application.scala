package controllers

import java.io.ByteArrayOutputStream
import javax.inject._

import actors.QuizActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.util.ByteString
import models.{RegisterUser, UserRepository}
import play.api.cache.Cached
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json, OWrites}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.libs.ws._
import service.MailerService
import utils.{HashUtil, VerifyCodeUtils}

import scala.concurrent.{ExecutionContext, Future}

case class Valid(valid: Boolean)

@Singleton
class Application @Inject()(cc: MessagesControllerComponents,
                            users: UserRepository,
                            mailer: MailerService,
                            ws: WSClient,
                            cached: Cached)
                           (implicit ec: ExecutionContext, system: ActorSystem, mat: Materializer) extends MessagesAbstractController(cc) {

  //private lazy val logger = play.api.Logger(this.getClass)

  implicit val residentWrites: OWrites[Valid] = Json.writes[Valid]

  val userForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "nickname" -> nonEmptyText,
      "password" -> nonEmptyText,
    )(RegisterUser.apply)(RegisterUser.unapply)
  )

  def verifyCode() = Action { implicit request: Request[AnyContent] =>
    val verifyCode = VerifyCodeUtils.generateVerifyCode(4)
    val out: ByteArrayOutputStream = new ByteArrayOutputStream
    VerifyCodeUtils.outputImage(200, 80, out, verifyCode)
    Ok(ByteString(out.toByteArray))
      .as("image/jpeg")
      .addingToSession("verifyCode" -> HashUtil.sha256(verifyCode.toLowerCase()))
  }

  def login: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.chatroom.login()))
  }

  def doLogin(): Action[AnyContent] = Action.async { implicit request =>
    Form(tuple("username" -> nonEmptyText, "password" -> nonEmptyText)).bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      tuple => users.check(tuple._1).flatMap {
        case None => Future.successful(Ok("用户名或密码错误"))
        case Some(user) =>
          if (user.password.equals(tuple._2))
            Future.successful(Ok("登录成功"))
          else
            Future.successful(Ok("用户名或密码错误"))
      }
    )
  }

  def validUsername(): Action[AnyContent] = Action.async { implicit request =>
    Form(single("username" -> nonEmptyText)).bindFromRequest().fold(
      _ => Future.successful(Ok(Json.toJson(Valid(false))).as(JSON)),
      username => users.check(username).flatMap {
        case Some(_) => Future.successful(Ok(Json.toJson(Valid(false))).as(JSON))
        case None => Future.successful(Ok(Json.toJson(Valid(true))).as(JSON))
      }
    )
  }

  def validLogin(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val username = (request.body \ "username").get.as[String]
    val password = (request.body \ "password").get.as[String]
    users.check(username).flatMap {
      case Some(user) => if (user.password.equals(password))
        Future.successful(Ok(Json.toJson(Valid(true))).as(JSON).withSession("username" -> user.username))
      else
        Future.successful(Ok(Json.toJson(Valid(false))).as(JSON))
      case None => Future.successful(Ok(Json.toJson(Valid(false))).as(JSON))
    }
  }

  def register: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.chatroom.register()))
  }

  def doRegister(): Action[AnyContent] = Action.async { implicit request =>
    userForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      user => users.insert(user).flatMap {
        case x: Int if x != 0 => Future.successful(Redirect(routes.Application.echo()).withSession("username" -> user.username))
        case _ => Future.successful(Ok("恭喜你注册失败了"))
      }
    )
  }

  def mailTest: Action[AnyContent] = Action.async { implicit request =>
    Future {
      mailer.sendEmail("笨蛋", "1060238139@qq.com", views.html.mail.activeMail("笨蛋", "1234").body)
      Ok("hello")
    }
  }

  def notFound(all: String): EssentialAction = cached("notFound") {
    Action.async { implicit request =>
      Future.successful(Ok("Oops..there are nothing in this page.."))
    }
  }

  def socket: WebSocket = WebSocket.acceptOrResult[String, String] { implicit request =>
    request.session.get("username") match {
      case Some(username) => users.check(username).map {
        case Some(user) => Right(ActorFlow.actorRef(out => QuizActor.props(out, user.nickname)))
        case None => Left(BadRequest)
      }
      case None => Future.successful(Left(BadRequest))
    }
  }

  def echo: Action[AnyContent] = Action.async { implicit request =>
    request.session.get("username") match {
      case Some(username) =>
        users.check(username).flatMap {
          case Some(user) => Future.successful(Ok(views.html.chatroom.echo(user.nickname)))
          case None => Future.successful(Redirect(routes.Application.login()))
        }
      case None => Future.successful(Redirect(routes.Application.login()))
    }
  }

  def homework: EssentialAction = cached("homework") {
    Action.async { implicit request =>
      Future.successful(Ok(views.html.homework()))
    }
  }

  def exit: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Redirect(routes.Application.login()).removingFromSession("username"))
  }

}
