import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
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
    private JPanel chatMessagesPanel = new JPanel();  // 채팅 메시지들을 담을 패널
    private JScrollPane chatScrollPane;  // 스크롤 패널
    private JTextPane activityLog = new JTextPane();  // 활동 로그
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
        this.userName = userName;  // 이미 필드에 선언되어 있음

        setLayout(new BorderLayout());

        // 왼쪽: 게임 화면
        JPanel gameArea = new JPanel(new BorderLayout());
        gameArea.add(ground, BorderLayout.CENTER);
        
        // 단어 입력 패널 - BorderLayout으로 가로 배치
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(new Color(238, 214, 175));  // 모래색 배경
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));  // 여백
        
        JLabel inputLabel = new JLabel("단어 입력:");
        inputLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        inputLabel.setForeground(new Color(101, 67, 33));  // 진한 갈색
        
        input.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        input.setBackground(new Color(255, 248, 220));  // 밝은 크림색
        input.setForeground(new Color(101, 67, 33));  // 진한 갈색 텍스트
        input.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(139, 90, 43), 2),  // 갈색 테두리
            BorderFactory.createEmptyBorder(5, 10, 5, 10)  // 내부 여백
        ));
        
        inputPanel.add(inputLabel, BorderLayout.WEST);  // 라벨 왼쪽
        inputPanel.add(input, BorderLayout.CENTER);     // 입력창 중앙 (자동 확장)
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
                    scorePanel.increase(inWord);     // 점수 = 돈
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
             // 잘못 입력했을 때도 자동으로 입력창 지우기
             input.setText("");
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

 // 채팅 패널 생성 부분만 수정 (원본 기반)

    private JPanel createChatPanel() {
        // 배경 이미지 패널
        JPanel panel = new JPanel(new BorderLayout()) {
            private Image bgImage;
            
            {
                try {
                    bgImage = new ImageIcon("Chatting.png").getImage();
                } catch (Exception e) {
                    System.out.println("Chatting.png 이미지를 불러올 수 없습니다");
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        
        panel.setPreferredSize(new Dimension(300, 450));
        panel.setOpaque(false);

        // === 상단: 채팅 메시지 영역 ===
        JPanel chatSection = new JPanel(new BorderLayout());
        chatSection.setOpaque(false);
        chatSection.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 채팅 메시지 패널 (BoxLayout - 위에서 아래로)
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setOpaque(false);
        chatMessagesPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        
        // 채팅 영역을 감싸는 컨테이너 (너비 고정)
        JPanel chatContainer = new JPanel(new BorderLayout());
        chatContainer.setOpaque(false);
        chatContainer.add(chatMessagesPanel, BorderLayout.NORTH);
        
        chatScrollPane = new JScrollPane(chatContainer);
        chatScrollPane.setBorder(null);
        chatScrollPane.setPreferredSize(new Dimension(280, 150));  // ✅ 높이 축소 (200 → 150)
        chatScrollPane.setOpaque(false);
        chatScrollPane.getViewport().setOpaque(false);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatSection.add(chatScrollPane, BorderLayout.CENTER);

        // 메시지 입력 - 중앙 정렬, 둥근 모서리
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 20));  // ✅ 위쪽 여백 제거 (5 → 0)
        
        // 둥근 모서리 입력창
        chatInput = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                super.paintComponent(g);
                g2.dispose();
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 180, 255));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        
        chatInput.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        chatInput.setBackground(new Color(255, 255, 255, 230));
        chatInput.setOpaque(false);
        chatInput.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        chatInput.setPreferredSize(new Dimension(240, 35));
        
        inputPanel.add(chatInput);
        chatSection.add(inputPanel, BorderLayout.SOUTH);

        panel.add(chatSection, BorderLayout.NORTH);

        // === 하단: 활동 로그 영역 (그대로 유지) ===
        JPanel activitySection = new JPanel(new BorderLayout());
        activitySection.setOpaque(false);
        activitySection.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        JLabel activityLabel = new JLabel("활동 로그", JLabel.CENTER);
        activityLabel.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        activityLabel.setForeground(new Color(0, 0, 0));
        activityLabel.setOpaque(false);
        activityLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 3, 5));
        activitySection.add(activityLabel, BorderLayout.NORTH);

        activityLog.setEditable(false);
        activityLog.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        activityLog.setOpaque(false);
        
        JScrollPane activityScroll = new JScrollPane(activityLog);
        activityScroll.setBorder(null);
        activityScroll.setOpaque(false);
        activityScroll.getViewport().setOpaque(false);
        activitySection.add(activityScroll, BorderLayout.CENTER);

        panel.add(activitySection, BorderLayout.CENTER);

        return panel;
    }

    // 경매 패널 생성 - 세련된 디자인
    private JPanel createAuctionPanel() {
        // 메인 패널 - 그라데이션 배경
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // 그라데이션 배경 (금색 테마)
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 250, 230),
                    0, getHeight(), new Color(255, 235, 205)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 190));  // 높이 축소 (230 → 190)
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 165, 32), 2),  // 금색 테두리
            BorderFactory.createEmptyBorder(6, 10, 8, 10)  // 여백 축소
        ));

        // 경매 헤더 - 아이콘과 이미지
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 경매사 이미지
        try {
            ImageIcon sellIcon = new ImageIcon("sell.png");
            Image scaledImage = sellIcon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);  // 크기 축소 (35 → 28)
            JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
            headerPanel.add(iconLabel);
        } catch (Exception e) {
            System.out.println("sell.png 이미지를 불러올 수 없습니다");
        }
        
        JLabel auctionHeader = new JLabel("실시간 경매");
        auctionHeader.setFont(new Font("맑은 고딕", Font.BOLD, 14));  // 폰트 축소 (15 → 14)
        auctionHeader.setForeground(new Color(139, 69, 19));  // 갈색
        headerPanel.add(auctionHeader);
        
        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(4));  // 간격 축소 (6 → 4)

        // 경매 정보 카드
        JPanel infoCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        infoCard.setOpaque(false);
        infoCard.setLayout(new BorderLayout());
        infoCard.setMaximumSize(new Dimension(280, 80));  // 높이 축소 (100 → 80)
        infoCard.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));  // 여백 축소
        
        auctionInfoLabel = new JLabel("<html><div style='text-align: center; color: #888888;'>진행 중인 경매가 없습니다</div></html>");
        auctionInfoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        auctionInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        auctionInfoLabel.setVerticalAlignment(SwingConstants.TOP);
        infoCard.add(auctionInfoLabel, BorderLayout.CENTER);
        
        panel.add(infoCard);
        panel.add(Box.createVerticalStrut(4));  // 간격 축소 (6 → 4)

        // 입찰 입력 - 세련된 스타일
        JPanel bidPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        bidPanel.setOpaque(false);
        bidPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel bidLabel = new JLabel("");
        bidLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        bidPanel.add(bidLabel);
        
        auctionBidField = new JTextField(8) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                super.paintComponent(g);
                g2.dispose();
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(218, 165, 32));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        auctionBidField.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        auctionBidField.setBackground(new Color(255, 255, 255));
        auctionBidField.setOpaque(false);
        auctionBidField.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        bidPanel.add(auctionBidField);
        
        auctionBidButton = new JButton("입찰") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isEnabled()) {
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(218, 165, 32),
                        0, getHeight(), new Color(184, 134, 11)
                    );
                    g2.setPaint(gp);
                } else {
                    g2.setColor(new Color(200, 200, 200));
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        auctionBidButton.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        auctionBidButton.setForeground(Color.WHITE);
        auctionBidButton.setEnabled(false);
        auctionBidButton.setPreferredSize(new Dimension(55, 25));
        auctionBidButton.setContentAreaFilled(false);
        auctionBidButton.setBorderPainted(false);
        auctionBidButton.setFocusPainted(false);
        auctionBidButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
                
                // ✅ ScorePanel에서 money 제거 → score로 체크
                if (scorePanel.getScore() < bidAmount) {
                    JOptionPane.showMessageDialog(this, 
                        "점수가 부족합니다!");
                    return;
                }
                
                // 서버에 입찰 전송
                sendToServer("/auction bid " + bidAmount);
                auctionBidField.setText("");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "올바른 금액을 입력하세요!");
            }
        });

        panel.add(Box.createVerticalStrut(3));  // 간격 축소 (5 → 3)

        // 내 물고기 경매 버튼 - 중앙 정렬
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton myAuctionButton = new JButton("내 물고기 경매") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(100, 150, 255),
                    0, getHeight(), new Color(70, 120, 220)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        myAuctionButton.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        myAuctionButton.setForeground(Color.WHITE);
        myAuctionButton.setPreferredSize(new Dimension(140, 28));
        myAuctionButton.setContentAreaFilled(false);
        myAuctionButton.setBorderPainted(false);
        myAuctionButton.setFocusPainted(false);
        myAuctionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        myAuctionButton.addActionListener(e -> openMyAuctionDialog());
        
        buttonPanel.add(myAuctionButton);
        panel.add(buttonPanel);

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

        // 시작가 자동 설정 (100%)
        int fishScore = textSource.getFishPrice(selectedFish);
        int startPrice = fishScore;

        // 확인 메시지
        int confirm = JOptionPane.showConfirmDialog(
            this,
            selectedFish + "을(를) 경매에 올리시겠습니까?\n" +
            "물고기 가격: " + fishScore + "점\n" +
            "시작가: " + startPrice + "점",
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
        
        // 그물망에서도 제거!
        scorePanel.removeCaughtFish(selectedFish);

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
            
            appendSystemMessage("서버에 접속했습니다!\n");

            // 서버 메시지 수신 스레드
            new Thread(() -> {
                while (true) {
                    try {
                        String message = dis.readUTF();
                        handleServerMessage(message);
                    } catch (IOException e) {
                        appendSystemMessage("서버 연결이 끊어졌습니다.\n");
                        break;
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            appendSystemMessage("서버 연결 실패!\n");
        }
    }

    // 서버로 메시지 전송
    private void sendToServer(String message) {
        try {
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException e) {
            appendSystemMessage("메시지 전송 실패\n");
        }
    }

 // 서버 메시지 처리
    private void handleServerMessage(String message) {
        if (message.startsWith("/chat ")) {
            String chatMsg = message.substring(6);
            appendChat(chatMsg + "\n");  // 채팅 영역에 추가
            
        } else if (message.startsWith("/catch ")) {
            String catchMsg = message.substring(7);
            appendActivityLog("🎣 " + catchMsg + "\n");  // 활동 로그에 추가
            
        } else if (message.startsWith("/system ")) {
            String sysMsg = message.substring(8);
            appendSystemMessage("📢 " + sysMsg + "\n");  // 시스템 메시지
            
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
                    appendActivityLog("💰 " + bidder + "님이 " + bidAmount + "점 입찰!\n");
                }
            }
            
        } else if (message.startsWith("/auction end ")) {
            String endInfo = message.substring(13);
            appendActivityLog("📢 " + endInfo + "\n");
            
            // ✅ 유찰 처리
            if (endInfo.contains("유찰")) {
                if (currentAuction != null && currentAuction.getSeller().equals(userName)) {
                    // 판매자 = 나 → 물고기 반환
                    String fish = currentAuction.getFishName();
                    myInventory.put(fish, myInventory.getOrDefault(fish, 0) + 1);
                    scorePanel.addCaughtFish(fish);
                    appendActivityLog("🔄 물고기가 반환되었습니다.\n");
                }
                endAuction();
                return;
            }
            
            // ✅ 낙찰 정보 파싱
            if (endInfo.contains("님이") && endInfo.contains("원에 낙찰!")) {
                String[] parts = endInfo.split("님이 ");
                if (parts.length >= 2) {
                    String winner = parts[0].trim();
                    String priceStr = parts[1].split("원에")[0].trim();
                    
                    try {
                        int price = Integer.parseInt(priceStr);
                        
                        // 낙찰자 = 나 → 점수 차감 + 그물망에 추가
                        if (winner.equals(userName)) {
                            int currentScore = scorePanel.getScore();
                            
                            // 점수 부족 체크
                            if (currentScore < price) {
                                appendActivityLog("❌ 점수 부족! (보유: " + currentScore + "점, 필요: " + price + "점)\n");
                                appendActivityLog("⚠️ 낙찰이 취소되었습니다.\n");
                            } else {
                                // 점수 차감
                                scorePanel.spendScore(price);
                                appendActivityLog("💰 " + price + "점을 지불했습니다.\n");
                                
                                // 물고기 획득
                                if (currentAuction != null) {
                                    String fishName = currentAuction.getFishName();
                                    myInventory.put(fishName, myInventory.getOrDefault(fishName, 0) + 1);
                                    scorePanel.addCaughtFish(fishName);
                                    appendActivityLog("🎣 " + fishName + "을(를) 획득했습니다!\n");
                                }
                            }
                        }
                        
                        // 판매자 = 나 → 점수 추가
                        if (currentAuction != null && currentAuction.getSeller().equals(userName)) {
                            scorePanel.addScore(price);
                            appendActivityLog("💰 " + price + "점을 받았습니다.\n");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("가격 파싱 오류: " + priceStr);
                    }
                }
            }
            
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
        
        appendActivityLog("📢 " + seller + "님이 " + fishName + "(" + fishScore + "점)을 경매에 올렸습니다!\n");
        appendActivityLog("   시작가: " + startPrice + "점\n");
        
        if (auctionTimer != null) {
            auctionTimer.stop();
        }
        
        // ✅ 타이머는 서버에 알림만 전송 (점수 조작 X)
        auctionTimer = new Timer(1000, e -> {
            if (currentAuction != null) {
                if (currentAuction.isExpired()) {
                    // 타이머 종료 시 서버에만 알림 전송 (점수 조작 X)
                    String winner = currentAuction.getHighestBidder();
                    int finalPrice = currentAuction.getCurrentPrice();
                    
                    if (winner.isEmpty()) {
                        sendToServer("/auction end 유찰되었습니다!");
                    } else {
                        sendToServer("/auction end " + winner + "님이 " + finalPrice + "원에 낙찰!");
                    }
                    
                    auctionTimer.stop();
                    // endAuction()은 서버 메시지 수신 시 호출됨
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
        auctionInfoLabel.setText("<html><div style='text-align: center; color: #888888;'>진행 중인 경매가 없습니다</div></html>");
        if (auctionTimer != null) {
            auctionTimer.stop();
        }
    }

    // 경매 정보 업데이트
    private void updateAuctionInfo() {
        if (currentAuction != null) {
            String info = "<html><div style='color: #444444;'>" +
                "<b style='color: #8B4513;'>🐟 " + currentAuction.getFishName() + "</b> <span style='color: #888888;'>(" + currentAuction.getFishScore() + "점)</span><br>" +
                "판매자: <b>" + currentAuction.getSeller() + "</b><br>" +
                "<span style='color: #DAA520;'>현재가: <b>" + currentAuction.getCurrentPrice() + "점</b></span><br>" +
                "최고입찰자: " + (currentAuction.getHighestBidder().isEmpty() ? "없음" : "<b>" + currentAuction.getHighestBidder() + "</b>") + "<br>" +
                "⏱️ 남은 시간: <b style='color: #D2691E;'>" + currentAuction.getRemainingSeconds() + "초</b>" +
                "</div></html>";
            auctionInfoLabel.setText(info);
        }
    }

    // 시스템 메시지 (회색, 중앙)
    private void appendSystemMessage(String message) {
        JLabel label = new JLabel(message.trim());
        label.setFont(new Font("맑은 고딕", Font.BOLD, 10));  // Bold로 변경
        label.setForeground(new Color(0x0066CC));  // 진한 파란색 (잘 보임)
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        chatMessagesPanel.add(label);
        chatMessagesPanel.add(Box.createVerticalStrut(3));
        chatMessagesPanel.revalidate();
        
        // 스크롤을 맨 아래로
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    // 플레이어 채팅 (말풍선 스타일)
    private void appendChat(String message) {
        // 메시지 파싱: "이름: 내용" 형식
        String[] parts = message.split(":", 2);
        if (parts.length < 2) {
            appendSystemMessage(message);
            return;
        }
        
        String senderName = parts[0].trim();
        String content = parts[1].trim();
        
        boolean isMyMessage = senderName.equals(userName);
        
        System.out.println("=== 채팅 디버그 ===");
        System.out.println("발신자: " + senderName);
        System.out.println("내 이름: " + userName);
        System.out.println("내 메시지? " + isMyMessage);
        System.out.println("내용: " + content);
        
        // 말풍선 패널
        JPanel bubblePanel = new JPanel();
        bubblePanel.setLayout(new BoxLayout(bubblePanel, BoxLayout.Y_AXIS));
        bubblePanel.setOpaque(true);
        
        // 말풍선 배경색
        if (isMyMessage) {
            bubblePanel.setBackground(new Color(255, 235, 100));  // 노란색
        } else {
            bubblePanel.setBackground(new Color(255, 255, 255));  // 흰색
        }
        
        // 둥근 테두리
        bubblePanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // 이름 라벨 (상대방만)
        if (!isMyMessage) {
            JLabel nameLabel = new JLabel(senderName);
            nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 10));
            nameLabel.setForeground(new Color(0x333333));  // 진한 회색 (거의 검정)
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubblePanel.add(nameLabel);
            bubblePanel.add(Box.createVerticalStrut(3));
        }
        
        // 메시지 내용
        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        contentLabel.setForeground(new Color(0x000000));  // 검정색
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubblePanel.add(contentLabel);
        
        // Row 패널 (좌우 정렬)
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setOpaque(false);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        if (isMyMessage) {
            // 내 메시지: 오른쪽
            rowPanel.add(Box.createHorizontalGlue());
            rowPanel.add(bubblePanel);
            rowPanel.add(Box.createHorizontalStrut(10));
        } else {
            // 상대 메시지: 왼쪽
            rowPanel.add(Box.createHorizontalStrut(10));
            rowPanel.add(bubblePanel);
            rowPanel.add(Box.createHorizontalGlue());
        }
        
        chatMessagesPanel.add(rowPanel);
        chatMessagesPanel.add(Box.createVerticalStrut(3));
        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();
        
        // 스크롤을 맨 아래로
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    // 둥근 테두리 클래스
    class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;
        
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }
    
    // 활동 로그 추가
    private void appendActivityLog(String message) {
        appendToPane(activityLog, message, new Color(0, 0, 0), 10, false);  // 검정색으로 변경
    }
    
    // JTextPane에 스타일이 적용된 텍스트 추가
    private void appendToPane(JTextPane pane, String message, Color color, int fontSize, boolean bold) {
        try {
            StyledDocument doc = pane.getStyledDocument();
            SimpleAttributeSet style = new SimpleAttributeSet();
            
            StyleConstants.setFontFamily(style, "맑은 고딕");
            StyleConstants.setFontSize(style, fontSize);
            StyleConstants.setForeground(style, color);
            StyleConstants.setBold(style, bold);
            
            doc.insertString(doc.getLength(), message, style);
            pane.setCaretPosition(doc.getLength());
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void checkLevelUp() {
        int goal = levelGoals.get(level);
        if (fishCaught >= goal) {
            stopGame(false);

            if (level.equals("어려움")) {
                // 3단계 완료 - 낚시왕!
                showFishingKing();
                return;
            }

            // 1, 2단계 완료 - 다음 단계로 갈지 선택
            int result = JOptionPane.showConfirmDialog(this, 
                "목표를 달성했습니다! 다음 단계도 도전하겠습니까?",
                "레벨 업", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                levelUp();
            } else {
                // 포기 선택 - Quit.png 표시
                showQuit();
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
        resetGameField();  // 점수/돈/그물망 유지하고 화면만 초기화
        
        // 타이틀바 업데이트
        updateFrameTitle();

        JOptionPane.showMessageDialog(this,
            "레벨 업! 난이도: " + level + "\n새로운 도전을 시작합니다!");

        Timer timer = new Timer(3000, e -> startGame());
        timer.setRepeats(false);
        timer.start();
    }
    
    // 프레임 타이틀 업데이트 메서드
    private void updateFrameTitle() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.setTitle(userName + "의 멀티플레이어 Fishing - 난이도: " + level);
        }
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
        // 게임 시작 목표 안내 추가
        int goal = levelGoals.get(level);
        JOptionPane.showMessageDialog(this, 
            "🎣 낚시 게임 시작!\n\n" +
            "난이도: " + level + "\n" +
            "목표: 물고기 " + goal + "마리 잡기\n\n" +
            "행운을 빕니다!",
            "게임 목표", 
            JOptionPane.INFORMATION_MESSAGE);

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
            // Keep_Going.png 표시
            showKeepGoing();
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
    
    // 레벨업 시 사용: 점수/돈/그물망 유지하고 화면만 초기화
    private void resetGameField() {
        // scorePanel.reset() 호출 안 함! (점수/돈/그물망 유지)
        lifePanel.resetLives();  // 목숨만 초기화
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
                        showGameOver();
                    }
                }
            }
        });
        fallingTimer.start();
        return fallingTimer;
    }

    // ========== 게임오버 화면 메서드들 ==========

    // Keep Going 화면 표시 (일시 정지)
    private void showKeepGoing() {
        gameOverPanel = new JPanel() {
            private Image keepGoingImage;
            
            {
                try {
                    keepGoingImage = new ImageIcon("Keep_Going.png").getImage();
                } catch (Exception e) {
                    System.out.println("Keep_Going.png 이미지를 불러올 수 없습니다");
                    e.printStackTrace();
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (keepGoingImage != null) {
                    g.drawImage(keepGoingImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(100, 200, 255, 200));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("맑은 고딕", Font.BOLD, 40));
                    String text = "KEEP GOING?";
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = getHeight() / 2;
                    g2d.drawString(text, x, y);
                }
            }
        };
        gameOverPanel.setOpaque(false);
        gameOverPanel.setLayout(null);
        gameOverPanel.setBounds(0, 0, ground.getWidth(), ground.getHeight());
        
        // CONTINUE 버튼 (Keep_Going.png의 버튼 위치에 맞게)
        JButton continueButton = new JButton();
        continueButton.setOpaque(false);
        continueButton.setContentAreaFilled(false);
        continueButton.setBorderPainted(false);
        continueButton.setFocusPainted(false);
        continueButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        continueButton.addActionListener(e -> {
            hideKeepGoing();
            resumeGame();
        });
        
        int panelWidth = ground.getWidth();
        int panelHeight = ground.getHeight();
        
        int buttonWidth = (int)(panelWidth * 0.45);
        int buttonHeight = (int)(panelHeight * 0.08);
        int x = (panelWidth - buttonWidth) / 2;
        int y = (int)(panelHeight * 0.42);  // 위로 올림 (화면의 42% 지점)
        
        continueButton.setBounds(x, y, buttonWidth, buttonHeight);
        
        gameOverPanel.add(continueButton);
        
        ground.setLayout(null);
        ground.add(gameOverPanel);
        ground.setComponentZOrder(gameOverPanel, 0);
        ground.revalidate();
        ground.repaint();
    }
    
    // Keep Going 화면 숨기기
    private void hideKeepGoing() {
        if (gameOverPanel != null) {
            ground.remove(gameOverPanel);
            gameOverPanel = null;
            ground.revalidate();
            ground.repaint();
        }
    }
    
    // 게임 재개
    private void resumeGame() {
        isPaused = false;
        if (gameTimer != null) gameTimer.start();
        for (Timer timer : fallingTimers) {
            timer.start();
        }
    }
    
    // 낚시왕 화면 표시 (3단계 완료)
    private void showFishingKing() {
        gameOverPanel = new JPanel() {
            private Image fishingKingImage;
            
            {
                try {
                    fishingKingImage = new ImageIcon("FishingKing.png").getImage();
                } catch (Exception e) {
                    System.out.println("FishingKing.png 이미지를 불러올 수 없습니다");
                    e.printStackTrace();
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (fishingKingImage != null) {
                    g.drawImage(fishingKingImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(173, 216, 230, 200));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("맑은 고딕", Font.BOLD, 40));
                    String text = "YOU ARE FISHING KING!";
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = getHeight() / 2;
                    g2d.drawString(text, x, y);
                }
            }
        };
        gameOverPanel.setOpaque(false);
        gameOverPanel.setLayout(null);
        gameOverPanel.setBounds(0, 0, ground.getWidth(), ground.getHeight());
        
        // MAIN MENU 버튼 (FishingKing.png의 버튼 위치에 맞게)
        JButton menuButton = new JButton();
        menuButton.setOpaque(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setBorderPainted(false);
        menuButton.setFocusPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuButton.addActionListener(e -> goToMainMenu());
        
        int panelWidth = ground.getWidth();
        int panelHeight = ground.getHeight();
        
        int buttonWidth = (int)(panelWidth * 0.4);
        int buttonHeight = (int)(panelHeight * 0.08);
        int x = (panelWidth - buttonWidth) / 2;
        int y = (int)(panelHeight * 0.3);  // FishingKing.png의 버튼 위치
        
        menuButton.setBounds(x, y, buttonWidth, buttonHeight);
        
        gameOverPanel.add(menuButton);
        
        ground.setLayout(null);
        ground.add(gameOverPanel);
        ground.setComponentZOrder(gameOverPanel, 0);
        ground.revalidate();
        ground.repaint();
    }
    
    // Quit 화면 표시 (중도 포기)
    private void showQuit() {
        gameOverPanel = new JPanel() {
            private Image quitImage;
            
            {
                try {
                    quitImage = new ImageIcon("Quit.png").getImage();
                } catch (Exception e) {
                    System.out.println("Quit.png 이미지를 불러올 수 없습니다");
                    e.printStackTrace();
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (quitImage != null) {
                    g.drawImage(quitImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(173, 216, 230, 200));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("맑은 고딕", Font.BOLD, 40));
                    String text = "TO BE CONTINUE";
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = getHeight() / 2;
                    g2d.drawString(text, x, y);
                }
            }
        };
        gameOverPanel.setOpaque(false);
        gameOverPanel.setLayout(null);
        gameOverPanel.setBounds(0, 0, ground.getWidth(), ground.getHeight());
        
        // MAIN MENU 버튼 (Quit.png의 버튼 위치에 맞게 - 중앙)
        JButton menuButton = new JButton();
        menuButton.setOpaque(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setBorderPainted(false);
        menuButton.setFocusPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuButton.addActionListener(e -> goToMainMenu());
        
        int panelWidth = ground.getWidth();
        int panelHeight = ground.getHeight();
        
        int buttonWidth = (int)(panelWidth * 0.35);
        int buttonHeight = (int)(panelHeight * 0.08);
        int x = (panelWidth - buttonWidth) / 2;
        int y = (int)(panelHeight * 0.48);  // 중앙 위치로 수정 (화면의 48% 지점)
        
        menuButton.setBounds(x, y, buttonWidth, buttonHeight);
        
        gameOverPanel.add(menuButton);
        
        ground.setLayout(null);
        ground.add(gameOverPanel);
        ground.setComponentZOrder(gameOverPanel, 0);
        ground.revalidate();
        ground.repaint();
    }
    
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
                    g.drawImage(gameOverImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        gameOverPanel.setOpaque(false);
        gameOverPanel.setLayout(null);
        gameOverPanel.setBounds(0, 0, ground.getWidth(), ground.getHeight());
        
        JButton menuButton = new JButton();
        menuButton.setOpaque(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setBorderPainted(false);
        menuButton.setFocusPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuButton.addActionListener(e -> goToMainMenu());
        
        int panelWidth = ground.getWidth();
        int panelHeight = ground.getHeight();
        
        int buttonWidth = (int)(panelWidth * 0.4);
        int buttonHeight = (int)(panelHeight * 0.08);
        int x = (panelWidth - buttonWidth) / 2;
        int y = (int)(panelHeight * 0.45);
        
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