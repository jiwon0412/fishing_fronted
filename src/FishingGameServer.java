import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FishingGameServer {
    static ArrayList<ClientHandler> clients = new ArrayList<>();  // 접속한 클라이언트 목록
    static int clientCount = 0;  // 접속한 클라이언트 총 수

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(30000);  // 30000번 포트로 서버 소켓 생성
        System.out.println("=================================");
        System.out.println("🎣 낚시 타자 게임 서버 시작!");
        System.out.println("포트: 30000");
        System.out.println("=================================");

        while (true) {  // 무한 루프로 클라이언트 접속 대기
            Socket socket = serverSocket.accept();  // 클라이언트 접속 수락
            System.out.println("새로운 플레이어 접속!");

            ClientHandler handler = new ClientHandler(socket, clientCount);  // 클라이언트 핸들러 생성
            clients.add(handler);  // 클라이언트 목록에 추가
            handler.start();  // 스레드 시작
            clientCount++;  // 클라이언트 카운트 증가
        }
    }

    // 모든 클라이언트에게 메시지 브로드캐스트
    public static void broadcast(String message, ClientHandler sender) {
        System.out.println("브로드캐스트: " + message);
        for (ClientHandler client : clients) {  // 모든 클라이언트에게 전송
            client.sendMessage(message);
        }
    }

    // 특정 클라이언트를 목록에서 제거
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println(client.userName + " 퇴장. 남은 플레이어: " + clients.size());
    }
}

class ClientHandler extends Thread {
    private Socket socket;  // 클라이언트 소켓
    private DataInputStream dis;  // 입력 스트림
    private DataOutputStream dos;  // 출력 스트림
    String userName;  // 클라이언트 이름
    private int clientId;  // 클라이언트 ID

    // 생성자: 소켓과 클라이언트 ID를 받아 초기화
    public ClientHandler(Socket socket, int clientId) {
        this.socket = socket;
        this.clientId = clientId;
        try {
            dis = new DataInputStream(socket.getInputStream());  // 입력 스트림 생성
            dos = new DataOutputStream(socket.getOutputStream());  // 출력 스트림 생성
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // 첫 메시지로 로그인 정보 수신
            String firstMsg = dis.readUTF();
            if (firstMsg.startsWith("/login ")) {  // 로그인 명령어 확인
                userName = firstMsg.substring(7).trim();  // 사용자 이름 추출
                System.out.println("플레이어 입장: " + userName);
                
                // 입장 메시지를 모든 클라이언트에게 브로드캐스트
                FishingGameServer.broadcast("/system " + userName + "님이 입장했습니다!", this);
            }

            // 메시지 수신 루프
            while (true) {
                String message = dis.readUTF();  // 메시지 수신
                handleMessage(message);  // 메시지 처리
            }
        } catch (IOException e) {
            System.out.println(userName + " 연결 종료");
        } finally {
            cleanup();  // 리소스 정리
        }
    }

    // 수신한 메시지를 유형별로 처리
    private void handleMessage(String message) {
        if (message.startsWith("/chat ")) {  // 채팅 메시지인 경우
            String chatMsg = message.substring(6);
            FishingGameServer.broadcast("/chat [" + userName + "] " + chatMsg, this);
            
        } else if (message.startsWith("/catch ")) {  // 물고기를 잡은 경우
            String[] parts = message.substring(7).split(" ");
            if (parts.length >= 2) {
                String fishName = parts[0];
                String score = parts[1];
                FishingGameServer.broadcast(
                    "/catch " + userName + "님이 " + fishName + "(" + score + "점)를 잡았습니다!", 
                    this
                );
            }
        } 
        else if (message.startsWith("/auction start ")) {  // 경매 시작인 경우
            String[] parts = message.substring(15).split(" ");
            if (parts.length >= 3) {
                String fishName = parts[0];
                String fishScore = parts[1];
                String startPrice = parts[2];
                FishingGameServer.broadcast(
                    "/auction start " + userName + " " + fishName + " " + fishScore + " " + startPrice,
                    this
                );
            }
            
        } else if (message.startsWith("/auction bid ")) {  // 경매 입찰인 경우
            String bidAmount = message.substring(13);
            FishingGameServer.broadcast(
                "/auction bid " + userName + " " + bidAmount,
                this
            );
            
        } else if (message.startsWith("/auction end ")) {  // 경매 종료인 경우
            String endInfo = message.substring(13);
            FishingGameServer.broadcast(
                "/auction end " + endInfo,
                this
            );
        }
        else if (message.startsWith("/score ")) {  // 점수 업데이트인 경우
            String scoreInfo = message.substring(7);
            FishingGameServer.broadcast("/score " + userName + " " + scoreInfo, this);
            
        } else {  // 기타 메시지는 그대로 브로드캐스트
            FishingGameServer.broadcast(message, this);
        }
    }

    // 클라이언트에게 메시지 전송
    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);  // 메시지 쓰기
            dos.flush();  // 버퍼 비우기
        } catch (IOException e) {
            System.out.println(userName + "에게 메시지 전송 실패");
        }
    }

    // 연결 종료 시 리소스 정리
    private void cleanup() {
        try {
            if (userName != null) {  // 사용자 이름이 있으면 퇴장 메시지 전송
                FishingGameServer.broadcast("/system " + userName + "님이 퇴장했습니다.", this);
            }
            if (dis != null) dis.close();  // 입력 스트림 닫기
            if (dos != null) dos.close();  // 출력 스트림 닫기
            if (socket != null && !socket.isClosed()) socket.close();  // 소켓 닫기
            FishingGameServer.removeClient(this);  // 클라이언트 목록에서 제거
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}