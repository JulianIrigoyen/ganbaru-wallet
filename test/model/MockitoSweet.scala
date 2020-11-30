package model


import org.mockito.{ArgumentCaptor, ArgumentMatchers, Mockito}
import org.mockito.stubbing.{Answer, OngoingStubbing}
import org.scalatest.mockito.MockitoSugar

import scala.reflect.ClassTag
import scala.collection.JavaConverters._

trait MockitoSweet extends MockitoSugar with ArgumentMatcher {

  implicit class StubBuilder[T](any: T) {

    private val stubbing: OngoingStubbing[T] = Mockito.when(any)

    def thenAnswer(answer: Answer[_]): OngoingStubbing[T] = stubbing.thenAnswer(answer)

    def thenCallRealMethod(): OngoingStubbing[T] = stubbing.thenCallRealMethod()

    def thenThrow(throwables: Throwable*): OngoingStubbing[T] = stubbing.thenThrow(throwables: _*)

    def thenReturn(value: T, values: T*): OngoingStubbing[T] = stubbing.thenReturn(value, values: _*)

    def thenReturn(value: T): OngoingStubbing[T] = stubbing.thenReturn(value)

  }

  implicit class GetMock(ongoingStubbing: OngoingStubbing[_]) {

    def mock[M]: M = ongoingStubbing.getMock[M]
  }

  implicit class VerifyMock[T](mock: T) {

    def verify: T = Mockito.verify(mock)

    def verify(times: Int): T = Mockito.verify(mock, Mockito.times(times))

    def verifyAtLeast(times: Int): T = Mockito.verify(mock, Mockito.atLeast(times))

    def verifyOnce: T = Mockito.verify(mock, Mockito.times(1))

    def verifyAtLeastOnce: T = Mockito.verify(mock, Mockito.atLeastOnce())

    def resetMock(): Unit = Mockito.reset(mock)
  }

}

trait ArgumentMatcher {

  def any[T]: T = ArgumentMatchers.any[T]()

  def eqArg[T](value: T): T = ArgumentMatchers.eq(value)

  def argumentCaptor[T](implicit classTag: ClassTag[T]): ArgumentCaptorWrapper[T] =
    new ArgumentCaptorWrapper[T](ArgumentCaptor.forClass(classTag.runtimeClass.asInstanceOf[Class[T]]))

  class ArgumentCaptorWrapper[T](argumentCaptor: ArgumentCaptor[T]) {

    def capture: T = argumentCaptor.capture()

    def values: List[T] = argumentCaptor.getAllValues.asScala.toList
  }


}
