import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScorePanel extends JPanel {
    private int score = 0; // 현재 점수를 저장하는 변수
    private int money = 0; // 경매용 돈 (점수와 동일하게 시작)
    private JLabel moneyLabel = new JLabel("0원");
    private JLabel textLabel = new JLabel("< 그물망 >");
    private JLabel scoreLabel = new JLabel(Integer.toString(score));
    private JPanel caughtFishPanel = new JPanel(); // 잡힌 물고기 이름을 표시할 패널
    private Map<String, Integer> caughtFishCount = new HashMap<>(); // 물고기 이름과 그 물고기가 잡힌 횟수를 저장하는 맵

    private Map<String, Integer> fishPrices = new HashMap<>(); // 물고기 이름과 가격을 저장하는 맵

    public ScorePanel() {
    	 // 레이아웃 설정: Y축으로 컴포넌트가 쌓이도록 설정
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

        // 점수 텍스트 레이블 설정
        textLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        textLabel.setForeground(Color.DARK_GRAY);
        add(textLabel);

        // 점수 값 레이블 설정
        scoreLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        scoreLabel.setForeground(Color.RED);
        add(scoreLabel);

        add(Box.createVerticalStrut(10)); // 간격 추가
     // 돈 표시 추가
        JLabel moneyTextLabel = new JLabel("💰 보유 금액:");
        moneyTextLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        moneyTextLabel.setForeground(Color.DARK_GRAY);
        add(moneyTextLabel);

        moneyLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        moneyLabel.setForeground(Color.BLUE);
        add(moneyLabel);
        add(Box.createVerticalStrut(10));

        // 잡힌 물고기 패널 설정
        caughtFishPanel.setLayout(new BoxLayout(caughtFishPanel, BoxLayout.Y_AXIS));
        add(caughtFishPanel);

        // 물고기 가격 로드
        loadFishPricesFromFile("fish_prices.txt");
    }

    // 점수 증가 메소드
    public void increase(String word) {
        Integer fishPrice = fishPrices.get(word);
        if (fishPrice != null) {
            score += fishPrice;
            money += fishPrice; // 돈도 같이 증가!
            scoreLabel.setText(Integer.toString(score));
            moneyLabel.setText(money + "원");
        }
    }
    
    // 잡힌 물고기 표시 메소드
    public void addCaughtFish(String fishName) {
        caughtFishCount.put(fishName, caughtFishCount.getOrDefault(fishName, 0) + 1);
        caughtFishPanel.removeAll(); // 기존 레이블 삭제

        // 잡힌 모든 물고기 이름과 개수를 레이블로 추가
        for (Map.Entry<String, Integer> entry : caughtFishCount.entrySet()) {
            JLabel fishLabel = new JLabel(entry.getKey() + ": " + entry.getValue());
            fishLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            caughtFishPanel.add(fishLabel); // 물고기 정보 레이블 추가
        }

        caughtFishPanel.revalidate(); // 레이아웃을 다시 계산하여 갱신
        caughtFishPanel.repaint(); // 화면을 다시 그리기
    }

    // 물고기 가격을 파일에서 읽어오는 메서드
    private void loadFishPricesFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                	// 물고기 이름과 가격을 맵에 추가
                    fishPrices.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // 파일 읽기 오류 발생 시 스택 트레이스 출력
        }
    }
    
    // 점수 반환 메소드 추가
    public int getScore() {
        return score;  // 현재 점수 반환
    }

    // 물고기 가격 맵을 반환하는 메서드
    public Map<String, Integer> getFishPrices() {
        return fishPrices;
    }
    
 // 점수와 잡힌 물고기 목록을 초기화하는 reset() 메서드 추가
    public void reset() {
        score = 0; // 점수 초기화
        caughtFishCount.clear(); // 잡힌 물고기 목록 초기화
        scoreLabel.setText(Integer.toString(score)); // 점수 레이블 업데이트
        caughtFishPanel.removeAll(); // 물고기 목록 패널 초기화
        caughtFishPanel.revalidate();  // 레이아웃을 다시 계산하여 갱신
        caughtFishPanel.repaint(); // 화면을 다시 그리기
    }
    public int getMoney() {
        return money;
    }

    // 돈 추가 (경매로 팔았을 때)
    public void addMoney(int amount) {
        money += amount;
        moneyLabel.setText(money + "원");
    }

    // 돈 차감 (경매로 샀을 때)
    public boolean spendMoney(int amount) {
        if (money >= amount) {
            money -= amount;
            moneyLabel.setText(money + "원");
            return true;
        }
        return false; // 돈이 부족함
    }
}
