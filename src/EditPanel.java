import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditPanel extends JPanel {
    private JTextField fishNameField = new JTextField(10);  // 물고기 이름 입력 필드
    private JTextField fishScoreField = new JTextField(10); // 물고기 점수 입력 필드
    private JButton addButton = new JButton("물고기 추가"); // 물고기 추가 버튼
    private TextSource textSource; // 물고기 정보를 저장하고 관리하는 TextSource 객체

    // EditPanel 생성자, TextSource 객체를 받아서 초기화
    public EditPanel(TextSource textSource) {
        this.textSource = textSource;

        // 레이아웃 설정: Y축 방향으로 구성
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 물고기 이름 입력 패널 생성
        JPanel namePanel = new JPanel();
        namePanel.add(new JLabel("물고기 이름:")); // 라벨 추가
        namePanel.add(fishNameField); // 물고기 이름 입력 필드 추가
        add(namePanel); // 이 패널을 EditPanel에 추가

        // 물고기 점수 입력 패널 생성
        JPanel scorePanel = new JPanel();
        scorePanel.add(new JLabel("물고기 점수:")); // 라벨 추가
        scorePanel.add(fishScoreField); // 물고기 점수 입력 필드 추가
        add(scorePanel); // 이 패널을 EditPanel에 추가

        // 물고기 추가 버튼
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton); // 추가 버튼을 buttonPanel에 추가
        add(buttonPanel); // 버튼 패널을 EditPanel에 추가

        // 물고기 추가 버튼 이벤트 처리
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fishName = fishNameField.getText().trim(); // 물고기 이름을 입력 필드에서 가져오기
                String fishScoreStr = fishScoreField.getText().trim(); // 물고기 점수를 입력 필드에서 가져오기

                // 입력 값이 비어있는지 확인
                if (fishName.isEmpty() || fishScoreStr.isEmpty()) {
                	 // 물고기 이름과 점수를 입력하지 않으면 오류 메시지 표시
                    JOptionPane.showMessageDialog(null, "물고기 이름과 점수를 모두 입력해주세요!", "오류", JOptionPane.ERROR_MESSAGE);
                    return; // 입력이 없으면 메서드 종료
                }

                try {
                    // 입력된 점수를 정수로 변환
                    int fishScore = Integer.parseInt(fishScoreStr);
                    // TextSource 객체에 물고기 이름과 점수를 추가
                    textSource.add(fishName, fishScore); 
                    // 물고기 추가 성공 메시지
                    JOptionPane.showMessageDialog(null, "물고기 '" + fishName + "'이(가) 추가되었습니다!");
                    // 입력 필드 초기화
                    fishNameField.setText(""); 
                    fishScoreField.setText(""); 
                } catch (NumberFormatException ex) {
                    // 점수가 정수가 아닐 경우 예외 처리
                    JOptionPane.showMessageDialog(null, "유효한 점수를 입력하세요!", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

    }
}
