import javax.swing.*;
import java.awt.*;

public class LifePanel extends JPanel {
    private int lives = 3; // 시작 시 목숨 3개
    private Image baitImage; // 목숨 이미지를 위한 이미지 객체

    public LifePanel() {
        // "bait.png" 이미지를 로드하여 아이콘 설정
        ImageIcon baitIcon = new ImageIcon("bait.png");
        baitImage = baitIcon.getImage();  // 이미지 객체로 변환하여 사용
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 배경 그리기 (필요한 경우)
        setBackground(Color.WHITE);

        // 패널의 크기
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // "남은 미끼" 텍스트 그리기
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 20));  // 폰트 설정
        String text = "< 남은 미끼 >";
        
        // 텍스트 중앙 정렬
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int textX = (panelWidth - textWidth) / 2;  // 중앙 정렬
        int textY = 30;  // 텍스트의 세로 위치 (위에서부터)

        g.drawString(text, textX, textY);  // 텍스트 그리기

        // 목숨을 세로로 정렬하려면 yOffset 위치를 수정
        int imageWidth = 60;  // 이미지 가로 크기
        int imageHeight = 60;  // 이미지 세로 크기
        int totalHeight = imageHeight * lives + 10 * (lives - 1);  // 총 이미지 세로 크기 (이미지 간격 포함)
        int startY = (panelHeight - totalHeight) / 2;  // 이미지의 시작 Y 위치를 중앙으로 설정
        int xOffset = (panelWidth - imageWidth) / 2;  // 이미지를 가로 중앙으로 정렬

        // 각 목숨을 이미지로 표시
        for (int i = 0; i < lives; i++) {
            g.drawImage(baitImage, xOffset, startY + i * (imageHeight + 10), imageWidth, imageHeight, this);
        }
    }

    // 목숨 감소 메서드
    public void decreaseLife() {
        if (lives > 0) {
            lives--; // 목숨 감소
            repaint(); // 패널을 다시 그려서 변경 사항을 반영
        }
    }

    // 목숨을 재설정하는 메서드 (게임 시작 시)
    public void resetLives() {
        lives = 3;  // 목숨 초기화
        repaint(); // 패널을 다시 그려서 초기화
    }

    // 현재 목숨 개수 반환
    public int getLives() {
        return lives;
    }
    
    // 게임 오버 여부 확인 메서드
    public boolean isGameOver() {
        return lives <= 0;  // 목숨이 0 이하일 경우 게임 오버
    }
}
