import javax.swing.*;
import java.awt.*;

public class MultiStartPanel extends JFrame {
    
    public MultiStartPanel() {
        setTitle("낚시 경매 마을");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(831, 510);  // 배경 이미지 크기
        setResizable(false);
        setLocationRelativeTo(null);
        
        // 메인 메뉴 화면 표시
        showMainMenu();
        
        setVisible(true);
    }
    
    // 메인 메뉴 화면
    private void showMainMenu() {
        JPanel mainPanel = new JPanel() {
            private Image backgroundImage;
            
            {
                // 배경 이미지 로드
                try {
                    backgroundImage = new ImageIcon("main.png").getImage();
                } catch (Exception e) {
                    System.out.println("배경 이미지를 불러올 수 없습니다: main.png");
                    e.printStackTrace();
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, this);
                } else {
                    // 이미지 로드 실패 시 파란색 배경
                    g.setColor(new Color(135, 206, 235));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(null);
        
        // START 버튼 (투명)
        JButton startButton = createTransparentButton();
        startButton.setBounds(210, 282, 180, 35);
        startButton.addActionListener(e -> openGameSetup());
        mainPanel.add(startButton);
        
        // HOW TO PLAY 버튼 (투명)
        JButton helpButton = createTransparentButton();
        helpButton.setBounds(458, 282, 180, 35);
        helpButton.addActionListener(e -> openHowToPlay());
        mainPanel.add(helpButton);
        
        setContentPane(mainPanel);
        revalidate();
        repaint();
    }
    
    // 투명 버튼 생성
    private JButton createTransparentButton() {
        JButton button = new JButton();
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    // 게임 설정 화면 열기
    private void openGameSetup() {
        new GameSetupPanel(this);
    }
    
    // 게임 방법 다이얼로그 열기
    private void openHowToPlay() {
        new HowToPlayDialog(this);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultiStartPanel());
    }
}