import javax.swing.*;
import java.awt.*;

public class GameSetupPanel extends JFrame {
    private JTextField nameField;
    private JTextField ipField;
    private JTextField portField;
    private JComboBox<String> levelCombo;
    private JFrame parentFrame;

    public GameSetupPanel(JFrame parent) {
        this.parentFrame = parent;

        setTitle("배 탑승");
        setSize(600, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ---------------------------
        // 배경 이미지 패널
        // ---------------------------
        JPanel bgPanel = new JPanel(null) {
            Image bg = new ImageIcon("SetupBG.png").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        setContentPane(bgPanel);

        Font font = new Font("맑은 고딕", Font.BOLD, 18);

        // ---------------------------
        // 입력 필드 생성 + 기본값 적용
        // ---------------------------
        nameField = createField(font);

        ipField = createField(font);
        ipField.setText("127.0.0.1");  // ⭐ 기본값 설정

        portField = createField(font);
        portField.setText("30000");    // ⭐ 기본값 설정

        levelCombo = new JComboBox<>(new String[]{"쉬움", "보통", "어려움"});
        levelCombo.setFont(font);
        levelCombo.setOpaque(false);
        levelCombo.setBackground(new Color(255, 255, 255, 180));
        levelCombo.setBorder(BorderFactory.createLineBorder(new Color(150, 120, 100), 3));
        bgPanel.add(levelCombo);

        // ---------------------------
        // 좌표 배치 (SetupBG 이미지 기준)
        // ---------------------------
        nameField.setBounds(300, 125, 220, 40);
        ipField.setBounds(300, 215, 220, 40);
        portField.setBounds(300, 305, 220, 40);
        levelCombo.setBounds(300, 395, 220, 40);

        bgPanel.add(nameField);
        bgPanel.add(ipField);
        bgPanel.add(portField);

        // ---------------------------
        // 이미지 버튼 클릭 영역
        // ---------------------------
        JButton imageStartButton = new JButton();
        imageStartButton.setBounds(190, 540, 260, 80);  // ← "낚시 시작" 이미지 위치
        imageStartButton.setOpaque(false);
        imageStartButton.setContentAreaFilled(false);
        imageStartButton.setBorderPainted(false);
        imageStartButton.setFocusPainted(false);
        imageStartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 클릭 시 게임 시작
        imageStartButton.addActionListener(e -> startGame());

        bgPanel.add(imageStartButton);

        setVisible(true);
    }

    private JTextField createField(Font font) {
        JTextField field = new JTextField();
        field.setFont(font);
        field.setOpaque(false);
        field.setForeground(Color.BLACK);
        field.setBackground(new Color(255, 255, 255, 180));
        field.setBorder(BorderFactory.createLineBorder(new Color(150, 120, 100), 3));
        return field;
    }

    // ---------------------------
    // 게임 시작
    // ---------------------------
    private void startGame() {
        String user = nameField.getText().trim();
        String ip = ipField.getText().trim();
        String port = portField.getText().trim();
        String level = (String) levelCombo.getSelectedItem();

        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름을 입력하세요!");
            return;
        }

        new MultiGameFrame(user, level, ip, port);

        dispose();
        parentFrame.dispose();
    }
}
