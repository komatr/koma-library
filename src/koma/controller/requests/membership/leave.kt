package koma.controller.requests.membership

import com.github.kittinunf.result.Result
import koma.util.coroutine.adapter.retrofit.MatrixException
import koma.util.coroutine.adapter.retrofit.awaitMatrix
import koma_app.appState
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import model.Room
import org.controlsfx.control.Notifications
import retrofit2.HttpException
import tornadofx.*

fun leaveRoom(mxroom: Room) {
    val api = appState.apiClient
    api ?: return
    val removeLocally = { launch(JavaFx) { api.profile.roomStore.remove(mxroom.id) } }
    launch {
        val roomname = mxroom.displayName.get()
        println("Leaving $roomname")
        val result = api.leavingRoom(mxroom.id).awaitMatrix()
        when(result) {
            is Result.Success -> { removeLocally() }
            is Result.Failure -> {
                val ex = result.error
                launch(JavaFx) {
                    Notifications.create()
                            .title("Had error leaving room $roomname")
                            .text("${ex.message}")
                            .owner(FX.primaryStage)
                            .showWarning()
                }
                if ((ex is HttpException && ex.code() == 404)
                        || (ex is MatrixException && ex.httpErr.code() == 404)) {
                    System.err.println()
                    removeLocally()
                }
            }
        }
    }
}