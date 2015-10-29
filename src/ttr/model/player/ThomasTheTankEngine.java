package ttr.model.player;

import java.util.ArrayList;
import ttr.model.destinationCards.*;
import java.util.HashMap;

import ttr.model.trainCards.TrainCardColor;
import ttr.model.player.HumanPlayer;





public class ThomasTheTankEngine extends Player {
	boolean finishedARoute = false;
	public ThomasTheTankEngine(String name) {
		super(name);
	}

	public ThomasTheTankEngine() {
		super("ThomasTheTankEngine");
	}

	/**
	 * MUST override the makeMove() method and implement it.
	 * */
	@Override
	public void makeMove(){	
		ArrayList<DestinationTicket> destTickets = this.getDestinationTickets();
		ArrayList<Route> allRoutes = new ArrayList<Route>();
		
		int initialSize = destTickets.size();
		int costOfTickets = 0;
		
		for (int i = 0; i < destTickets.size(); i++) { //sum value of all destination tickets
			costOfTickets += destTickets.get(i).getValue();
		}
		
		//Decide whether we will draw more destination cards
		if(finishedARoute && initialSize < 3 && costOfTickets < this.getNumTrainPieces()){
			//do we still have a lot of trains
			
			//do we have a lot of cards in our deck that we could use???? O.o
			
			//do we have enough train pieces to finish our routes???? O__o
			
			finishedARoute = false;
			super.drawDestinationTickets();
		}
		
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

		boolean claimed = false;
		//checks to see if enough cars to buy route depending on whether or not on critical points list
		for(int i = 0; i < allRoutes.size(); i++) {
			if(getNumTrainCardsByColor(allRoutes.get(i).getColor()) >= allRoutes.get(i).getCost()) {
				if(criticalPoints().contains(allRoutes.get(i).getDest1().toString()) || criticalPoints().contains(allRoutes.get(i).getDest2().toString())) {
					super.claimRoute(allRoutes.get(i), allRoutes.get(i).getColor());
					claimed = true;
				}
			}
		}

		//checks to see if enough cars to buy a route and if so, buys route of given color
		if(!claimed) {
			for(int i = 0; i < allRoutes.size(); i++) {
				if(getNumTrainCardsByColor(allRoutes.get(i).getColor()) >= allRoutes.get(i).getCost()) {
					super.claimRoute(allRoutes.get(i), allRoutes.get(i).getColor());
					//System.out.println(allRoutes.get(i).getColor());
					claimed = true;
				}
			}
		}
		
		int newSize = this.getDestinationTickets().size();
		if(newSize != initialSize){
			this.finishedARoute = true;
		}
		
		//always pick rainbow card if available
		if(!claimed) {
			for (int s = 0; s < getFaceUpCards().size(); s++ ) {
				if (getFaceUpCards().get(s).getColor().toString().equals("rainbow")) {
					super.drawTrainCard(s+1);
					claimed = true;
				}
			}
		}

		//draws card that will lead to path being completed
		int difference = 6;
		if(!claimed) {
			for(int i = 1; i < difference; i++) {
				for(int j = 0; j < allRoutes.size(); j++) //checks difference between cost of route and number of cards needed to complete route & checks if card needed in face up cards
					for(int s = 0; s < getFaceUpCards().size(); s++){
						if( (allRoutes.get(j).getCost() - getNumTrainCardsByColor(allRoutes.get(j).getColor()) == i) && getFaceUpCards().get(s).getColor().equals(allRoutes.get(j).getColor())) {
							super.drawTrainCard(s+1);
							claimed = true;
						}	
					}
					
			}
		}

		if(!claimed){
			super.drawTrainCard(0);
			claimed = false;
		}
		/* NOTE: This is just an example, a player cannot actually do all three of these things in one turn. The simulator won't allow it */

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
}
