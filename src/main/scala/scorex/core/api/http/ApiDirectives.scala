package scorex.core.api.http

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive0, Directives}
import scorex.core.settings.RESTApiSettings
import scorex.core.utils.ScorexEncoding
import scorex.crypto.hash.Blake2b256

trait ApiDirectives extends Directives with ScorexEncoding {
  val settings: RESTApiSettings
  val apiKeyHeaderName: String

  lazy val withCors: Directive0 = settings.corsAllowedOrigin.fold(pass) { origin =>
    respondWithHeaders(RawHeader("Access-Control-Allow-Origin", origin))
  }

  lazy val withAuth: Directive0 = optionalHeaderValueByName(apiKeyHeaderName).flatMap {
    case _ if settings.apiKeyHash.isEmpty => pass
    case None => reject(AuthorizationFailedRejection)
    case Some(key) =>
      val keyHashStr: String = encoder.encode(Blake2b256(key))
      if (settings.apiKeyHash.contains(keyHashStr)) pass
      else reject(AuthorizationFailedRejection)
  }
}
