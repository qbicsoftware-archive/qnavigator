package model;

import java.io.Serializable;
import java.util.List;

public class PropertyBean implements Serializable{
	
	private String label;
	private String code;
	private String type;
	private String description;
	private Object value;
	private List<String> vocabularyValues;
	
	public PropertyBean(String label, String code, String description, Object value) {
		this.label = label;
		this.code = code;
		this.description = description;
		this.setValue(value);
	}

	public PropertyBean() {
		// TODO Auto-generated constructor stub
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public List<String> getVocabularyValues() {
		return vocabularyValues;
	}

	public void setVocabularyValues(List<String> vocabularyValues) {
		this.vocabularyValues = vocabularyValues;
	}
}
