package uk.ac.hope.mcse.android.coursework.model.rewards;

public class FreeSide extends Reward{
    public String side;

    public FreeSide(String sideName) {
        side = sideName;
        reward_name = "Free "+side;
        reward_description = "Get a free side!";
        points_cost = 250;
    }
}
