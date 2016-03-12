package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

// import spray.httpx.marshalling._
import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport

case class Reponse(entiers: List[Int], graine: Int, mot: String)
object JsonReponse extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val json = jsonFormat3(Reponse)
}

import JsonReponse._



// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}

// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    } ~
  path("hello") {
    get {
      respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Love this!</h1>
              </body>
            </html>
          }
        }
    }
  } ~
  pathPrefix("api") {
    jsonpWithParameter("callback") {
      path("numbers") {
        get {
          parameter("seed".as[Int]) { seed =>
            detach () {
              validate(seed >= 0, "query parameter 'seed' must be >= 0") {
                complete {
                  Reponse((1 to seed).toList, seed, "hello")
                }
              }
            }
          }
        }
      }
    }
  }
}
