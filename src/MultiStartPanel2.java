import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MultiStartPanel2 extends JPanel {
    private JTextField userNameField = new JTextField(15);
    private JTextField ipField = new JTextField(15);
    private JTextField portField = new JTextField(15);
    private JComboBox<String> levelComboBox;
    private JButton startButton = new JButton("🎣 게임 시작");
    private JButton addFishButton = new JButton("🐟 Fish Edit");
    private Image backgroundImage;
    private TextSource textSource;

    public MultiStartPanel2(JFrame parentFrame) {
        backgroundImage = new ImageIcon("ocean.jpg").getImage();
        textSource = new TextSource();

        setLayout(new BorderLayout());

        // 중앙 패널
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(startButton);
        buttonPanel.add(addFishButton);
        add(buttonPanel, BorderLayout.SOUTH);

        customizeButtons();
        setupButtonActions(parentFrame);
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // 타이틀
        JLabel titleLabel = new JLabel("🎣 멀티플레이어 낚시 타자 게임 🎣");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        titleLabel.setForeground(Color.BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));

        // 입력 필드들
        panel.add(createInputRow("👤 사용자 이름:", userNameField));
        panel.add(Box.createVerticalStrut(15));
        
        panel.add(createInputRow("🌐 서버 IP:", ipField));
        ipField.setText("127.0.0.1");
        panel.add(Box.createVerticalStrut(15));
        
        panel.add(createInputRow("🔌 포트:", portField));
        portField.setText("30000");
        panel.add(Box.createVerticalStrut(15));

        // 난이도 선택
        JPanel levelPanel = new JPanel();
        levelPanel.setOpaque(false);
        levelPanel.add(new JLabel("⭐ 난이도:"));
        levelComboBox = new JComboBox<>(new String[]{"쉬움", "보통", "어려움"});
        levelComboBox.setPreferredSize(new Dimension(200, 30));
        levelComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        levelPanel.add(levelComboBox);
        panel.add(levelPanel);

        return panel;
    }

    private JPanel createInputRow(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        label.setPreferredSize(new Dimension(150, 30));
        
        textField.setPreferredSize(new Dimension(200, 30));
        textField.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        
        panel.add(label);
        panel.add(textField);
        
        return panel;
    }

    private void customizeButtons() {
        Font buttonFont = new Font("맑은 고딕", Font.BOLD, 20);
        JButton[] buttons = {startButton, addFishButton};
        
        for (JButton button : buttons) {
            button.setFont(buttonFont);
            button.setPreferredSize(new Dimension(200, 50));
            button.setBackground(new Color(100, 200, 255));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(true);
        }
    }

    private void setupButtonActions(JFrame parentFrame) {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName = userNameField.getText().trim();
                String ipAddress = ipField.getText().trim();
                String portNo = portField.getText().trim();
                String level = (String) levelComboBox.getSelectedItem();

                if (userName.isEmpty()) {
                    JOptionPane.showMessageDialog(null, 
                        "사용자 이름을 입력하세요!", 
                        "오류", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (ipAddress.isEmpty()) {
                    JOptionPane.showMessageDialog(null, 
                        "서버 IP를 입력하세요!", 
                        "오류", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (portNo.isEmpty()) {
                    JOptionPane.showMessageDialog(null, 
                        "포트 번호를 입력하세요!", 
                        "오류", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                parentFrame.dispose();
                new MultiGameFrame(userName, level, ipAddress, portNo);
            }
        });

        addFishButton.addActionListener(e -> {
            JFrame editFrame = new JFrame("물고기 추가");
            editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            editFrame.setSize(400, 300);
            editFrame.setContentPane(new EditPanel(textSource));
            editFrame.setVisible(true);
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("멀티플레이어 낚시 타자 게임");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new MultiStartPanel2(frame));
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}