import java.net.{HttpURLConnection, URL}
import java.util.concurrent.TimeoutException

import sbt._
import sbt.Keys._
import sbt.Tests

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.control.NonFatal

/**
  * This class is responsible for running integration tests.
  * @author Dmitriy Yefremov
  */
object IntegrationTestSettings {

  /**
    * Basic settings needed to enable integration testing of the project.
    */
  private val itSettings = Defaults.itSettings ++ Seq (
    libraryDependencies += "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % "it",
    unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base =>  Seq(base / "it"))
  )

  /**
    * Settings need to enable integration testing with a real application instance running.
    */
  val settings = itSettings ++ Seq(
    testOptions in IntegrationTest += Tests.Setup(() => setup()),
    testOptions in IntegrationTest += Tests.Cleanup(() => cleanup())
  )

  /**
    * HTTP port the application under test will be listening on.
    */
  private val AppPort = 9000

  /**
    * The URL to hit to check that the app is running.
    */
  private val AppUrl = new URL(s"http://localhost:$AppPort")

  /**
    * Screen session name. It is only needed to kill the session by name later.
    */
  private val ScreenName = "playFuncTest"

  /**
    * The command that runs the application.
    */
  private val RunCommand = s"""screen -dmSL $ScreenName play run -Dhttp.port=$AppPort"""

  /**
    * The command that kills the running application.
    */
  private val KillCommand = s"""screen -S $ScreenName -X quit"""

  /**
    * How long to wait for the application to start before failing.
    */
  private val StartupTimeout = 60.seconds

  /**
    * How long to wait for the application to stop before failing.
    */
  private val ShutdownTimeout = 10.seconds


  /**
    * Test initialization call back. This method should be called before running functional tests.
    */
  private def setup(): Unit = {
    println("Launching the app...")
    println(RunCommand)
    RunCommand.run()
    // setup a shutdown hook to make sure the app is killed even if execution is interrupted
    sys.addShutdownHook(KillCommand.run())
    // wait until the app is ready
    waitUntil(StartupTimeout) {
      println("Waiting for the app to start up...")
      isAppRunning()
    }
    println("The app is now ready")
  }

  /**
    * Test cleanup call back. This method should be called after running functional tests.
    */
  private def cleanup(): Unit = {
    println("Killing the app...")
    println(KillCommand)
    KillCommand.run()
    waitUntil(ShutdownTimeout) {
      println("Waiting for the app to shutdown...")
      !isAppRunning()
    }
  }

  /**
    * Tests if the app is up and running. It also serves as a warm up call before running any tests (Play in dev mode will
    * not compile any classes before it receives the first request).
    */
  private def isAppRunning(): Boolean = {
    try {
      val connection = AppUrl.openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET")
      connection.connect()
      true
    } catch {
      case NonFatal(e) =>
        println(s"${e.getClass.getSimpleName}: ${e.getMessage}")
        false
    }
  }

  /**
    * Waits until either the given predicate returns `true` or the given timeout period is reached.
    * Throws a [[TimeoutException]] in case of a timeout.
    */
  private def waitUntil(timeout: Duration)(predicate: => Boolean): Unit = {
    val startTimeMillis = System.currentTimeMillis
    while (!predicate) {
      Thread.sleep(5.seconds.toMillis)
      if ((System.currentTimeMillis - startTimeMillis).millis > timeout) {
        throw new TimeoutException
      }
    }
  }


}