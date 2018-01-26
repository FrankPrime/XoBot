import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import com.dridia.Actions.Combat;
import com.dridia.Actions.Teleport;
import com.dridia.GUI.controller.MainFrameController;
import xobot.client.callback.listeners.MessageListener;
import xobot.client.callback.listeners.PaintListener;
import xobot.client.input.Mouse;
import xobot.script.ActiveScript;
import xobot.script.Manifest;

import xobot.script.methods.*;
import xobot.script.util.Time;
import xobot.script.util.Timer;
import xobot.script.wrappers.interactive.Character;
import xobot.script.wrappers.interactive.Player;

import com.dridia.Data.Variables;
import com.dridia.Actions.Supplies;

import javax.imageio.ImageIO;

/**
 * Created by Dridia on 2018-01-16.
 */

@Manifest(authors = { "Dridia" }, name = "Dridia's Glacor Killer", version = 1.0, description = "Kill Glacors at Glacors Lair")
public class dridiaGlacorHunter extends ActiveScript implements PaintListener, MessageListener, Mouse{

    public Timer startTime;

    private final int paintToggleX = 493;
    private final int paintToggleY = 351;
    private final int paintToggleWidth = 40;
    private final int paintToggleHeight = 40;

    private boolean paintToggle = true;

    public final Image img1 = getImage("https://i.imgur.com/8AiQjha.png");
    public final Image img2 = getImage("https://vignette.wikia.nocookie.net/runescape2/images/f/f6/Options_button.png/revision/latest?cb=20150622123124");

    @Override
    public boolean onStart() {

        MainFrameController frame =  new MainFrameController(600, 500, true);
        frame.show();

        if(Settings.get(172) != 1){
            Packets.sendAction(169, 0, 0, 150); //Turn on Auto-Retaliate
        }

        while(frame.isVisible()) {
            Time.sleep(500);
        }

        /* Set all values that the user have chosen.*/

        //Get the prayer setting:
        Variables.USE_PIETY = frame.checkBoxPiety.isSelected();
        System.out.println("Piety: " + Variables.USE_PIETY);

        //Get the amount of potions to withdraw
        int sprRestore = (Integer)frame.withdrawSprRestSpinner.getValue();
        int sprAttack = (Integer)frame.withdrawSprAtkSpinner.getValue();
        int sprStrength = (Integer)frame.withdrawSprStrSpinner.getValue();
        int sprDefence = (Integer)frame.withdrawSprDefSpinner.getValue();

        Variables.SUPER_RESTORE_AMOUNT = sprRestore; //6 is Recommended
        Variables.SUPER_ATTACK_AMOUNT = sprAttack;  //2 is Recommended
        Variables.SUPER_STRENGTH_AMOUNT = sprStrength;//2 is Recommended
        Variables.SUPER_DEFENCE_AMOUNT = sprDefence; //2 is Recommended
        System.out.println("Super Restore Amount: " + Variables.SUPER_RESTORE_AMOUNT);
        System.out.println("Super Attack Amount: " + Variables.SUPER_ATTACK_AMOUNT);
        System.out.println("Super Strenght Amount: " + Variables.SUPER_STRENGTH_AMOUNT);
        System.out.println("Super Defence Amout: " + Variables.SUPER_DEFENCE_AMOUNT);


        //At what lvl to repot:
        Variables.SUPER_RESTORE_USEAT = (Integer)frame.useWhenSprRestSpinner.getValue();
        Variables.SUPER_ATTACK_USEAT = (Integer)frame.useWhenSprAtkSpinner.getValue();
        Variables.SUPER_STRENGTH_USEAT = (Integer)frame.useWhenSprStrSpinner.getValue();
        Variables.SUPER_DEFENCE_USEAT = (Integer)frame.useWhenSprDefSpinner.getValue();
        System.out.println("Restore UseAt: " + Variables.SUPER_RESTORE_USEAT);
        System.out.println("Attack UseAt: " + Variables.SUPER_ATTACK_USEAT);
        System.out.println("Strength UseAt: " + Variables.SUPER_STRENGTH_USEAT);
        System.out.println("Defence UseAt: " + Variables.SUPER_DEFENCE_USEAT);

        //MantaRay = 391
        //Get the foodID and at what hp to eat.
        Variables.FOOD_ID = Integer.parseInt(frame.txtFoodID.getText());
        Variables.EAT_AT_HP = (Integer)frame.eatAtSpinner.getValue();
        Variables.FOOD_AMOUNT = 28 - sprRestore - sprAttack - sprStrength - sprDefence; // The amount is 28 minus all the potions
        System.out.println("Food ID: " + Variables.FOOD_ID);
        System.out.println("Eat at hp: " + Variables.EAT_AT_HP);
        System.out.println("Food withdrawal: " + Variables.FOOD_AMOUNT);


        Variables.DROP_LIST = new ArrayList<>();
        //Add the drops to the DropList
        for(int i = 0; i < frame.lootTable.getModel().getSize();i++){
            int item = Integer.valueOf((String) frame.lootTable.getModel().getElementAt(i));
            Variables.DROP_LIST.add(item);
        }

        //Hardcoded loot:
        Variables.DROP_LIST.add(21790); //Glaiven boots
        Variables.DROP_LIST.add(21793); //Ragefire boots
        Variables.DROP_LIST.add(21787); //Steadfast boots
        Variables.DROP_LIST.add(21776); // Shards of armadyl

        startTime = new Timer(System.currentTimeMillis());

        return true;
    }

    @Override
    public void onStop() {
        System.out.println("Script stopped");
    }

    @Override
    public int loop() {
        /*
        * Bug 1: Sometimes get stuck looking at the Glacor.
        * Solution 1: Check if player has attacked once if Combat.Attack() is called. (to make sure it attacks)
        *
        * Bug 2: If you pick up Coins but has maxcash in bank, it enters a deadLock
        * Solution 2: When you get cash, add them to the pouch.
        * */

        if(!Supplies.shouldRefill()) {
            if(!Combat.isAtLair()){
                Teleport.teleportGlacorsLair();
            }
            if(Supplies.shouldEat()){
                Variables.STATUS = "Eat";
                Supplies.eatFood();
            }
            if (Supplies.prayerLow()) {
                Variables.STATUS = "Restore prayer";
                Supplies.restorePrayer();
            }
            if(!Supplies.shouldPotUp().equals("null")){
                Supplies.potUp(Supplies.shouldPotUp());
            }
            if(Combat.lootFound()) {
                Variables.STATUS = "Loot";
                Combat.pickUpLoot();
            }else {
                if (!Player.getMyPlayer().isInCombat()) {
                    if (Combat.canAttack()) {
                        Variables.STATUS = "Attack";
                        Combat.Attack();
                    }
                }
            }
        }else{
            Variables.STATUS = "Refill";
            Supplies.doRefill();
        }

        if(Players.getMyPlayer().getInteractingIndex() != -1 || Player.getMyPlayer().isInCombat()){
            Combat.turnOnPrayer();
        }else{
            Combat.turnOffPrayer();
        }

        return 80;
    }


    @Override
    public void repaint(Graphics g) {
        Character interactingCharacter = Players.getMyPlayer().getInteractingCharacter();
        if(interactingCharacter != null){
            Color c = new Color(0, 255, 0);
            interactingCharacter.getLocation().draw(g, c);
        }

        if(paintToggle) {
            Graphics2D g1 = (Graphics2D) g;
            g.drawImage(img1, 6, 332, null);
            g1.setColor(Color.WHITE);
            g1.setFont(new Font("Verdana", Font.BOLD, 14));
            g1.drawString("Time running: " + startTime.toElapsedString(), 10, 390);

            g1.setColor(Color.WHITE);
            g1.setFont(new Font("Verdana", Font.BOLD, 14));
            g1.drawString(Integer.toString(Variables.STEADS), 46, 462);
            g1.drawString(Integer.toString(Variables.RAGES), 106, 462);
            g1.drawString(Integer.toString(Variables.GLAIVEN), 166, 462);
            g1.drawString(Integer.toString(Variables.SHARDS), 220, 462);
        }else {
            Graphics2D g1 = (Graphics2D) g;
            g.drawImage(img2, paintToggleX, paintToggleY-10, null);
        }
    }

    @Override
    public void MessageRecieved(String arg0, int arg1, String arg2) {
        // TODO Auto-generated method stub

    }



    public Image getImage(String url) { // Adds image.
        try {
            return ImageIO.read(new URL(url));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void _mouseExited(MouseEvent mouseEvent) {

    }

    @Override
    public void _mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void _mouseMoved(MouseEvent mouseEvent) {

    }

    @Override
    public void _mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void _mouseClicked(MouseEvent mouseEvent) {
        if((mouseEvent.getX() > paintToggleX && mouseEvent.getX() < (paintToggleX + paintToggleWidth)) && (mouseEvent.getY() > paintToggleY) && mouseEvent.getY() < (paintToggleY + paintToggleHeight)){
            if(paintToggle) {
                paintToggle = false;
            }else{
                paintToggle = true;
            }
        }

    }

    @Override
    public void _mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void _mouseReleased(MouseEvent mouseEvent) {

    }
}

