import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
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
    private String lastFishWord = "";

    // 네트워크 관련
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    
    // 채팅 관련
    private JTextArea chatArea = new JTextArea(10, 30);
    private JTextField chatInput = new JTextField(30);
    private JPanel chatPanel;

    // 경매 관련
    private Auction currentAuction = null;
    private JPanel auctionPanel;
    private JLabel auctionInfoLabel;
    private JTextField auctionBidField;
    private JButton auctionBidButton;
    private Timer auctionTimer;
    private Map<String, Integer> myInventory = new HashMap<>();
    
    // 게임오버 화면
    private JPanel gameOverPanel;

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
        inputPanel.add(new JLabel("단어 입력:"));
        inputPanel.add(input);
        gameArea.add(inputPanel, BorderLayout.SOUTH);

        // 오른쪽: 채팅 패널
        chatPanel = createChatPanel();
        
        // 경매 패널 생성
        auctionPanel = createAuctionPanel();

        // 오른쪽에 채팅 + 경매 패널
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(auctionPanel, BorderLayout.SOUTH);

        // 분할 패널로 게임과 오른쪽 패널 나누기
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gameArea, rightPanel);
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
                    
                    // 내 인벤토리에 추가
                    myInventory.put(inWord, myInventory.getOrDefault(inWord, 0) + 1);
                    
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
        panel.setPreferredSize(new Dimension(300, 400));

        JLabel chatLabel = new JLabel("채팅 및 활동", JLabel.CENTER);
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

    // 경매 패널 생성
    private JPanel createAuctionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 200));
        panel.setBorder(BorderFactory.createTitledBorder("🔨 경매"));

        auctionInfoLabel = new JLabel("진행 중인 경매가 없습니다");
        auctionInfoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        panel.add(auctionInfoLabel);

        panel.add(Box.createVerticalStrut(10));

        // 입찰 입력
        JPanel bidPanel = new JPanel(new FlowLayout());
        bidPanel.add(new JLabel("입찰가:"));
        auctionBidField = new JTextField(10);
        bidPanel.add(auctionBidField);
        auctionBidButton = new JButton("입찰");
        auctionBidButton.setEnabled(false);
        bidPanel.add(auctionBidButton);
        panel.add(bidPanel);

        // 입찰 버튼 이벤트
        auctionBidButton.addActionListener(e -> {
            if (currentAuction == null) return;
            
            try {
                int bidAmount = Integer.parseInt(auctionBidField.getText().trim());
                
                if (bidAmount <= currentAuction.getCurrentPrice()) {
                    JOptionPane.showMessageDialog(this, 
                        "현재가보다 높은 금액을 입찰하세요!");
                    return;
                }
                
                if (scorePanel.getMoney() < bidAmount) {
                    JOptionPane.showMessageDialog(this, 
                        "돈이 부족합니다!");
                    return;
                }
                
                // 서버에 입찰 전송
                sendToServer("/auction bid " + bidAmount);
                auctionBidField.setText("");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "올바른 금액을 입력하세요!");
            }
        });

        panel.add(Box.createVerticalStrut(10));

        // 내 물고기 경매 버튼
        JButton myAuctionButton = new JButton("내 물고기 경매");
        myAuctionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        myAuctionButton.addActionListener(e -> openMyAuctionDialog());
        panel.add(myAuctionButton);

        return panel;
    }

    // 내 물고기 경매 다이얼로그
    private void openMyAuctionDialog() {
        if (currentAuction != null) {
            JOptionPane.showMessageDialog(this, "이미 경매가 진행 중입니다!");
            return;
        }

        if (myInventory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "경매할 물고기가 없습니다!");
            return;
        }

        // 내 물고기 목록
        String[] fishList = myInventory.keySet().toArray(new String[0]);
        String selectedFish = (String) JOptionPane.showInputDialog(
            this,
            "경매할 물고기를 선택하세요:",
            "물고기 선택",
            JOptionPane.QUESTION_MESSAGE,
            null,
            fishList,
            fishList[0]
        );

        if (selectedFish == null) return;

        // 시작가 자동 설정 (물고기 가격의 90%)
        int fishScore = textSource.getFishPrice(selectedFish);
        int startPrice = (int)(fishScore * 0.9);

        // 확인 메시지
        int confirm = JOptionPane.showConfirmDialog(
            this,
            selectedFish + "을(를) 경매에 올리시겠습니까?\n" +
            "물고기 가격: " + fishScore + "점\n" +
            "시작가: " + startPrice + "원 (10% 할인)",
            "경매 확인",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        // 인벤토리에서 제거
        int count = myInventory.get(selectedFish);
        if (count == 1) {
            myInventory.remove(selectedFish);
        } else {
            myInventory.put(selectedFish, count - 1);
        }

        // 서버에 경매 시작 알림
        sendToServer("/auction start " + selectedFish + " " + fishScore + " " + startPrice);
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
            String chatMsg = message.substring(6);
            appendChat(chatMsg + "\n");
            
        } else if (message.startsWith("/catch ")) {
            String catchMsg = message.substring(7);
            appendChat("🎣 " + catchMsg + "\n");
            
        } else if (message.startsWith("/system ")) {
            String sysMsg = message.substring(8);
            appendChat("📢 " + sysMsg + "\n");
            
        } else if (message.startsWith("/auction start ")) {
            String[] parts = message.substring(15).split(" ");
            if (parts.length >= 4) {
                String seller = parts[0];
                String fishName = parts[1];
                int fishScore = Integer.parseInt(parts[2]);
                int startPrice = Integer.parseInt(parts[3]);
                startAuction(seller, fishName, fishScore, startPrice);
            }
            
        } else if (message.startsWith("/auction bid ")) {
            String[] parts = message.substring(13).split(" ");
            if (parts.length >= 2) {
                String bidder = parts[0];
                int bidAmount = Integer.parseInt(parts[1]);
                
                if (currentAuction != null) {
                    currentAuction.placeBid(bidder, bidAmount);
                    updateAuctionInfo();
                    appendChat("💰 " + bidder + "님이 " + bidAmount + "원 입찰!\n");
                }
            }
            
        } else if (message.startsWith("/auction end ")) {
            String endInfo = message.substring(13);
            appendChat("📢 " + endInfo + "\n");
            endAuction();
            
        } else {
            appendChat(message + "\n");
        }
    }

    // 경매 시작
    private void startAuction(String seller, String fishName, int fishScore, int startPrice) {
        currentAuction = new Auction(seller, fishName, fishScore, startPrice);
        
        auctionBidButton.setEnabled(!seller.equals(userName));
        updateAuctionInfo();
        
        appendChat("📢 " + seller + "님이 " + fishName + "(" + fishScore + "점)을 경매에 올렸습니다!\n");
        appendChat("   시작가: " + startPrice + "원\n");
        
        if (auctionTimer != null) {
            auctionTimer.stop();
        }
        
        auctionTimer = new Timer(1000, e -> {
            if (currentAuction != null) {
                if (currentAuction.isExpired()) {
                    String winner = currentAuction.getHighestBidder();
                    int finalPrice = currentAuction.getCurrentPrice();
                    String seller2 = currentAuction.getSeller();
                    String fish = currentAuction.getFishName();
                    
                    if (winner.isEmpty()) {
                        sendToServer("/auction end 유찰되었습니다!");
                        
                        if (seller2.equals(userName)) {
                            myInventory.put(fish, myInventory.getOrDefault(fish, 0) + 1);
                        }
                    } else {
                        sendToServer("/auction end " + winner + "님이 " + finalPrice + "원에 낙찰!");
                        
                        if (winner.equals(userName)) {
                            scorePanel.spendMoney(finalPrice);
                            myInventory.put(fish, myInventory.getOrDefault(fish, 0) + 1);
                        }
                        
                        if (seller2.equals(userName)) {
                            scorePanel.addMoney(finalPrice);
                        }
                    }
                    
                    auctionTimer.stop();
                    endAuction();
                } else {
                    updateAuctionInfo();
                }
            }
        });
        auctionTimer.start();
    }

    // 경매 종료
    private void endAuction() {
        currentAuction = null;
        auctionBidButton.setEnabled(false);
        auctionInfoLabel.setText("진행 중인 경매가 없습니다");
        if (auctionTimer != null) {
            auctionTimer.stop();
        }
    }

    // 경매 정보 업데이트
    private void updateAuctionInfo() {
        if (currentAuction != null) {
            String info = "<html>" +
                "물고기: " + currentAuction.getFishName() + " (" + currentAuction.getFishScore() + "점)<br>" +
                "판매자: " + currentAuction.getSeller() + "<br>" +
                "현재가: " + currentAuction.getCurrentPrice() + "원<br>" +
                "최고입찰자: " + (currentAuction.getHighestBidder().isEmpty() ? "없음" : currentAuction.getHighestBidder()) + "<br>" +
                "남은 시간: " + currentAuction.getRemainingSeconds() + "초" +
                "</html>";
            auctionInfoLabel.setText(info);
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
        String newWord;
        int attempts = 0;
        
        do {
            if (rand.nextInt(100) < 70) {
                newWord = textSource.getRandomFishPriceWord();
            } else {
                newWord = textSource.getRandomFishWord();
            }
            attempts++;
        } while (newWord.equals(lastFishWord) && attempts < 10);
        
        lastFishWord = newWord;
        
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
                        showGameOver(); // ← 변경됨!
                    }
                }
            }
        });
        fallingTimer.start();
        return fallingTimer;
    }

    // ========== 게임오버 화면 메서드들 ==========
    
    private void showGameOver() {
        gameOverPanel = new JPanel() {
            private Image gameOverImage;
            
            {
                try {
                    gameOverImage = new ImageIcon("GameOver.png").getImage();
                } catch (Exception e) {
                    System.out.println("GameOver.png 이미지를 불러올 수 없습니다");
                    e.printStackTrace();
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (gameOverImage != null) {
                    // 이미지를 패널 크기에 맞게 그리기
                    g.drawImage(gameOverImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // 이미지 로드 실패 시 반투명 배경
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        gameOverPanel.setOpaque(false);
        gameOverPanel.setLayout(null);
        gameOverPanel.setBounds(0, 0, ground.getWidth(), ground.getHeight());
        
        // 투명 버튼 (이미지의 MAIN MENU 버튼 위치에 배치)
        JButton menuButton = new JButton();
        menuButton.setOpaque(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setBorderPainted(false);
        menuButton.setFocusPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuButton.addActionListener(e -> goToMainMenu());
        
        // 이미지 비율에 맞게 버튼 위치 계산
        // 원본 이미지: 847x924
        // MAIN MENU 버튼 위치: 중앙 약간 위쪽
        int panelWidth = ground.getWidth();
        int panelHeight = ground.getHeight();
        
        // 이미지 비율로 버튼 위치 계산
        int buttonWidth = (int)(panelWidth * 0.4);   // 화면의 40%
        int buttonHeight = (int)(panelHeight * 0.08); // 화면의 8%
        int x = (panelWidth - buttonWidth) / 2;       // 중앙 정렬
        int y = (int)(panelHeight * 0.45);            // 화면의 45% 위치
        
        menuButton.setBounds(x, y, buttonWidth, buttonHeight);
        
        gameOverPanel.add(menuButton);
        
        ground.setLayout(null);
        ground.add(gameOverPanel);
        ground.setComponentZOrder(gameOverPanel, 0);
        ground.revalidate();
        ground.repaint();
    }
    
    private void hideGameOver() {
        if (gameOverPanel != null) {
            ground.remove(gameOverPanel);
            gameOverPanel = null;
            ground.revalidate();
            ground.repaint();
        }
    }
    
    private void goToMainMenu() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
        SwingUtilities.invokeLater(() -> new MultiStartPanel());
    }

    // ========== 내부 클래스 ==========

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
                    backgroundImage = new ImageIcon("BackGround1.png").getImage();
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