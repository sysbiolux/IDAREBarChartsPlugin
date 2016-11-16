package idare.BarCharts;

import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Properties.Localisation;
import idare.imagenode.Properties.IMAGENODEPROPERTIES.LayoutStyle;
import idare.imagenode.Properties.Localisation.Position;

import java.awt.Dimension;
import java.awt.Rectangle;
/**
 * Container Class for a Bar Chart
 * @author Thomas Pfau
 *
 */
public class BarChartContainer implements  DataContainer {
	/**
	 * The {@link Localisation} is commonly center, with no flexibility, so we fix this localisation
	 */
	static Localisation loc = new Localisation(Position.CENTER, false);
	/**
	 * A BarChart uses a minimal width of 10 (out of the 10 allowed) by IDARE. i.e. we require a maximal stretch. 
	 */
	protected static int minwidth = 10;
	/**
	 * A Barchart uses a minimal height of 4 (out of the 10 allowed) by IDARE. i.e. there are at most 2 barcharts in one imagenode, otherwise this gets too crowded. 
	 */
	protected static int minheight = 4;
	
	DataSet origin;
	NodeData data;
	
	/**
	 * A Default Container with an origin and some node Data associated to it.
	 * @param origin
	 * @param data
	 */
	public BarChartContainer(DataSet origin, NodeData data)
	{
		this.origin = origin;
		this.data = data;		
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataContainer#getMinimalSize()
	 */
	@Override
	public Rectangle getMinimalSize() {		
		return new Rectangle(new Dimension(minwidth,minheight));
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataContainer#getDataSet()
	 */
	@Override
	public DataSet getDataSet() {
		// TODO Auto-generated method stub
		return origin;
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataContainer#getPreferredSize(java.awt.Dimension, idare.imagenode.Properties.METANODEPROPERTIES.LayoutStyle)
	 */
	@Override
	public Dimension getPreferredSize(Dimension availablearea, LayoutStyle style ) {
		//For layouting purposes take at most the minimal size or, whatever is left..
		return new Dimension(Math.min(availablearea.width,minwidth), Math.min(availablearea.height,minheight));
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataContainer#getData()
	 */
	@Override
	public NodeData getData()
	{
		return data;
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataContainer#createEmptyLayout()
	 */
	@Override
	public ContainerLayout createEmptyLayout() {
		// TODO Auto-generated method stub
		return new BarChartContainerLayout();
	}
}

