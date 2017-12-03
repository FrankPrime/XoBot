
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import com.dridia.data.Data;
import com.dridia.methods.Methods;
import com.dridia.methods.SleepCondition;
import com.dridia.methods.bankingMethods;
import com.dridia.methods.fishingMethods;
import xobot.client.callback.listeners.MessageListener;
import xobot.client.callback.listeners.PaintListener;
import xobot.script.ActiveScript;
import xobot.script.Manifest;
import xobot.script.methods.NPCs;
import xobot.script.methods.Packets;
import xobot.script.methods.tabs.Inventory;
import xobot.script.methods.tabs.Skills;
import xobot.script.util.Time;
import xobot.script.util.Timer;
import xobot.script.wrappers.interactive.Item;
import xobot.script.wrappers.interactive.NPC;

import com.dridia.customListeners.Inventory.InventoryEvent;
import com.dridia.customListeners.Inventory.InventoryEventSource;
import com.dridia.customListeners.Inventory.InventoryListener;
import com.dridia.customListeners.EventDispatcher;

import javax.imageio.ImageIO;

@Manifest(authors = { "Dridia" }, name = "Dridia's Karambwan Fisher", version = 1.0, description = "Everything you need to make bank from Karambwans. Start with a \"small fishing net\" and a \"Karambwan vessel\". Start near a fishing spot.")
public class dridiasKarambwanFisher extends ActiveScript implements PaintListener, MessageListener, InventoryListener{

    public final Image img1 = getImage("https://i.imgur.com/7Le35eG.png");
    public Timer startTime;
    public int startXP = 0;

    private EventDispatcher eventDispatcher;

    @Override
    public boolean onStart() {
        if(Inventory.Contains(303) && Inventory.Contains(3157) || Inventory.Contains(303) && Inventory.Contains(3159)){
            startXP = Skills.getCurrentExp(Skills.FISHING);
            startTime = new Timer(System.currentTimeMillis());
            eventDispatcher = new EventDispatcher();
            eventDispatcher.addListener(this);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onStop() {
        System.out.println("Script stopped");
    }

    @Override
    public int loop() {
        if(fishingMethods.areFishing()){
            NPC nSpot = NPCs.getNearest(1236);
            if(nSpot != null){
                Methods.conditionalSleep(new SleepCondition() {
                    @Override
                    public boolean isValid() {
                        return !Inventory.Contains(3159);
                    }
                }, 6500);
            }
            return 100;
        }
        if(Inventory.Contains(317)){
            for(Item item : Inventory.getItems()) {
                if(item.getID() == 317) {
                    if(item != null){
                        Packets.sendAction(847, item.getID(), item.getSlot(), 3214);
                        Time.sleep(125, 150);
                    }
                }
            }
        }

        if(fishingMethods.shouldFishBig()){
            fishingMethods.fishBig();
        }else if(fishingMethods.shouldFishSmall()) {
            fishingMethods.fishSmall();
        }else if(bankingMethods.shouldBank()){
            bankingMethods.doBank();
        }

        return 100;
    }

    private final Color color = new Color(19, 197, 255);

    @Override
    public void repaint(Graphics g1) {
        Graphics2D g = (Graphics2D)g1;
        g.drawImage(img1, 6, 320, null);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString(startTime.toElapsedString(), 205, 400);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString("" + Data.fishCaught, 205, 430);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString((new StringBuilder("").append(Skills.getCurrentExp(Skills.FISHING) - startXP)).toString(), 205,
                460);
    }

    @Override
    public void MessageRecieved(String arg0, int arg1, String arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onInventoryChanged(InventoryEvent inventoryEvent){
        if (inventoryEvent.getNewItem() != null) {
            Item newid = inventoryEvent.getNewItem();

            if(newid != null) {
                if (newid.getID() == 3142) {
                    Data.fishCaught++;

                }
            }
        }
    }

    public Image getImage(String url) { // Adds image.
        try {
            return ImageIO.read(new URL(url));
        } catch(IOException e) {
            return null;
        }
    }

}

