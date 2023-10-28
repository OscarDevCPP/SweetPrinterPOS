package pe.puyu.pukafx.services.printer;

import org.json.JSONObject;
import pe.puyu.jticketdesing.core.SweetTicketDesign;
import pe.puyu.pukafx.model.UserConfig;
import pe.puyu.pukafx.util.JsonUtil;
import pe.puyu.pukafx.util.PukaUtil;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SweetTicketPrinter {
  private Runnable onSuccess;
  private Consumer<String> onError;

  private final JSONObject printerInfo;
  private final JSONObject ticket ;

  public SweetTicketPrinter(JSONObject ticket) {
    this.ticket = ticket;
    this.printerInfo = ticket.getJSONObject("printer");
    this.onSuccess = () -> System.out.println("on success not implemented: SweetTicketPrinter");
    this.onError = System.out::println;
  }

  public void printTicket() {
    CompletableFuture.runAsync(() -> {
      try {
        var outputStream = getOutputStreamByPrinterType();
        loadMetadata();
        outputStream.write(new SweetTicketDesign(ticket).getBytes());
        outputStream.close();
        onSuccess.run();
      } catch (Exception e) {
        onError.accept(makeErrorMessageForException(e));
      }
    });
  }

  public SweetTicketPrinter setOnSuccess(Runnable onSuccess) {
    this.onSuccess = onSuccess;
    return this;
  }

  public SweetTicketPrinter setOnError(Consumer<String> onError) {
    this.onError = onError;
    return this;
  }

  private void loadMetadata() throws Exception {
    var metadata = new JSONObject();
    if (ticket.has("metadata") && !ticket.isNull("metadata")) {
      metadata = ticket.getJSONObject("metadata");
    }
    var userConfig = JsonUtil.convertFromJson(PukaUtil.getUserConfigFileDir(), UserConfig.class);
    if ((!metadata.has("logoPath") || metadata.isNull("logoPath")) && userConfig.isPresent()) {
      metadata.put("logoPath", userConfig.get().getLogoPath());
      ticket.put("metadata", metadata);
    }
  }

  private OutputStream getOutputStreamByPrinterType() throws Exception {
    if (this.printerInfo.isNull("name_system"))
      throw new Exception("name_system esta vacio");
    var name_system = this.printerInfo.getString("name_system");
    var port = this.printerInfo.getInt("port");
    var outputStream = Printer.getOutputStreamFor(name_system, port, this.printerInfo.getString("type"));
    Printer.setOnUncaughtExceptionFor(outputStream,
        (t, e) -> onError.accept(makeErrorMessageForException(e.getMessage())));
    return outputStream;
  }

  private String makeErrorMessageForException(String error) {
    return String.format("Error al imprimir un ticket, name_system: %s, port: %d, type: %s, mensaje error: %s",
        printerInfo.getString("name_system"), printerInfo.getInt("port"), printerInfo.getString("type"),
        error);
  }

  private String makeErrorMessageForException(Exception e) {
    return makeErrorMessageForException(e.getMessage());
  }

}
