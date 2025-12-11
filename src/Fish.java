public class Fish {
    private String name;  // 물고기 이름
    private int score;    // 물고기 점수

    // 생성자: 물고기 이름과 점수를 받아 객체 생성
    public Fish(String name, int score) {
        this.name = name;
        this.score = score;
    }

    // 물고기 이름 반환
    public String getName() {
        return name;
    }

    // 물고기 점수 반환
    public int getScore() {
        return score;
    }

    // 물고기 점수 설정 (점수 업데이트 시 사용)
    public void setScore(int score) {
        this.score = score;
    }

    // 물고기 정보를 문자열로 반환 (이름: 점수점 형식)
    @Override
    public String toString() {
        return name + ": " + score + "점";
    }
}