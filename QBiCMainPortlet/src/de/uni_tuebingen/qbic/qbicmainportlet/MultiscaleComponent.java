package de.uni_tuebingen.qbic.qbicmainportlet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import model.ExperimentStatusBean;
import model.notes.Note;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import controllers.MultiscaleController;

public class MultiscaleComponent extends CustomComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 4700958245761376884L;
  private MultiscaleController controller;
  private VerticalLayout mainlayout;
  private Grid pastcomments;
  private Panel commentsPanel;
  
  
  public MultiscaleComponent(MultiscaleController c) {
    this.controller = c;
    initUI();
  }

  public void initUI() {
    mainlayout = new VerticalLayout();
    mainlayout.setWidth(100, Unit.PERCENTAGE);
    commentsPanel = new Panel();
    
    Label commentsLabel = new Label("No comments added so far.", ContentMode.HTML);
    commentsPanel.setContent(commentsLabel);
    
    commentsPanel.setImmediate(true);
    commentsPanel.setWidth(50, Unit.PERCENTAGE);
    commentsPanel.setHeight(UI.getCurrent().getPage().getBrowserWindowHeight() * 0.2f, Unit.PIXELS);
    
    setCompositionRoot(mainlayout);
  }
  
  public void updateUI(String sampleCode) {
   controller.update(sampleCode);
   setNotes();   
  }

  void buildEmptyComments() {

    // add comments
    VerticalLayout addComment = new VerticalLayout();
    addComment.setMargin(true);
    addComment.setWidth(100, Unit.PERCENTAGE);
    final TextArea comments = new TextArea();
    comments.setInputPrompt("Write your comment here...");
    comments.setWidth(50, Unit.PERCENTAGE);
    comments.setRows(5);
    addComment.addComponent(comments);
    Button commentsOk = new Button("Add Comment");
    commentsOk.addStyleName(ValoTheme.BUTTON_FRIENDLY);
    commentsOk.addClickListener(new ClickListener() {
      /**
       * 
       */
      private static final long serialVersionUID = -5369241494545155677L;

      public void buttonClick(ClickEvent event) {
        if ("".equals(comments.getValue()))
          return;

        String newComment = comments.getValue();
        // reset comments
        comments.setValue("");
        // use some date format
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        Note note = new Note();
        note.setComment(newComment);
        note.setUsername(controller.getUser());
        note.setTime(ft.format(dNow));

        // show it now
        pastcomments.getContainerDataSource().addItem(note);

         //TODO write back
        Label commentsLabel = new Label(translateComments((BeanItemContainer<Note>) pastcomments.getContainerDataSource()), ContentMode.HTML);
        commentsPanel.setContent(commentsLabel);

        // write back to openbis
        if (!controller.addNote(note)) {
          Notification.show("Could not add comment to sample. How did you do that?");
        }

      }

    });
    addComment.addComponent(commentsOk);
    addComment.addComponent(commentsPanel);
    addComment.setComponentAlignment(comments, Alignment.TOP_CENTER);
    addComment.setComponentAlignment(commentsOk, Alignment.MIDDLE_CENTER);
    addComment.setComponentAlignment(commentsPanel, Alignment.TOP_CENTER);
    
    mainlayout.addComponent(addComment);

    // visualize previous comments
    pastcomments = new Grid();
    pastcomments.setWidth(100, Unit.PERCENTAGE);
    //mainlayout.addComponent(pastcomments);
    Label commentsLabel = new Label("No comments added so far.", ContentMode.HTML);
    commentsPanel.setContent(commentsLabel);
    
    //mainlayout.addComponent(commentsPanel);
    //mainlayout.setComponentAlignment(commentsPanel, Alignment.TOP_CENTER);
  }

  void setNotes() {
    if (pastcomments == null) {
      buildEmptyComments();
    }
    Label commentsLabel = new Label(translateComments(controller.getContainer()), ContentMode.HTML);
    commentsPanel.setContent(commentsLabel);

    pastcomments.setContainerDataSource(controller.getContainer());
    pastcomments.setColumnOrder("time", "username", "comment");
    pastcomments.setHeightMode(HeightMode.ROW);
    //pastcomments.setHeightByRows(controller.getContainer().size());
  }


  public void resize(float width, float height) {
    setWidth(width * 0.8f, Unit.PIXELS);
    // setHeight(height*0.6f, Unit.PIXELS);
  }
    
  public String translateComments(BeanItemContainer<Note> notes) {
    
    String lastDay = "";
    String labelString = "";
    
    for (Iterator i = notes.getItemIds().iterator(); i.hasNext();) {
      Note noteBean = (Note) i.next();
      String date = noteBean.getTime();
      String day = date.split("T1")[0];
      String time = date.split("T1")[1].split("\\.")[0];
      
      if(!lastDay.equals(day)) {
        lastDay = day;
        labelString += String.format("%s\n", "<u>" + day + "</u>" );
      }
     
      labelString += String.format("%s\n%s %s\n", "<p><b>" + controller.getLiferayUser(noteBean.getUsername()) + "</b>.</p>",noteBean.getComment(),"<p><i><small>" + time + "</small></i>.</p>");
   }
    
    return labelString;
    
  }


}
