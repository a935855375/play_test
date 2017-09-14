package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db.DBApi

import scala.concurrent.Future

case class RegisterUser(mail: String,
                        name: String,
                        password: String)

@javax.inject.Singleton
class UserRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private val simple = {
    get[String]("mail") ~
      get[String]("name") ~
      get[String]("password") map {
      case mail ~ name ~ password => RegisterUser(mail, name, password)
    }
  }

  def check(mail: String) = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
           select * from user where mail = {mail}
        """
      ).on('mail -> mail).as(simple singleOpt)
    }
  }

  def insert(user: RegisterUser) = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into user(mail,name,password) values({mail},{name},{password})
        """
      ).on(
        'mail -> user.mail,
        'name -> user.name,
        'password -> user.password
      ).executeUpdate()
    }
  }

}
