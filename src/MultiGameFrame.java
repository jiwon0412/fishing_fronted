import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

// 멀티플레이어 게임의 메인 프레임 클래스
public class MultiGameFrame extends JFrame {
    private String userName; // 사용자 이름
    private String level; // 게임 난이도
    private String ipAddress; // 서버 IP 주소
    private String portNo; // 서버 포트 번호
    
    private MultiGamePanel gamePanel; // 게임 패널
    
    // 버튼 이미지 아이콘
    private ImageIcon normalIcon = new ImageIcon("normal.png");
    private ImageIcon pressedIcon = new ImageIcon("pressed.png");
    private ImageIcon overIcon = new ImageIcon("over.png");

    // 메뉴 아이템
    private JMenuItem startItem = new JMenuItem("start");
    private JMenuItem stopItem = new JMenuItem("stop");

    // 툴바 버튼
    private JButton startBtn = new JButton("Start");
    private JButton stopBtn = new JButton("Stop");

    // 게임 관련 패널들
    private TextSource textSource = new TextSource(); // 물고기 데이터 소스
    private ScorePanel scorePanel = new ScorePanel(); // 점수 패널
    private LifePanel lifePanel = new LifePanel(); // 생명 패널

    // 생성자 - 게임 설정 정보를 받아서 프레임 초기화
    public MultiGameFrame(String userName, String level, String ipAddress, String portNo) {
        // 난이도가 null이면 기본값 "쉬움" 설정
        if (level == null) {
            level = "쉬움";
        }
        
        // 멤버 변수 초기화
        this.userName = userName;
        this.level = level;
        this.ipAddress = ipAddress;
        this.portNo = portNo;

        // 프레임 기본 설정
        setTitle(userName + "의 멀티플레이어 Fishing - 난이도: " + level);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 닫기 버튼 클릭 시 종료
        setSize(1000, 600); // 프레임 크기
        setResizable(false); // 크기 변경 불가

        // 게임 패널 생성
        gamePanel = new MultiGamePanel(scorePanel, lifePanel, level, userName, ipAddress, portNo);

        // UI 구성요소 생성
        makeMenu(); // 메뉴 바 생성
        makeToolBar(); // 툴바 생성
        makeSplit(); // 분할 패널 생성
        setVisible(true); // 프레임 표시
    }

    // 분할 패널 생성 - 게임 화면과 사이드 패널 배치
    private void makeSplit() {
        // 가로 분할 패널 생성 (게임 화면 | 사이드 패널)
        JSplitPane hPane = new JSplitPane();
        hPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        hPane.setDividerLocation(850); // 분할 위치 설정
        getContentPane().add(hPane, BorderLayout.CENTER);

        // 세로 분할 패널 생성 (점수 패널 / 생명 패널)
        JSplitPane vPane = new JSplitPane();
        vPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        hPane.setRightComponent(vPane); // 가로 분할의 오른쪽에 세로 분할 배치

        hPane.setLeftComponent(gamePanel); // 왼쪽에 게임 패널 배치
        vPane.setDividerLocation(250); // 세로 분할 위치 설정
        vPane.setTopComponent(scorePanel); // 위쪽에 점수 패널
        lifePanel.setPreferredSize(new Dimension(200, 50)); // 생명 패널 크기 설정
        vPane.setBottomComponent(lifePanel); // 아래쪽에 생명 패널
    }

    // 툴바 생성 및 버튼 추가
    private void makeToolBar() {
        JToolBar tBar = new JToolBar();
        tBar.setFloatable(false); // 툴바 이동 불가
        getContentPane().add(tBar, BorderLayout.NORTH); // 상단에 툴바 배치
        tBar.add(startBtn); // Start 버튼 추가
        tBar.add(stopBtn); // Stop 버튼 추가

        // 버튼 이벤트 리스너 등록
        startBtn.addActionListener(new StartAction());
        stopBtn.addActionListener(new StopAction());
    }

    // 메뉴 바 생성
    private void makeMenu() {
        JMenuBar mBar = new JMenuBar();
        this.setJMenuBar(mBar);

        // Fishing 메뉴 생성 및 아이템 추가
        JMenu fileMenu = new JMenu("Fishing");
        fileMenu.add(startItem); // start 메뉴 아이템
        fileMenu.add(stopItem); // stop 메뉴 아이템
        fileMenu.addSeparator(); // 구분선

        // exit 메뉴 아이템 생성 및 추가
        JMenuItem exitItem = new JMenuItem("exit");
        fileMenu.add(exitItem);
        mBar.add(fileMenu);

        // 메뉴 아이템 이벤트 리스너 등록
        startItem.addActionListener(new StartAction());
        stopItem.addActionListener(new StopAction());

        // exit 메뉴 클릭 시 종료 확인 다이얼로그 표시
        exitItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, 
                "게임을 종료하시겠습니까?", 
                "종료", 
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                dispose(); // 프레임 닫기
                System.exit(0); // 프로그램 종료
            }
        });
    }

    // Start 버튼/메뉴 액션 리스너
    private class StartAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            gamePanel.startGame(); // 게임 시작
        }
    }

    // Stop 버튼/메뉴 액션 리스너
    private class StopAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            gamePanel.stopGame(true); // 게임 정지
        }
    }

    // 메인 메서드 - 테스트용
    public static void main(String[] args) {
        // 테스트용 (순서: userName, level, ipAddress, portNo)
        new MultiGameFrame("테스터", "쉬움", "127.0.0.1", "30000");
    }
}