package zarvis.bakery;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import zarvis.bakery.agents.BakeryAgent;
import zarvis.bakery.agents.CustomerAgent;
import zarvis.bakery.agents.KneedingMachineAgent;
import zarvis.bakery.agents.manager.KneedingMachineManager;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.BakeryJsonWrapper;
import zarvis.bakery.models.Customer;
import zarvis.bakery.utils.Util;
public class MainContainer {
	public static void main(String[] args) {
		try{
			
			Runtime runtime = Runtime.instance();
			runtime.setCloseVM(true);
			
			Properties properties=new ExtendedProperties();
			properties.setProperty(Profile.GUI,"true");
			
			ProfileImpl profileImpl=new ProfileImpl(properties);
			
			AgentContainer mainContainer = runtime.createMainContainer(profileImpl);
			
			BakeryJsonWrapper wrapper = Util.getWrapper();
			
			// create multiple bakery agents
			for (Bakery bakery : wrapper.getBakeries()) {
				mainContainer.acceptNewAgent(bakery.getName(), new BakeryAgent(bakery)).start();
				mainContainer.acceptNewAgent("kneeding_machine_manager-"+bakery.getGuid(), new KneedingMachineManager(bakery)).start();
				for (int i = 1; i <= bakery.getKneading_machines(); i++) {
					mainContainer.acceptNewAgent("kneeding_agent-"+bakery.getGuid()+"-"+i, new KneedingMachineAgent(bakery)).start();
				}
			}
			
			// create multiple customer agents
			for (Customer customer : wrapper.getCustomers()) {
				mainContainer.acceptNewAgent(customer.getName(), new CustomerAgent(customer)).start();
			}

			mainContainer.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}