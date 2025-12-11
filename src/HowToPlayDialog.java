import javax.swing.*;
import java.awt.*;

// 게임 방법을 설명하는 다이얼로그 클래스
public class HowToPlayDialog extends JDialog {
    
    // 생성자 - 부모 프레임을 받아서 모달 다이얼로그 생성
    public HowToPlayDialog(JFrame parent) {
        super(parent, "게임 방법", true);  // 모달 다이얼로그로 설정
        
        // 다이얼로그 크기 및 위치 설정
        setSize(920, 620);
        setResizable(false); // 크기 변경 불가
        setLocationRelativeTo(parent); // 부모 중앙에 위치
        
        createUI(); // UI 생성
        setVisible(true); // 다이얼로그 표시
    }
    
    // UI 컴포넌트 생성 메서드
    private void createUI() {
        setLayout(new BorderLayout()); // BorderLayout 설정
        getContentPane().setBackground(new Color(135, 206, 235)); // 하늘색 배경
        
        // 이미지를 표시하는 패널 (익명 클래스)
        JPanel imagePanel = new JPanel() {
            private Image howToPlayImage; // 게임 설명 이미지
            
            {
                // 이미지 로드 초기화 블록
                try {
                    howToPlayImage = new ImageIcon("HowToPlay.png").getImage();
                } catch (Exception e) {
                    System.out.println("HowToPlay.png 이미지를 불러올 수 없습니다");
                    e.printStackTrace();
                }
            }
            
            // 패널에 이미지 그리기
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (howToPlayImage != null) {
                    // 이미지가 있으면 패널 크기에 맞게 그리기
                    g.drawImage(howToPlayImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // 이미지 로드 실패 시 대체 메시지 표시
                    g.setColor(new Color(135, 206, 235)); // 하늘색 배경
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("맑은 고딕", Font.BOLD, 20));
                    g.drawString("HowToPlay.png를 찾을 수 없습니다", 250, 250);
                }
            }
        };
        
        add(imagePanel, BorderLayout.CENTER); // 이미지 패널을 중앙에 배치
        
        // 투명 버튼을 이미지 위에 오버레이
        imagePanel.setLayout(null); // 절대 위치 레이아웃
        
        // 닫기 버튼 생성 및 투명하게 설정
        JButton closeButton = new JButton();
        closeButton.setOpaque(false); // 불투명 해제
        closeButton.setContentAreaFilled(false); // 배경 제거
        closeButton.setBorderPainted(false); // 테두리 제거
        closeButton.setFocusPainted(false); // 포커스 테두리 제거
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 손가락 커서
        closeButton.addActionListener(e -> dispose()); // 클릭 시 다이얼로그 닫기
        
        // 버튼 위치 및 크기 설정 (이미지 내 MAIN MENU 버튼 위치에 맞춤)
        int buttonWidth = 340;
        int buttonHeight = 70;
        int x = (920 - buttonWidth) / 2;  // 가로 중앙 정렬
        int y = 470;  // 세로 위치 (이미지의 약 85% 지점)
        
        closeButton.setBounds(x, y, buttonWidth, buttonHeight); // 절대 위치 설정
        imagePanel.add(closeButton); // 이미지 패널에 버튼 추가
    }
}