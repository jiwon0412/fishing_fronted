import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// 물고기를 추가하는 편집 패널 클래스
public class EditPanel extends JPanel {
    private JTextField fishNameField = new JTextField(10);  // 물고기 이름 입력 필드
    private JTextField fishScoreField = new JTextField(10); // 물고기 점수 입력 필드
    private JButton addButton = new JButton("물고기 추가"); // 물고기 추가 버튼
    private TextSource textSource; // 물고기 정보를 저장하고 관리하는 TextSource 객체

    // EditPanel 생성자 - TextSource를 받아서 초기화
    public EditPanel(TextSource textSource) {
        this.textSource = textSource;

        // Y축 방향 박스 레이아웃 설정
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 물고기 이름 입력 패널 생성 및 추가
        JPanel namePanel = new JPanel();
        namePanel.add(new JLabel("물고기 이름:")); // 라벨 추가
        namePanel.add(fishNameField); // 입력 필드 추가
        add(namePanel); // EditPanel에 추가

        // 물고기 점수 입력 패널 생성 및 추가
        JPanel scorePanel = new JPanel();
        scorePanel.add(new JLabel("물고기 점수:")); // 라벨 추가
        scorePanel.add(fishScoreField); // 입력 필드 추가
        add(scorePanel); // EditPanel에 추가

        // 물고기 추가 버튼 패널 생성 및 추가
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton); // 추가 버튼 추가
        add(buttonPanel); // EditPanel에 추가

        // 물고기 추가 버튼 클릭 이벤트 처리
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 입력 필드에서 텍스트 가져오기
                String fishName = fishNameField.getText().trim(); // 물고기 이름
                String fishScoreStr = fishScoreField.getText().trim(); // 물고기 점수

                // 입력값이 비어있는지 확인
                if (fishName.isEmpty() || fishScoreStr.isEmpty()) {
                    // 입력값이 없으면 오류 메시지 표시
                    JOptionPane.showMessageDialog(null, "물고기 이름과 점수를 모두 입력해주세요!", "오류", JOptionPane.ERROR_MESSAGE);
                    return; // 메서드 종료
                }

                try {
                    // 문자열을 정수로 변환
                    int fishScore = Integer.parseInt(fishScoreStr);
                    // TextSource에 물고기 정보 추가
                    textSource.add(fishName, fishScore); 
                    // 성공 메시지 표시
                    JOptionPane.showMessageDialog(null, "물고기 '" + fishName + "'이(가) 추가되었습니다!");
                    // 입력 필드 초기화
                    fishNameField.setText(""); 
                    fishScoreField.setText(""); 
                } catch (NumberFormatException ex) {
                    // 점수가 정수가 아닐 경우 오류 메시지 표시
                    JOptionPane.showMessageDialog(null, "유효한 점수를 입력하세요!", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // 다른 생성자 (자동 생성된 코드)
    public EditPanel(GameSetupPanel gameSetupPanel, String string) {
        // TODO Auto-generated constructor stub
    }
}