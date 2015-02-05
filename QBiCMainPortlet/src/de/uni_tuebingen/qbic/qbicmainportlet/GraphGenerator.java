package de.uni_tuebingen.qbic.qbicmainportlet;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.SwingConstants;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.UI;

//public class HelloWorld extends JFrame
public class GraphGenerator 
{

	private String url;
	private StreamResource res;

	public StreamResource getRes() {
		return res;
	}

	public void setRes(StreamResource res) {
		this.res = res;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public GraphGenerator(String project) throws IOException
	{
		//super("openBIS graph");

	    DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
	    
	    Project p = dh.openBisClient.getProjectByCode(project);
	    
		//Project p = this.open_client.getProjectByIdentifier(project);
		//System.out.println(p);

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();

		mxStylesheet stylesheet = graph.getStylesheet();
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE); 
		style.put(mxConstants.STYLE_OPACITY, 100);
		style.put(mxConstants.STYLE_FONTCOLOR, "#ffffff");
		style.put(mxConstants.STYLE_DIRECTION, "east");
		stylesheet.putCellStyle("ROUNDED", style);


		Double width = new Double(120.0);
		Double height = new Double(40.0);



		try
		{
			//Object found_project = graph.insertVertex(parent, null, p.getIdentifier(), 20, 20, 120, height,"ROUNDED;strokeColor=#005FAA;fillColor=#005FAA");
			//Object dummy_node = graph.insertVertex(parent, null, "HALLO", 50,20,120,height, "ROUNDED");

			// could be done more efficiently with SearchCriteria (at least I hope so)
			Map<String, Integer> sample_count = new HashMap<String, Integer>();
			List<Sample> all_project_samples = dh.openBisClient.getSamplesOfProject(p.getCode());
			//List<Sample> all_project_samples = dh.project_to_samples.get(p.getId());
			
            List<PropertyType> bioSampleProperties = dh.openBisClient.listPropertiesForType(dh.openBisClient.getSampleTypeByString("Q_BIOLOGICAL_SAMPLE"));
            List<PropertyType> testSampleProperties = dh.openBisClient.listPropertiesForType(dh.openBisClient.getSampleTypeByString("Q_TEST_SAMPLE"));


			
			// count the different sample types
			Integer num_measurement_samples = new Integer(0);

	         List<Sample> samps = new ArrayList<Sample>();

			for (Sample s : all_project_samples) {
				String key = s.getSampleTypeCode();

				if (sample_count.containsKey(key)) {

					sample_count.put(key, sample_count.get(s.getSampleTypeCode()) + 1);
				}
				else {
					sample_count.put(key, 1);
				}

				if (key.equals("Q_TEST_SAMPLE")) {
					//List<Sample> tmp = this.open_client.getFacade().listSamplesOfSample(s.getPermId());
				  List<Sample> tmp = dh.openBisClient.getFacade().listSamplesOfSample(s.getPermId());
				  num_measurement_samples += tmp.size();
				}
				
				if(key.equals("Q_BIOLOGICAL_ENTITY")) {
				  samps.add(s);
				}
			}


			if (sample_count.containsKey("Q_BIOLOGICAL_ENTITY")) {
				Object dummy_node_level1 = graph.insertVertex(parent, null, sample_count.get("Q_BIOLOGICAL_ENTITY") + "\n" + "biological entities", 20,  20,  width, height, "NONE;strokeWidth=0;strokeColor=#FFFFFF;fillColor=#FFFFFF");


				if (sample_count.containsKey("Q_BIOLOGICAL_SAMPLE")) {
					Object dummy_node_level2 = graph.insertVertex(parent, null, sample_count.get("Q_BIOLOGICAL_SAMPLE") + "\n" + "biological samples", 20,  20,  width, height, "NONE;strokeWidth=0;strokeColor=#FFFFFF;fillColor=#FFFFFF");
					graph.insertEdge(parent, null, "", dummy_node_level1, dummy_node_level2, "strokeWidth=0;strokeColor=#FFFFFF");

					if (sample_count.containsKey("Q_TEST_SAMPLE")) {
						Object dummy_node_level3 = graph.insertVertex(parent, null, sample_count.get("Q_TEST_SAMPLE") + "\n" + "test samples", 20,  20,  width, height, "NONE;strokeWidth=0;strokeColor=#FFFFFF;fillColor=#FFFFFF");
						graph.insertEdge(parent, null, "", dummy_node_level2, dummy_node_level3, "strokeWidth=0;strokeColor=#FFFFFF");

						if (num_measurement_samples > 0) {
							Object dummy_node_level4 = graph.insertVertex(parent, null, num_measurement_samples + "\n" + "measured samples", 20,  20,  width, height, "NONE;strokeWidth=0;strokeColor=#FFFFFF;fillColor=#FFFFFF");

							graph.insertEdge(parent, null, "", dummy_node_level3, dummy_node_level4, "strokeWidth=0;strokeColor=#FFFFFF");

						}

					}



				}
			}

			//List<Experiment> exps = this.open_client.getExperimentsOfProjectByIdentifier(p.getIdentifier());
	        
			//List<Experiment> exps = dh.openBisClient.getExperimentsOfProjectByIdentifier(p.getIdentifier());

			//Map<String, Object> experiments = new HashMap<String, Object>();
			int set_off_exp = 0;

			//for(Experiment e: exps) {
				//String type = e.getExperimentTypeCode();
				//String type_nice_name = this.open_client.openBIScodeToString(e.getExperimentTypeCode());
                //String type_nice_name = dh.openBIScodeToString(type);


				//System.out.println(type);

				// draw the first layer of samples
				//if (type.equals("Q_EXPERIMENTAL_DESIGN")) {
			      //samps.addAll(this.open_client.getSamplesofExperiment(e.getIdentifier()));
				    //samps.addAll(dh.openBisClient.getSamplesofExperiment(e.getIdentifier()));
				    
					for (Sample s : samps) {
						//List <Object> subtree_vertices = new ArrayList<Object>();
						//s.getProperties().get(key)
                      //TODO get Labels of properties.... for organism                        
                        List<PropertyType> completeProperties = dh.openBisClient.listPropertiesForType(dh.openBisClient.getSampleTypeByString(s.getSampleTypeCode()));
                        
                        String species = "";
                        for(PropertyType pType: completeProperties) {
                          if(pType.getCode().equals("Q_NCBI_ORGANISM")){
                            species = dh.openBisClient.getCVLabelForProperty(pType, s.getProperties().get("Q_NCBI_ORGANISM"));
                          }
                        }
                        
					  
					    Object mother_node = graph.insertVertex(parent, s.getPermId(), String.format("%s\n%s", s.getCode(), species), 20, 20, width, height, "ROUNDED;strokeColor=#ffffff;fillColor=#0365C0");
						//subtree_vertices.add(mother_node);
						List<Sample>  children = new ArrayList<Sample>();
						
						//children.addAll(this.open_client.getFacade().listSamplesOfSample(s.getPermId()));
                        children.addAll(dh.openBisClient.getFacade().listSamplesOfSample(s.getPermId()));

						
						//Object group_node = graph.insertVertex(parent, null, "Entities", 20, 20, width, height);

						for (Sample c : children) {

							if (c.getSampleTypeCode().equals("Q_BIOLOGICAL_SAMPLE")) {
		                        
		                        String primaryTissue = "";
		                        for(PropertyType pType: bioSampleProperties) {
		                          if(pType.getCode().equals("Q_PRIMARY_TISSUE")){
		                            primaryTissue = dh.openBisClient.getCVLabelForProperty(pType, dh.openBisClient.getSampleByIdentifier(c.getIdentifier()).getProperties().get("Q_PRIMARY_TISSUE"));
		                          }
		                        }
							  
								Object daughter_node = graph.insertVertex(parent, c.getPermId(), String.format("%s\n%s", c.getCode(), primaryTissue), 20, 20, width, height, "ROUNDED;strokeColor=#ffffff;fillColor=#51A7F9");
								graph.insertEdge(parent, null, "", mother_node, daughter_node);
								//graph.groupCells(group_node, 1.0, new Object[] {daughter_node});

								List<Sample>  grandchildren = new ArrayList<Sample>();

								//grandchildren.addAll(this.open_client.getFacade().listSamplesOfSample(c.getPermId()));
								grandchildren.addAll(dh.openBisClient.getFacade().listSamplesOfSample(c.getPermId()));
								
								for (Sample gc : grandchildren) {
									if (gc.getSampleTypeCode().equals("Q_TEST_SAMPLE")) {
									  
			                             String testSampleType = "";
			                                for(PropertyType pType: testSampleProperties) {
			                                  if(pType.getCode().equals("Q_SAMPLE_TYPE")){
			                                    testSampleType = dh.openBIScodeToString(dh.openBisClient.getCVLabelForProperty(pType, dh.openBisClient.getSampleByIdentifier(gc.getIdentifier()).getProperties().get("Q_SAMPLE_TYPE")));
			                                  }
			                                }
									  
										Object granddaughter_node = graph.insertVertex(parent, gc.getPermId(), String.format("%s\n%s", gc.getCode(), testSampleType), 20, 20, width, height,"ROUNDED;strokeColor=#ffffff;fillColor=#70BF41");
										graph.insertEdge(parent, null, "", daughter_node, granddaughter_node);

										List<Sample>  grandgrandchildren = new ArrayList<Sample>();

										//grandgrandchildren.addAll(this.open_client.getFacade().listSamplesOfSample(gc.getPermId()));
										grandgrandchildren.addAll(dh.openBisClient.getFacade().listSamplesOfSample(gc.getPermId()));
										
										for (Sample ggc : grandgrandchildren) {
										  
										  
											Object grandgranddaughter_node = graph.insertVertex(parent, ggc.getPermId(), String.format("%s\n%s", ggc.getCode(), dh.openBIScodeToString(ggc.getSampleTypeCode())), 20, 20, width + 20.0, height,"ROUNDED;strokeColor=#ffffff;fillColor=#F39019");
											graph.insertEdge(parent, null, "", granddaughter_node, grandgranddaughter_node);

										}
									}
								}
							}
						}
					}

		//		}

				//String status = e.getProperties().get("STATUS");
				//experiments.put(e.getIdentifier(), graph.insertVertex(parent, null, e.getCode() + "\n" + type + "\n" + "STATUS: " + status, 20 + set_off_exp, 120, 120, 60,"ROUNDED;strokeColor=#82B9A0;fillColor=#82B9A0"));

				//graph.insertEdge(parent, null, "", found_project, experiments.get(e.getIdentifier()));

				//graph.insertEdge(parent, null, "", dummy_node, experiments.get(e.getIdentifier()));

				//set_off_exp += 150;

		//	}

			//			Map<String, Object> samples = new HashMap<String, Object>();
			//			int set_off = 0;
			//
			//			// TODO extract functions
			//			List<Sample>  childrens = new ArrayList<Sample>();
			//			Map<String, String> shared_childrens = new HashMap<String, String>();
			//			for(Sample s: samps) {
			//
			//				childrens.addAll(this.open_client.getFacade().listSamplesOfSample(s.getPermId()));
			//				//check.addAll(this.open_client.getFacade().listSamplesOfSample(s.getPermId()));
			//
			//				//if(samples.containsKey(s.getIdentifier())) {
			//				//	System.out.println(s.getSampleTypeCode());
			//				//continue;
			//				//}
			//
			//				//else {
			//				if(childrens.isEmpty() & !(samples.containsKey(s.getIdentifier()))) {
			//					String type = this.open_client.openBIScodeToString(s.getSampleTypeCode());
			//					samples.put(s.getIdentifier(), graph.insertVertex(parent, null, s.getCode() + "\n" + type, 20 + set_off, 220, 120, 60));
			//					graph.insertEdge(parent, null, "", experiments.get(s.getExperimentIdentifierOrNull()), samples.get(s.getIdentifier()));
			//					set_off += 150;
			//				}
			//
			//				else if (!(samples.containsKey(s.getIdentifier()))){//(samples.containsKey(s.getIdentifier())) {
			//
			//					String type = this.open_client.openBIScodeToString(s.getSampleTypeCode());
			//					samples.put(s.getIdentifier(), graph.insertVertex(parent, null, s.getCode() + "\n" + type, 20 + set_off, 220, 120, 60,"ROUNDED;strokeColor=#50AAC8;fillColor=#50AAC8"));
			//
			//					graph.insertEdge(parent, null, "", experiments.get(s.getExperimentIdentifierOrNull()), samples.get(s.getIdentifier()));
			//
			//					for(Sample children_sample: childrens) {
			//
			//						//Sample children_sample = childrens.get(0);
			//						Sample children_samp = this.open_client.getSampleByIdentifier(children_sample.getIdentifier());
			//						type = this.open_client.openBIScodeToString(children_sample.getSampleTypeCode());
			//						samples.put(children_sample.getIdentifier(), graph.insertVertex(parent, null, children_samp.getCode() + "\n" + type, 20 + set_off, 320, 120, 60,"ROUNDED;strokeColor=#50AAC8;fillColor=#50AAC8"));
			//						graph.insertEdge(parent, null, "", samples.get(s.getIdentifier()),samples.get(children_samp.getIdentifier()));
			//						graph.insertEdge(parent, null, "", experiments.get(children_samp.getExperimentIdentifierOrNull()), samples.get(children_samp.getIdentifier()));
			//						set_off += 150;
			//					}
			//				}
			//				else {
			//					String type = this.open_client.openBIScodeToString(s.getSampleTypeCode());
			//
			//					for(Sample children_sample: childrens) {
			//
			//						if(shared_childrens.containsKey(children_sample.getIdentifier())) {
			//							Sample children_samp = this.open_client.getSampleByIdentifier(children_sample.getIdentifier());
			//							type = this.open_client.openBIScodeToString(children_sample.getSampleTypeCode());
			//							graph.insertEdge(parent, null, "", samples.get(s.getIdentifier()),samples.get(children_samp.getIdentifier()));
			//						}
			//
			//						else {
			//							//Sample children_sample = childrens.get(0);
			//							Sample children_samp = this.open_client.getSampleByIdentifier(children_sample.getIdentifier());
			//							type = this.open_client.openBIScodeToString(children_sample.getSampleTypeCode());
			//							samples.put(children_sample.getIdentifier(), graph.insertVertex(parent, null, children_samp.getCode() + "\n" + type, 20 + set_off, 320, 120, 60,"ROUNDED;strokeColor=#50AAC8;fillColor=#50AAC8"));
			//							//shared_childrens.put(children_sample.getIdentifier(), graph.insertVertex(parent, null, children_samp.getProperties().get("QBIC_BARCODE").toString() + "\n" + type, 20 + set_off, 320, 120, 60));
			//							shared_childrens.put(children_sample.getIdentifier(), "yes");
			//							graph.insertEdge(parent, null, "", samples.get(s.getIdentifier()),samples.get(children_samp.getIdentifier()));
			//							graph.insertEdge(parent, null, "", experiments.get(children_samp.getExperimentIdentifierOrNull()), samples.get(children_samp.getIdentifier()));
			//							set_off += 150;
			//						}
			//					}
			//				}
			//				//else {
			//				//	String type = this.portlet.openBIScodeToString(s.getSampleTypeCode());
			//				//	samples.put(s.getIdentifier(), graph.insertVertex(parent, null, s.getProperties().get("QBIC_BARCODE").toString() + "\n" + type, 20 + set_off, 220, 120, 60));
			//
			//				//					graph.insertEdge(parent, null, "", experiments.get(s.getExperimentIdentifierOrNull()), samples.get(s.getIdentifier()));
			//				//				set_off += 150;
			//
			//				//		}
			//				childrens.removeAll(childrens);
			//			}

			//check.add(s);

			mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);
			layout.setDisableEdgeStyle(true);
			//layout.setInterHierarchySpacing(10);
			layout.setInterRankCellSpacing(100);


			// mxIGraphLayout layout = new mxFastOrganicLayout(graph);

			layout.execute(graph.getDefaultParent()); // Run the layout on the facade.\n

			//mxGraphComponent graphComponent = new mxGraphComponent(graph);
			//getContentPane().add(graphComponent);

			BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, null);
			//String url = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/WEB-INF/images/graph.png";
			//this.url = url;
			final ByteArrayOutputStream bas = new ByteArrayOutputStream();
			
			if(image != null) { 
			  ImageIO.write(image, "PNG", bas);
			  this.res = showFile(project, "PNG", bas);
			}
			else {
			  this.res = null;
			}
		}

		finally
		{
			graph.getModel().endUpdate();
		}
	}

	private StreamResource showFile(final String name, final String type,
			final ByteArrayOutputStream bas) {
		// resource for serving the file contents	
		final StreamSource streamSource = new StreamSource() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6632340984219486654L;

			@Override
			public InputStream getStream() {
				if (bas != null) {
					final byte[] byteArray = bas.toByteArray();
					return new ByteArrayInputStream(byteArray);
				}
				return null;
			}
		};
		StreamResource resource = new StreamResource(streamSource,
				name);
		return resource;
	}
}