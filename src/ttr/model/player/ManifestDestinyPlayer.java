package ttr.model.player;

import java.util.ArrayList;
import java.util.HashMap;

import org.omg.Messaging.SyncScopeHelper;

import ttr.model.destinationCards.Destination;
import ttr.model.destinationCards.DestinationTicket;
import ttr.model.destinationCards.Route;
import ttr.model.destinationCards.Routes;
import ttr.model.trainCards.TrainCardColor;

public class ManifestDestinyPlayer extends Player {
	boolean finishedARoute = false;
	
	public ManifestDestinyPlayer(String name) {
		super(name);
	}

	public ManifestDestinyPlayer() {
		super("ManifestDestinyPlayer");
	}
	
	//STATES:					ACTIONS:
	//0 = enough cards 			a1 = claim routes
	//1 = few cards				a2_r = pick random card 
	//2 = many good				a2_k = pick known card 
	//3 = many bad				a3 = pick destination

	//matrices for probabilities upon transition
	double prob_a1[][] = new double[][]{
		{0.1, 0, 0, 0},
		{0.5, 1, 0, 0},
		{0.2, 0, 0, 0},
		{0.2, 0, 0, 0},
	};
	double prob_a2_r[][] = new double[][]{
		{1, 0.1, 0.5, 0.02},
		{0, 0.5, 0, 0},
		{0, 0.2, 0.5, 0.2},
		{0, 0.2, 0, 0.78},
	};
	double prob_a2_k[][] = new double[][]{
		{1, 0.15, 0.6, 0.05},
		{0, 0.5, 0, 0},
		{0, 0.18, 0.4, 0.25},
		{0, 0.17, 0, 0.7},
	};
	double prob_a3[][] = new double[][]{
		{1, 0, 0.5, 0.33},
		{0, 1, 0, 0},
		{0, 0, 0.5, 0.34},
		{0, 0, 0, 0.33},
	};

	
	//matrices for rewards upon transition
	double rew_a1[][] = new double[][]{
		{10, 0, 0, 0},
		{5, 3, 0, 0},
		{8, 0, 0, 0},
		{5, 0, 0, 0},
	};
	double rew_a2_r[][] = new double[][]{
		{5, 7, 10, 8},
		{0, 2, 0, 0},
		{0, 5, 7, 6},
		{0, 2, 0, 2},
	};
	double rew_a2_k[][] = new double[][]{
		{5, 7, 10, 8},
		{0, 2, 0, 0},
		{0, 5, 7, 6},
		{0, 2, 2, 2},
	};
	double rew_a3[][] = new double[][]{
		{3, 0, 5, 6},
		{0, -1, 0, 0},
		{0, 0, 1, 2},
		{0, 0, 0, -1},
	};
	
	double gamma = 0.5;

	/**
	 * MUST override the makeMove() method and implement it.
	 * */
	@Override
	public void makeMove(){
	
		ArrayList<DestinationTicket> destTickets = this.getDestinationTickets();
		ArrayList<Route> allRoutes = new ArrayList<Route>();
		
		//inserts all of the routes from destination tickets into allRoutes
		for(int i = 0; i < destTickets.size(); i++) {
			ArrayList<Destination> dest = shortestPathcost(destTickets.get(i).getTo(), destTickets.get(i).getFrom()); //gets to and from values from destination tickets and calculates shortest path
			for (int j = 0; j < dest.size()-1; j++ ) {
				ArrayList <Route> routes = Routes.getInstance().getRoutes(dest.get(j), dest.get(j+1)); //gets the routes from list of destinations
				for (int k = 0; k < routes.size(); k++)
					if (!allRoutes.contains(routes.get(k)) && routes.get(k).getOwner() == null)
						allRoutes.add(routes.get(k)); //adds all the routes from 
			}
		}

		//this is used in deciding whether we need more destination tickets
		int initialSize = destTickets.size();
		int costOfTickets = 0;
		
		for (int i = 0; i < destTickets.size(); i++) { //sum value of all destination tickets
			costOfTickets += destTickets.get(i).getValue();
		}	
		
		//Determine what state we are in
		boolean enough = false;
		boolean few = false;
		boolean many_good = false;
		boolean many_bad = false;
		int s = 1; //value of current state: default to few cards
		
		//checks to see if enough train cards to buy a route
		for (int i = 0; i < allRoutes.size(); i++) {
			if(getNumTrainCardsByColor(allRoutes.get(i).getColor()) >= allRoutes.get(i).getCost()) {
				enough = true;
				s = 0;
				break;
			}
		}
		
		HashMap<TrainCardColor, Integer> cardColors = new HashMap<TrainCardColor, Integer>();
		
		//maps colors to number of cards needed
		for (int j = 0; j < allRoutes.size(); j++) { 
			if (cardColors.containsKey(allRoutes.get(j))) 
				cardColors.put(allRoutes.get(j).getColor(), cardColors.get(allRoutes.get(j)) + allRoutes.get(j).getCost());
			else
				cardColors.put(allRoutes.get(j).getColor(), allRoutes.get(j).getCost());
		}
		
		int sum = 0;
		
		//gets the exact number of train cards needed by color
		for (TrainCardColor color : cardColors.keySet()) {
			if (this.getNumTrainCardsByColor(color) <= cardColors.get(color))
				sum += this.getNumTrainCardsByColor(color);
			else
				sum += cardColors.get(color);
		}
		
		//checks to see if many good
		if(this.getHand().size() > 10) {
			if(sum > 10) {
				many_good = true;
				s = 2;
			}
			else {
				many_bad = true;
				s = 3;
			}
		}
			
		//checks to see if few cards
		if(this.getHand().size() < 11) {
			few = true;
			s = 1;
		}
		
		if(!enough && !many_good && !many_bad)
			few = true;
		
		
		//passing through to prob/reward function ---> just an example
		int s_prime = 0;
		probability(s, "a1", s_prime);
		reward(s, "a1", s_prime);
	
		int future_index = 0;
		
		//evaluates between picking rainbow card, designated color card, or from deck
		for (int k = 0; k < getFaceUpCards().size(); k++ ) {
			if (getFaceUpCards().get(k).getColor().toString().equals("rainbow")) {
				future_index = k+1;
			}
		}
		
		if(future_index == 0) {
			for( int k = 0; k < getFaceUpCards().size(); k++ ) {
				if (cardColors.containsKey(getFaceUpCards().get(k).getColor())) {
					future_index = k+1;
				}
			}
		}
		
		int newSize = this.getDestinationTickets().size();
		if(newSize != initialSize){
			this.finishedARoute = true;
		}
		
		//final check for whether we will draw more destination cards
		if(finishedARoute && initialSize < 3 && costOfTickets < this.getNumTrainPieces()){
			super.drawDestinationTickets();
		}


		
	}

	public ArrayList<String> criticalPoints () {
		ArrayList<String> criticalPoints = new ArrayList<String>();
		criticalPoints.add("Boston");
		criticalPoints.add("LasVegas");
		criticalPoints.add("LosAngeles");
		criticalPoints.add("Miami");
		criticalPoints.add("NewYork");
		criticalPoints.add("Portland");
		criticalPoints.add("SanFrancisco");
		criticalPoints.add("Vancouver");

		return criticalPoints;
	}




	//testing dijkstra's
	public void itinerary() {
		for (DestinationTicket ticket : this.getDestinationTickets()){			
			System.out.println(shortestPathcost(ticket.getFrom(), ticket.getTo()).toString());
		}


	}





	public ArrayList<Destination> shortestPathcost(Destination from, Destination to){

		/* If same, just return false */
		ArrayList<Destination> shortestPath = new ArrayList<Destination>();
		if(from == to) return shortestPath;

		/* Open and Closed lists (breadth first search) */
		HashMap<Destination, Integer> openList = new HashMap<Destination, Integer>();
		HashMap<Destination, Integer> closedList = new HashMap<Destination, Integer>();
		HashMap<Destination, Destination> parent = new HashMap<Destination, Destination>();

		openList.put(from, 0);

		while(openList.size() > 0){

			/* Pop something off the open list, if destination then return true */
			Destination next = null;
			int minCost = 9999;
			for(Destination key : openList.keySet()){
				if(openList.get(key) < minCost){
					next = key;
					minCost = openList.get(key);
				}

			}

			/* Take it off the open list and put on the closed list */
			openList.remove(next);
			closedList.put(next, minCost);

			/* If this is the destination, then return!!!! */
			if(next == to) break;

			/* Get all the neighbors of the next city that aren't on open or closed lists already */
			for(Destination neighbor : Routes.getInstance().getNeighbors(next)){
				if(closedList.containsKey(neighbor)) continue;

				/* get route between next and neighbor and see if better than neighbor's value */
				ArrayList<Route> routesToNeighbor = Routes.getInstance().getRoutes(next, neighbor);
				for(Route routeToNeighbor : routesToNeighbor){
					int newCost;
					if(this.getPlayerClaimedRoutes().contains(routeToNeighbor)){
						newCost = closedList.get(next);
					}
					else newCost = closedList.get(next) + routeToNeighbor.getCost();

					if(Routes.getInstance().ownsRoute(this, routeToNeighbor) | !Routes.getInstance().isRouteClaimed(routeToNeighbor)){	
						if(openList.containsKey(neighbor)){	
							if(newCost < openList.get(neighbor)){
								openList.put(neighbor, newCost);
								parent.put(neighbor, next);
							}
						}
						else{
							openList.put(neighbor, newCost);
							parent.put(neighbor, next);
						}
					}

				}
			}
		}
		/*Closed list and parent are complete, retrace path*/

		Destination current = to;
		while(current != from){
			shortestPath.add(current);
			current = parent.get(current);		
		}
		shortestPath.add(from); //list is reversed but we don't carrrre

		return shortestPath;
	}
	
	public double probability(int s, String a, int s_prime) {
		
		//STATES:					ACTIONS:
		//0 = enough cards 			a1 = claim routes
		//1 = few cards				a2_r = pick random card 
		//2 = many good				a2_k = pick known card 
		//3 = many bad				a3 = pick destination
		
		if(a.equals("a1")) {
			return prob_a1[s][s_prime];
		}
		
		else if(a.equals("a2_r")) {
			return prob_a2_r[s][s_prime];
		}
		
		else if(a.equals("a2_k")) {
			return prob_a2_k[s][s_prime];
		}
		
		else if(a.equals("a3")) {
			return prob_a3[s][s_prime];
		}
		else 
			return 0;
	}
	
	public double reward(int s, String a, int s_prime) {
		
		//STATES:					ACTIONS:
		//0 = enough cards 			a1 = claim routes
		//1 = few cards				a2_r = pick random card 
		//2 = many good				a2_k = pick known card 
		//3 = many bad				a3 = pick destination
		
		if(a.equals("a1")) {
			return rew_a1[s][s_prime];
		}
		
		else if(a.equals("a2_r")) {
			return rew_a2_r[s][s_prime];
		}
		
		else if(a.equals("a2_k")) {
			return rew_a2_k[s][s_prime];
		}
		
		else if(a.equals("a3")) {
			return rew_a3[s][s_prime];
		}
		else 
			return 0;
	}
	
	//map specific states to values <state, value>
	HashMap<Integer, Double> value = new HashMap<Integer, Double>();
	
	//call this function before beginning to quality checking
	public void initValue(HashMap<Integer, Integer> val) {
		val.put(0, 10);
		val.put(1, 10);
		val.put(2, 10);
		val.put(3, 10);
	}

	public String value(int s) {
		String max = "";
		double currMax = 0;
		if (quality(s, "a1") > quality(s, "a2")) { // find the max between taking action 1 and action 2 
			max = "a1";
			currMax = quality(s, "a1");
		}
		else {
			max = "a2";
			currMax = quality(s, "a2");
		}
		if(quality(s, "a3") > quality(s, max)) {//find the max between previous max and taking action 3
			max = "a3";
			currMax = quality(s, "a3");
		}
		
		value.put(s, currMax); //replace value with the update max for given state
		
		return max;
	}

	public double quality(int s, String a) {

		double q = 0;
		
		for(int sp = 0; sp < 4; sp ++) {				
			q += probability(s, a, sp) * (reward(s, a, sp) + gamma*value.get(sp)); //calculate the Q based on the weight of the rewards and values
		}
		return q;
	}

	
	
}
