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

// 점수와 잡은 물고기를 표시하는 패널 클래스
public class ScorePanel extends JPanel {

    private int score = 0; // 점수 = 돈 통합
    private JLabel moneyLabel = new JLabel("0원"); // 보유 금액 라벨
    private JLabel titleLabel = new JLabel("< 그물망 >"); // 제목 라벨

    private JPanel caughtFishPanel = new JPanel(); // 잡은 물고기 목록 패널
    private JScrollPane scrollPane; // 스크롤 가능한 패널

    private Map<String, Integer> caughtFishCount = new HashMap<>(); // 물고기 이름과 개수 저장
    private Map<String, Integer> fishPrices = new HashMap<>(); // 물고기 이름과 가격 저장

    private Image backgroundImage; // 배경 이미지
    private Image netImage; // 그물망 이미지

    // 생성자 - UI 초기화 및 물고기 가격 로드
    public ScorePanel() {

        // 배경 이미지 로드
        try { backgroundImage = new ImageIcon("NetBG.png").getImage(); } catch (Exception e) {}
        // 그물망 이미지 로드
        try { netImage = new ImageIcon("Net.png").getImage(); } catch (Exception e) {}

        // Y축 방향 박스 레이아웃 설정
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false); // 투명 배경

        // 제목 라벨 설정 및 추가
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setForeground(Color.DARK_GRAY);
        add(titleLabel);

        add(Box.createVerticalStrut(5)); // 5픽셀 간격

        // 보유 금액 라벨 설정 및 추가
        moneyLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        moneyLabel.setForeground(Color.BLUE);
        add(moneyLabel);

        add(Box.createVerticalStrut(10)); // 10픽셀 간격

        // 잡은 물고기 목록 패널 설정
        caughtFishPanel.setLayout(new BoxLayout(caughtFishPanel, BoxLayout.Y_AXIS));
        caughtFishPanel.setOpaque(false); // 투명 배경

        // 스크롤 패널 설정 및 추가
        scrollPane = new JScrollPane(caughtFishPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // 필요시 세로 스크롤바
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // 가로 스크롤바 없음
        scrollPane.setOpaque(false); // 투명 배경
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null); // 테두리 제거

        add(scrollPane);

        // 파일에서 물고기 가격 로드
        loadFishPricesFromFile("fish_prices.txt");
    }

    // 물고기를 잡았을 때 점수/돈 증가
    public void increase(String fishName) {
        Integer fishPrice = fishPrices.get(fishName); // 물고기 가격 조회
        if (fishPrice != null) {
            score += fishPrice; // 점수에 가격 추가
            updateLabels(); // UI 업데이트
        }
    }

    // 점수 UI 갱신
    private void updateLabels() {
        moneyLabel.setText(score + "원");
    }

    // 돈(=점수) 사용 - 충분한 금액이 있으면 true 반환
    public boolean spendScore(int amount) {
        if (score >= amount) {
            score -= amount; // 점수 차감
            updateLabels(); // UI 업데이트
            return true;
        }
        return false;
    }

    // 돈(=점수) 증가
    public void addScore(int amount) {
        score += amount;
        updateLabels();
    }

    // 현재 점수 반환
    public int getScore() {
        return score;
    }

    // 게임 리셋 - 모든 데이터 초기화
    public void reset() {
        score = 0;
        caughtFishCount.clear(); // 잡은 물고기 목록 초기화
        updateLabels();
        caughtFishPanel.removeAll(); // 패널의 모든 컴포넌트 제거
        caughtFishPanel.revalidate();
        caughtFishPanel.repaint();
    }

    // ================================
    //      잡은 물고기 관리 기능
    // ================================

    // 잡은 물고기 추가
    public void addCaughtFish(String fishName) {
        caughtFishCount.put(fishName, caughtFishCount.getOrDefault(fishName, 0) + 1);
        updateCaughtFishDisplay(); // 화면 업데이트
    }

    // 잡은 물고기 제거 (경매 등에 사용)
    public void removeCaughtFish(String fishName) {
        int count = caughtFishCount.getOrDefault(fishName, 0);

        if (count > 1) {
            caughtFishCount.put(fishName, count - 1); // 개수 1 감소
        } else if (count == 1) {
            caughtFishCount.remove(fishName); // 목록에서 제거
        }

        updateCaughtFishDisplay(); // 화면 업데이트
    }

    // 잡은 물고기 목록 화면 업데이트
    private void updateCaughtFishDisplay() {
        caughtFishPanel.removeAll(); // 기존 라벨 모두 제거

        // 각 물고기를 라벨로 추가
        for (Map.Entry<String, Integer> entry : caughtFishCount.entrySet()) {
            JLabel fishLabel = new JLabel(entry.getKey() + ": " + entry.getValue());
            fishLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            caughtFishPanel.add(fishLabel);
        }

        caughtFishPanel.revalidate(); // 레이아웃 재계산
        caughtFishPanel.repaint(); // 화면 다시 그리기
    }

    // ================================
    //       물고기 가격 로딩
    // ================================

    // 파일에서 물고기 가격 정보 읽기
    private void loadFishPricesFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(","); // 쉼표로 구분
                if (parts.length == 2) {
                    // 물고기 이름과 가격을 맵에 저장
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

    // 패널에 배경 이미지 그리기
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 배경 이미지 그리기
        if (backgroundImage != null)
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // 그물망 이미지 그리기
        if (netImage != null)
            g.drawImage(netImage, 0, 0, getWidth(), getHeight(), this);
    }
}