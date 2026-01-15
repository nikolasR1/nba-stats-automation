package core;

import java.util.List;
import java.util.Scanner;

public class PlayerSelector {

    public static Player choosePlayer(List<Player> players) {
        Scanner scanner = new Scanner(System.in);

        // Display all players with an index
        System.out.println("Select a player from the list:");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            System.out.printf("%d: %s %s | Team: %s | Position: %s%n",
                    i + 1, p.getFirstName(), p.getLastName(), p.getTeamAbbr(), p.getPosition());
        }

        int selection = -1;
        while (selection < 1 || selection > players.size()) {
            System.out.print("Enter the number of the player you want: ");
            if (scanner.hasNextInt()) {
                selection = scanner.nextInt();
                if (selection < 1 || selection > players.size()) {
                    System.out.println("Invalid number, try again.");
                }
            } else {
                scanner.next(); // consume invalid input
                System.out.println("Invalid input, enter a number.");
            }
        }

        Player chosen = players.get(selection - 1);
        System.out.println("You selected: " + chosen.getFirstName() + " " + chosen.getLastName() +
                " | Team: " + chosen.getTeamAbbr());
        return chosen;
    }
}

