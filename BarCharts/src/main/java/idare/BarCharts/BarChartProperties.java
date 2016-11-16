package idare.BarCharts;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayDataSet;
import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayDataSet;
import idare.imagenode.GUI.Legend.Utilities.TextPaneResizer;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.Properties.Localisation.Position;
import idare.imagenode.Utilities.GUIUtils;
import idare.imagenode.exceptions.io.WrongFormat;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;
/**
 * This is an example of a Visualisation Property plugin that can be used for two different types of datasets (i.e. the basic types of Datasets
 * @author Thomas Pfau
 *
 */
public class BarChartProperties extends DataSetLayoutProperties {

	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.DataSetProperties#getLocalisationPreference()
	 */
	@Override
	public Position getLocalisationPreference() {
		// TODO Auto-generated method stub
		return Position.CENTER;
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.DataSetProperties#getItemFlexibility()
	 */
	@Override
	public boolean getItemFlexibility() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.DataSetProperties#newContainerInstance(idare.imagenode.Interfaces.DataSets.DataSet, idare.imagenode.Interfaces.DataSets.NodeData)
	 */
	@Override
	public DataContainer newContainerInstance(DataSet origin, NodeData data) {
		// TODO Auto-generated method stub
		return new BarChartContainer(origin, data);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.DataSetProperties#getTypeName()
	 */
	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return "Bar Charts";
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.DataSetProperties#testValidity(idare.imagenode.Interfaces.DataSets.DataSet)
	 */
	@Override
	public void testValidity(DataSet set) throws WrongFormat {

		//Either ValueSetDataSet or ItemDataSet are allowed to be used with these properties.
		//And they should not have too many bars, so that each bar gets a minimum size.
		try{
			MultiArrayDataSet vds = (MultiArrayDataSet) set;
//			System.out.println("There are " + vds.getHeaders().size() + " headers and " + vds.getSetNames().size() + " different sets.");
			if(5 * (vds.getHeaders().size() + 2) * vds.getSetNames().size() > 180)
			{
				throw new WrongFormat("Too many bars to plot for a BarChart");
			}		
			for(String sheet : vds.getSetNames())
			{
				Vector<Comparable> header = vds.getHeadersForSheet(sheet);
				Set<Comparable> uniqueHeaders = new HashSet<Comparable>(header);
				if(uniqueHeaders.size() < header.size())
				{
					throw new WrongFormat("BarCharts are incompatible with non unique headers in a single sheet.");		
				}					
			}
		}
		catch(ClassCastException e)
		{
			try{
				//the cast is mainly to test if this is indeed an itemDataSet
				ArrayDataSet itemDS = (ArrayDataSet) set;
				if(!itemDS.isnumeric)
				{
					throw new WrongFormat("Only numeric sets allowed for BarChart!");
				}
				if(5*itemDS.getHeaders().size() > 180)
				{
					throw new WrongFormat("Too many bars to plot for a BarChart");	
				}				
			}
			catch(ClassCastException ex)
			{
				throw new WrongFormat("Cannot create a BarChart Visualisation on this type of data.");
			}
		}
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.DataSetProperties#getDataSetDescriptionPane(javax.swing.JScrollPane, java.lang.String, idare.imagenode.internal.ColorManagement.ColorMap, idare.imagenode.Interfaces.DataSets.DataSet)
	 */
	@Override
	public JPanel getDataSetDescriptionPane(JScrollPane Legend, String DataSetLabel, ColorMap map, DataSet set)
	{
		Insets InnerInsets = new Insets(0,0,0,0);
		//create a panel to display
		JPanel DataSetPane = new JPanel();
		DataSetPane.setLayout(new BoxLayout(DataSetPane, BoxLayout.PAGE_AXIS));		
		//create the title area and adjust its properties to not request focus..
		JTextPane area = new JTextPane();
		DefaultCaret caret = (DefaultCaret)area.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		//add a ResizeListener to the Legend, that resizes this component. 
		Legend.addComponentListener(new TextPaneResizer(area));
		area.setPreferredSize(new Dimension());
		area.setText(DataSetLabel + ": " + set.Description);
		area.setEditable(false);
		area.setFont(area.getFont().deriveFont(Font.BOLD,22f));
		area.setBorder(null);
		area.setMargin(InnerInsets);
		//set up the preferred size based on the Legend Size. This will get regularily updated when the Legend is resized..
		area.setPreferredSize(GUIUtils.getPreferredSize(area, Legend.getViewport().getSize(), 300));		
		DataSetPane.add(area);		
		DataSetPane.add(Box.createVerticalGlue());
		//Add and setup a Header description
		HeaderDescription hdesc = new HeaderDescription();
		hdesc.setupItemDescription(set.getDefaultData(), DataSetLabel, Legend);
		DataSetPane.add(hdesc);
		DataSetPane.add(Box.createVerticalGlue());	
		//Add and setup the description for each individual series of BarCharts.
		BarChartDescription desc = new BarChartDescription(Legend);
		desc.setupItemDescription(set,map);
		DataSetPane.add(desc);						
		desc.setVisibleWidth(Legend.getViewport().getWidth()-2);
		return DataSetPane;
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.DataSetProperties#getWorkingClassTypes()
	 */
	@Override
	public Collection<Class<? extends DataSet>> getWorkingClassTypes() {
		Vector<Class<? extends DataSet>> acceptableclasses = new Vector<Class<? extends DataSet>>();
		acceptableclasses.add(MultiArrayDataSet.class);
		acceptableclasses.add(ArrayDataSet.class);
		return acceptableclasses;
	}

}
