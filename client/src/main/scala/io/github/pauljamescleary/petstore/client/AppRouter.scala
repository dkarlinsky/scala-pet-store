package io.github.pauljamescleary.petstore.client

import io.github.pauljamescleary.petstore.client.components.Footer
import io.github.pauljamescleary.petstore.client.pages._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import services._

object AppRouter {
  // Define the page routes used in the Petstore
  sealed trait AppPage
  case object HomePageRt extends AppPage
  case object SignInRt extends AppPage
  case object SignOutRt extends AppPage
  case object RegisterRt extends AppPage
  case object RecoveryRt extends AppPage
  case class PasswordResetRt(token:String) extends AppPage
  case class AccountActivationRt(token:String) extends AppPage

  val userProfileWrapper = AppCircuit.connect(_.userProfile)

  def layout(c: RouterCtl[AppPage], r: Resolution[AppPage]) =
    <.div(
      userProfileWrapper(AppMenu(_, r.page, c)),
      r.render(),
      Footer()
    )

  // configure the router
  val routerConfig = RouterConfigDsl[AppPage].buildConfig { dsl =>
    import dsl._

    val rootModelWrapper = AppCircuit.connect(x => x)

    // wrap/connect components to the circuit
    (staticRoute("#/home", HomePageRt) ~> renderR(ctl => rootModelWrapper(HomePage(ctl,_)))
    | staticRoute("#/sign-in", SignInRt) ~> renderR(ctl => userProfileWrapper(SignInPage(ctl,_)))
    | staticRoute("#/sign-out", SignOutRt) ~> renderR(ctl => userProfileWrapper(SignOutPage(ctl,_)))
    | staticRoute("#/register", RegisterRt) ~> renderR(ctl => userProfileWrapper(RegistrationPage(ctl,_)))
    | staticRoute("#/recovery", RecoveryRt) ~> renderR(RecoveryPage(_))
    | dynamicRouteCT("#/password-reset" / remainingPath.caseClass[PasswordResetRt]) ~> dynRenderR((rt, ctl) => PasswordResetPage(ctl,rt.token))
    | dynamicRouteCT("#/activation" / remainingPath.caseClass[AccountActivationRt]) ~> dynRenderR((rt, ctl) => AccountActivationPage(ctl,rt.token))
    | emptyRule
    ).notFound(redirectToPage(SignInRt)(Redirect.Replace))
    .renderWith(layout)
  }

  // create the router
  val router: Router[AppPage] = Router(BaseUrl.until_#, routerConfig)
}
