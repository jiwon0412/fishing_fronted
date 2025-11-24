import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FishingGameServer {
    static ArrayList<ClientHandler> clients = new ArrayList<>();
    static int clientCount = 0;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(30000);
        System.out.println("=================================");
        System.out.println("🎣 낚시 타자 게임 서버 시작!");
        System.out.println("포트: 30000");
        System.out.println("=================================");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("새로운 플레이어 접속!");

            ClientHandler handler = new ClientHandler(socket, clientCount);
            clients.add(handler);
            handler.start();
            clientCount++;
        }
    }

    // 모든 클라이언트에게 메시지 브로드캐스트
    public static void broadcast(String message, ClientHandler sender) {
        System.out.println("브로드캐스트: " + message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // 특정 클라이언트 제거
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println(client.userName + " 퇴장. 남은 플레이어: " + clients.size());
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    String userName;
    private int clientId;

    public ClientHandler(Socket socket, int clientId) {
        this.socket = socket;
        this.clientId = clientId;
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // 첫 메시지는 로그인 정보
            String firstMsg = dis.readUTF();
            if (firstMsg.startsWith("/login ")) {
                userName = firstMsg.substring(7).trim();
                System.out.println("플레이어 입장: " + userName);
                
                // 입장 메시지 브로드캐스트
                FishingGameServer.broadcast("/system " + userName + "님이 입장했습니다!", this);
            }

            // 메시지 수신 루프
            while (true) {
                String message = dis.readUTF();
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println(userName + " 연결 종료");
        } finally {
            cleanup();
        }
    }

    // 메시지 처리
    private void handleMessage(String message) {
        if (message.startsWith("/chat ")) {
            // 채팅 메시지
            String chatMsg = message.substring(6);
            FishingGameServer.broadcast("/chat [" + userName + "] " + chatMsg, this);
            
        } else if (message.startsWith("/catch ")) {
            // 물고기 잡았을 때
            // 형식: /catch 물고기이름 점수
            String[] parts = message.substring(7).split(" ");
            if (parts.length >= 2) {
                String fishName = parts[0];
                String score = parts[1];
                FishingGameServer.broadcast(
                    "/catch " + userName + "님이 " + fishName + "(" + score + "점)를 잡았습니다!", 
                    this
                );
            }
        } else if (message.startsWith("/score ")) {
            // 점수 업데이트
            String scoreInfo = message.substring(7);
            FishingGameServer.broadcast("/score " + userName + " " + scoreInfo, this);
            
        } else {
            // 기타 메시지는 그대로 브로드캐스트
            FishingGameServer.broadcast(message, this);
        }
    }

    // 클라이언트에게 메시지 전송
    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException e) {
            System.out.println(userName + "에게 메시지 전송 실패");
        }
    }

    // 리소스 정리
    private void cleanup() {
        try {
            if (userName != null) {
                FishingGameServer.broadcast("/system " + userName + "님이 퇴장했습니다.", this);
            }
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (socket != null && !socket.isClosed()) socket.close();
            FishingGameServer.removeClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}