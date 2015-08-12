package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.List;

import model.NewIvacSampleBean;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;

public class GridEditForm extends GridLayout {

  public BeanFieldGroup<NewIvacSampleBean> fieldGroup = new BeanFieldGroup<NewIvacSampleBean>(
      NewIvacSampleBean.class);

  private TextField type = new TextField("Type");
  private TextField amount = new TextField("Amount");
  private TextField secondaryName = new TextField("Secondary Name");
  private CheckBox dnaSeq = new CheckBox("DNA Seq");
  private CheckBox rnaSeq = new CheckBox("RNA Seq");
  private CheckBox deepSeq = new CheckBox("Deep Seq");
  private ComboBox tissue = new ComboBox("Tissue");
  private ComboBox seqDevice = new ComboBox("Sequencing Device");

  public GridEditForm(List<String> tissueOptions, List<String> sequenceOptions) {
    super(5, 3);
    setSpacing(true);
    fieldGroup.buildAndBindMemberFields(this);

    addComponent(type, 0, 0);
    addComponent(secondaryName,1,0);
    addComponent(amount, 2, 0);
    addComponent(tissue, 3, 0);
    addComponent(seqDevice, 4, 0);
    addComponent(dnaSeq, 0, 1);
    addComponent(rnaSeq, 1, 1);
    addComponent(deepSeq, 2, 1);

    amount.setConverter(new StringToIntegerConverter());
    tissue.addItems(tissueOptions);
    seqDevice.addItems(sequenceOptions);

    space();
    addComponent(new Button("Save", new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        try {
          fieldGroup.commit();
        } catch (CommitException e) {
          // TODO: Say and do something meaningful
        }
      }
    }), 0, 2);

    addComponent(new Button("Cancel", new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        fieldGroup.discard();
      }
    }), 1, 2);
  }
}
