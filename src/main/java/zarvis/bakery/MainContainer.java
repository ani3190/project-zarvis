package zarvis.bakery;
import java.util.ArrayList;
import java.util.List;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import javafx.application.Application;
import zarvis.bakery.Gui.MainApp;
import zarvis.bakery.agents.BakeryAgent;
import zarvis.bakery.agents.CustomerAgent;
import zarvis.bakery.agents.KneedingMachineAgent;
import zarvis.bakery.agents.OvenAgent;
import zarvis.bakery.agents.PreparationTableAgent;
import zarvis.bakery.agents.TruckAgent;
import zarvis.bakery.agents.manager.OvenManager;
import zarvis.bakery.agents.manager.PreparationTableManager;
import zarvis.bakery.agents.manager.KneedingMachineManager;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.BakeryJsonWrapper;
import zarvis.bakery.models.Customer;
import zarvis.bakery.models.DoughPrepTable;
import zarvis.bakery.models.KneedingMachine;
import zarvis.bakery.models.Oven;
import zarvis.bakery.models.Truck;
import zarvis.bakery.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class MainContainer {
	public static void main(String[] args) {
		
		try {
			//Delay for 5 seconds to wait for all to initialize
			long delay = 2000;
			long globalStartTime = System.currentTimeMillis() + delay;
			
			Logger logger = LoggerFactory.getLogger(BakeryAgent.class);
			
			List<CustomerAgent> customerAgentsList = new ArrayList<>();
			Runtime runtime = Runtime.instance();
			runtime.setCloseVM(true);

			Properties properties = new ExtendedProperties();
			properties.setProperty(Profile.GUI, "true");

			ProfileImpl profileImpl = new ProfileImpl(properties);

			AgentContainer mainContainer = runtime.createMainContainer(profileImpl);
			
			BakeryJsonWrapper wrapper = Util.getWrapper();
			

			// create multiple bakery agents
			for (Bakery bakery : wrapper.getBakeries()) {
				mainContainer.acceptNewAgent(bakery.getGuid(), new BakeryAgent(bakery, globalStartTime)).start();
				for (KneedingMachine kneedingMachine : bakery.getKneading_machines()) {
					mainContainer.acceptNewAgent(kneedingMachine.getGuid(),
							new KneedingMachineAgent(bakery)).start();
				}
				mainContainer.acceptNewAgent("kneeding_machine_manager-" + bakery.getGuid(),new KneedingMachineManager(bakery)).start();
				
				for(DoughPrepTable prepaTable: bakery.getDough_prep_tables()){
					mainContainer.acceptNewAgent(prepaTable.getGuid()+"-"+bakery.getGuid(),
							new PreparationTableAgent(bakery)).start();
				}
				
				mainContainer.acceptNewAgent("preparationTableManager-"+bakery.getGuid(),
						new PreparationTableManager(bakery)).start();
				
				
				for (Oven ovenMachine : bakery.getOvens()) {
					mainContainer.acceptNewAgent(ovenMachine.getGuid() + "-" + bakery.getGuid(),new OvenAgent(bakery)).start();
				}
				
				mainContainer.acceptNewAgent("ovenManager-" + bakery.getGuid(),new OvenManager(bakery)).start();
				
				
				for (Truck truck: bakery.getTrucks()) {
					mainContainer.acceptNewAgent(truck.getGuid(),new TruckAgent(bakery)).start();
				}
				
				break;
			}

			
			// create multiple customer agents
			for (Customer customer : wrapper.getCustomers().subList(0, 1)) {
				CustomerAgent agent =  new CustomerAgent(customer,globalStartTime);
				customerAgentsList.add(agent);
				mainContainer.acceptNewAgent(customer.getGuid(), agent).start();
			}

			Runnable runnable = () -> Application.launch(MainApp.class);
			new Thread(runnable).start();
			
			while (true) {
				Thread.sleep(300000);
				boolean finished = true;
				for (CustomerAgent customerAgent : customerAgentsList) {
					if (!customerAgent.isFinished()) {
						finished = false;
					}
				}
				if (finished) {
					logger.info("All customers are done, exit the platform...");
					mainContainer.kill();
					break;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}