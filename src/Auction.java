// 경매 정보를 관리하는 클래스
public class Auction {
    private String seller;          // 판매자 이름
    private String fishName;        // 물고기 이름
    private int fishScore;          // 물고기 점수
    private int startPrice;         // 시작 가격
    private int currentPrice;       // 현재 최고 입찰가
    private String highestBidder;   // 최고 입찰자
    private long endTime;           // 경매 종료 시간
    
    // 경매 객체 생성자 - 30초 타이머로 경매 시작
    public Auction(String seller, String fishName, int fishScore, int startPrice) {
        this.seller = seller;
        this.fishName = fishName;
        this.fishScore = fishScore;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.highestBidder = "";
        this.endTime = System.currentTimeMillis() + 30000; // 30초 후 종료
    }
    
    // Getters - 각 필드 값 반환
    public String getSeller() { return seller; }
    public String getFishName() { return fishName; }
    public int getFishScore() { return fishScore; }
    public int getCurrentPrice() { return currentPrice; }
    public String getHighestBidder() { return highestBidder; }
    public long getEndTime() { return endTime; }
    
    // 입찰 처리 - 현재가보다 높으면 입찰 성공
    public boolean placeBid(String bidder, int bidAmount) {
        if (bidAmount > currentPrice) {
            currentPrice = bidAmount;
            highestBidder = bidder;
            return true;
        }
        return false;
    }
    
    // 경매 종료 여부 확인
    public boolean isExpired() {
        return System.currentTimeMillis() >= endTime;
    }
    
    // 남은 시간을 초 단위로 계산
    public int getRemainingSeconds() {
        long remaining = (endTime - System.currentTimeMillis()) / 1000;
        return (int) Math.max(0, remaining);
    }
}