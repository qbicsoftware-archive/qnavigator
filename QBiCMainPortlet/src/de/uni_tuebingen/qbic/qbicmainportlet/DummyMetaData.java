package de.uni_tuebingen.qbic.qbicmainportlet;
import java.util.Date;


	enum MetaDataType {UNDEFINED, QSPACE, QPROJECT, QSAMPLE, QEXPERIMENT};

	public class DummyMetaData {
		public DummyMetaData() {
			this.identifier = new String();
			this.description = new String();
			this.type = MetaDataType.UNDEFINED;
			this.num_of_children = new Integer(0);
			this.creation_date = new Date();

		}

		public String getIdentifier() {
			return identifier;
		}
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public MetaDataType getType() {
			return type;
		}
		public void setType(MetaDataType type) {
			this.type = type;
		}
		public Integer getNumOfChildren() {
			return num_of_children;
		}
		public void setNumOfChildren(Integer num_of_children) {
			this.num_of_children = num_of_children;
		}
		public Date getCreationDate() {
			return creation_date;
		}
		public void setCreationDate(Date creation_date) {
			this.creation_date = creation_date;
		}

		private String identifier;
		private String description;
		private MetaDataType type;
		private Integer num_of_children;
		private Date creation_date;
	}
