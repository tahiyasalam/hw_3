package ttr.model.player;

import java.util.ArrayList;
import ttr.model.destinationCards.*;
import java.util.HashMap;

import ttr.model.destinationCards.Destination;
import ttr.model.destinationCards.Route;

public class ThomasTheTankEngine extends Player {

	public ThomasTheTankEngine(String name) {
		super(name);
	}

	public ThomasTheTankEngine() {
		super("ThomasTheTankEngine");
	}

	@Override
	public void makeMove() {
		// TODO Auto-generated method stub
		//use dijkstras on both destination cards
		//tabulate cards needed
		//pick cards until card values are fulfilled
		//when card value met, 
		//check for critical points
		//manhattan distance + longest piece
		
	}
	
	public int shortestPathcost(Destination from, Destination to){
		/* If same, just return false */
		if(from == to) return 0;
		
		/* Open and Closed lists (breadth first search) */
		HashMap<Destination, Integer> openList = new HashMap<Destination, Integer>();
		HashMap<Destination, Integer> closedList = new HashMap<Destination, Integer>();
		HashMap<Destination, Integer> path = new HashMap<Destination, Integer>();
		
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
			if(next == to) return closedList.get(next);
			
			/* Get all the neighbors of the next city that aren't on open or closed lists already */
			for(Destination neighbor : Routes.getInstance().getNeighbors(next)){
				if(closedList.containsKey(neighbor)) continue;
				
				/* get route between next and neighbor and see if better than neighbor's value */
				ArrayList<Route> routesToNeighbor = Routes.getInstance().getRoutes(next, neighbor);
				for(Route routeToNeighbor : routesToNeighbor){
					int newCost = closedList.get(next) + routeToNeighbor.getCost();
					
					if(openList.containsKey(neighbor)){	
						if(newCost < openList.get(neighbor)){
							openList.put(neighbor, newCost);
							path.put(neighbor, newCost);
						}
					}
					else{
						openList.put(neighbor, newCost);
						path.put(neighbor, newCost);
					}
				}
			}
		}
		
		return 0;
	}
}
