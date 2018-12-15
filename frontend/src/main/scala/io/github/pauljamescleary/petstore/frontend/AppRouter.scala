package io.github.pauljamescleary.petstore.frontend

import io.github.pauljamescleary.petstore.frontend.components.{Footer, TopNav}
import io.github.pauljamescleary.petstore.frontend.models.Menu
import io.github.pauljamescleary.petstore.frontend.pages.{HomePage, SignInPage}
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import services._

/**
  * @author Mark Kegel (mkegel@vast.com)
  */
object AppRouter {
  // Define the page routes used in the Petstore
  sealed trait AppPage
  case object HomePageRt extends AppPage
  case object SignInRt extends AppPage
  case object SignOutRt extends AppPage

  val mainMenu = Vector(
    Menu("Sign In", SignInRt),
    Menu("Sign Out", SignOutRt)
  )

  def layout(c: RouterCtl[AppPage], r: Resolution[AppPage]) =
    <.div(
      TopNav(TopNav.Props(mainMenu, r.page, c)),
      r.render(),
      Footer()
    )

  // configure the router
  val routerConfig = RouterConfigDsl[AppPage].buildConfig { dsl =>
    import dsl._

    //val petWrapper = AppCircuit.connect(_.pets)

    // wrap/connect components to the circuit
    (staticRoute("#/home", HomePageRt) ~> renderR(ctl => AppCircuit.wrap(_.motd)(proxy => HomePage(ctl, proxy)))
        | staticRoute("#/sign-in", SignInRt) ~> renderR(ctl => AppCircuit.wrap(_.userProfile)(proxy => SignInPage(ctl, proxy)))
        | emptyRule
        ).notFound(redirectToPage(SignInRt)(Redirect.Replace))
        .renderWith(layout)
  }

  // create the router
  val router: Router[AppPage] = Router(BaseUrl.until_#, routerConfig)
}

object HeaderStyle {
  /**
    * #header {
    * border: 0 none;
    *
    * .navbar-brand {
    * width: 54px;
    * position: relative;
    *
    * svg {
    * position: absolute;
    * top: 12px;
    * left: 13px;
    * }
    * }
    *
    * .navbar-nav li .dropdown-menu {
    * border-top: 0 none;
    * margin-left: -1px;
    * }
    *
    * .navbar-nav.authenticated a {
    * padding: 5px;
    * }
    *
    * .navbar-text.navbar-right.authenticated {
    * margin: 17px 15px 15px 13px;
    * font-size: 13px;
    *
    * span {
    * font-weight: bold;
    * }
    * }
    * }
    */
}