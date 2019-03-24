package higherkindness.compendium.parser

import cats.effect.Sync
import cats.syntax.either._
import higherkindness.compendium.models.{ParserError, Protocol}
import higherkindness.compendium.models.types.ParserResult

object ProtocolParser {

  def impl[F[_]: Sync]: ProtocolParserService[F] = new ProtocolParserService[F] {
    ]
    override def parse(protocol: Option[Protocol], target: String): F[ParserResult] =
      protocol.fold(Sync[F].pure(ParserError("No Protocol Found").asLeft[Protocol]))(???)
  }

}
