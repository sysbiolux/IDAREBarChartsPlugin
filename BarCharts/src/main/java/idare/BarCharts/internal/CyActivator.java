package idare.BarCharts.internal;

import idare.BarCharts.BarChartProperties;
import idare.imagenode.IDAREImageNodeAppService;
import idare.imagenode.Interfaces.Plugin.IDAREPlugin;
import idare.imagenode.internal.IDAREService;

import java.util.Vector;

import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;
/**
 * This is a Simple implementation of an {@link IDAREPlugin}.
 * 
 * @author Thomas Pfau
 *
 */
public class CyActivator extends AbstractCyActivator implements IDAREPlugin{

	BundleContext context;
	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		IDAREImageNodeAppService appserv = getService(context, IDAREImageNodeAppService.class);
		appserv.registerPlugin(this);		
	}	
	
	public void shutDown()
	{			
		System.out.println("DeRegistering Bar Charts.");
		try{
			IDAREImageNodeAppService appserv = getService(context, IDAREImageNodeAppService.class);
			appserv.deRegisterPlugin(this);
		}
		catch(Exception e)
		{
			System.out.println("No App to deregister from.");
		}
	}

	@Override
	public Vector<IDAREService> getServices() {
		//We simply add a BarChartProperties Object.
		//This object will be used for visualisation.
		Vector<IDAREService> services = new Vector<IDAREService>();
		services.add(new BarChartProperties());
		return services;
	}
}
