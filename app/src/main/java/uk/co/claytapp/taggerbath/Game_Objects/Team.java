package uk.co.claytapp.taggerbath.Game_Objects;

/**
 * Created by Sam on 08/03/2017.
 */

public class Team {

    String team;
    int score;

    public Team(String TEAM, int SCORE){
        team = TEAM;
        score = SCORE;

    }

    public String getTeam(){
        return team;
    }

    public int getScore(){
        return score;
    }
}
