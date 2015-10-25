import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.ws._

import play.api.test._


@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification with FutureAwaits with DefaultAwaitTimeout {

  val baseUrl = "http://localhost:9000"

  "application" should {

    "return 'foo' from /foo" in {
      val response = await(WS.url(s"$baseUrl/foo").get())
      response.body must beEqualTo("foo")
    }

    "return 'bar' from /bar" in {
      val response = await(WS.url(s"$baseUrl/bar").get())
      response.body must beEqualTo("bar")
    }
  }
}
