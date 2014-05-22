package de.uni_tuebingen.qbic.qbicmainportlet;

import java.util.ArrayList;
import java.util.Arrays;

public class SampleBean implements Comparable<Object> {
	
	private String id;
	private String experiment;
	private String type;
	private String description;
	private String parents;
	private String species;
	private String status;
	
	public SampleBean(String sampleID, String exp, String sType, String desc, String parents, String species, String stat) {
		id = sampleID;
		type = sType;
		experiment = exp;
		description = desc;
		this.parents = parents;
		this.species = species;
		status = stat;
	}
	
	public String toString() {
		return id+" "+experiment+" "+description+" "+type+" "+species+" "+parents;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String stat) {
		this.status = stat;
	}
	
	public String getSpecies() {
		return species;
	}
	
	public void setSpecies(String species) {
		this.species = species;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getParents() {
		return parents;
	}
	
	public void setParents(String parents) {
		this.parents = parents;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getExperiment() {
		return experiment;
	}

	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}
	
	public boolean hasParents() {
		return !getParents().equals("None");
	}
	
	public ArrayList<String> fetchParentIDs() {
		if(!hasParents()) return new ArrayList<String>();
		else return new ArrayList<String>(Arrays.asList(getParents().split(" ")));
	}

	@Override
	public int compareTo(Object o) {
		return id.compareTo(((SampleBean) o).getId());
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof SampleBean) {
			SampleBean b = (SampleBean) o;
			return id.equals(b.getId()) && status.equals(b.getStatus());
		} else return false;
	}
	
	@Override
	public int hashCode() {
	    return id.hashCode();
	}
}
