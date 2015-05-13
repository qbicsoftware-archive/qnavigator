package helpers;

import java.util.ArrayList;
import java.util.List;

import main.BarcodeCreator;
import model.IBarcodeBean;

import com.vaadin.server.FileDownloader;

import de.uni_tuebingen.qbic.qbicmainportlet.BarcodeView;

/**
 * Class implementing the Runnable interface so it can trigger a response in the view after the barcode creation thread finishes
 * @author Andreas Friedrich
 *
 */
public class BarcodesReadyRunnable implements Runnable {

  private BarcodeView view;
  private FileDownloader pdfDL;
  private FileDownloader sheetDL;
  private List<IBarcodeBean> barcodeBeans;
  BarcodeCreator creator;

  public BarcodesReadyRunnable(BarcodeView view, BarcodeCreator creator,
      ArrayList<IBarcodeBean> barcodeBeans) {
    this.view = view;
    this.barcodeBeans = barcodeBeans;
    this.creator = creator;
  }

  private void attachDownloadsToButtons() {
    if (pdfDL != null)
      pdfDL.remove();
    
    pdfDL = new FileDownloader(creator.zipAndDownloadBarcodes(barcodeBeans));
    pdfDL.extend(view.getButtonTube());
    if (sheetDL != null)
      sheetDL.remove();
    for(IBarcodeBean b : barcodeBeans)
      System.out.println(b);
    System.out.println(view.getSorter());
    sheetDL = new FileDownloader(creator.createAndDLSheet(barcodeBeans, view.getSorter()));
    sheetDL.extend(view.getButtonSheet());
  }

  @Override
  public void run() {
    attachDownloadsToButtons();
    view.creationDone();
  }
}
