public class Auction {
    private String seller;          // 판매자 이름
    private String fishName;        // 물고기 이름
    private int fishScore;          // 물고기 점수
    private int startPrice;         // 시작 가격
    private int currentPrice;       // 현재 최고 입찰가
    private String highestBidder;   // 최고 입찰자
    private long endTime;           // 경매 종료 시간
    
    public Auction(String seller, String fishName, int fishScore, int startPrice) {
        this.seller = seller;
        this.fishName = fishName;
        this.fishScore = fishScore;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.highestBidder = "";
        this.endTime = System.currentTimeMillis() + 30000; // 30초 후 종료
    }
    
    // Getters
    public String getSeller() { return seller; }
    public String getFishName() { return fishName; }
    public int getFishScore() { return fishScore; }
    public int getCurrentPrice() { return currentPrice; }
    public String getHighestBidder() { return highestBidder; }
    public long getEndTime() { return endTime; }
    
    // 입찰하기
    public boolean placeBid(String bidder, int bidAmount) {
        if (bidAmount > currentPrice) {
            currentPrice = bidAmount;
            highestBidder = bidder;
            return true;
        }
        return false;
    }
    
    // 경매 종료 여부
    public boolean isExpired() {
        return System.currentTimeMillis() >= endTime;
    }
    
    // 남은 시간 (초)
    public int getRemainingSeconds() {
        long remaining = (endTime - System.currentTimeMillis()) / 1000;
        return (int) Math.max(0, remaining);
    }
}