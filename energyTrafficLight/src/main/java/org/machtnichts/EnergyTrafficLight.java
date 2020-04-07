package org.machtnichts;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EnergyTrafficLight
{
  private static TrayIcon trayIcon;
  private static Image redImage;
  private static Image greenImage;
  private static boolean isImporting;

  public static void main(String[] args)
  {
    redImage = createImage(SystemTray.getSystemTray().getTrayIconSize(), Color.RED);
    greenImage = createImage(SystemTray.getSystemTray().getTrayIconSize(), Color.GREEN);
    trayIcon = new TrayIcon(redImage);
    PopupMenu pp = new PopupMenu();
    MenuItem exitItem = new MenuItem("Exit");
    pp.add(exitItem);
    exitItem.addActionListener(e -> {
      SystemTray.getSystemTray().remove(trayIcon);
      System.exit(0);
    });
    trayIcon.setPopupMenu(pp);
    try
    {
      SystemTray.getSystemTray().add(trayIcon);
    }
    catch (AWTException e)
    {
      e.printStackTrace();
    }
    onImportChanged(isImporting);
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(EnergyTrafficLight::update, 5, 5, TimeUnit.SECONDS);
  }

  private static Image createImage(Dimension dimension, Color color)
  {
    BufferedImage bufferedImage = new BufferedImage(dimension.height, dimension.width, BufferedImage.TYPE_INT_ARGB);
    Graphics g = bufferedImage.getGraphics();
    g.setColor(color);
    g.fillOval(0, 0, dimension.width - 1, dimension.height - 1);
    return bufferedImage;
  }

  private static void onImportChanged(boolean isImporting)
  {
    trayIcon.displayMessage("Import changed", "text ("+isImporting+")", isImporting ? TrayIcon.MessageType.WARNING : TrayIcon.MessageType.INFO);
    trayIcon.setImage(isImporting ? redImage : greenImage);
  }

  private static boolean isImporting()
  {
    boolean importing = false;
    try
    {
      HttpRequest request = HttpRequest.newBuilder().GET().uri(new URI("http://192.168.178.9:8080/rest/items/switchGridImport/state"))
          .build();
      HttpResponse<String> response =
          HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
      importing = "ON".equalsIgnoreCase(response.body());
    }
    catch (URISyntaxException | InterruptedException | IOException e)
    {
      e.printStackTrace();
    }
    return importing;
  }

  private static void update()
  {
    boolean newImporting = isImporting();
    if (newImporting != isImporting)
    {
      isImporting = newImporting;
      onImportChanged(isImporting);
    }
  }

}