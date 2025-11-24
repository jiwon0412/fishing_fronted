public class Fish {
    private String name;  // 물고기 이름
    private int score;    // 물고기 점수

    // 생성자
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

    // 물고기 점수 설정 (필요한 경우 점수 업데이트)
    public void setScore(int score) {
        this.score = score;
    }

    // 물고기 이름과 점수를 출력하는 메서드
    @Override
    public String toString() {
        return name + ": " + score + "점";
    }
}
