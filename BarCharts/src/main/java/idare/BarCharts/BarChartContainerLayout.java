package idare.BarCharts;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayDataSet;
import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayNodeData;
import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayDataSet;
import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayDataValue;
import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayNodeData;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.Utilities.LayoutUtils;
import idare.imagenode.Utilities.LegendLabel;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import org.apache.batik.svggen.SVGGraphics2D;
/**
 * ContainerLayout Class for Graph Data, storing common information like the location fo axes, labels and frames.
 * @author Thomas Pfau
 *
 */
public class BarChartContainerLayout extends ContainerLayout {
	
	private static final long serialVersionUID = 1L;
	
	
	/*
	 * Several Pathes and rectangles that are the same for all Images 
	 * The Axes for the legend, along with the zero-lines and the Legend Frame are the same for all nodes, so we can save them to avoid recalculation.
	 */
	/**
	 * The Legend Axes used for each legend layout.
	 */
	private Path2D legendAxes;
	/**
	 * The Node Axes used for each legend layout.
	 */
	private Path2D nodeAxes;
	/**
	 * The Node Zero Line used (if available) in each node image
	 */
	private Line2D zeroLine;
	/**
	 * The Legend Zero Line used (if available) in each legend image
	 */
	private Line2D legendZeroLine;
	/**
	 * The Frame enclosing the legend in the Legend description image
	 */
	private Rectangle2D legendFrame;
	/**
	 * The Label of the DataSet shown in the Legend description image
	 */
	private LegendLabel labelForDataSet; 
	/**
	 * The Label of the minimal Y value in the legend
	 */
	private LegendLabel yMinval;
	/**
	 * The Label of the maximal Y value in the legend
	 */
	private LegendLabel yMaxval;
	/**
	 * The Labels for the x-axes if applicable
	 */
	private Vector<LegendLabel> xLabels = new Vector<LegendLabel>();
	/**
	 * the minimal y value
	 */
	private Double yminval;
	/**
	 * the maximal y value
	 */
	private Double ymaxval;
	/**
	 * The range of y values (to calculate positions and avoiding constant recalulation).
	 */
	private Double yrange;
	/**
	 * the minimal x value
	 */
	private Double xminval;
	/**
	 * the maximal x value
	 */
	private Double xmaxval;
	/**
	 * The range of x values (to calculate positions and avoiding constant recalulation).
	 */
	private Double xrange;
	/**
	 * The Area in which to plot the bars. 
	 */
	private Rectangle2D barArea;
	/**
	 * The Area in which to plot the bars in the legend. 
	 */
	private Rectangle2D legendBarArea;
	/**
	 * The positions for the labels, and bars on the x axis for each header.  
	 */
	private HashMap<Comparable,LabelAndPosition> itemxPositions = new HashMap<Comparable, BarChartContainerLayout.LabelAndPosition>();
	/**
	 * a vector to stroe the positions of the Sheets in the Dataset (and obtain the correct position during layout). 
	 */
	private Vector<String> sheetPos;
	/**
	 * Width of a single bar
	 */
	private double normalbarwidth;
	/**
	 * Width of a bar in a legend
	 */
	private double legendbarwidth;
	/**
	 * Offset between bar groups
	 */
	private double normalbaroffset;
	/**
	 * Offset between bargroups in the Legend
	 */
	private double legendbaroffset;
	
	/**
	 * Default Constructor
	 */
	 public BarChartContainerLayout() {
		 super();		 
		 // TODO Auto-generated constructor stub
	 }
	 @Override	
	 public void setupLayout(NodeData data, Rectangle area, String DataSetLabel, DataSetLayoutProperties props) throws WrongDatasetTypeException {
		 //Overall this is very similar to any other plotting method.
		 //We first need some information about the data. we know some details about it
		 
		 DataSet dataset = data.getDataSet();
		 Vector<Comparable> headers = dataset.getHeaders();
		 
		 Double[] valuerange;
		 
		 if(dataset instanceof MultiArrayDataSet)
		 {
			 MultiArrayDataSet valset = (MultiArrayDataSet)dataset;
			 sheetPos = valset.getSetNames();
		 	 
		 }
		 if(dataset instanceof ArrayDataSet)
		 {
			 sheetPos = new Vector<String>();
			 sheetPos.add(ArrayDataSet.DEFAULT_SERIES_NAME);
		 }
		 valuerange = dataset.getYAxisLimits();
		 Double[] displayrange = determineDisplayRange(valuerange);
		 yminval = displayrange[0];
		 ymaxval = displayrange[1];
		 // BarCharts only make sense, when zero is involved.
		 // if the lower end is larger than zero set it to 0
		 if(yminval > 0)
		 {
			 yminval = 0.;
		 }
		 //if the upper end is smaller than zero, set it to zero
		 if(ymaxval < 0)
		 {
			 ymaxval = 0.;
		 }
		 yrange = ymaxval - yminval;
		 //BarCharts only make sense with non numeric headers
		 int i = 0;
		 xminval = 0.;
		 HashMap<Comparable,String> HeaderLabels = LayoutUtils.getLabelsForData(headers);
		 // The offset is #(headers)/((#(headers)+2)*#(sets)) * width * 0.5; 
		 double abstractbarwidth = (double)headers.size() / (headers.size()*(sheetPos.size() + 2));
		 double placeforoneplot = (sheetPos.size() + 2) * abstractbarwidth;
		 double middleofoneplot = placeforoneplot/2;
		 
		 //For each header, we get an associated position on the x axis, in such a way, that we can build the bar groups.
		 for(Comparable header : headers)
		 {
//			 System.out.println("Setting position for header " + header.toString() + " to " + (middleofoneplot + i*placeforoneplot));
			 itemxPositions.put(header,new LabelAndPosition(middleofoneplot + i * placeforoneplot, HeaderLabels.get(header)));
			 i++;
		 }
		 //And determine the properties for the x axis.
		 xmaxval = (double) headers.size() ;		
		 xrange = xmaxval - xminval;
		 
		 calcNormalLayoutpositions(area);
		 calcLegendLayoutPositions(area, DataSetLabel);
		 calcBarWidth(headers.size(),sheetPos.size());

	 }
	 /**
	  * Calculate the barWidth depending on the available areas and the sets and properties. 
	  * @param headercount
	  * @param setCount
	  */
	 private void calcBarWidth(int headercount, int setCount)
	 {
		 double legendwidth = legendBarArea.getWidth();
		 double normalwidth = barArea.getWidth();
		 int barcount = (setCount+2) * headercount;
		 normalbarwidth = normalwidth / barcount;
		 legendbarwidth = legendwidth / barcount;
		 normalbaroffset = 0.5 * setCount * normalbarwidth;
		 legendbaroffset = 0.5 * setCount * legendbarwidth;
	 }
	 /**
	  * Calc the localisatin of default positions (LineArea, Axes)
	  * @param area - the area in which to plot the data.
	  */
	 private void calcNormalLayoutpositions(Rectangle area)
	 {
		 // Get a few parameters to define the minimal/maximal draw positions and the ranges
		 double xdrawMin = area.getX() + 2;
		 double xdrawMax = area.getX() + area.getWidth();
		 double xdrawRange = xdrawMax - xdrawMin;
		 double ydrawMin = area.getY();
		 double ydrawMax = area.getY() + area.getHeight() -2;
		 double ydrawRange = ydrawMax - ydrawMin;
		 //Leave a little margin for the area to allow for axes 
		 barArea = new Rectangle2D.Double(xdrawMin+1,ydrawMin+1,xdrawRange-2,ydrawRange);
		 nodeAxes = new Path2D.Double();
		 nodeAxes.append(new Line2D.Double(new Point2D.Double(area.getX()+1,ydrawMin),new Point2D.Double(area.getX()+1,ydrawMin +area.getHeight()-1)), false);		
		 //Define the position of the zero-line.
		 double zerofraction = ymaxval/yrange;
		 double zerolineypos = ydrawMax - ydrawRange + zerofraction * ydrawRange ; 
		 zeroLine = new Line2D.Double(xdrawMin,zerolineypos,xdrawMax,zerolineypos);
	 }
	 /**
	  * Calculate the Default positions (Labelpositions, Frame, Axes) for the Legendlayout.
	  * @param area - the area for plotting
	  * @param DataSetLabel - the label used for the Dataset
	  */
	 private void calcLegendLayoutPositions(Rectangle area,String DataSetLabel)
	 {
		 int FrameOffset = 1;
		 int FrameWidth = 2;
		 //Define the legend Frame
		 legendFrame = new Rectangle.Double(area.getX()+FrameWidth/2,
				 area.getY()+FrameWidth/2,
				 area.getWidth()-FrameWidth,
				 area.getHeight()-FrameWidth);


		 //Define the new Area remaining for displaying the legend data
		 //This is located within the legend Frame.
		 Rectangle2D newArea = new Rectangle2D.Double(FrameWidth,
				 FrameWidth,
				 area.getWidth()-2*FrameWidth,
				 area.getHeight()-2* FrameWidth);

		 //Define the Font for the legend label.
		 Font LegendLabelFont = new Font(Font.MONOSPACED,Font.BOLD,Math.max(20,(int)Math.min((legendFrame.getWidth() * 0.1),(legendFrame.getHeight() * 0.1))));

		 //Determine the Label properties
		 FontMetrics fm = LayoutUtils.getSVGFontMetrics(LegendLabelFont);
		 int LabelWidth = fm.stringWidth(DataSetLabel);
		 int LabelHeight = fm.getAscent();		
		 int LabelOffSet = 2;
		 int BaseLinePositionx =  (int) (newArea.getWidth() - (LabelWidth + LabelOffSet)); // One pixel off from the upper right corner. 
		 int BaseLinePositiony =  (int) (LabelOffSet + LabelHeight);
		 labelForDataSet = new LegendLabel(LegendLabelFont, new Point(BaseLinePositionx,BaseLinePositiony), DataSetLabel);
		 //Determine the labels for X and Y axis.
		 Double[] ylims = new Double[]{yminval,ymaxval}; 
		 HashMap<Double, String> ylabels = ColorMap.getDisplayStrings(ylims);


		 Font AxisLabelFont = new Font(Font.MONOSPACED,Font.BOLD,Math.max(12,(int)Math.min((newArea.getWidth() * 0.1),(newArea.getHeight() * 0.1))));	
		 
		 fm = LayoutUtils.getSVGFontMetrics(AxisLabelFont);

		 // place the y labels
		 // first, place the minimum value.
		 //this has to be placed at 2 * labeloffset + height of the text from the bottom.

		 int yminypos = (int)(newArea.getHeight() - (fm.getHeight() + 2 * fm.getDescent() + 2 * LabelOffSet));
		 int ymaxypos = LabelOffSet + fm.getAscent();

		 int xoffset = (int) FrameWidth + Math.max(fm.stringWidth(ylabels.get(ylims[0])),
				 fm.stringWidth(ylabels.get(ylims[1]))) 
				 + 2 * LabelOffSet;

		 yMinval = new LegendLabel(AxisLabelFont, new Point(xoffset - fm.stringWidth(ylabels.get(ylims[0])) - LabelOffSet,yminypos+fm.getAscent()/2), ylabels.get(ylims[0]));
		 yMaxval = new LegendLabel(AxisLabelFont, new Point(xoffset - fm.stringWidth(ylabels.get(ylims[1])) - LabelOffSet,ymaxypos), ylabels.get(ylims[1]));




		 // place the X Labels
		 int x_ypos =  (int)( newArea.getHeight() - LabelOffSet - fm.getDescent());		 
		 int xminxpos = xoffset + FrameOffset;
		 //int xvalrange = area.width - xoffset - 3*FrameOffset; 
		 legendBarArea = new Rectangle2D.Double(xoffset + FrameWidth,0,legendFrame.getWidth() - xoffset - FrameWidth - LabelWidth, yminypos + fm.getDescent());

		 for(Comparable comp : itemxPositions.keySet())
		 {
			 LabelAndPosition labpos = itemxPositions.get(comp);
			 int cstringWidth = fm.stringWidth(labpos.label);
			 double cxval = labpos.position;			
			 int cxpos = (int)((cxval / xrange) * legendBarArea.getWidth() - cstringWidth/2) + xoffset;
			 xLabels.add(new LegendLabel(AxisLabelFont, new Point(cxpos,x_ypos ), labpos.label));
		 }
		 //Determine the Legend Axes.
		 legendAxes = new Path2D.Double();
		 legendAxes.append(new Line2D.Double(new Point(xoffset,2*FrameOffset),
				 new Point(xoffset,yminypos + fm.getDescent())), false);
		 //We don't need the x axis, as we have the zero axis.
		 //LegendAxes.append(new Line2D.Double(new Point(xoffset,yminypos + fm.getDescent()), new Point(area.width-2*FrameOffset,yminypos + fm.getDescent())), true);

		 //And define the LegendLineArea
		 
		 LabelWidth = fm.stringWidth(DataSetLabel);
		 //since top left is y = 0, we have to invert the zerofraction
		 double zerolineypos = getPlotPoint(0, 0, 20, legendBarArea.getHeight()).getY(); 
		 legendZeroLine = new Line2D.Double(xminxpos,zerolineypos,newArea.getWidth()-FrameOffset,zerolineypos);


	 }
	 /**
	  * Layout a legend node with the given NodeData, in the given graphics context with the given colormap.
	  * @param data
	  * @param g2d
	  * @param colors
	  */
	 private void LayoutLegendNode(NodeData data, SVGGraphics2D g2d, ColorMap colors)
	 {
		 //get the original paint and stroke, to restore it.
		 Paint currentPaint = g2d.getPaint();
		 Stroke currentStroke = g2d.getStroke();
		 g2d.setPaint(Color.black);
		 g2d.setStroke(new BasicStroke(2));
		 //Draw standard comoponents
		 g2d.draw(legendFrame);
		 //Move into the Legend Frame.
		 g2d.translate(legendFrame.getX(), legendFrame.getY());
		 //Draw the axes and labels
		 g2d.draw(legendAxes);
		 yMinval.draw(g2d);
		 for(LegendLabel lab : xLabels)
		 {
			 lab.draw(g2d);
		 }
		 yMaxval.draw(g2d);
		 
		 //Plot the Bars in the LegendLineArea
		 makePlot(data, g2d, legendBarArea, colors,legendbarwidth,legendbaroffset);
		 if(legendZeroLine != null)
		 {
			 g2d.draw(legendZeroLine);
		 }
		 labelForDataSet.draw(g2d);
		 //restore the original graphics context
		 g2d.translate(-legendFrame.getX(), -legendFrame.getY());
		 g2d.setStroke(currentStroke);
		 g2d.setPaint(currentPaint);
	 }

	 /**
	  * 
	  * Layout a node with the given NodeData, in the given graphics context with the given colormap.
	  * @param data
	  * @param g2d
	  * @param colors
	  */
	 private void LayoutNode(NodeData data, SVGGraphics2D g2d, ColorMap colors)
	 {
		 //store the original paint and stroke properties
		 Paint currentPaint = g2d.getPaint();
		 Stroke currentStroke = g2d.getStroke();
		 g2d.setPaint(Color.black);
		 g2d.setStroke(new BasicStroke(2));
		 //plot the axes
		 g2d.draw(nodeAxes);
		 //plot the bars.
		  makePlot(data, g2d, barArea,colors,normalbarwidth,normalbaroffset);
		 if(zeroLine != null)
		 {
			 //g2d.setStroke(dashedStroke);
			 g2d.draw(zeroLine);			
		 }
		 g2d.setStroke(currentStroke);
		 g2d.setPaint(currentPaint);
		
	 }
	 /**
	  * Plot the lines using the data from a node.
	  * @param data - the data to use
	  * @param g2d - the context to plot in
	  * @param area - the area available for plotting
	  * @param colors - the colors to use.
	  */
	 private void makePlot(NodeData data, SVGGraphics2D g2d, Rectangle2D area, ColorMap colors, double barwidth, double baroffset)
	 {
		 //get the node data.
		 		 
		 Paint origPaint = g2d.getPaint();
		 Stroke currentStroke = g2d.getStroke();
		 g2d.setStroke(new BasicStroke(1));
		 //move to the plotting area
		 g2d.translate(area.getX(), area.getY());
//		 System.out.println("Translating to " + area.getX() + "/" + area.getY());
		 for(String name : sheetPos)
		 {
			 
			 Color linecolor = null;
			 Vector<Double> LineYValues = new Vector<Double>(); 
			 Vector<Comparable> lineHeaders = new Vector<Comparable>();
			 //if we have a ValueSetDataSet its rather easy.
			 if(data instanceof MultiArrayNodeData)
			 {
				 MultiArrayNodeData nd = (MultiArrayNodeData) data;
				 MultiArrayDataValue vsd = nd.getData(name);
				 if(vsd != null)
				 {
					 LineYValues = vsd.getEntryData();
					 linecolor = colors.getColor(name);
					 lineHeaders = ((MultiArrayDataSet)data.getDataSet()).getHeadersForSheet(name);				 				 
				 }	
			 }
			 //if it is an Item Dataset, we have to select a color, so we use the default color of the map.
			 if(data instanceof ArrayNodeData)
			 {
				 ArrayNodeData itemdata = (ArrayNodeData)data;
				 LineYValues = new Vector<Double>();
				 lineHeaders = itemdata.getDataSet().getHeaders();
				 for(int i = 0; i < itemdata.getValueCount(); i++)
				 {
					 Comparable Value = itemdata.getData(i).getValue();
					 if(Value != null)
					 {
						 LineYValues.add((Double)Value);
					 }
					 else
					 {
						 LineYValues.add(0.);
					 }
				 }
				 linecolor = colors.getDefaultColor();

			 }
			 g2d.setPaint(linecolor);
			 plotBars(name,lineHeaders,LineYValues,area,g2d,barwidth, baroffset);
		 }
		 g2d.translate(-area.getX(), -area.getY());
		 g2d.setStroke(currentStroke);
		 g2d.setPaint(origPaint);
	 }
	  
	 /**
	  * Plot the Items defined by the x and y values with a shape determined by the Sheetname on the provided graphics context.
	  * @param SheetName The name of the sheet used for this set of bars.
	  * @param xvalues the x positions of the bars
	  * @param yvalues the 
	  * @param area
	  * @param g2d
	  * @return
	  */
	 private void plotBars(String SheetName, Vector<Comparable> xvalues, Vector<Double> yvalues, Rectangle2D area, SVGGraphics2D g2d, double barwidth, double baroffset)
	 {
		 //Path2D result = new Path2D.Double();
		 for(int i = 0; i < xvalues.size(); i++)
		 {
			 if(yvalues.get(i) != null)
			 {
				 Double val = yvalues.get(i);
				 Double coord;
				 coord = itemxPositions.get(xvalues.get(i)).position;
				 Rectangle2D loc = getPlotArea(coord,val,area,barwidth,sheetPos.indexOf(SheetName), baroffset);				 
				 Paint currentpaint = g2d.getPaint();
				 g2d.fill(loc);				 
				 g2d.setPaint(Color.BLACK);				 
				 g2d.draw(loc);
				 g2d.setPaint(currentpaint);
			 }
		 }
	 }

	 
	 
	 /**
	  * Helper function to translate a x/y value pair to points in the plot area.
	  * @param xvalue
	  * @param yvalue
	  * @param width - plot area width
	  * @param height - plot area height
	  * @return
	  */
	 private Rectangle2D getPlotArea(double centerx, double yvalue, Rectangle2D area, double barwidth, double relpos, double baroffset)
	 {
		 Point2D zeropoint = getPlotPoint(centerx,0,area.getWidth(),area.getHeight());
		 Point2D toppoint = getPlotPoint(centerx, yvalue, area.getWidth(), area.getHeight());		 
		 double xstart = zeropoint.getX() + relpos * barwidth - baroffset;
		 if(yvalue < 0)
		 {
			 //if the y-value is smaller than zero, we have to plot below the zero line. Thus we have to
			 //start at the zeroline, and plot from there.
			return new Rectangle2D.Double(xstart,zeropoint.getY(),barwidth,toppoint.getY()-zeropoint.getY()); 
		 }
		 else
		 {
			 return new Rectangle2D.Double(xstart, toppoint.getY(), barwidth, zeropoint.getY()-toppoint.getY());
		 }
	 }

	 /**
	  * Helper function to translate a x/y value pair to points in the plot area.
	  * @param xvalue
	  * @param yvalue
	  * @param width - plot area width
	  * @param height - plot area height
	  * @return
	  */
	 private Point2D getPlotPoint(double xvalue, double yvalue, double width, double height)
	 {
		 double xval = ((xvalue - xminval) / xrange) * width;
		 double yval = height - ((yvalue - yminval) / yrange) * height;
		 Point2D res = new Point2D.Double(xval,yval);
		 return res;

	 }
	 /*
	  * (non-Javadoc)
	  * @see idare.imagenode.Interfaces.Layout.ContainerLayout#LayoutDataForNode(idare.imagenode.Interfaces.DataSets.NodeData, org.apache.batik.svggen.SVGGraphics2D, boolean, idare.imagenode.internal.ColorManagement.ColorMap)
	  */
	 @Override
	 public void LayoutDataForNode(NodeData data, SVGGraphics2D context,
			 boolean Legend, ColorMap colors) {
		 if(Legend)
		 {
			 LayoutLegendNode(data,context, colors);
		 }
		 else
		 {
			 LayoutNode(data,context, colors);
		 }		
	 }
	 
	 /**
	  * A small helper struct, that combines a Double for the position and a String for the label.
	  * @author Thomas Pfau
	  */
	 private class LabelAndPosition implements Serializable
	 {

		private static final long serialVersionUID = 1L;
		public String label;
		 public Double position;
		 /**
		  * Basic Constructor defining the two fields.
		  * @param Position
		  * @param Label
		  */
		 public LabelAndPosition(Double Position, String Label)
		 {
			 this.label = Label;
			 this.position = Position;
		 }
		 
	 }

	@Override
	public void updateLabel(String DatasetLabel) {
		// TODO Auto-generated method stub
		labelForDataSet.Label = DatasetLabel;
	}
	 
}

