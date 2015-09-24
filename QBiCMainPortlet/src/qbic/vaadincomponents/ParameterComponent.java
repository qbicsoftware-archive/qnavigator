package qbic.vaadincomponents;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import logging.Log4j2Logger;
import submitter.Workflow;
import submitter.parameters.FloatParameter;
import submitter.parameters.InputList;
import submitter.parameters.IntParameter;
import submitter.parameters.Parameter;
import submitter.parameters.ParameterSet;
import submitter.parameters.StringParameter;

import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.StringToFloatConverter;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.validator.FloatRangeValidator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;

public class ParameterComponent extends WorkflowParameterComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -3182823029401916444L;
  private logging.Logger LOGGER = new Log4j2Logger(ParameterComponent.class);

  private FormLayout parameterForm = new FormLayout();
  private FieldGroup parameterFieldGroup;
  private FieldGroup inputListFieldGroup;
  private Workflow workFlow;

  public ParameterComponent(Workflow workFlow) {
    this.workFlow = workFlow;
    this.buildLayout(workFlow);
    setCompositionRoot(parameterForm);
  }

  public ParameterComponent() {
    setCompositionRoot(parameterForm);
  }

  @Override
  public void buildLayout(Workflow workFlow) {
    this.workFlow = workFlow;
    this.setCaption("<font color=#FF0000> Set Parameter Values for Workflow Submission </font>");
    this.setCaptionAsHtml(true);
    buildForm(workFlow);
  }

  public void buildForm(final Workflow workFlow) {

    parameterForm.removeAllComponents();
    parameterFieldGroup = new FieldGroup();
    inputListFieldGroup = new FieldGroup();

    /*
     * for (Map.Entry<String, Parameter> entry : workFlow.getData().getData().entrySet()) {
     * FileParameter param = (FileParameter) entry.getValue(); FileNameValidator fileNameValidator =
     * new FileNameValidator("Please provide a valid file path"); TextField newField =
     * createInputField(param, fileNameValidator);
     * 
     * parameterForm.addComponent(newField); inputListFieldGroup.bind(newField, entry.getKey()); //
     * Have to set it here because field gets cleared upon binding
     * newField.setValue(param.getValue().toString()); }
     */

    for (Map.Entry<String, Parameter> entry : workFlow.getParameters().getParams().entrySet()) {
      if (entry.getValue() instanceof FloatParameter) {
        FloatParameter param = (FloatParameter) entry.getValue();
        FloatRangeValidator floatValidator =
            new FloatRangeValidator(String.format("Parameter has to be in the range of %s to %s",
                param.getMinimum(), param.getMaximum()), param.getMinimum(), param.getMaximum());
        TextField newField =
            createParameterField(param, floatValidator, new StringToFloatConverter());

        parameterForm.addComponent(newField);
        parameterFieldGroup.bind(newField, entry.getKey());
        // Have to set it here because field gets cleared upon binding
        newField.setValue(param.getValue().toString());
      }

      else if (entry.getValue() instanceof IntParameter) {
        IntParameter param = (IntParameter) entry.getValue();
        IntegerRangeValidator intValidator =
            new IntegerRangeValidator(String.format("Parameter has to be in the range of %s to %s",
                param.getMinimum(), param.getMaximum()), param.getMinimum(), param.getMaximum());
        TextField newField =
            createParameterField(param, intValidator, new StringToIntegerConverter());

        parameterForm.addComponent(newField);
        parameterFieldGroup.bind(newField, entry.getKey());
        // Have to set it here because field gets cleared upon binding
        newField.setValue(param.getValue().toString());
      }

      else if (entry.getValue() instanceof StringParameter) {
        StringParameter param = (StringParameter) entry.getValue();
        ComboBox newField = createStringSelectionParameterField(param);

        parameterForm.addComponent(newField);
        parameterFieldGroup.bind(newField, entry.getKey());
        // Have to set it here because field gets cleared upon binding
        newField.setValue(param.getValue().toString());
      }
    }
  }

  @Override
  public Workflow getWorkflow() {
    boolean parametersValid = writeSetParameters();
    if (!parametersValid)
      return null;
    writetInputList();
    return this.workFlow;
  }

  /**
   * writes UI parameters to their model(workflow) equivalent returns true if all fields where set
   * with meaningfull values.
   * 
   * @return
   */
  boolean writeSetParameters() {
    Collection<Field<?>> registeredFields = parameterFieldGroup.getFields();
    ParameterSet paramSet = workFlow.getParameters();

    for (Field<?> field : registeredFields) {
      if (!field.isValid() || field.isEmpty()) {
        //String errorMessage = "Warning: Parameter " + field.getCaption() + "is invalid!";
        String errorMessage = "Warning: Parameter " + field.getDescription() + "is invalid!";
        Notification.show(errorMessage, Type.TRAY_NOTIFICATION);
        LOGGER.info(errorMessage);
        return false;
      }
     
      String value = field.getValue().toString();
      //paramSet.getParam(field.getCaption()).setValue(value);
      paramSet.getParam(field.getDescription()).setValue(value);
    }
    return true;
  }
  
  /**
   * Can be used to add Options to a Combobox that are only available at runtime (e.g. are created from
   * openBIS metainformation
   * @param caption Caption of the field, normally wf name followed by port and parameter name, e.g. "Microarray QC.1.f"
   * @param params Set of options for this parameter
   */
  public void setComboboxOptions(String caption, Set<String> params) {
    for (Field<?> field : parameterFieldGroup.getFields()) {
      LOGGER.debug(field.getDescription());
      LOGGER.debug(field.getCaption());
      if (field.getDescription().equals(caption)) {
        if (field instanceof ComboBox) {
          ((ComboBox) field).addItems(params);
        }
      }
    }
  }

  void writetInputList() {
    Collection<Field<?>> registeredFields = inputListFieldGroup.getFields();
    InputList inpList = workFlow.getData();

    for (Field<?> field : registeredFields) {
      inpList.getParam(field.getCaption()).setValue(field.getValue().toString());
    }
  }

  @Override
  public void resetParameters() {
    Collection<Field<?>> registeredFields = parameterFieldGroup.getFields();
    ParameterSet paramSet = workFlow.getParameters();

    for (Field field : registeredFields) {
      //String resetValue = paramSet.getParam(field.getCaption()).getValue().toString();
    	String resetValue = paramSet.getParam(field.getDescription()).getValue().toString();
      field.setValue(resetValue);
    }
  }

  public void resetInputList() {
    Collection<Field<?>> registeredFields = inputListFieldGroup.getFields();
    InputList inpList = workFlow.getData();

    for (Field<?> field : registeredFields) {
      TextField fieldToReset = (TextField) field;
      fieldToReset.setValue(inpList.getParam(field.getCaption()).getValue().toString());
    }
  }

  private TextField createParameterField(Parameter param, Validator validator, Converter converter) {
    //TextField field = new TextField(param.getTitle());
    //field.setDescription(param.getDescription());
	TextField field = new TextField(param.getDescription());
	field.setDescription(param.getTitle());
	field.addValidator(validator);
    field.setImmediate(true);
    field.setConverter(converter);
    return field;
  }

  private TextField createInputField(Parameter param, Validator validator) {
    TextField field = new TextField(param.getTitle());
    field.setDescription(param.getDescription());
    field.setWidth("50%");
    field.addValidator(validator);
    field.setImmediate(true);
    return field;
  }

  private ComboBox createStringSelectionParameterField(StringParameter param) {
    //ComboBox box = new ComboBox(param.getTitle());
    //box.setDescription(param.getDescription());
	ComboBox box = new ComboBox(param.getDescription());
	box.setDescription(param.getTitle());
	box.setFilteringMode(FilteringMode.CONTAINS);
    box.addItems(param.getRange());
    // should only be the range.
    box.setNullSelectionAllowed(false);
    box.setImmediate(true);
    return box;
  }

  @Override
  public void buildLayout() {
    // TODO Auto-generated method stub

  }


  @Override
  public ParameterSet getParameters() {
    Collection<Field<?>> registeredFields = parameterFieldGroup.getFields();
    ParameterSet paramSet = workFlow.getParameters();

    Map<String, Parameter> updatedParams = new HashMap<String, Parameter>();

    for (Field<?> field : registeredFields) {
      //Parameter updatedParam = paramSet.getParam(field.getCaption());
      Parameter updatedParam = paramSet.getParam(field.getDescription());
      updatedParam.setValue(field.getValue().toString());
      //updatedParams.put(updatedParam.getTitle(), updatedParam);
      updatedParams.put(updatedParam.getDescription(), updatedParam);
    }

    ParameterSet updatedParamSet =
        new ParameterSet(workFlow.getName(), workFlow.getDescription(), updatedParams);
    return updatedParamSet;
  }
}
