import javax.swing.*;
import java.awt.*;

public class HowToPlayDialog extends JDialog {
    
    public HowToPlayDialog(JFrame parent) {
        super(parent, "게임 방법", true);  // 모달 다이얼로그
        
        setSize(700, 600);
        setResizable(false);
        setLocationRelativeTo(parent);
        
        createUI();
        setVisible(true);
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(135, 206, 235));
        
        // 제목
        JLabel titleLabel = new JLabel("🎮 게임 방법 🎮", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // 게임 방법 텍스트
        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setMargin(new Insets(20, 30, 20, 30));
        helpText.setBackground(new Color(245, 255, 250));
        helpText.setText(
            "=== 🎯 게임 목표 ===\n" +
            "떨어지는 물고기 이름을 타이핑하여 물고기를 잡고,\n" +
            "경매로 거래하며 돈을 모으세요!\n\n" +
            
            "=== 🎮 게임 방법 ===\n" +
            "1. 화면 위에서 떨어지는 단어를 입력창에 타이핑하세요\n" +
            "2. Enter를 누르면 물고기를 잡을 수 있습니다\n" +
            "3. 물고기를 잡으면 점수와 돈을 얻습니다\n" +
            "4. 물고기가 바닥에 떨어지면 미끼(생명) 1개를 잃습니다\n" +
            "5. 미끼 3개가 모두 소진되면 게임 오버!\n\n" +
            
            "=== 🐟 물고기 종류 ===\n" +
            "• 저렴한 물고기 (50점 이하): 50% 확률로 등장\n" +
            "  → 고등어, 삼치, 도미 등\n" +
            "• 중간 가격 (51-90점): 30% 확률로 등장\n" +
            "  → 연어, 갈치, 우럭 등\n" +
            "• 고가 물고기 (91점 이상): 20% 확률로 등장\n" +
            "  → 참치, 광어, 농어, 방어 등\n\n" +
            
            "=== 🔨 경매 시스템 ===\n" +
            "1. '내 물고기 경매' 버튼으로 보유 물고기를 경매 등록\n" +
            "2. 시작가는 물고기 가격의 90% (10% 할인)\n" +
            "3. 다른 플레이어들이 30초 동안 입찰 가능\n" +
            "4. 최고가 입찰자가 자동 낙찰!\n" +
            "5. 판매자는 돈을 받고, 구매자는 물고기를 받습니다\n\n" +
            
            "=== 💬 채팅 기능 ===\n" +
            "• 우측 채팅창에서 다른 플레이어와 대화하세요\n" +
            "• 물고기 잡기, 경매 등 게임 활동이 실시간으로 표시됩니다\n" +
            "• 입장/퇴장 알림도 자동으로 표시됩니다\n\n" +
            
            "=== 💰 돈 시스템 ===\n" +
            "• 잡은 물고기 점수 = 보유 금액\n" +
            "• 경매로 물고기를 팔면 돈이 증가합니다\n" +
            "• 경매로 물고기를 사면 돈이 차감됩니다\n\n" +
            
            "=== 💡 게임 팁 ===\n" +
            "✨ 고가 물고기는 희귀하니 신중하게 거래하세요!\n" +
            "✨ 빠른 타이핑 실력이 승리의 열쇠입니다!\n" +
            "✨ 협동하여 더 많은 물고기를 모으세요!\n" +
            "✨ 같은 물고기가 연속으로 나오지 않으니 집중하세요!"
        );
        
        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);
        
        // 닫기 버튼
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(135, 206, 235));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        JButton closeButton = new JButton("✓ 확인");
        closeButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        closeButton.setPreferredSize(new Dimension(150, 40));
        closeButton.setBackground(new Color(70, 130, 180));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}