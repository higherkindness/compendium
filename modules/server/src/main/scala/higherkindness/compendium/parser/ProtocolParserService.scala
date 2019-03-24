package higherkindness.compendium.parser

import cats.effect.Sync
import higherkindness.compendium.models.Protocol
import higherkindness.compendium.models.types.ParserResult

trait ProtocolParserService[F[_]] {

  def parse(protocol: Option[Protocol], target: String): F[ParserResult]
}

object ProtocolParserService {

  def apply[F[_]: Sync](implicit F: ProtocolParserService[F]): ProtocolParserService[F] = F

}