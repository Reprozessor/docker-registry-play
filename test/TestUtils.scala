import java.io.File

import org.specs2.execute.{Result, AsResult}
import play.api.test.{FakeApplication, WithApplication}

import scala.util.Random

object TestUtils {
  val random = new scala.util.Random

  def randomString(prefix: String) = {
    prefix + Random.alphanumeric.take(8).mkString
  }

  def delete(files: Iterable[File]): Unit = files.foreach(delete)

  def delete(file: File) {
    if(file.isDirectory) {
      delete(listFiles(file))
      file.delete
    } else if(file.exists)
      file.delete
  }

  def listFiles(filter: java.io.FileFilter)(dir: File): Array[File] = wrapNull(dir.listFiles(filter))
  def listFiles(dir: File, filter: java.io.FileFilter): Array[File] = wrapNull(dir.listFiles(filter))
  def listFiles(dir: File): Array[File] = wrapNull(dir.listFiles())

  private def wrapNull(array: Array[File]) = {
    if(array == null)
      new Array[File](0)
    else
      array
  }

  val fakeApp = FakeApplication(additionalConfiguration = Map("registry.data.path" -> TestUtils.randomString("target/test-data-")))
  abstract class WithTestApplication extends WithApplication(fakeApp) {
    override def around[T: AsResult](t: => T): Result = super.around {
      cleanupData()
      t // result
    }

    def cleanupData(): Unit = {
      // Why do you specify and override the fake application. Instead you can use TestUtils.randomString directly.
      val path = new File(TestUtils.randomString("target/test-data-"))
      TestUtils.delete(path)
    }
  }
}
