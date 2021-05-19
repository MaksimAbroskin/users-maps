package ru.dins.scalaschool.users_maps.telegram.model

import io.circe.Decoder

final case class Offset(value: Long) {
  def next: Offset = Offset(this.value + 1)
}

object Offset {
  val zero: Offset = Offset(0L)

  implicit val offsetOrder: Ordering[Offset]  = Ordering.by((offset: Offset) => offset.value)
  implicit val offsetDecoder: Decoder[Offset] = Decoder[Long].map(Offset.apply)
}
