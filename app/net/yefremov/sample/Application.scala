package net.yefremov.sample

import play.api.mvc._

object Application extends Controller {

  def foo = Action {
    Ok("foo")
  }

  def bar = Action {
    Ok("bar")
  }

}
