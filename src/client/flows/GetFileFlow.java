package client.flows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import client.abstracts.Flow;
import client.config.Constants;
import client.interfaces.Request;
import client.interfaces.Response;
import client.session.RequestImpl;
import client.session.ResponseImpl;

public class GetFileFlow extends Flow {

  private static final File USER_DIR = new File(Constants.USER_PATH);

  private Request req = new RequestImpl(output);
  private Response res = new ResponseImpl(input);

  public GetFileFlow(DataInputStream input, DataOutputStream output) {
    super(input, output);
  }

  @Override
  public void begin() {
    boolean proceed = true;

    proceed = prepareRequest();
    if (!proceed)
      return;

    proceed = sendRequest();
    if (!proceed)
      return;

    receiveResponse();
  }

  private boolean prepareRequest() {
    req.setMethod(Constants.GET);

    while (req.getPath() == null) {
      printer.action("""
          Select variant:
          [1] - get by name
          [2] - get by ID
          [q] - return""");

      String variant = scanner.nextLine();
      switch (variant) {
        case "1":
          req.setPath("/");
          askForName();
          break;

        case "2":
          req.setPath("/id");
          askForId();
          break;

        case "q":
          return false;

        default:
          printer.warn("Invalid variant selected '" + variant + "'.");
      }
    }

    return true;
  }

  private void askForName() {
    String name = "";
    while (name.length() < 1) {
      printer.action("Enter name of the file:");
      name = scanner.nextLine();
    }
    req.setParam("file-name", name);
  }

  private void askForId() {
    String id = "";
    while (id.length() < 1) {
      printer.action("Enter ID:");
      id = scanner.nextLine();
    }
    req.setParam("file-id", id);
  }

  private boolean sendRequest() {
    try {
      req.send();
    } catch (IOException e) {
      printer.error("Request error: " + e.getMessage());
      return false;
    }

    return true;
  }

  private boolean receiveResponse() {
    try {
      res.receive();
    } catch (IOException e) {
      printer.error("Response error: " + e.getMessage());
      return false;
    }

    if (res.getStatusCode().equals("200")) {
      printer.info(res.getMessage());
      askForNameToSave();
    } else {
      printer.warn(res.getMessage());
    }

    return true;
  }

  @SuppressWarnings("java:S135")
  private void askForNameToSave() {
    String name = "";
    while (true) {
      printer.action("Specify name for the file:");
      name = scanner.nextLine();

      if (name.length() == 0) {
        name = req.getParam("file-name");
      }

      if (name.length() < 3) {
        printer.warn("Name must be at least 3 characters length.");
        continue;
      }

      if (Files.exists(new File(USER_DIR, name).toPath())) {
        printer.warn("File with name '" + name + "' already exists.");
        continue;
      }

      break;
    }

    try {
      File file = res.getFile();
      Files.move(file.toPath(), file.toPath().resolveSibling(name));
    } catch (IOException e) {
      printer.error("File could not be saved with specified name.");
      return;
    }

    printer.info("File saved with name '" + name + "'.");
  }

}
