import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class MultiGamePanel extends JPanel {
    // 게임 관련
    private JTextField input = new JTextField(30);
    private ScorePanel scorePanel;
    private TextSource textSource = new TextSource();
    private GameGroundPanel ground = new GameGroundPanel();
    private Random rand = new Random();
    private Timer gameTimer;
    private ArrayList<FallingLabel> fallingLabels = new ArrayList<>();
    private ArrayList<Timer> fallingTimers = new ArrayList<>();
    private ArrayList<Point> labelPositions = new ArrayList<>();
    private LifePanel lifePanel;
    private String level;
    private int fishCaught = 0;
    private boolean isPaused = false;
    private String userName;

    // 네트워크 관련
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    
    // 채팅 관련
    private JTextArea chatArea = new JTextArea(10, 30);
    private JTextField chatInput = new JTextField(30);
    private JPanel chatPanel;

    private final Map<String, Integer> levelGoals = Map.of(
            "쉬움", 7,
            "보통", 10,
            "어려움", 13
    );

    public MultiGamePanel(ScorePanel scorePanel, LifePanel lifePanel, String level, 
                          String userName, String ipAddress, String portNo) {
        this.scorePanel = scorePanel;
        this.lifePanel = lifePanel;
        this.level = level;
        this.userName = userName;

        setLayout(new BorderLayout());

        // 왼쪽: 게임 화면
        JPanel gameArea = new JPanel(new BorderLayout());
        gameArea.add(ground, BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.CYAN);
        inputPanel.add(new JLabel("단어 입력:"));
        inputPanel.add(input);
        gameArea.add(inputPanel, BorderLayout.SOUTH);

        // 오른쪽: 채팅 패널
        chatPanel = createChatPanel();

        // 분할 패널로 게임과 채팅 나누기
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gameArea, chatPanel);
        splitPane.setDividerLocation(550);
        add(splitPane, BorderLayout.CENTER);

        ground.setBackgroundImage(level);

        // 네트워크 연결
        connectToServer(ipAddress, portNo);

        // 게임 입력 이벤트
        input.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String inWord = input.getText().trim();
                if (inWord.isEmpty()) return;

                // 물고기 잡기 체크
                if (textSource.getFishPrices().containsKey(inWord)) {
                    int fishScore = textSource.getFishPrice(inWord);
                    scorePanel.increase(inWord);
                    scorePanel.addCaughtFish(inWord);
                    fishCaught++;

                    // 서버에 알림
                    sendToServer("/catch " + inWord + " " + fishScore);

                    checkLevelUp();
                }

                // 떨어진 물고기 제거
                for (int i = 0; i < fallingLabels.size(); i++) {
                    FallingLabel fallingLabel = fallingLabels.get(i);
                    if (fallingLabel.getWord().equals(inWord)) {
                        ground.remove(fallingLabel);
                        fallingTimers.get(i).stop();
                        fallingLabels.remove(i);
                        fallingTimers.remove(i);
                        labelPositions.remove(i);
                        ground.repaint();
                        input.setText("");
                        break;
                    }
                }
            }
        });

        // 채팅 입력 이벤트
        chatInput.addActionListener(e -> {
            String chatMsg = chatInput.getText().trim();
            if (!chatMsg.isEmpty()) {
                sendToServer("/chat " + chatMsg);
                chatInput.setText("");
            }
        });
    }

    // 채팅 패널 생성
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 600));

        JLabel chatLabel = new JLabel("💬 채팅 & 활동", JLabel.CENTER);
        chatLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        panel.add(chatLabel, BorderLayout.NORTH);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("메시지: "), BorderLayout.WEST);
        inputPanel.add(chatInput, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    // 서버 연결
    private void connectToServer(String ipAddress, String portNo) {
        try {
            socket = new Socket(ipAddress, Integer.parseInt(portNo));
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            // 로그인 메시지 전송
            sendToServer("/login " + userName);
            
            appendChat("✅ 서버에 접속했습니다!\n");

            // 서버 메시지 수신 스레드
            new Thread(() -> {
                while (true) {
                    try {
                        String message = dis.readUTF();
                        handleServerMessage(message);
                    } catch (IOException e) {
                        appendChat("❌ 서버 연결이 끊어졌습니다.\n");
                        break;
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            appendChat("❌ 서버 연결 실패!\n");
        }
    }

    // 서버로 메시지 전송
    private void sendToServer(String message) {
        try {
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException e) {
            appendChat("❌ 메시지 전송 실패\n");
        }
    }

    // 서버 메시지 처리
    private void handleServerMessage(String message) {
        if (message.startsWith("/chat ")) {
            // 채팅 메시지
            String chatMsg = message.substring(6);
            appendChat(chatMsg + "\n");
            
        } else if (message.startsWith("/catch ")) {
            // 물고기 잡기 알림
            String catchMsg = message.substring(7);
            appendChat("🎣 " + catchMsg + "\n");
            
        } else if (message.startsWith("/system ")) {
            // 시스템 메시지
            String sysMsg = message.substring(8);
            appendChat("📢 " + sysMsg + "\n");
            
        } else {
            // 기타 메시지
            appendChat(message + "\n");
        }
    }

    // 채팅 영역에 메시지 추가
    private void appendChat(String message) {
        chatArea.append(message);
        chatArea.setCaretPosition(chatArea.getText().length());
    }

    private void checkLevelUp() {
        int goal = levelGoals.get(level);
        if (fishCaught >= goal) {
            stopGame(false);

            if (level.equals("어려움")) {
                JOptionPane.showMessageDialog(this, 
                    "축하합니다! 낚시왕이 되셨습니다! 최종 점수: " + scorePanel.getScore());
                return;
            }

            int result = JOptionPane.showConfirmDialog(this, 
                "목표를 달성했습니다! 다음 단계도 도전하겠습니까?",
                "레벨 업", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                levelUp();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "게임 종료! 최종 점수: " + scorePanel.getScore());
            }
        }
    }

    private void levelUp() {
        switch (level) {
            case "쉬움":
                level = "보통";
                break;
            case "보통":
                level = "어려움";
                break;
        }

        fishCaught = 0;
        resetGame();

        JOptionPane.showMessageDialog(this, 
            "레벨 업! " + level + " 레벨이 시작됩니다! 목표: " + levelGoals.get(level) + "개의 물고기 잡기!");

        Timer timer = new Timer(3000, e -> startGame());
        timer.setRepeats(false);
        timer.start();
    }

    private int getFallingSpeed(String level) {
        switch (level) {
            case "어려움": return 20;
            case "보통": return 30;
            case "쉬움":
            default: return 40;
        }
    }

    public void startGame() {
        if (isPaused) {
            isPaused = false;
            if (gameTimer != null) gameTimer.start();
            for (Timer timer : fallingTimers) {
                timer.start();
            }
        } else {
            resetGame();
            addNewWord();
            gameTimer = new Timer(2000, e -> addNewWord());
            gameTimer.start();
        }
    }

    public void stopGame(boolean showStopMessage) {
        isPaused = true;
        if (gameTimer != null) gameTimer.stop();
        for (Timer timer : fallingTimers) {
            timer.stop();
        }

        if (showStopMessage) {
            JOptionPane.showMessageDialog(this, "게임이 일시 정지되었습니다.");
        }
    }

    private void resetGame() {
        scorePanel.reset();
        lifePanel.resetLives();
        fallingLabels.clear();
        fallingTimers.clear();
        labelPositions.clear();
        ground.removeAll();
        input.setText("");
        ground.setBackgroundImage(level);
    }

    public void addNewWord() {
        String newWord = rand.nextBoolean() ? textSource.getRandomFishWord() : textSource.getRandomFishPriceWord();
        FallingLabel newLabel = new FallingLabel(newWord);

        int randomX = rand.nextInt(ground.getWidth() - newLabel.getWidth());
        newLabel.setLocation(randomX, 0);
        ground.add(newLabel);
        fallingLabels.add(newLabel);
        labelPositions.add(newLabel.getLocation());

        Timer fallingTimer = createFallingTimer(newLabel, randomX);
        fallingTimers.add(fallingTimer);
    }

    private Timer createFallingTimer(FallingLabel label, int x) {
        int fallingSpeed = getFallingSpeed(level);
        Timer fallingTimer = new Timer(fallingSpeed, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                label.setLocation(x, label.getY() + 5);
                if (label.getY() > ground.getHeight()) {
                    ((Timer) e.getSource()).stop();
                    ground.remove(label);
                    fallingLabels.remove(label);
                    fallingTimers.remove(e.getSource());
                    ground.repaint();
                    lifePanel.decreaseLife();

                    if (lifePanel.isGameOver()) {
                        stopGame(false);
                        JOptionPane.showMessageDialog(MultiGamePanel.this, 
                            "게임 종료! 최종 점수: " + scorePanel.getScore(),
                            "게임 종료", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
        fallingTimer.start();
        return fallingTimer;
    }

    class FallingLabel extends JLabel {
        private String word;
        private ImageIcon icon;

        public FallingLabel(String word) {
            this.word = word;
            setText(word);
            setFont(new Font("맑은 고딕", Font.BOLD, 16));
            setForeground(Color.RED);

            if (textSource.getFishPrices().containsKey(word)) {
                icon = new ImageIcon("fish.png");
            } else if (textSource.getFishWords().contains(word)) {
                icon = new ImageIcon("ship.png");
            }

            Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
            setIcon(icon);

            int labelWidth = Math.max(icon.getIconWidth(), getFontMetrics(getFont()).stringWidth(word));
            int labelHeight = icon.getIconHeight() + 20;

            setSize(labelWidth, labelHeight);
            setPreferredSize(new Dimension(labelWidth, labelHeight));

            setHorizontalTextPosition(JLabel.CENTER);
            setVerticalTextPosition(JLabel.BOTTOM);
        }

        public String getWord() {
            return word;
        }
    }

    class GameGroundPanel extends JPanel {
        private Image backgroundImage;

        public GameGroundPanel() {
            setPreferredSize(new Dimension(550, 500));
        }

        public void setBackgroundImage(String level) {
            switch (level) {
                case "어려움":
                    backgroundImage = new ImageIcon("level3.jpg").getImage();
                    break;
                case "보통":
                    backgroundImage = new ImageIcon("level2.jpg").getImage();
                    break;
                case "쉬움":
                default:
                    backgroundImage = new ImageIcon("level1.jpg").getImage();
                    break;
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}