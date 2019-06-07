package higherkindness.compendium.models

object parserModels {

  final case class ParserError(msg: String)

  type ParserResult = Either[ParserError, Protocol]

}
