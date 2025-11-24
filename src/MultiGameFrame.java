import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class MultiGameFrame extends JFrame {
    private String userName;
    private String level;
    private String ipAddress;
    private String portNo;
    
    private MultiGamePanel gamePanel;
    
    private ImageIcon normalIcon = new ImageIcon("normal.png");
    private ImageIcon pressedIcon = new ImageIcon("pressed.png");
    private ImageIcon overIcon = new ImageIcon("over.png");

    private JMenuItem startItem = new JMenuItem("start");
    private JMenuItem stopItem = new JMenuItem("stop");

    private JButton startBtn = new JButton("Start");
    private JButton stopBtn = new JButton("Stop");

    private TextSource textSource = new TextSource();
    private ScorePanel scorePanel = new ScorePanel();
    private LifePanel lifePanel = new LifePanel();

    public MultiGameFrame(String userName, String level, String ipAddress, String portNo) {
        if (level == null) {
            level = "쉬움";
        }
        
        this.userName = userName;
        this.level = level;
        this.ipAddress = ipAddress;
        this.portNo = portNo;

        setTitle(userName + "의 멀티플레이어 Fishing - 난이도: " + level);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setResizable(false);

        gamePanel = new MultiGamePanel(scorePanel, lifePanel, level, userName, ipAddress, portNo);

        makeMenu();
        makeToolBar();
        makeSplit();
        setVisible(true);
    }

    private void makeSplit() {
        JSplitPane hPane = new JSplitPane();
        hPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        hPane.setDividerLocation(850);
        getContentPane().add(hPane, BorderLayout.CENTER);

        JSplitPane vPane = new JSplitPane();
        vPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        hPane.setRightComponent(vPane);

        hPane.setLeftComponent(gamePanel);
        vPane.setDividerLocation(250);
        vPane.setTopComponent(scorePanel);
        lifePanel.setPreferredSize(new Dimension(200, 50));
        vPane.setBottomComponent(lifePanel);
    }

    private void makeToolBar() {
        JToolBar tBar = new JToolBar();
        tBar.setFloatable(false);
        getContentPane().add(tBar, BorderLayout.NORTH);
        tBar.add(startBtn);
        tBar.add(stopBtn);

        startBtn.addActionListener(new StartAction());
        stopBtn.addActionListener(new StopAction());
    }

    private void makeMenu() {
        JMenuBar mBar = new JMenuBar();
        this.setJMenuBar(mBar);

        JMenu fileMenu = new JMenu("Fishing");
        fileMenu.add(startItem);
        fileMenu.add(stopItem);
        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("exit");
        fileMenu.add(exitItem);
        mBar.add(fileMenu);

        startItem.addActionListener(new StartAction());
        stopItem.addActionListener(new StopAction());

        exitItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, 
                "게임을 종료하시겠습니까?", 
                "종료", 
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });
    }

    private class StartAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            gamePanel.startGame();
        }
    }

    private class StopAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            gamePanel.stopGame(true);
        }
    }

    public static void main(String[] args) {
        // 테스트용
        new MultiGameFrame("테스터", "쉬움", "127.0.0.1", "30000");
    }
}