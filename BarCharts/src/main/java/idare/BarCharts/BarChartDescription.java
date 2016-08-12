package idare.BarCharts;

import idare.imagenode.Data.BasicDataTypes.ValueSetData.SetDataDescription;
import idare.imagenode.Data.BasicDataTypes.ValueSetData.SetEntryDescriptionPane;
import idare.imagenode.Data.BasicDataTypes.ValueSetData.SetEntryPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.JScrollPane;
/**
 * Class to Generate Descriptions for Lines 
 * @author Thomas Pfau
 *
 */

public class BarChartDescription extends SetDataDescription{
	private static final long serialVersionUID = 1001;
	/**
	 * Create a new LineDataDescription, which is contained in a JScrollPane.
	 * The ViewPort of the JScrollPane has to show the visible part of this Itemdescription. 
	 * @param Parent
	 */
	public BarChartDescription(JScrollPane Parent)
	{
		super(Parent);
	}
	/**
	 * A Pane that visualises a a rectangle with the color of one bar from a barchart and its description.
	 * @author Thomas Pfau
	 *
	 */
	private class BarDescriptionPane extends SetEntryDescriptionPane
	{
		private static final long serialVersionUID = 1001;
		public BarDescriptionPane(Color barColor, String DescriptionString)
		{
			super(barColor,DescriptionString);
		}
		@Override
		public SetEntryPanel getEntry(Color entrycolor) {
			// TODO Auto-generated method stub
			return new ColorPanel(entrycolor);
		}
	}
	/**
	 * A Single Panel for a single entry that only draws the rectangle shape..
	 * @author Thomas Pfau
	 *
	 */
	private class ColorPanel extends SetEntryPanel
	{
		private static final long serialVersionUID = 1001;
		public ColorPanel(Color ShapeColor)
		{
			super(ShapeColor);

		}		
		/*
		 * (non-Javadoc)
		 * @see idare.imagenode.Data.BasicDataTypes.ValueSetData.SetEntryPanel#getShape(double, double, double, double)
		 */
		@Override
		public Shape getShape(double xpos, double ypos, double width,
				double height) {
			// TODO Auto-generated method stub
			return new Rectangle2D.Double(xpos+1,ypos,width,height);
		}
		
		@Override
		/**
		 * Override the original method to instead fill the provided shape.
		 * Otherwise, we would only get an empty rectangle, which would be odd.
		 */
		public void plotShape(Shape shape, Graphics2D g2d)
		{
			g2d.fill(shape);
		}

	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Data.BasicDataTypes.ValueSetData.SetDataDescription#getDescriptionPane(java.awt.Color, java.lang.String)
	 */
	@Override
	public SetEntryDescriptionPane getDescriptionPane(Color color,
			String EntryName) {
		// TODO Auto-generated method stub
		return new BarDescriptionPane(color, EntryName);
	}
}
