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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.SwingConstants;

import main.OpenBisClient;
import model.ExperimentBean;
import model.ProjectBean;
import model.SampleBean;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;

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

	public GraphGenerator(ProjectBean projectBean, OpenBisClient openBisClient) throws IOException
	{
		//super("openBIS graph");

	    
	    //Project p = dh.openBisClient.getProjectByCode(project);
	    
		//Project p = dh.openBisClient.getProjectByIdentifier(projectBean);
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
			
			
			//List<Sample> all_project_samples = dh.openBisClient.getSamplesOfProject(p.getCode());
			//List<Sample> all_project_samples = dh.project_to_samples.get(p.getId());
			List<SampleBean> all_project_samples = new ArrayList<SampleBean>();
			
			for (Iterator i = projectBean.getExperiments().getItemIds().iterator(); i.hasNext();) {
			    // Get the current item identifier, which is an integer.
			    ExperimentBean exp = (ExperimentBean) i.next();
		      
			    for (Iterator j = exp.getSamples().getItemIds().iterator(); j.hasNext();) {
			      SampleBean samp = (SampleBean) j.next();

			      all_project_samples.add(samp);
			    }
			}
			
            List<PropertyType> bioSampleProperties = openBisClient.listPropertiesForType(openBisClient.getSampleTypeByString("Q_BIOLOGICAL_SAMPLE"));
            List<PropertyType> testSampleProperties = openBisClient.listPropertiesForType(openBisClient.getSampleTypeByString("Q_TEST_SAMPLE"));


			
			// count the different sample types
			Integer num_measurement_samples = new Integer(0);

	        List<SampleBean> samps = new ArrayList<SampleBean>();

			for (SampleBean s : all_project_samples) {
			  //System.out.println(s);
				//String key = s.getSampleTypeCode();
			    String key = s.getType();
			  
				if (sample_count.containsKey(key)) {

					sample_count.put(key, sample_count.get(s.getType()) + 1);
				}
				else {
					sample_count.put(key, 1);
				}

				if (key.equals("Q_TEST_SAMPLE")) {
					//List<Sample> tmp = this.open_client.getFacade().listSamplesOfSample(s.getPermId());
				  //List<Sample> tmp = dh.openBisClient.getFacade().listSamplesOfSample(s.getPermId());
				  List<Sample> tmp = s.getParents();
				  num_measurement_samples += tmp.size();
				}
				
				if(key.equals("Q_BIOLOGICAL_ENTITY")) {
				  samps.add(s);
				}
			}


			//System.out.println(sample_count);
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
				    
					for (SampleBean s : samps) {
						//List <Object> subtree_vertices = new ArrayList<Object>();
						//s.getProperties().get(key)
                      
                      String species = "";

					  for (Map.Entry<String, String> entry : s.getProperties().entrySet())
					  {
					    if(entry.getKey().equals("Q_NCBI_ORGANISM")) {
					      species = entry.getValue();
					    }
					  }
					  
					  //TODO get Labels of properties.... for organism                        
                        //List<PropertyType> completeProperties = dh.openBisClient.listPropertiesForType(dh.openBisClient.getSampleTypeByString(s.getType()));
                        
                        //String species = "";
                        //for(PropertyType pType: completeProperties) {
                        //  if(pType.getCode().equals("Q_NCBI_ORGANISM")){
                         //   species = dh.openBisClient.getCVLabelForProperty(pType, s.getProperties().get("Q_NCBI_ORGANISM"));
                          //}
                       // }
                        
					  
					    Object mother_node = graph.insertVertex(parent, s.getId(), String.format("%s\n%s", s.getCode(), species), 20, 20, width, height, "ROUNDED;strokeColor=#ffffff;fillColor=#0365C0");
						//subtree_vertices.add(mother_node);
						List<Sample>  children = new ArrayList<Sample>();
						
						//children.addAll(this.open_client.getFacade().listSamplesOfSample(s.getPermId()));
                        //children.addAll(dh.openBisClient.getFacade().listSamplesOfSample(s.getPermId()));
						
						children.addAll(s.getChildren());
					    
						
						//Object group_node = graph.insertVertex(parent, null, "Entities", 20, 20, width, height);

						for (Sample c : children) {

							if (c.getSampleTypeCode().equals("Q_BIOLOGICAL_SAMPLE")) {
		                        
		                        String primaryTissue = "";
		                        for(PropertyType pType: bioSampleProperties) {
		                          if(pType.getCode().equals("Q_PRIMARY_TISSUE")){
		                            primaryTissue = openBisClient.getCVLabelForProperty(pType, openBisClient.getSampleByIdentifier(c.getIdentifier()).getProperties().get("Q_PRIMARY_TISSUE"));
		                          }
		                        }
							  
								Object daughter_node = graph.insertVertex(parent, c.getPermId(), String.format("%s\n%s", c.getCode(), primaryTissue), 20, 20, width, height, "ROUNDED;strokeColor=#ffffff;fillColor=#51A7F9");
								graph.insertEdge(parent, null, "", mother_node, daughter_node);
								//graph.groupCells(group_node, 1.0, new Object[] {daughter_node});

								List<Sample>  grandchildren = new ArrayList<Sample>();

								//grandchildren.addAll(this.open_client.getFacade().listSamplesOfSample(c.getPermId()));
								grandchildren.addAll(openBisClient.getFacade().listSamplesOfSample(c.getPermId()));
								
								for (Sample gc : grandchildren) {
									if (gc.getSampleTypeCode().equals("Q_TEST_SAMPLE")) {
									  
			                             String testSampleType = "";
			                                for(PropertyType pType: testSampleProperties) {
			                                  if(pType.getCode().equals("Q_SAMPLE_TYPE")){
			                                    testSampleType = openBisClient.openBIScodeToString(openBisClient.getCVLabelForProperty(pType, openBisClient.getSampleByIdentifier(gc.getIdentifier()).getProperties().get("Q_SAMPLE_TYPE")));
			                                  }
			                                }
									  
										Object granddaughter_node = graph.insertVertex(parent, gc.getPermId(), String.format("%s\n%s", gc.getCode(), testSampleType), 20, 20, width, height,"ROUNDED;strokeColor=#ffffff;fillColor=#70BF41");
										graph.insertEdge(parent, null, "", daughter_node, granddaughter_node);

										List<Sample>  grandgrandchildren = new ArrayList<Sample>();

										//grandgrandchildren.addAll(this.open_client.getFacade().listSamplesOfSample(gc.getPermId()));
										grandgrandchildren.addAll(openBisClient.getFacade().listSamplesOfSample(gc.getPermId()));
										
										for (Sample ggc : grandgrandchildren) {
										  
										  
											Object grandgranddaughter_node = graph.insertVertex(parent, ggc.getPermId(), String.format("%s\n%s", ggc.getCode(), openBisClient.openBIScodeToString(ggc.getSampleTypeCode())), 20, 20, width + 20.0, height,"ROUNDED;strokeColor=#ffffff;fillColor=#F39019");
											graph.insertEdge(parent, null, "", granddaughter_node, grandgranddaughter_node);

										}
									}
								}
							}
						}
					}

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
			  this.res = showFile(projectBean.getId(), "PNG", bas);
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
