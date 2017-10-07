package models

import javax.inject._

import anorm.SqlParser._
import anorm._
import play.api.db.DBApi

import scala.concurrent.Future

case class RegisterUser(username: String,
                        nickname: String,
                        password: String)

@Singleton
class UserRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private val simple = {
    get[String]("username") ~
      get[String]("nickname") ~
      get[String]("password") map {
      case username ~ nickname ~ password => RegisterUser(username, nickname, password)
    }
  }

  def check(username: String): Future[Option[RegisterUser]] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
           select * from user where username = {mail}
        """
      ).on('mail -> username).as(simple singleOpt)
    }
  }(ec)

  def insert(user: RegisterUser): Future[Int] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into user(username,nickname,password) values({username},{nickname},{password})
        """
      ).on(
        'username -> user.username,
        'nickname -> user.nickname,
        'password -> user.password
      ).executeUpdate()
    }
  }(ec)

}
