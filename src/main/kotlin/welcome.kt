import kotlinx.browser.window
import kotlinx.css.*
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onKeyDownFunction
import kotlinx.html.js.onKeyPressFunction
import kotlinx.html.onKeyPress
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import org.w3c.dom.events.KeyboardEvent
import react.*
import react.dom.*
import styled.css
import styled.styledDiv
import styled.styledInput

@Serializable
data class ClientResponse(val english: String, val polish: String)

data class WidgetState(val revealed: Boolean, val response: ClientResponse?) : RState

@JsExport
class Widget : RComponent<RProps, WidgetState>() {

    init {
        state = WidgetState(false, null)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun doThing() {
        window.fetch("http://localhost:9001").then {
            it.json().then { json ->
                val decoded = Json.decodeFromDynamic<ClientResponse>(json)
                setState(WidgetState(false, decoded))
            }
        }
    }

    fun progress() {
        if (state.response == null || state.revealed) {
            doThing()
        } else {
            setState(WidgetState(true, state.response))
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                width = 100.pct
                height = 70.pct
                display = Display.flex
                flexDirection = FlexDirection.column
                alignItems = Align.center
                justifyContent = JustifyContent.center
                fontSize = 20.px
                textAlign = TextAlign.center
            }
            if (state.response == null) {
                div {
                    +"Start?"
                }
            } else {
                div {
                    p {
                        +state.response!!.english
                    }
                    p {
                        if (state.revealed) {
                            +state.response!!.polish
                        } else {
                            +"..."
                        }
                    }
                }
            }
            styledInput {
                css {
                    width = 100.px
                    height = 40.px
                }
                attrs {
                    type = InputType.button
                    value = when {
                        state.response == null -> "Start!"
                        state.revealed -> "Next"
                        else -> "Reveal"
                    }
                    onKeyDownFunction = {
                        it.preventDefault()
                        if (it.asDynamic().keyCode == 13) progress()
                    }
                    onClickFunction = { it.preventDefault(); progress() }
                }
            }
        }
    }
}
