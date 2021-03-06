package view

import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.scene.control.PasswordField
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import koma.controller.requests.account.login.doLogin
import koma.gui.view.window.auth.RegistrationWizard
import koma.gui.view.window.preferences.PreferenceWindow
import koma.matrix.user.identity.UserId_new
import koma.storage.config.profile.getRecentUsers
import koma.storage.config.server.loadServerConf
import koma.storage.config.settings.AppSettings
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

/**
 * Created by developer on 2017/6/21.
 */
class LoginScreen(): View() {

    override val root = VBox()

    var userId: ComboBox<String> by singleAssign()
    var serverCombo: ComboBox<String> by singleAssign()
    var password: PasswordField by singleAssign()

    init {
        title = "Koma"

        val iconstream = javaClass.getResourceAsStream("/icon/koma.png");
        FX.primaryStage.icons.add(Image(iconstream))

        val grid = GridPane()
        with(grid) {
            paddingAll = 5.0
            row("Username") {
                val recentUsers = getRecentUsers().map { it.toString() }
                userId = combobox(values = recentUsers) {
                    isEditable = true
                    selectionModel.selectFirst()

                }
            }
            row("Server") {
                val serverCommonUrls = listOf("https://matrix.org")
                serverCombo = combobox(values = serverCommonUrls) {
                    isEditable = true
                    selectionModel.select(0)
                }
            }
            row("Password") {
                password = passwordfield() {
                }
            }
        }
        with(root) {
            style {
                fontSize=AppSettings.settings.scaling.em
            }
            add(grid)

            val settings = PreferenceWindow()
            button("More Options") {
                action { settings.openModal() }
            }
            buttonbar {
                button("Register") {
                    action {
                        openInternalWindow(RegistrationWizard(), owner = this@LoginScreen.root.parent.parent)
                    }
                }
                button("Login") {
                    isDefaultButton = true
                    action {
                        GlobalScope.launch {
                            doLogin(userId.value, password.text, serverCombo.editor.text)
                        }
                    }
                }
            }
        }
        userId.selectionModel.selectedItem?.let { setServerAddr(it) }
        userId.selectionModel.selectedItemProperty().onChange { it?.let { setServerAddr(it) } }
    }

    private fun setServerAddr(input: String){
        val id = UserId_new(input)
        val addrs = loadServerConf(id.server).addresses
        serverCombo.items = FXCollections.observableArrayList(addrs)
        serverCombo.selectionModel.selectFirst()
    }
}
