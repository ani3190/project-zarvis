package zarvis.bakery.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.Product;
import zarvis.bakery.models.Truck;
import zarvis.bakery.utils.ContentExtractor;
import zarvis.bakery.utils.NeiGraph;
import zarvis.bakery.utils.Util;

public class BakeryAgent extends TimeAgent {

	private static final long serialVersionUID = 1L;
//	private Logger logger = LoggerFactory.getLogger(BakeryAgent.class);
	private Bakery bakery;
	private List<Product> products;
	@SuppressWarnings("unused")
	private MessageTemplate orderTemplate;
	@SuppressWarnings("unused")
	private ACLMessage processingMsg;
	@SuppressWarnings("unused")
	private int sucess;
	@SuppressWarnings("unused")
	private int failure;
	@SuppressWarnings("unused")
	private boolean managingProduction;
	private boolean[] availableKnead;
	
	//Test
	private transient List<ContentExtractor> ordersList = new ArrayList<>();
	@SuppressWarnings("unused")
	private transient Map<ContentExtractor, Integer> todaysOrderMap = new HashMap<>();
	private transient List<ContentExtractor> todaysOrder = new ArrayList<>();
	private transient List<ContentExtractor> waitOrder = new ArrayList<>();
	private int[] todayGoals = new int[Util.getProductnames().size()];
	private int[] currentMadeAmounts = new int[Util.getProductnames().size()];
	private int[] currentOrderAmounts = new int[Util.getProductnames().size()];
	//private ContentExtractor currentCE;
	Map<String,Integer> currentOrderProducts = new HashMap<>();
	
	//Other time management
	private long lastHourChecked = 0;
	
	private int step = 0;
	
	private boolean[] isTruckAvailable;
	private NeiGraph neig;
	private DFAgentDescription[] trucks;
	private boolean talkToTruck = false;
	private String noBoxes = "";
	private String customer2 = "";
	private String guid2 = "";
	private List<String> toBeDelivered = new ArrayList<>();
 
	public BakeryAgent(Bakery bakery, long globalStartTime) {
		super(globalStartTime);
		this.bakery = bakery;
		this.products = bakery.getProducts();
		this.sucess = 0;
		this.failure = 0;
		this.managingProduction = false;
		this.availableKnead = new boolean[1];
		Arrays.fill(todayGoals, 0);
		Arrays.fill(currentMadeAmounts,0);
		Arrays.fill(currentOrderAmounts,0);
		Arrays.fill(availableKnead, true);
		if (this.products.size() > 0) {
			Collections.sort(this.products, new Comparator<Product>() {
				public int compare(final Product object1, final Product object2) {
					return object1.getGuid().compareTo(object2.getGuid());
				}
			});
		}
		
		
		
		setNeig(Util.InitializeGraph());
		
//		for (Product p: this.products) {
//			System.out.println(p.getGuid());
//		}
		
	}

	@Override
	protected void setup() {
		this.bakery.setAid(getAID());
		Util.registerInYellowPage(this, "BakeryService", bakery.getGuid());
		setTrucks(Util.searchInYellowPage(this, "TruckAgent", bakery.getGuid()));
		ParallelBehaviour pal = new ParallelBehaviour();
		this.isTruckAvailable = new boolean[bakery.getTrucks().size()];
		Arrays.fill(isTruckAvailable, true);
		
		pal.addSubBehaviour(new ReceiveOrder());
		pal.addSubBehaviour(new CheckTime());
		pal.addSubBehaviour(new ManageProduction());
		pal.addSubBehaviour(new OrderFinishListener());
		pal.addSubBehaviour(new TalkToTruck());
		pal.addSubBehaviour(new TruckReturn());
		addBehaviour(pal);
	}

	protected void takeDown() {
	}
	
	private class ReceiveOrder extends CyclicBehaviour{
		private static final long serialVersionUID = 1L;
		private MessageTemplate receviveOrderTemplate = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CFP),
				MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));

		public void action(){
			String orders;
			String price;
			ContentExtractor currentOrder;
			
			ACLMessage data = myAgent.receive(receviveOrderTemplate);
			if( data != null){
				UpdateTime();
    			if(data.getPerformative() == ACLMessage.CFP){
    				ACLMessage msg = data;
	    			orders = msg.getContent();
//	    			System.out.println(bakery.getGuid() + " [BAKERY] Order: " + orders);
	    			
	    			String[] splitOrders = orders.split(";");
	    			
	    			for (String o: splitOrders) {
	    				currentOrder = new ContentExtractor(o);
	    				if(checkOrders(currentOrder)){
							price = getPrice(currentOrder);
							System.out.println(bakery.getGuid() + " [BAKERY] Order: " + orders + " price " + price);
							Util.sendReply(myAgent, msg, ACLMessage.PROPOSE, price);						
						}
						else{
							Util.sendReply(myAgent, msg, ACLMessage.REJECT_PROPOSAL, orders);
						}		
	    			}
    			}
    			if(data.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
    				orders = data.getContent();
    				String[] splitOrders = orders.split(";");
    				for (String o: splitOrders) {
    					ContentExtractor extractor = new ContentExtractor(o);
        				ordersList.add(extractor);
        				ordersList.sort(Comparator.comparing(ContentExtractor::getDeliveryTime));
        				System.out.println(bakery.getGuid() + " [BAKERY] Order List size: " + ordersList.size());
        				if(extractor.getDeliveryDay()==daysElapsed){
        					addBehaviour(new UpdateOrder(extractor));
        				}
        				Util.sendReply(myAgent, data, ACLMessage.CONFIRM, extractor.getDeliveryDateString());
    				}
  
    			}
    		} else {
    			UpdateTime();
    			block();
    		}
		}
    	
		@SuppressWarnings("unused")
		public Map<String, String> extractOrder(String orders){
			String [] orderArray;
			Map<String,String> ordersMap = new HashMap<>();
			orders = orders.substring(1, orders.length()-1);
			orderArray = orders.split(",");
			for(String order : orderArray)
			{
			    String[] entry = order.split("="); 
			    ordersMap.put(entry[0].trim(), entry[1].trim());
			}
			return ordersMap;
			
		}
		
		public boolean checkOrders(ContentExtractor orderExtractor) {
			//Many criteria, return true for now
			//Invalid delivery date
			UpdateTime();
			long timeToFulfill = ((orderExtractor.getDeliveryDay()-1)*24 + orderExtractor.getDeliveryHour()) - totalHoursElapsed;
//			System.out.println(orderExtractor.getDeliveryDay()*24 + orderExtractor.getDeliveryHour());
//			System.out.println(totalHoursElapsed);
			if (timeToFulfill < 0) {
//				System.out.println("Can't do it!");
				return false;
			} else {
				return true;
			}
			
		}
		
		public String getPrice(ContentExtractor orderExtractor){
			
			double price = 0;
			for(Map.Entry<String, Integer> item: orderExtractor.getProducts().entrySet()){
				for(Product p: products) {
					if(p.getGuid().equals(item.getKey())) {
						price += item.getValue()*p.getSales_price();
					}
				}
			}
			price = Math.round(price * 100.0) / 100.0;
			return String.valueOf(price);
		}
	}
	
	private class CheckTime extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;
		private MessageTemplate dummyTemplate = MessageTemplate.MatchPerformative(CustomMessage.DUMMY);

		public void action() {
			ACLMessage dummyMsg = myAgent.receive(dummyTemplate);
			UpdateTime();
			if (totalHoursElapsed%24 == 0 && totalHoursElapsed != lastHourChecked) {
				lastHourChecked = totalHoursElapsed;
				addBehaviour(new setTodayOrder());
			} 
			if (dummyMsg == null) {
				block(millisLeft);
			}
			// Add todaysOrder
			
		}
	}
	
	public class setTodayOrder extends OneShotBehaviour{
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			UpdateTime();
			//Remaining orders are failed
			for (ContentExtractor cont: todaysOrder) {
				String cus = cont.getCustomer();
				String failOrderGuid = cont.getGuid();
				Util.sendMessage(myAgent, new AID(cus, AID.ISLOCALNAME), CustomMessage.FINISH_ORDER, failOrderGuid, "to-customer-finish-order");
			}
			todaysOrder.clear();
			Arrays.fill(currentMadeAmounts, 0);
			Arrays.fill(todayGoals, 0);
//			step = 0;
			for (int i = 0; i < ordersList.size(); i++) {
				ContentExtractor ce = ordersList.get(i);
				if (ce.getDeliveryDay() == daysElapsed) {
					todaysOrder.add(ce);
					Map<String,Integer> productAmounts = ce.getProducts();
					for (int j = 0; j < productAmounts.size(); j ++) {
						todayGoals[j] += productAmounts.get(Util.getProductnames().get(j));
					}
				}
			}
			Util.sendMessage(myAgent,
					new AID("kneeding_machine_manager-"+myAgent.getLocalName(), AID.ISLOCALNAME),
					CustomMessage.NEW_DAY,
					"",
					"new-day");
			Util.sendMessage(myAgent,
					new AID("ovenManager-"+myAgent.getLocalName(), AID.ISLOCALNAME),
					CustomMessage.NEW_DAY,
					"",
					"new-day");
			Util.sendMessage(myAgent,
					new AID("preparationTableManager-"+myAgent.getLocalName(), AID.ISLOCALNAME),
					CustomMessage.NEW_DAY,
					"",
					"new-day");
		}
		
	}
	
	public class UpdateOrder extends OneShotBehaviour {
		private static final long serialVersionUID = 1L;
		private ContentExtractor ce;
		public UpdateOrder(ContentExtractor ce) {
			this.ce = ce;
		}

		@Override
		public void action() {
			UpdateTime();
			todaysOrder.add(ce);
			Map<String,Integer> productAmounts = ce.getProducts();
			for (int j = 0; j < productAmounts.size(); j ++) {
				todayGoals[j] += productAmounts.get(Util.getProductnames().get(j));
			}
//			System.out.println(bakery.getGuid() + " [BAKERY] Todays Order out update:" + todaysOrder.size());
			todaysOrder.sort(Comparator.comparing(ContentExtractor::getDeliveryTime));
		}
	}
	
	public class ManageProduction extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;
		private int stuckCase1 = 0;
		private MessageTemplate avaiTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(CustomMessage.RESPOND_AVAILABILITY),
				MessageTemplate.MatchConversationId("kneading-availability"));
		private MessageTemplate kneadConfirmTemplate = MessageTemplate.MatchConversationId("kneading-order");
		private MessageTemplate finishTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(CustomMessage.FINISH_ORDER),
				MessageTemplate.MatchConversationId("FINISH"));
		private String customer;
		private String orderID;

		@Override
		public void action() {
			switch(step) {
			case 0:
				if (todaysOrder.size() > 0) {
					
					Util.sendMessage(myAgent,
							new AID("kneeding_machine_manager-"+myAgent.getLocalName(), AID.ISLOCALNAME),
							CustomMessage.INQUIRE_AVAILABILITY,
							"",
							"kneading-availability");
					stuckCase1 = 0;
					step = 1;
				} else {
					block(15*Util.MILLIS_PER_MIN);
				}
				break;
			case 1:
				ACLMessage avaiReply = myAgent.receive(avaiTemplate);
				if (avaiReply!=null) {
					stuckCase1 = 0;
					if (avaiReply.getContent().equals("A")) {
						ContentExtractor sendingCE = todaysOrder.get(0);

						customer = sendingCE.getCustomer();
						orderID = sendingCE.getGuid();
						String orderString = orderID+","+sendingCE.getProductString();
						Util.sendMessage(myAgent,
								new AID("kneeding_machine_manager-"+myAgent.getLocalName(), AID.ISLOCALNAME),
								CustomMessage.INFORM_ORDER,
								orderString,
								"kneading-order");
						step = 2;
					} else {
						step = 0;
					}
				} else {
					stuckCase1++;
					if (stuckCase1 > 2) {
						step = 0;
					}
					block(15*Util.MILLIS_PER_MIN);
				}
				break;
			case 2:
				ACLMessage orderReply = myAgent.receive(kneadConfirmTemplate);
				if (orderReply!=null && orderReply.getPerformative()==ACLMessage.CONFIRM) {
					waitOrder.add(todaysOrder.get(0));
					todaysOrder.remove(0);
					step = 0;
				} else if (orderReply!=null && orderReply.getPerformative()==ACLMessage.REFUSE) {
					step = 0;
				} else {
					block();
				}
				break;
			case 3:
				ACLMessage finishReply = myAgent.receive(finishTemplate);
				if (finishReply!=null) {
					String returnOrderGuid = finishReply.getContent();
					System.out.println(bakery.getGuid() + " [BAKERY] Finished order: " + returnOrderGuid);
					String deliveryMsg="";
					for (ContentExtractor ce : waitOrder) {
						if (ce.getGuid().equals(returnOrderGuid)) {
							deliveryMsg = ProductPackage(ce);
						}
					}
					Util.sendMessage(myAgent, new AID(customer, AID.ISLOCALNAME), CustomMessage.FINISH_ORDER, deliveryMsg, "to-customer-finish-order");
					step = 0;
				} else {
					block();
				}
				break;
			}
		}
	}
	
	private class OrderFinishListener extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;
		private MessageTemplate finishTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(CustomMessage.FINISH_ORDER),
				MessageTemplate.MatchConversationId("FINISH"));
		
		@Override
		public void action() {
			ACLMessage finishReply = myAgent.receive(finishTemplate);
			if (finishReply!=null) {
				String returnOrderGuid = finishReply.getContent();
				System.out.println(bakery.getGuid() + " [BAKERY] Finished order: " + returnOrderGuid + " orders left: " + todaysOrder.size() +"!!!!");
//				String deliveryMsg="";
				
				
				for (ContentExtractor ce : waitOrder) {
					if (ce.getGuid().equals(returnOrderGuid)) {
						noBoxes = ProductPackage(ce);
						customer2 = ce.getCustomer();
						guid2 = ce.getGuid();
						toBeDelivered.add(guid2+","+customer2);
						break;
					}
				}
				
				talkToTruck = true;
			} else {
				block();
			}
		}
	}
	
	private class TalkToTruck extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;
		private int step2 = 0;
		private int consideringTruck = 0;
		private String consideringDelivery = "";
		private MessageTemplate truckTemplate = 
				MessageTemplate.MatchConversationId("delivery-request");
		private int noTruck = 0;
		
		public void action() {
			switch(step2) {
			
			case 0:
				if (talkToTruck && toBeDelivered.size() > 0) {
					for (int i = 0; i < isTruckAvailable.length; i++) {
						if (isTruckAvailable[i] && Integer.parseInt(noBoxes) < bakery.getTrucks().get(i).getLoad_capacity()) {
							System.out.println(bakery.getGuid() + " [TRUCK] Request delivery to " + bakery.getTrucks().get(i).getGuid());
							consideringDelivery = toBeDelivered.get(0);
							Util.sendMessage(myAgent, new AID(bakery.getTrucks().get(i).getGuid(), AID.ISLOCALNAME),
									CustomMessage.REQUEST_DELIVERY, consideringDelivery, "delivery-request");
							consideringTruck = i;
							step2 = 1;
							noTruck = 0;
							break;
						}
					}
					block(15*Util.MILLIS_PER_MIN);
				} else if (toBeDelivered.size() <= 0) {
					talkToTruck = false;
				}
				break;
			case 1:
				ACLMessage truckReply = myAgent.receive(truckTemplate);
				if (truckReply != null) {
					if (truckReply.getPerformative() == ACLMessage.CONFIRM) {
						System.out.println(bakery.getGuid() + " [TRUCK] Confirmed");
						toBeDelivered.remove(0);
						if (toBeDelivered.size() == 0) {
							talkToTruck = false;
						}
						isTruckAvailable[consideringTruck] = false;
						step2 = 0;
					}
					
					else if (truckReply.getPerformative() == ACLMessage.REFUSE) {
						isTruckAvailable[consideringTruck] = false;
						step2 = 0;
					}
					
				} else {
					if (noTruck > 2) {
						System.out.println(bakery.getGuid() + " [TRUCK] No Available Truck!");
						step2 = 0;
					}
					
					noTruck++;
					block(15*Util.MILLIS_PER_MIN);
				}
				break;
			}
		}
	}
	
	private class TruckReturn extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;
		private MessageTemplate truckReturnTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(CustomMessage.HAS_RETURNED),
				MessageTemplate.MatchConversationId("truck-return"));

		@Override
		public void action() {
			ACLMessage truckReturn = myAgent.receive(truckReturnTemplate);
			if (truckReturn!=null) {
				for (int i = 0; i < isTruckAvailable.length; i++ ) {
					if (bakery.getTrucks().get(i).getGuid().equals(truckReturn.getContent())) {
						System.out.println(bakery.getGuid() + " [TRUCK] " + bakery.getTrucks().get(i).getGuid() + " returned");
						isTruckAvailable[i] = true;
						break;
					}
				}
			} else {
				block();
			}
			
		}
	}
	
	private String ProductPackage(ContentExtractor ce) {
		int noBoxes = 0;
		
		Map<String, Integer> productList = ce.getProducts();
		for (Map.Entry<String,Integer> P : productList.entrySet()) {
			for (Product p : bakery.getProducts()){
				if (p.getGuid().equals(P.getKey())) {
					int noProducts = P.getValue();
					double breadPerBox = (double) p.getBreads_per_box();
					noBoxes += (int) Math.ceil(noProducts/breadPerBox);
				}
			}
		}
		@SuppressWarnings("unused")
		String truckGuid = "";
		for (Truck t: bakery.getTrucks()) {
			if (t.getLoad_capacity() >= noBoxes) {
				truckGuid = t.getGuid();
				break;
			}
		}
		String returnMsg=Integer.toString(noBoxes);
		return returnMsg;
	}

	public NeiGraph getNeig() {
		return neig;
	}

	public void setNeig(NeiGraph neig) {
		this.neig = neig;
	}

	public DFAgentDescription[] getTrucks() {
		return trucks;
	}

	public void setTrucks(DFAgentDescription[] trucks) {
		this.trucks = trucks;
	}
	
	
	
}
