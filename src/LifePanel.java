import javax.swing.*;
import java.awt.*;

public class LifePanel extends JPanel {
    private int lives = 3;  // 시작 시 목숨 3개
    private Image baitImage;  // 미끼(목숨) 이미지
    private Image backgroundImage;  // 패널 배경 이미지

    public LifePanel() {
        // 미끼 이미지 로드
        ImageIcon baitIcon = new ImageIcon("bait.png");
        baitImage = baitIcon.getImage();
        
        // 배경 이미지 로드
        try {
            backgroundImage = new ImageIcon("baitBG.png").getImage();
        } catch (Exception e) {
            System.out.println("baitBG.png 이미지를 불러올 수 없습니다");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 배경 이미지가 있으면 그리기, 없으면 흰색 배경
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            setBackground(Color.WHITE);
        }

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // "남은 미끼" 텍스트 그리기
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        String text = "< 남은 미끼 >";
        
        // 텍스트를 중앙 정렬하여 그리기
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int textX = (panelWidth - textWidth) / 2;
        int textY = 30;

        g.drawString(text, textX, textY);

        // 미끼 이미지 크기 계산 (원본의 70% 크기)
        int imageWidth = (int)(60 * 0.7);
        int imageHeight = (int)(60 * 0.7);
        int totalHeight = imageHeight * lives + 10 * (lives - 1);  // 이미지 간격 포함한 총 높이
        int startY = (panelHeight - totalHeight) / 2;  // 세로 중앙 정렬
        int xOffset = (panelWidth - imageWidth) / 2;  // 가로 중앙 정렬

        // 남은 목숨만큼 미끼 이미지 세로로 그리기
        for (int i = 0; i < lives; i++) {
            g.drawImage(baitImage, xOffset, startY + i * (imageHeight + 10), imageWidth, imageHeight, this);
        }
    }

    // 목숨 감소 메서드
    public void decreaseLife() {
        if (lives > 0) {
            lives--;  // 목숨 1 감소
            repaint();  // 화면 다시 그리기
        }
    }

    // 목숨을 3개로 초기화하는 메서드
    public void resetLives() {
        lives = 3;
        repaint();
    }

    // 현재 남은 목숨 개수 반환
    public int getLives() {
        return lives;
    }
    
    // 게임 오버 여부 확인 (목숨이 0 이하면 true)
    public boolean isGameOver() {
        return lives <= 0;
    }
}