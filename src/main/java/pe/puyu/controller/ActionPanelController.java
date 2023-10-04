package pe.puyu.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.LoggerFactory;
import com.github.anastaciocintra.output.PrinterOutputStream;

import ch.qos.logback.classic.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import pe.puyu.model.BifrostConfig;
import pe.puyu.model.UserConfig;
import pe.puyu.service.bifrost.BifrostService;
import pe.puyu.util.JsonUtil;
import pe.puyu.util.PukaAlerts;
import pe.puyu.util.PukaUtil;

public class ActionPanelController implements Initializable {
  private static final Logger logger = (Logger) LoggerFactory.getLogger("pe.puyu.controller.actionPanel");
  private BifrostService bifrostService;

  public void initBifrostService(BifrostService bifrostService) {
    try {
      if (this.bifrostService == null) {
        this.bifrostService = bifrostService;
        this.bifrostService.addUpdateItemsQueueListener(this::onUpdateNumberItemsQueue);
        this.bifrostService.requestItemsQueue();
      }
    } catch (Exception e) {
      logger.error(String.format("Excepcion al inicilizar BifrostService: %s", e.getMessage(), e));
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    initPerfilTab();
    lblVersion.setText(PukaUtil.getPukaVersion());
    reloadPrintServices();
  }

  @FXML
  void onReprint(ActionEvent event) {
    try {
      boolean result = PukaAlerts.showConfirmation("¿Seguro que deseas reemprimir estos tickets?",
          "");
      if (result) {
        bifrostService.requestToGetPrintingQueue();
      }
    } catch (Exception e) {
      logger.error("Excepcion al reemprimir elementos en cola: {}", e.getMessage(), e);
    }
  }

  @FXML
  void onReleaseQueue(ActionEvent event) {
    try {
      boolean result = PukaAlerts.showConfirmation("¿Seguro que deseas liberar los tickets?",
          "Esta accion no es reversible");
      if (result) {
        this.bifrostService.requestToReleaseQueue();
      }
    } catch (Exception e) {
      logger.error("Excepcion al liberar cola de impresion: {}", e.getMessage(), e);
    }
  }

  @FXML
  void onHideWindow(ActionEvent event) {
    this.getStage().hide();
  }

  @FXML
  void onReloadPrintServices(ActionEvent event) {
    reloadPrintServices();
  }

  @FXML
  void onClickListView(MouseEvent event) {
    if (event.getClickCount() == 1) {
      String selectedItem = listViewServices.getSelectionModel().getSelectedItem();
      Clipboard clipboard = Clipboard.getSystemClipboard();
      ClipboardContent content = new ClipboardContent();
      content.putString(selectedItem);
      clipboard.setContent(content);
      PukaUtil.toast(getStage(), String.format("Se copio %s!!", selectedItem));
    }
  }

  private void onUpdateNumberItemsQueue(int numberItemsQueue) {
    Platform.runLater(() -> {
      lblNumberItemsQueue.setText("" + numberItemsQueue);
      btnReprint.setDisable(numberItemsQueue == 0);
      btnRelease.setDisable(numberItemsQueue == 0);
    });
  }

  private void initPerfilTab() {
    Platform.runLater(() -> {
      try {
        var userConfig = JsonUtil.convertFromJson(PukaUtil.getUserConfigFileDir(), UserConfig.class);
        var bifrostConfig = JsonUtil.convertFromJson(PukaUtil.getBifrostConfigFileDir(), BifrostConfig.class);
        if (userConfig.isPresent()) {
          File logoFile = new File(userConfig.get().getLogoPath());
          if (logoFile.exists()) {
            String imgUrl = logoFile.toURI().toURL().toString();
            imgLogo.setImage(new Image(imgUrl));
          }
        }

        if (bifrostConfig.isPresent()) {
          lblRuc.setText(bifrostConfig.get().getRuc());
          lblBranch.setText(bifrostConfig.get().getBranch());
        }

      } catch (IOException e) {
        lblRuc.setText("----------");
        lblBranch.setText("-");
        logger.error("Excepcion al iniacilizar la pestaña de perfil: {}", e.getMessage(), e);
      }
    });
  }

  private void reloadPrintServices() {
    String[] printServicesNames = PrinterOutputStream.getListPrintServicesNames();
    listViewServices.getItems().clear();
    for (String printServiceName : printServicesNames) {
      listViewServices.getItems().add(printServiceName);
    }
  }

  private Stage getStage() {
    return (Stage) root.getScene().getWindow();
  }

  @FXML
  private Label lblNumberItemsQueue;

  @FXML
  private GridPane root;

  @FXML
  private Button btnReprint;

  @FXML
  private Button btnRelease;

  @FXML
  private Button btnHide;

  @FXML
  private Label lblVersion;

  @FXML
  private Label lblBranch;

  @FXML
  private Label lblRuc;

  @FXML
  private ImageView imgLogo;

  @FXML
  private ListView<String> listViewServices;

}
