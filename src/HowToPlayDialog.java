import javax.swing.*;
import java.awt.*;

public class HowToPlayDialog extends JDialog {
    
    public HowToPlayDialog(JFrame parent) {
        super(parent, "게임 방법", true);  // 모달 다이얼로그
        
        setSize(920, 620);
        setResizable(false);
        setLocationRelativeTo(parent);
        
        createUI();
        setVisible(true);
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(135, 206, 235));
        
        // 이미지 패널
        JPanel imagePanel = new JPanel() {
            private Image howToPlayImage;
            
            {
                try {
                    howToPlayImage = new ImageIcon("HowToPlay.png").getImage();
                } catch (Exception e) {
                    System.out.println("HowToPlay.png 이미지를 불러올 수 없습니다");
                    e.printStackTrace();
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (howToPlayImage != null) {
                    // 이미지를 패널 크기에 맞게 그리기
                    g.drawImage(howToPlayImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // 이미지 로드 실패 시 파란 배경
                    g.setColor(new Color(135, 206, 235));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("맑은 고딕", Font.BOLD, 20));
                    g.drawString("HowToPlay.png를 찾을 수 없습니다", 250, 250);
                }
            }
        };
        
        add(imagePanel, BorderLayout.CENTER);
        
        // 투명 버튼을 이미지에 직접 오버레이
        imagePanel.setLayout(null);
        
        JButton closeButton = new JButton();
        closeButton.setOpaque(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());
        
        // 이미지 내 MAIN MENU 버튼 위치
        // 원본 이미지: 1309x765
        // MAIN MENU 버튼: 하단 중앙 (약 85% 높이 위치)
        int buttonWidth = 340;
        int buttonHeight = 70;
        int x = (920 - buttonWidth) / 2;  // 중앙 정렬
        int y = 470;  // 이미지 내 버튼 위치 (약 85% 지점)
        
        closeButton.setBounds(x, y, buttonWidth, buttonHeight);
        imagePanel.add(closeButton);
    }
}