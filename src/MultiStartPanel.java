import javax.swing.*;
import java.awt.*;

public class MultiStartPanel extends JFrame {
    
    public MultiStartPanel() {
        setTitle("낚시 경매 마을");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(831, 510);  // 배경 이미지 크기에 맞춤
        setResizable(false);  // 창 크기 조절 불가
        setLocationRelativeTo(null);  // 화면 중앙에 배치
        
        // 메인 메뉴 화면 표시
        showMainMenu();
        
        setVisible(true);
    }
    
    // 메인 메뉴 화면 구성
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
                    g.drawImage(backgroundImage, 0, 0, this);  // 배경 이미지 그리기
                } else {
                    // 이미지 로드 실패 시 하늘색 배경
                    g.setColor(new Color(135, 206, 235));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(null);  // 절대 위치 레이아웃
        
        // START 버튼 (투명)
        JButton startButton = createTransparentButton();
        startButton.setBounds(210, 282, 180, 35);  // 버튼 위치 및 크기 설정
        startButton.addActionListener(e -> openGameSetup());  // 클릭 시 게임 설정 창 열기
        mainPanel.add(startButton);
        
        // HOW TO PLAY 버튼 (투명)
        JButton helpButton = createTransparentButton();
        helpButton.setBounds(458, 282, 180, 35);  // 버튼 위치 및 크기 설정
        helpButton.addActionListener(e -> openHowToPlay());  // 클릭 시 게임 방법 다이얼로그 열기
        mainPanel.add(helpButton);
        
        setContentPane(mainPanel);  // 메인 패널을 컨텐츠로 설정
        revalidate();  // 레이아웃 갱신
        repaint();  // 화면 다시 그리기
    }
    
    // 투명 버튼 생성 메서드
    private JButton createTransparentButton() {
        JButton button = new JButton();
        button.setOpaque(false);  // 불투명 설정 해제
        button.setContentAreaFilled(false);  // 내용 영역 채우기 해제
        button.setBorderPainted(false);  // 테두리 그리기 해제
        button.setFocusPainted(false);  // 포커스 표시 해제
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));  // 손 모양 커서
        return button;
    }
    
    // 게임 설정 창 열기
    private void openGameSetup() {
        new GameSetupPanel(this);
    }
    
    // 게임 방법 다이얼로그 열기
    private void openHowToPlay() {
        new HowToPlayDialog(this);
    }
    
    // 프로그램 시작점
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultiStartPanel());
    }
}