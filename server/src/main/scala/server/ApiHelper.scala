package server

import org.http4s.{EntityEncoder, Headers, Response, Status}
import org.http4s.dsl._

import scalaz.{EitherT, IList, NonEmptyList, \/}
import scalaz.concurrent.Task
import scalaz.syntax.either._
import scalaz.syntax.monad._
import ArgonautCodec._
import argonaut.Argonaut
import argonaut._
import org.http4s.EntityEncoder.Entity
import scodec.bits.ByteVector

final case class Failure(status: Status, messages: NonEmptyList[String])

object ApiHelper {
  val MESSAGES = "messages"

  type MonadResponseOr[F[_],A] = EitherT[F, Task[Response], A]
  type TaskResponseOr[A] = MonadResponseOr[Task,A]
  type MonadFailureOr[F[_],A] = EitherT[F, Failure, A]
  type TaskFailureOr[A] = MonadFailureOr[Task,A]

  def failure(messages: NonEmptyList[String], status: Status = InternalServerError) =
    Failure(status, messages)

  def notFound(messages: NonEmptyList[String]): Failure = failure(messages, Status.NotFound)
  def notFound(message: String): Failure = notFound(NonEmptyList(message))

  def wrap[A](oneOf: Task[Response] \/ A): TaskResponseOr[A] = EitherT.eitherT { oneOf.point[Task]}
  def wrap[A](t: Task[A]): TaskResponseOr[A] =  t.liftM[MonadResponseOr]
  def success[A](a: A): TaskResponseOr[A] = wrap(a.right)

  def wrapM[A](oneOf: Failure \/ A): TaskFailureOr[A] = EitherT.eitherT { oneOf.point[Task]}
  def wrapM[A](t: Task[A]): TaskFailureOr[A] =  t.liftM[MonadFailureOr]
  def successM[A](a: A): TaskFailureOr[A] = wrapM(a.right)

  def join[A](result: TaskFailureOr[A])(implicit enc: EncodeJson[A]): Task[Response] =
    result
      .run
      .flatMap(_.fold(
        f => Response(f.status).withBody(makeMessageReply(f.messages)),
        a => Ok(a)
      ))

  def joinRaw(result: TaskFailureOr[String]): Task[Response] =
    result
      .run
      .flatMap(_.fold(
        f => Response(f.status).withBody(makeMessageReply(f.messages)),
        a => {
          Ok(a)
        }
      ))

  def makeMessageReply(messages: NonEmptyList[String]) = Map(MESSAGES -> messages.list.toList)
}
