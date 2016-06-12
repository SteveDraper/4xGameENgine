package server

import org.http4s._
import org.http4s.dsl._

import scalaz.{EitherT, IList, NonEmptyList, \/}
import scalaz.concurrent.Task
import scalaz.syntax.either._
import scalaz.syntax.monad._
import scalaz.std.option._
import scalaz.syntax.std.option._
import ArgonautCodec._
import _root_.argonaut.Argonaut
import _root_.argonaut._
import org.http4s.EntityEncoder.Entity
import scodec.bits.ByteVector

import scala.util.Try

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
  def badRequest(messages: NonEmptyList[String]): Failure = failure(messages, Status.BadRequest)
  def badRequest(message: String): Failure = badRequest(NonEmptyList(message))
  def internalServerError(messages: NonEmptyList[String]): Failure = failure(messages, Status.InternalServerError)
  def internalServerError(message: String): Failure = internalServerError(NonEmptyList(message))

  def wrap[A](oneOf: Task[Response] \/ A): TaskResponseOr[A] = EitherT.eitherT { oneOf.point[Task]}
  def wrap[A](t: Task[A]): TaskResponseOr[A] =  t.liftM[MonadResponseOr]
  def success[A](a: A): TaskResponseOr[A] = wrap(a.right)

  def wrapM[A](oneOf: Failure \/ A): TaskFailureOr[A] = EitherT.eitherT { oneOf.point[Task]}
  def wrapM[A](t: Task[A]): TaskFailureOr[A] = t.liftM[MonadFailureOr]
  def wrapTM[A](oneOf: Failure \/ Task[A]): TaskFailureOr[A] =
    oneOf.fold(f => wrapM(f.left), ta => wrapM(ta))
  def successM[A](a: A): TaskFailureOr[A] = wrapM(a.right)

  def join[A](result: TaskFailureOr[A])(implicit enc: EncodeJson[A]): Task[Response] =
    result
      .run
      .flatMap(_.fold(
        f => Response(f.status).withBody(makeMessageReply(f.messages)),
        a => Ok(a)
      ))

  def joinResponse(result: TaskFailureOr[Response]): Task[Response] =
    result
      .run
      .flatMap(_.fold(failureToResponseTask,Task.now))

  def joinRaw(result: TaskFailureOr[String]): Task[Response] =
    result
      .run
      .flatMap(_.fold(
        failureToResponseTask,
        Ok(_)
      ))

  def failureToResponseTask(f: Failure) =
    Response(f.status).withBody(makeMessageReply(f.messages))

  def makeMessageReply(messages: NonEmptyList[String]) = Map(MESSAGES -> messages.list.toList)
}

import ApiHelper._

trait QueryParamHelper {
  def optionalString(req: Request, name: String): TaskFailureOr[Option[String]] =
    successM(req.params.get(name))

  def optionalInt(req: Request, name: String): TaskFailureOr[Option[Int]] =
    wrapM(req.params.get(name).fold(none[Int].right[Failure])(v =>
      Try(v.toInt).toOption.fold(
        badRequest(s"'$v' is not a valid integer value for parameter '$name'").left[Option[Int]])(n =>
        some(n).right[Failure])))

  def optionalDouble(req: Request, name: String): TaskFailureOr[Option[Double]] =
    wrapM(req.params.get(name).fold(none[Double].right[Failure])(v =>
      Try(v.toDouble).toOption.fold(
        badRequest(s"'$v' is not a valid double value for parameter '$name'").left[Option[Double]])(n =>
        some(n).right[Failure])))

  def optionalBoolean(req: Request, name: String): TaskFailureOr[Option[Boolean]] =
    wrapM(req.params.get(name).fold(none[Boolean].right[Failure])(v =>
      Try(v.toBoolean).toOption.fold(
        badRequest(s"'$v' is not a valid boolean value for parameter '$name'").left[Option[Boolean]])(n =>
        some(n).right[Failure])))

  def requiredString(req: Request, name: String): TaskFailureOr[String] =
    for {
      maybeS <- optionalString(req, name)
      s <- wrapM(maybeS \/> badRequest(s"Missing required header '$name'"))
    } yield s

  def requiredInt(req: Request, name: String): TaskFailureOr[Int] =
    for {
      maybeI <- optionalInt(req, name)
      i <- wrapM(maybeI \/> badRequest(s"Missing required header '$name'"))
    } yield i

  def requiredDouble(req: Request, name: String): TaskFailureOr[Double] =
    for {
      maybeD <- optionalDouble(req, name)
      d <- wrapM(maybeD \/> badRequest(s"Missing required header '$name'"))
    } yield d

  def requiredBoolean(req: Request, name: String): TaskFailureOr[Boolean] =
    for {
      maybeB <- optionalBoolean(req, name)
      b <- wrapM(maybeB \/> badRequest(s"Missing required header '$name'"))
    } yield b
}