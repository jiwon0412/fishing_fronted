import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScorePanel extends JPanel {

    private int score = 0; // 점수 = 돈 통합
    private JLabel moneyLabel = new JLabel("0원");
    private JLabel titleLabel = new JLabel("< 그물망 >");

    private JPanel caughtFishPanel = new JPanel();
    private JScrollPane scrollPane;

    private Map<String, Integer> caughtFishCount = new HashMap<>();
    private Map<String, Integer> fishPrices = new HashMap<>();

    private Image backgroundImage;
    private Image netImage;

    public ScorePanel() {

        // 배경 이미지 로드
        try { backgroundImage = new ImageIcon("NetBG.png").getImage(); } catch (Exception e) {}
        try { netImage = new ImageIcon("Net.png").getImage(); } catch (Exception e) {}

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // 제목
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setForeground(Color.DARK_GRAY);
        add(titleLabel);

        add(Box.createVerticalStrut(5));

        // 보유 금액 (점수=돈)
        moneyLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        moneyLabel.setForeground(Color.BLUE);
        add(moneyLabel);

        add(Box.createVerticalStrut(10));

        // 잡힌 물고기 목록 패널
        caughtFishPanel.setLayout(new BoxLayout(caughtFishPanel, BoxLayout.Y_AXIS));
        caughtFishPanel.setOpaque(false);

        scrollPane = new JScrollPane(caughtFishPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        add(scrollPane);

        // 물고기 가격 로드
        loadFishPricesFromFile("fish_prices.txt");
    }

    // 점수/돈 증가
    public void increase(String fishName) {
        Integer fishPrice = fishPrices.get(fishName);
        if (fishPrice != null) {
            score += fishPrice;
            updateLabels();
        }
    }

    // 점수 UI 갱신
    private void updateLabels() {
        moneyLabel.setText(score + "원");
    }

    // 돈(=점수) 사용
    public boolean spendScore(int amount) {
        if (score >= amount) {
            score -= amount;
            updateLabels();
            return true;
        }
        return false;
    }

    // 돈(=점수) 증가
    public void addScore(int amount) {
        score += amount;
        updateLabels();
    }

    public int getScore() {
        return score;
    }

    // 게임 리셋
    public void reset() {
        score = 0;
        caughtFishCount.clear();
        updateLabels();
        caughtFishPanel.removeAll();
        caughtFishPanel.revalidate();
        caughtFishPanel.repaint();
    }

    // ================================
    //      잡은 물고기 관리 기능
    // ================================

    public void addCaughtFish(String fishName) {
        caughtFishCount.put(fishName, caughtFishCount.getOrDefault(fishName, 0) + 1);
        updateCaughtFishDisplay();
    }

    public void removeCaughtFish(String fishName) {
        int count = caughtFishCount.getOrDefault(fishName, 0);

        if (count > 1) {
            caughtFishCount.put(fishName, count - 1);
        } else if (count == 1) {
            caughtFishCount.remove(fishName);
        }

        updateCaughtFishDisplay();
    }

    private void updateCaughtFishDisplay() {
        caughtFishPanel.removeAll();

        for (Map.Entry<String, Integer> entry : caughtFishCount.entrySet()) {
            JLabel fishLabel = new JLabel(entry.getKey() + ": " + entry.getValue());
            fishLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            caughtFishPanel.add(fishLabel);
        }

        caughtFishPanel.revalidate();
        caughtFishPanel.repaint();
    }

    // ================================
    //       물고기 가격 로딩
    // ================================

    private void loadFishPricesFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    fishPrices.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                }
            }
        } catch (IOException e) {
            System.out.println("물고기 가격 파일을 읽을 수 없습니다.");
        }
    }

    // ================================
    //        배경 그리기
    // ================================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 배경 이미지
        if (backgroundImage != null)
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // 그물망 이미지
        if (netImage != null)
            g.drawImage(netImage, 0, 0, getWidth(), getHeight(), this);
    }
}
