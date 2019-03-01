package io.github.pauljamescleary.petstore.client.css

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import org.querki.jquery._

import scala.language.implicitConversions
import scala.scalajs.js
import scalacss.ScalaCssReact._

import io.github.pauljamescleary.petstore.client.logger._

/**
 * Common Bootstrap components for scalajs-react
 */
object Bootstrap {

  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  // FIXME this should be replaced by react-bootstrap

  // Common Bootstrap contextual styles
  object CommonStyle extends Enumeration {
    val default, primary, success, info, warning, danger = Value
  }

  // Replaced by Cards
  object Panel {

    case class Props(heading: String, style: CommonStyle.Value = CommonStyle.default)

    val component = ScalaComponent.builder[Props]("Panel")
      .renderPC((_, p, c) =>
        <.div(bss.panelOpt(p.style),
          <.div(bss.panelHeading, p.heading),
          <.div(bss.panelBody, c)
        )
      ).build


    def apply(props: Props, children: VdomNode*) = component(props)(children: _*)
    def apply() = component
  }

  object Modal {

    // header and footer are functions, so that they can get access to the the hide() function for their buttons
    case class Props(header: Callback => VdomNode, footer: Callback => VdomNode, closed: Callback, backdrop: Boolean = true,
                     keyboard: Boolean = true)

    class Backend(t: BackendScope[Props, Unit]) {
      def hide =
        // instruct Bootstrap to hide the modal
        t.getDOMNode.map(node => $(node.toElement.get).hide()).void

      // jQuery event handler to be fired when the modal has been hidden
      def hidden(e: JQueryEventObject): js.Any = {
        // inform the owner of the component that the modal was closed/hidden
        t.props.flatMap(_.closed).runNow()
      }

      def render(p: Props, c: PropsChildren) = {
        val modalStyle = bss.modal
        <.div(modalStyle.modal, modalStyle.fade, ^.role := "dialog", ^.aria.hidden := true,
          <.div(modalStyle.dialog,
            <.div(modalStyle.content,
              <.div(modalStyle.header, p.header(hide)),
              <.div(modalStyle.body, c),
              <.div(modalStyle.footer, p.footer(hide))
            )
          )
        )
      }
    }

    val component = ScalaComponent.builder[Props]("Modal")
      .renderBackendWithChildren[Backend]
      .componentDidMount(scope => Callback {
        val p = scope.props
        // instruct Bootstrap to show the modal
        log.debug("Constructing Modal")
        $(scope.getDOMNode.asElement()).show(10, () => js.Dynamic.literal("backdrop" -> p.backdrop, "keyboard" -> p.keyboard, "show" -> true))
        // register event listener to be notified when the modal is closed
        $(scope.getDOMNode.asElement()).on("hidden.bs.modal", null, null, scope.backend.hidden _)
        log.debug("Done constructing Modal")
      })
      .build

    def apply(props: Props, children: VdomElement*) = component(props)(children: _*)
    def apply() = component
  }

}
