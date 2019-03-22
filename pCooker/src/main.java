import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;

import javax.swing.*;
import java.awt.*;

@ScriptManifest(category = Category.COOKING, name = "pCooker", author = "Peso", version = 1.3, description = "Cooks almost any food at four different locations!")
public class main extends AbstractScript {

    private Area cook;
    private Area toCook;
    private Area bank;
    private Area toBank;
    private String location;
    private String food;
    private String tool;
    private boolean isRunning;
    private boolean needToClick;
    private Timer timer;
    private int stopCooking;
    private int startCooking;
    private int startExp;
    private int max;

    @Override
    public void onStart() {
        createGUI();
        timer = new Timer();
        startCooking = getSkills().getRealLevel(Skill.COOKING);
        startExp = getSkills().getExperience(Skill.COOKING);
        max = Calculations.random(10,15);
    }

    @Override
    public int onLoop() {
        int rand = Calculations.random(1,max);

        if(isRunning) {
            if(getSkills().getRealLevel(Skill.COOKING) < stopCooking) {
                if (cook.contains(getLocalPlayer()) && getInventory().contains(food)) {
                    if(!getLocalPlayer().isAnimating()) {
                        sleep(1000,1800);
                        if(getLocalPlayer().isAnimating()) {
                            return Calculations.random(300,400);
                        }
                        if (getWidgets().getWidgetChild(270, 14) == null) {
                            getInventory().interact(food, "Use");
                            sleep(400, 800);
                            getGameObjects().closest(tool).interact("Use");
                            sleepUntil(() -> getWidgets().getWidgetChild(270, 14) != null, Calculations.random(3000, 5000));
                        } else {
                            if(!needToClick) {
                                if(getWidgets().getWidgetChild(270,12).interact()) {
                                    needToClick = true;
                                    return Calculations.random(500, 800);
                                }
                            } else {
                                getKeyboard().type(" ");
                                sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(2000, 3000));
                                if (rand < 5) {
                                    getMouse().moveMouseOutsideScreen();
                                }
                                if (getLocalPlayer().isAnimating()) {
                                    sleepUntil(() -> !getInventory().contains(food) || getDialogues().canContinue(), Calculations.random(85000, 95000));
                                    return Calculations.random(800, 1200);
                                }
                            }
                        }
                    } else {
                        return Calculations.random(800,900);
                    }
                } else if (!cook.contains(getLocalPlayer()) && getInventory().contains(food)) {
                    if(location.equals("Lumbridge") && getLocalPlayer().getZ() != 0) {
                        Area stairCaseArea = new Area(3205,3209,3206,3210,2);
                        GameObject staircase = getGameObjects().closest(f -> f != null && f.getName().equals("Staircase") && f.getTile().getY() < 3215);
                        if (getLocalPlayer().distance(stairCaseArea.getNearestTile(getLocalPlayer())) < 4) {
                            log("" + staircase.getTile().getY());
                            staircase.interact("Climb-down");
                            sleep(2000, 3000);
                        } else {
                            getWalking().walk(stairCaseArea.getRandomTile());
                            sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(1000, 2000));
                            if (getLocalPlayer().isMoving()) {
                                sleepUntil(() -> !getLocalPlayer().isMoving() ||
                                        getLocalPlayer().distance(stairCaseArea.getNearestTile(getLocalPlayer())) < 4, Calculations.random(3000, 5000));
                            }
                        }
                    } else {
                        log("walking number 2");
                        getWalking().walk(toCook.getRandomTile());
                        sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(1000, 2000));
                        if (getLocalPlayer().isMoving()) {
                            sleepUntil(() -> !getLocalPlayer().isMoving() || cook.contains(getLocalPlayer()), Calculations.random(3000, 5000));
                        }
                    }
                } else {
                    if (bank.contains(getLocalPlayer())) {
                        if (getBank().isOpen()) {
                            getBank().depositAllItems();
                            sleep(400,500);
                            if(getBank().contains(food)) {
                                getBank().withdrawAll(food);
                                sleep(400,500);
                                getBank().close();
                                sleep(300,500);
                            } else {
                                log("Out of raw food, stopping script.");
                                stop();
                            }
                        } else {
                            getBank().openClosest();
                            sleepUntil(() -> getBank().isOpen(), Calculations.random(3000,5000));
                        }
                    } else {
                        getWalking().walk(toBank.getRandomTile());
                        sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(1000, 2000));
                        if (getLocalPlayer().isMoving()) {
                            sleepUntil(() -> !getLocalPlayer().isMoving() || bank.contains(getLocalPlayer()), Calculations.random(3000, 5000));
                        }
                    }
                }
            } else {
                log("Cooking is now level " + getSkills().getRealLevel(Skill.COOKING) + ", stopping script.");
                stop();
            }
        }

        return Calculations.random(300,500);
    }

    @Override
    public void onPaint(Graphics g) {

        g.setColor(Color.CYAN.darker());
        g.fillRect(20,33,180,80);
        g.setColor(Color.GRAY);
        g.fillRect(25,38,170,70);
        g.setFont(new Font("Times New Roman", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Run time: " + timer.formatTime(),30, 55);
        g.drawString("Exp gained: " + (getSkills().getExperience(Skill.COOKING) - startExp)
                + "(" + timer.getHourlyRate(getSkills().getExperience(Skill.COOKING) - startExp) + ")",30, 70);
        g.drawString("Cooking level: " + getSkills().getRealLevel(Skill.COOKING) + "(" + (getSkills().getRealLevel(Skill.COOKING) - startCooking) + ")", 30, 85);
        g.drawString("by Peso", 30,100);
    }

    private void createGUI() {
        JFrame frame = new JFrame();
        frame.setTitle("pCooker");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(getClient().getInstance().getCanvas());
        frame.setPreferredSize(new Dimension(200,180));
        frame.getContentPane().setLayout(new BorderLayout());

        JPanel settingPanel = new JPanel();
        settingPanel.setLayout(new GridLayout(6, 0));

        JLabel foodLabel = new JLabel();
        foodLabel.setText("Food to cook: ");
        settingPanel.add(foodLabel);
        JTextField foodTextField = new JTextField();
        settingPanel.add(foodTextField);

        JLabel stopCookingLabel = new JLabel();
        stopCookingLabel.setText("Level to stop cooking at: ");
        settingPanel.add(stopCookingLabel);
        JTextField stopCookingTextField = new JTextField();
        settingPanel.add(stopCookingTextField);

        JLabel locationLabel = new JLabel();
        locationLabel.setText("Location: ");
        settingPanel.add(locationLabel);
        JComboBox<String> locationComboBox = new JComboBox<>(new String[] {
                "Lumbridge", "Al-Kharid", "Varrock West", "Varrock East"
        });
        settingPanel.add(locationComboBox);

        frame.getContentPane().add(settingPanel, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton button = new JButton();
        button.setText("Start");
        button.addActionListener(l -> {
            stopCooking = Integer.parseInt(stopCookingTextField.getText());
            food = (foodTextField.getText());
            location = (locationComboBox.getSelectedItem().toString());
            if(location == "Al-Kharid") {
                tool = "Range";
                cook = new Area(3271,3179,3273,3182,0);
                toCook = new Area(3272,3180,3272,3181,0);
                bank = new Area(3270,3170,3269,3165,0);
                toBank = new Area(3269,3166,3269,3169,0);
            } else if(location == "Varrock West") {
                tool = "Range";
                cook = new Area(3159,3427,3162,3429,0);
                toCook = new Area(3161,3428,3160,3428,0);
                bank = new Area(3185,3437,3183,3434,0);
                toBank = new Area(3184,3435,3184,3436,0);
            } else if(location == "Varrock East") {
                tool = "Range";
                cook = new Area(3239,3409,3236,3411,0);
                toCook = new Area(3237,3410,3238,3410,0);
                bank = new Area(3254,3420,3251,3422,0);
                toBank = new Area(3252,3421,3253,3421,0);
            } else if(location == "Lumbridge") {
                tool = "Cooking range";
                cook = new Area(3211,3216,3210,3213,0);
                toCook = new Area(3211,3214,3211,3215,0);
                bank = new Area(3207,3220,3210,3218,2);
                toBank = new Area(3209,3219,3208,3219,2);
            }
            isRunning = true;
            frame.dispose();
        });
        buttonPanel.add(button);

        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }
}