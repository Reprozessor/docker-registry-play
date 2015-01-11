package docker

import org.specs2.mutable._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}

class RootControllerTest extends Specification {
  "RootController" should {
    "return index text" in new WithApplication {
      val res = route(FakeRequest(GET, "/")).get

      status(res) must_== 200
    }
  }
}