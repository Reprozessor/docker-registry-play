import org.specs2.mutable._
import play.api.libs.json.JsBoolean
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import controllers.DockerHeaders._

class RegistryControllerTest extends Specification {
  "RegistryController" should {
    "return server name" in new WithApplication {
      val res = route(FakeRequest(GET, "/")).get

      status(res) must_== 200
    }
    "respond to _ping" in new WithApplication {
      val res = route(FakeRequest(GET, "/v1/_ping")).get

      val reportedDockerRegistryVersion = header(REGISTRY_VERSION, res).getOrElse("UNKNOWN")

      reportedDockerRegistryVersion must_== "0.6.3"

      status(res) must_== 200
      contentAsJson(res) match {
        case JsBoolean(value) => value must_== true
        case other => sys.error(s"Unexpected result type: $other")
      }
    }
    "fail on getting JSON image for non-exiting image" in new WithApplication {
      val res = route(FakeRequest(GET, "/v1/images/NONEXISTING/json")).get
      status(res) must_== 404
    }
  }

}