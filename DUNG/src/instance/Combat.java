// Written originally by Jared Hawkins, and copied over from another project to here with some heavy edits. 

package instance;

import java.util.ArrayList;
import display.Display;
import characters.Entity;
import item.*;

public class Combat extends Instance {
	protected int victor; // Number of the team that has won this Combat
	protected boolean isActive = true; //is this combat still active?
	protected ArrayList<Entity> dead = new ArrayList<Entity>(); //holds all the dead people, for counting and looting
	protected ArrayList<Item> droppedLoot = new ArrayList<Item>(); //for clean up looting inventories.
	private boolean allDead = false; //changes to true if everyone dies at the same time and is an extra check at the end to prevent an infinite loop.
	private Entity currentEntity; //the Entity currently taking a turn.
	

	public void launch() {
		currentTeam = 0;
		int turnCount = 0;
		int whoToAtk = 0; // Index on the InitiativeList of who the current target is. This system should probably be replaced later, and currently is just for the AI and players in a poor format.
		int currentInitiative = -1; // Set to negative one due to how initiative loops around. Could be changed, but works nicely for now, and there was some reason I decided this was better.
		int lastInitiative = 0;
		RPGAction currentAction;

		Display.print("Entering Combat!\n");
		//Display.setInstance(this); // Updates the current instance. Marked out from a previous project, but something that might need to stay.
		//Display.print("Current location: " + /*environment +*/ ".\n");

		while (checkActive()) { 

			do { // Cycle through InitiativeList
				currentInitiative++;
				if (currentInitiative >= InitiativeList.size())
					currentInitiative = 0;
			} while (InitiativeList.get(currentInitiative).isAlive() == false);
			currentEntity = InitiativeList.get(currentInitiative);

			for (int i=0; i<team.size(); i++) // Find the team of the current Entity.
				if (team.get(i).contains(currentEntity)) 
					currentTeam = i;
			//Display.print("Team of current combatant: " + currentTeam + "\n");

			if ((currentInitiative < lastInitiative) || (turnCount == 0))  { // Tick at the end of the turn of the first person currently in the initiative (but not the first time)
				turnCount++;
				Display.print("Turn " + turnCount + ".\n");
			}

			// Action Discision Time
			currentAction = null; // In theory we should be overriding this no matter what in a moment, and clearing it like this is pointless.
			//RPG.gui.guiPrimary.actionPanel.clearAction(); // I do not like where this is currently, but I couldn't find a place to set it in the GUI that didn't clear it too early.

			if (currentEntity.isAI()) {
				// A very rudimentary and temporary AI. Random function to attack someone not on your team (or dead)
				ArrayList<Entity> potentialTargets = new ArrayList<Entity>(InitiativeList); // Does this load properly?
				for (Entity i: dead) //makes sure target is alive.
					potentialTargets.remove(i);
				for (Entity i: team.get(currentTeam)) //makes sure target isn't on same team.
					potentialTargets.remove(i);
				whoToAtk =  (int)Math.round((float)(Math.random()*(potentialTargets.size()-1))); //choose a random person to attack
				//Display.print("Random Target: " + whoToAtk + "\n");
				ArrayList<Entity> tempTargets = new ArrayList<Entity>();
				tempTargets.add(InitiativeList.get(whoToAtk));
				currentAction = new RPGAction("attack", tempTargets);
			}

			// Start Player Action Menu
			if (currentEntity.isAI() == false) { // For players to input.
				Display.println("Choose an action for " + currentEntity.getName() + ": \n");
				//refreshGUI();
				do {
					String tempActionType = Display.input("Input action (attack, special, run, inventory): ");
					Display.print(tempActionType);
					if ((tempActionType == "attack")||(tempActionType == "special")) {
						ArrayList<Entity> targets = new ArrayList<Entity>();
						targets.add(InitiativeList.get(0)); // TODO target selector. This is temporary and needs some work.
						currentAction = new RPGAction(tempActionType, targets);
					}
					else ;
				} while(currentAction.isValid() == false);
			}

			// Redirect to perform currentAction
			if (currentAction.getActionType() == "attack")
				Display.print(fight(currentEntity, currentAction.getTargets().get(0)));
			else if (currentAction.getActionType() == "special")
				; //TODO special
			else if (currentAction.getActionType() == "run") {
				if (run(currentEntity))
					currentInitiative--;
			}
			else if (currentAction.getActionType() == "inventory")
				accessInventory(currentEntity);

			checkDead();



			if (currentInitiative < lastInitiative) { // Tick at the end of the turn of the first person currently in the initiative (but not the first time)
				//gameTick(1); // Do we have a status effect system in the works? if not we can remove this.
				Display.print("Game ticks.\n");
				checkDead();
			}
			lastInitiative = currentInitiative;

			if (turnCount == 20) { //error catch so we can see what happened easier
				Display.print("Too many turns have happened. Exiting loop prematurely.\n");
				break; 
			}
			//Display.print(output);
		}
		endCombat();
		Display.print("End of combat.\n");
	}

	public String fight(Entity attacker, Entity defender) {

		String output = "";
		int damage, damageFinal;
		Weapon weapon;
		
		if (defender.isAlive()) {
			//damage = (int)(Math.random()*(attacker.getStatI(attacker.getEquippedItems()[4].getStatS("primaryStat"))/2)+(attacker.getStatI(attacker.getEquippedItems()[4].getStatS("primaryStat"))/2)) + attacker.getEquippedItems()[4].getAttack();
			weapon = (Weapon)attacker.getEquippedItems()[4];
			damage = (int)(Math.random()*weapon.getAttack())+1; // Damage will be from 1 to weapon attack.
			if (damage < 0) damage = 0;
			double damageReduction = (defender.getBlocking()+100)/100; // Move blocking up to entity?
			damageFinal = (int) (damage/damageReduction);
			defender.setHealth(defender.getHealth() - damageFinal);

			output += attacker.getName() + " strikes at " + defender.getName() + " with " + attacker.getEquippedItems()[4].getName() + " dealing " + damageFinal + " damage.\n";
			output += defender.getName() + " now has " + defender.getHealth() +"/"+ defender.getMaxHealth() + " health points.\n";

		}
		else output += defender.getName() + " is already dead.\n"; // like an error catch

		return output;
	}

	public boolean run(Entity runner) {
		//runner compare dex stats against those of people of other team, roll number, if beats them:
		int highestEnemyDex = 0;
		Display.print(runner.getName() + " attempts to run from combat... ");
		for (int i=0; i<team.size(); i++)
			for (int j=0; j<team.get(i).size(); j++) //should add something here to skip over the entire currentTeam
				if (currentTeam != i)
					if (team.get(i).get(j).getPerception() > highestEnemyDex) // TODO replace perception with something that makes more sense.
						highestEnemyDex = team.get(i).get(j).getPerception();
		if (runner.getPerception() >= highestEnemyDex) {
			Display.print(" Sucess!\n");
			InitiativeList.remove(runner);
			team.get(currentTeam).remove(runner);
			return true;
		}
		else {
			Display.print(" Failure.\n");
			return false;
		}
	}

	public void accessInventory(Entity current) {
		current.getInventory();
		//activate the typical (need it somewhere else) inventory menu, including the ability to use a consumable, and give it targeting access data for whatever the current even it happening, so it can allow choosing of targets.
	}

	public boolean checkActive() { // TODO redo something here, I think there might be issue.
		int deadCount = 0, deadTeams = 0;

		for (int i=0; i<team.size(); i++) {
			deadCount = 0;
			for (int j=0; j<team.get(i).size(); j++) {
				if (team.get(i).get(j).isAlive()) {
					victor = i;
					break;
				}
				else deadCount++;
				if (deadCount == team.get(i).size()) {
					deadTeams++;
				}
			}
		}
		if (deadTeams >= team.size()-1)
			isActive = false;
		if (deadTeams >= team.size())
			allDead = true;
		return isActive; 
	}

	public void checkDead() { // Checks to add dead to the dead list and declare them dead.
		for (int i=0; i<team.size(); i++) {
			for (int j=0; j<team.get(i).size(); j++) {
				if ((team.get(i).get(j).isAlive() == false) && (dead.contains(team.get(i).get(j)) == false)) {
					Display.print(team.get(i).get(j).getName() + " has died.\n");
					dead.add(team.get(i).get(j));
				}
			}
		}
	}

	public void endCombat() { //combat clean up
		if (allDead == false) {
			Display.print("Team " + victor + " is the victor.\n");
			Display.print("There are " + dead.size() + " dead.\n");

			int xpPreSplit = 0;
			for (Entity i: dead) // total up the xp value of the kills.
				if (team.get(victor).contains(i) == false)
					xpPreSplit += i.getLevel(); 

			int xpSplitWays = 0;
			for (Entity i: team.get(victor))
				if (i.isAlive()) 
					xpSplitWays++;

			int xpSplit = xpPreSplit/xpSplitWays;
			for (Entity i: team.get(victor))
				if (i.isAlive())
					i.addXP(xpSplit);

			Display.print(xpPreSplit + "xp is split between all " + xpSplitWays + " of the victors recieve who each recieve " + xpSplit + "xp.\n");
			//Display.print(dead.toString() + "\n");
			//Display.print("\n");

			// All dead loot goes in one pile that the victor can pick it up.
			for(Entity i: dead) 
				for(Item j: i.getInventory())
					droppedLoot.add(j);
			int moneyDroped = 0;
			for (Entity i: dead)
				moneyDroped += i.getMoney();

			for(Entity i: team.get(victor)) {
				if (i.isAlive()) {
					for(Item j: droppedLoot)
						i.getInventory().add(j);
					i.setMoney(i.getMoney() + moneyDroped); 
					Display.print(i.getName() + " picks up all of the loot, and " + moneyDroped + " coins.\n");
					break;
				}
			}
		}
		else {
			Display.print("All teams are dead.\nOops!\n");
		}
		//refresh GUI

	}

}
