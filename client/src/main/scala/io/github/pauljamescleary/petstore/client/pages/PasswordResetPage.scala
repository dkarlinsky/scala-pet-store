package io.github.pauljamescleary.petstore.client.pages

import io.github.pauljamescleary.petstore.client.AppRouter.{AppPage, RecoveryRt, RegisterRt, SignInRt}
import io.github.pauljamescleary.petstore.client._
import io.github.littlenag.scalajs.components.reactbootstrap.{Card, CardBody, CardHeader, CardTitle}
import io.github.pauljamescleary.petstore.client.css.GlobalStyles
import io.github.pauljamescleary.petstore.client.services.{AppCircuit, PasswordReset, PetStoreClient}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^.{^, _}

import scala.concurrent.ExecutionContext
import scala.language.existentials
import scala.util.Try

object PasswordResetPage {

  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(router: RouterCtl[AppPage], token: String)

  case class State(password1: String, password2: String, isValidToken:Option[Boolean])

  import css.CssSettings._
  import scalacss.ScalaCssReact._

  object Style extends StyleSheet.Inline {
    import dsl._

    val outerDiv = style(
      textAlign.center,
      alignItems.flexStart,
      paddingTop(120.px),
      display.flex,
      flexDirection.column
    )

    val innerDiv = style(
      textAlign.left,
      //fontSize(20.px),
      minHeight(450.px),
      width(400.px),
      alignItems.flexStart,
      float.none,
      margin(0 px, auto)
    )
  }

  def passwordResetForm($: BackendScope[Props, State], p: Props, s: State) = {
    <.form(^.onSubmit ==> {_ => Callback(AppCircuit.dispatch(PasswordReset(p.token,s.password1))) >> p.router.set(SignInRt)},
      <.div(bss.formGroup,
        <.label(^.`for` := "description", "Password"),
        <.input.text(bss.formControl,
          ^.id := "password",
          ^.value := s.password1,
          ^.`type` := "password",
          ^.placeholder := "Password",
          ^.onChange ==> {ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(password1 = text))}
        )
      ),
      <.div(bss.formGroup,
        <.label(^.`for` := "description", "Confirm Password"),
        <.input.text(bss.formControl,
          ^.id := "password2",
          ^.value := s.password2,
          ^.`type` := "password",
          ^.placeholder := "Confirm Password",
          ^.onChange ==> {ev: ReactEventFromInput => val text = ev.target.value; $.modState(_.copy(password2 = text))}
        )
      ),
      <.button(^.disabled := {s.password1 != s.password2})("Reset")
    )
  }

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div(Style.outerDiv,
        <.div(Style.innerDiv,
          s.isValidToken match {
            case Some(true) =>
              Card()(
                CardHeader()(s"Password Reset"),
                CardBody()(passwordResetForm($,p,s))
              )
            case Some(false) =>
              Card()(
                CardBody()(
                  CardTitle()("The token is not valid or has expired."),
                  <.span(p.router.link(RecoveryRt)("Forgot your password?")),
                  <.br,
                  <.span(p.router.link(RegisterRt)("Create an account."))
                )
              )
            case None =>
              Card()(
                CardBody()(s"Verifying Token"),
              )
          }
        )
      )
    }
  }

  import ExecutionContext.Implicits.global

  val component = ScalaComponent.builder[Props]("PasswordReset")
    // create and store the connect proxy in state for later use
    .initialState(State("","",None))
    .renderBackend[Backend]
    .componentDidMount { $ =>
      // This should be a callback, but oh well.
      Callback.future(
        PetStoreClient
        .validateResetToken($.props.token)
        .transform { t =>
          if (t.isFailure)
            logger.log.error("Failed.", t.failed.get.asInstanceOf[Exception])
          Try($.modState(_.copy(isValidToken = Some(t.isSuccess))))
        }
      )
    }
    .build

  // create the React component for Dashboard
  def apply(router: RouterCtl[AppPage], token: String) = component(Props(router, token))
}
