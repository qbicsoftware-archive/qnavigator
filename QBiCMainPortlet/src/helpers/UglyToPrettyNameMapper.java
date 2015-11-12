package helpers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class UglyToPrettyNameMapper {
	// private Map<String,String> namesMapping = new HashMap<String, String>();
	private BidiMap<String, String> namesMapping = new DualHashBidiMap<String, String>();

	public UglyToPrettyNameMapper() {
		// openBIS experiment types translated
		namesMapping.put("Q_EXPERIMENTAL_DESIGN", "Sampling Units");
		namesMapping.put("Q_SAMPLE_EXTRACTION", "Sample Extraction");
		namesMapping.put("Q_SAMPLE_PREPARATION", "Sample Preparation");
		namesMapping.put("Q_PROJECT_DETAILS", "Project Details");
		
		/*
		namesMapping.put("Q_EXT_MS_QUALITYCONTROL", "MS Quality Control");
		namesMapping.put("Q_EXT_NGS_QUALITYCONTROL", "NGS Quality Control");
		namesMapping.put("Q_MICROARRAY_MEASUREMENT", "Microarray Measurement");
		namesMapping.put("Q_MS_MEASUREMENT", "MS Measurement");
		namesMapping.put("Q_NGS_EPITOPE_PREDICTION", "Prediction of MHC binding Epitopes");
		namesMapping.put("Q_NGS_FLOWCELL_RUN", "Flowcell Run");
		namesMapping.put("Q_NGS_HLATYPING", "HLA Typing");
		namesMapping.put("Q_NGS_IMMUNE_MONITORING", "Immune Monitoring");
		namesMapping.put("Q_NGS_MAPPING", "Mapping of NGS Reads");
		namesMapping.put("Q_NGS_MEASUREMENT", "Next-Generation Sequencing Run");
		namesMapping.put("Q_NGS_SINGLE_SAMPLE_RUN", "Next-Generation Sequencing Run");
		namesMapping.put("Q_NGS_VARIANT_CALLING", "Variant Calling");
		namesMapping.put("Q_WF_MA_QUALITYCONTROL", "Microarray Quality Control Workflow");
		namesMapping.put("Q_WF_MS_MAXQUANT", "MaxQuant Workflow");
		namesMapping.put("Q_WF_MS_PEPTIDEID", "Peptide Identification Workflow");
		namesMapping.put("Q_WF_MS_QUALITYCONTROL", "MS Quality Control Worfklow");
		namesMapping.put("Q_WF_NGS_EPITOPE_PREDICTION", "Prediction of MHC binding Epitopes Workflow");
		namesMapping.put("Q_WF_NGS_HLATYPING", "HLA Typing Workflow");
		namesMapping.put("Q_WF_NGS_MERGE", "Merging of NGS Reads");
		namesMapping.put("Q_WF_NGS_QUALITYCONTROL", "NGS Quality Control Workflow");
		namesMapping.put("Q_WF_NGS_RNA_EXPRESSION_ANALYSIS", "RNA Expression Analysis Workflow");
		namesMapping.put("Q_WF_NGS_VARIANT_ANNOTATION", "Variant Annotation Workflow");
		namesMapping.put("Q_WF_NGS_VARIANT_CALLING", "Variant Calling Workflow");
		*/
		// openBIS sample types translated
		namesMapping.put("Q_BIOLOGICAL_ENTITY", "Experimental unit");
		namesMapping.put("Q_BIOLOGICAL_SAMPLE", "Extracted sample");
		namesMapping.put("Q_TEST_SAMPLE", "Prepared sample");

		/*
		namesMapping.put("Q_ATTACHMENT_SAMPLE", "Project Attachment"); 
		namesMapping.put("Q_EXT_MS_QUALITYCONTROL_RUN", "External MS Quality Control Run");
		namesMapping.put("Q_EXT_NGS_QUALITYCONTROL_RUN", "External NGS Quality Control Run");
		namesMapping.put("Q_MICROARRAY_RUN", "Microarray Run");
		namesMapping.put("Q_MS_RUN", "Mass Spectrometry Run");
		namesMapping.put("Q_NGS_EPITOPES", "MHC Binding Epitopes");
		namesMapping.put("Q_NGS_FLOWCELL_RUN", "Flowcell Run");
		namesMapping.put("Q_NGS_HLATYPING", "HLA Typing Run");
		namesMapping.put("Q_NGS_IMMUNE_MONITORING", "Immune Monitoring");
		namesMapping.put("Q_NGS_MAPPING", "Mapping of NGS Reads");
		namesMapping.put("Q_NGS_SINGLE_SAMPLE_RUN", "Next-Generation Sequencing Run");
		namesMapping.put("Q_NGS_VARIANT_CALLING", "Variant Calling Run");
		namesMapping.put("Q_WF_MA_QUALITYCONTROL_RUN", "Microarray Qualit Control Workflow Run");
		namesMapping.put("Q_WF_MS_MAXQUANT_RUN", "MaxQuant Workflow Run");
		namesMapping.put("Q_WF_MS_PEPTIDEID_RUN","Peptide Identification Workflow Run");
		namesMapping.put("Q_WF_MS_QUALITYCONTROL_RUN", "MS Quality Control Workflow Run");
		namesMapping.put("Q_WF_NGS_EPITOPE_PREDICTION_RUN", "Epitope Prediction Workflow Run");
		namesMapping.put("Q_WF_NGS_HLATYPING_RUN", "HLA Typing Workflow Run");
		namesMapping.put("Q_WF_NGS_QUALITYCONTROL_RUN","NGS Quality Control Workflow Run");
		namesMapping.put("Q_WF_NGS_RNA_EXPRESSION_ANALYSIS_RUN", "RNA Expression Workflow Run");
		namesMapping.put("Q_WF_NGS_VARIANT_ANNOTATION_RUN", "Variant Annotation Workflow Run");
		namesMapping.put("Q_WF_NGS_VARIANT_CALLING_RUN", "Variant Calling Workflow Run");
		
		// openBIS dataset types translated
		namesMapping.put("Q_EXT_MS_QUALITYCONTROL_RESULTS", "Mass Spectrometry Quality Control (External)");
		namesMapping.put("Q_EXT_NGS_QUALITYCONTROL_RESULTS", "NGS Quality Control (External)");
		namesMapping.put("Q_MA_RAW_DATA", "Microarray Raw Data");
		namesMapping.put("Q_MS_RAW_DATA", "Mass Spectrometry Raw Data");
		namesMapping.put("Q_NGS_HLATYPING_DATA", "HLA Typing Results");
		namesMapping.put("Q_NGS_IMMUNE_MONITORING_DATA", "Immune Monitoring Data");
		namesMapping.put("Q_NGS_MAPPING_DATA", "Mapped NGS Reads");
		namesMapping.put("Q_NGS_RAW_DATA", "NGS Raw Data");
		namesMapping.put("Q_NGS_VARIANT_CALLING_DATA", "Variant Calling Data");
		namesMapping.put("Q_PROJECT_DATA", "Project related Data");
		namesMapping.put("Q_WF_MA_QUALITYCONTROL_LOGS", "Microarray Qualit Control Logs");
		namesMapping.put("Q_WF_MA_QUALITYCONTROL_RESULTS", "Micoarray Quality Control");
		namesMapping.put("Q_WF_MS_MAXQUANT_ORIGINAL_OUT", "MaxQuant Outfile");
		namesMapping.put("Q_WF_MS_MAXQUANT_RESULTS", "MaxQuant Results");
		namesMapping.put("Q_WF_MS_PEPTIDEID_LOGS", "Peptide Identification Logs");
		namesMapping.put("Q_WF_MS_PEPTIDEID_RESULTS", "Peptide Identification Results");
		namesMapping.put("Q_WF_MS_QUALITYCONTROL_LOGS", "Mass Spectrometry Quality Control Logs");
		namesMapping.put("Q_WF_MS_QUALITYCONTROL_RESULTS", "Mass Spectrometry Quality Control Results");
		namesMapping.put("Q_WF_NGS_EPITOPE_PREDICTION_LOGS", "Epitope Prediction Logs");
		namesMapping.put("Q_WF_NGS_EPITOPE_PREDICTION_RESULTS", "Epitope Prediction Results");
		namesMapping.put("Q_WF_NGS_HLATYPING_LOGS", "HLA Typing Logs");
		namesMapping.put("Q_WF_NGS_HLATYPING_RESULTS", "HLA Typing Results");
		namesMapping.put("Q_WF_NGS_QUALITYCONTROL_RESULTS", "NGS Quality Control Results");
		namesMapping.put("Q_WF_NGS_RNAEXPRESSIONANALYSIS_LOGS", "Gene Expression Analysis Logs");
		namesMapping.put("Q_WF_NGS_RNAEXPRESSIONANALYSIS_RESULTS", "Gene Expression Analysis Results");
		namesMapping.put("Q_WF_NGS_VARIANT_ANNOTATION_LOGS", "Variant Annotation Logs");
		namesMapping.put("Q_WF_NGS_VARIANT_ANNOTATION_RESULTS", "Variant Annotation Results");
		namesMapping.put("Q_WF_NGS_VARIANT_CALLING_LOGS", "Variant Calling Logs");
		namesMapping.put("Q_WF_NGS_VARIANT_CALLING_RESULTS", "Variant Calling Results");
		*/
		
		// Mulstiscale HCC specific stuff
		namesMapping.put("MSH_UNDEFINED_STATE",
				"Sample not yet part of the MultiscaleHCC workflow");
		namesMapping.put("MSH_SURGERY_SAMPLE_TAKEN",
				"Liver tumor biopsy finished");
		namesMapping.put("MSH_SENT_TO_PATHOLOGY",
				"Tumor sample sent to pathology");
		namesMapping.put("MSH_PATHOLOGY_REVIEW_STARTED",
				"Tumor sample is under review");
		namesMapping.put("MSH_PATHOLOGY_REVIEW_FINISHED",
				"Tumor sample review completed.");
		namesMapping.put("MSH_SENT_TO_HUMAN_GENETICS",
				"Tumor sample sent to Human Genetics department");
		
	}

	public String getPrettyName(String uglyName) {
		String prettyName = uglyName;

		if (namesMapping.containsKey(uglyName)) {
			prettyName = namesMapping.get(uglyName);
		}
		return prettyName;
	}

	public String getOpenBisName(String prettyName) {
		String uglyName = prettyName;

		if (namesMapping.containsValue(prettyName)) {
			uglyName = namesMapping.getKey(prettyName);
		}
		return uglyName;
	}
  
}
