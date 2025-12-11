import javax.swing.*;
import java.awt.*;

public class GameSetupPanel extends JFrame {
    private JTextField nameField;  // 이름 입력 필드
    private JTextField ipField;  // IP 주소 입력 필드
    private JTextField portField;  // 포트 번호 입력 필드
    private JComboBox<String> levelCombo;  // 난이도 선택 콤보박스
    private JFrame parentFrame;  // 부모 프레임 참조

    // 생성자: 부모 프레임을 받아 게임 설정 창 생성
    public GameSetupPanel(JFrame parent) {
        this.parentFrame = parent;

        setTitle("배 탑승");
        setSize(600, 720);
        setResizable(false);  // 창 크기 조절 불가
        setLocationRelativeTo(null);  // 화면 중앙에 배치
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 배경 이미지가 있는 패널 생성
        JPanel bgPanel = new JPanel(null) {
            Image bg = new ImageIcon("SetupBG.png").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);  // 배경 이미지 그리기
            }
        };
        setContentPane(bgPanel);

        Font font = new Font("맑은 고딕", Font.BOLD, 18);

        // 입력 필드 생성
        nameField = createField(font);

        ipField = createField(font);
        ipField.setText("127.0.0.1");  // IP 기본값 설정

        portField = createField(font);
        portField.setText("30000");  // 포트 기본값 설정

        // 난이도 선택 콤보박스 생성
        levelCombo = new JComboBox<>(new String[]{"쉬움", "보통", "어려움"});
        levelCombo.setFont(font);
        levelCombo.setOpaque(false);
        levelCombo.setBackground(new Color(255, 255, 255, 180));  // 반투명 배경
        levelCombo.setBorder(BorderFactory.createLineBorder(new Color(150, 120, 100), 3));
        bgPanel.add(levelCombo);

        // 각 컴포넌트의 위치 설정 (배경 이미지 기준)
        nameField.setBounds(300, 125, 220, 40);
        ipField.setBounds(300, 215, 220, 40);
        portField.setBounds(300, 305, 220, 40);
        levelCombo.setBounds(300, 395, 220, 40);

        bgPanel.add(nameField);
        bgPanel.add(ipField);
        bgPanel.add(portField);

        // 낚시 시작 버튼 (이미지 위에 투명 버튼 배치)
        JButton imageStartButton = new JButton();
        imageStartButton.setBounds(190, 540, 260, 80);  // 버튼 위치 설정
        imageStartButton.setOpaque(false);  // 투명하게 설정
        imageStartButton.setContentAreaFilled(false);
        imageStartButton.setBorderPainted(false);
        imageStartButton.setFocusPainted(false);
        imageStartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));  // 손 모양 커서

        // 버튼 클릭 시 게임 시작
        imageStartButton.addActionListener(e -> startGame());

        bgPanel.add(imageStartButton);

        setVisible(true);
    }

    // 투명한 텍스트 필드 생성 메서드
    private JTextField createField(Font font) {
        JTextField field = new JTextField();
        field.setFont(font);
        field.setOpaque(false);  // 투명하게 설정
        field.setForeground(Color.BLACK);
        field.setBackground(new Color(255, 255, 255, 180));  // 반투명 배경
        field.setBorder(BorderFactory.createLineBorder(new Color(150, 120, 100), 3));
        return field;
    }

    // 게임 시작 메서드
    private void startGame() {
        String user = nameField.getText().trim();  // 이름 입력값 가져오기
        String ip = ipField.getText().trim();  // IP 입력값 가져오기
        String port = portField.getText().trim();  // 포트 입력값 가져오기
        String level = (String) levelCombo.getSelectedItem();  // 선택된 난이도 가져오기

        if (user.isEmpty()) {  // 이름이 비어있으면 경고
            JOptionPane.showMessageDialog(this, "이름을 입력하세요!");
            return;
        }

        new MultiGameFrame(user, level, ip, port);  // 게임 프레임 생성

        dispose();  // 현재 창 닫기
        parentFrame.dispose();  // 부모 창 닫기
    }
}