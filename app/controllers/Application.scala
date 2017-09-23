package controllers

import java.io.ByteArrayOutputStream
import javax.inject.Inject

import actors.QuizActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.util.ByteString
import models.UserRepository
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.libs.ws._
import service.MailerService
import utils.{HashUtil, VerifyCodeUtils}
import views.html

import scala.concurrent.Future

class Application @Inject()(cc: MessagesControllerComponents,
                            users: UserRepository,
                            mailer: MailerService,
                            ws: WSClient)
                           (implicit system: ActorSystem, mat: Materializer) extends MessagesAbstractController(cc) {

  //private lazy val logger = play.api.Logger(this.getClass)

  def verifyCode() = Action { implicit request: Request[AnyContent] =>
    val verifyCode = VerifyCodeUtils.generateVerifyCode(4)
    val out: ByteArrayOutputStream = new ByteArrayOutputStream
    VerifyCodeUtils.outputImage(200, 80, out, verifyCode)
    Ok(ByteString(out.toByteArray))
      .as("image/jpeg")
      .addingToSession("verifyCode" -> HashUtil.sha256(verifyCode.toLowerCase()))
  }

  def test(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(html.codetest()))
  }

  def doTest(): Action[AnyContent] = Action.async { implicit request =>
    Form(single("code" -> nonEmptyText)).bindFromRequest().fold(
      _ => Future.successful(Ok("验证码错误")),
      single => if (HashUtil.sha256(single.toLowerCase()) == request.session.get("verifyCode").getOrElse(""))
        Future.successful(Ok("验证码正确"))
      else
        Future.successful(Ok("验证码错误2"))
    )
  }

  def login: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.login()))
  }

  def doLogin(): Action[AnyContent] = Action.async { implicit request =>
    Form(tuple("mail" -> nonEmptyText, "password" -> nonEmptyText, "verifyCode" -> nonEmptyText)).bindFromRequest().fold(
      _ => Future.successful(Ok("请输入账号和密码")),
      tuple => {
        val (mail, password, verifyCode) = tuple
        if (HashUtil.sha256(verifyCode.toLowerCase()) == request.session.get("verifyCode").getOrElse("")) {
          if (password.equals(mail))
            Future.successful(Ok("登录成功"))
          else
            Future.successful(Ok("用户名或密码错误"))
        } else {
          Future.successful(Ok("验证码输入错误"))
        }
      }
    )
  }

  def register: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.register()))
  }

  def doRegister(): Action[AnyContent] = Action.async { implicit request =>
    //logger.info(request.body.asFormUrlEncoded.get.toString())
    Form(tuple("mail" -> nonEmptyText, "name" -> nonEmptyText, "password" ->
      nonEmptyText, "repassword" -> nonEmptyText, "verifyCode" -> nonEmptyText)).bindFromRequest().fold(
      _ => Future.successful(Ok("注册出错了,您的填写有误！")),
      tuple => {
        val (mail, name, password, repassword, verifyCode) = tuple
        if (HashUtil.sha256(verifyCode.toLowerCase()) == request.session.get("verifyCode").getOrElse("")) {
          users.check(mail).flatMap {
            case Some(_) => Future.successful(Ok("注册出错了，您已经注册过了！"))
            case None =>
              if (password == repassword) {

              }
              Future.successful(Ok("注册成功！"))
          }
        } else {
          Future.successful(Ok("操作出错了！,验证码输入错误！"))
        }
      }
    )
  }

  def mailTest: Action[AnyContent] = Action.async { implicit request =>
    Future {
      mailer.sendEmail("笨蛋", "1060238139@qq.com", views.html.mail.activeMail("笨蛋", "1234").body)
      Ok("hello")
    }
  }

  def notFound(all: String): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.notFound()))
  }

  def socket: WebSocket = WebSocket.accept[String, String] { implicit request =>
    ActorFlow.actorRef { out =>
      QuizActor.props(out)
    }
  }

  def echo: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.echo()))
  }

}
