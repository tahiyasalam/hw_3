package ttr.model.player;

import java.util.HashMap;

public class TestMDP {

	//STATES:					ACTIONS:
	//0 = enough cards 			a1 = claim routes
	//1 = few cards				a2_r = pick random card 
	//2 = many good				a2_k = pick known card 
	//3 = many bad				a3 = pick destination

	//matrices for probabilities upon transition
	static double prob_a1[][] = new double[][]{
		{0.1, 0, 0, 0},
		{0.5, 1, 0, 0},
		{0.2, 0, 0, 0},
		{0.2, 0, 0, 0},
	};
	static double prob_a2_r[][] = new double[][]{
		{1, 0.1, 0.5, 0.02},
		{0, 0.5, 0, 0},
		{0, 0.2, 0.5, 0.2},
		{0, 0.2, 0, 0.78},
	};
	static double prob_a2_k[][] = new double[][]{
		{1, 0.15, 0.6, 0.05},
		{0, 0.5, 0, 0},
		{0, 0.18, 0.4, 0.25},
		{0, 0.17, 0, 0.7},
	};
	static double prob_a3[][] = new double[][]{
		{1, 0, 0.5, 0.33},
		{0, 1, 0, 0},
		{0, 0, 0.5, 0.34},
		{0, 0, 0, 0.33},
	};


	//matrices for rewards upon transition
	static double rew_a1[][] = new double[][]{
		{10, 0, 0, 0},
		{5, 3, 0, 0},
		{8, 0, 0, 0},
		{5, 0, 0, 0},
	};
	static double rew_a2_r[][] = new double[][]{
		{5, 7, 10, 8},
		{0, 2, 0, 0},
		{0, 5, 7, 6},
		{0, 2, 0, 2},
	};
	static double rew_a2_k[][] = new double[][]{
		{5, 7, 10, 8},
		{0, 2, 0, 0},
		{0, 5, 7, 6},
		{0, 2, 2, 2},
	};
	static double rew_a3[][] = new double[][]{
		{3, 0, 5, 6},
		{0, -1, 0, 0},
		{0, 0, 1, 2},
		{0, 0, 0, -1},
	};

	static double gamma = 0.5;
	
	public static double probability(int s, String a, int s_prime) {

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

	public static double reward(int s, String a, int s_prime) {

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
	static HashMap<Integer, Double> value = new HashMap<Integer, Double>();

	//call this function before beginning to quality checking
	public static void initValue(HashMap<Integer, Double> value2) {
		value2.put(0, 10.0);
		value2.put(1, 10.0);
		value2.put(2, 10.0);
		value2.put(3, 10.0);
	}

	public static String value(int s) {
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

		return max; //returns maximum state associated with new current maximum
	}

	public static double quality(int s, String a) {

		double q = 0;

		for(int sp = 0; sp < 4; sp ++) {				
			q += probability(s, a, sp) * (reward(s, a, sp) + gamma*value.get(sp)); //calculate the Q based on the weight of the rewards and values
		}
		return q;
	}

	static String []actions = {"a1", "a2", "a3"};

	public static void MarkovDecisionProcess() {
	//	initValue(value);
		for (int i = 0; i < 50; i++) {
			for (int state = 0; state < 4; state++)
				for (String action : actions) {
					quality(state, action);
					value(state);
				}
		}
	}
	
	public static void main(String[] args) {
		initValue(value);
		System.out.println(value);
		MarkovDecisionProcess();
		System.out.println(value);
		}
	}
