import java.io.*;
import java.util.*;

public class TextSource {
    private List<String> fishNames = new ArrayList<>();  // fish_words.txt의 물고기 이름 목록
    private Map<String, Integer> fishPrices = new HashMap<>();  // fish_prices.txt의 물고기 가격 정보

    // 생성자: 두 개의 텍스트 파일에서 데이터 로드
    public TextSource() {
        loadFishNamesFromFile("fish_words.txt");
        loadFishPricesFromFile("fish_prices.txt");
    }

    // fish_words.txt에서 물고기 이름 로드
    private void loadFishNamesFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                fishNames.add(line.trim());  // 공백 제거 후 목록에 추가
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // fish_prices.txt에서 물고기 가격 정보 로드 (이름,가격 형식)
    private void loadFishPricesFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");  // 쉼표로 분리
                if (parts.length == 2) {
                    String fishName = parts[0].trim();
                    int price = Integer.parseInt(parts[1].trim());
                    fishPrices.put(fishName, price);  // 해시맵에 저장
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // fish_words.txt에서 랜덤 물고기 이름 반환
    public String getRandomFishWord() {
        Random rand = new Random();
        return fishNames.get(rand.nextInt(fishNames.size()));
    }

    // fish_prices.txt에서 랜덤 물고기 이름 반환 (기존 호환성 유지)
    public String getRandomFishPriceWord() {
        List<String> fishPriceWords = new ArrayList<>(fishPrices.keySet());
        Random rand = new Random();
        return fishPriceWords.get(rand.nextInt(fishPriceWords.size()));
    }

    // 가격에 따른 확률을 적용한 랜덤 물고기 반환
    public String getRandomFishPriceWordWithProbability() {
        Random rand = new Random();
        int randomValue = rand.nextInt(100);  // 0-99 사이 랜덤 값
        
        // 가격별로 물고기 분류
        List<String> cheap = new ArrayList<>();      // 50점 이하
        List<String> medium = new ArrayList<>();     // 51-90점
        List<String> expensive = new ArrayList<>();  // 91점 이상
        
        // 모든 물고기를 가격대별로 분류
        for (Map.Entry<String, Integer> entry : fishPrices.entrySet()) {
            int price = entry.getValue();
            String fishName = entry.getKey();
            
            if (price <= 50) {
                cheap.add(fishName);
            } else if (price <= 90) {
                medium.add(fishName);
            } else {
                expensive.add(fishName);
            }
        }
        
        // 확률에 따라 가격대 선택 (저렴 50%, 중간 30%, 비싼 20%)
        if (randomValue < 50) {
            // 50% 확률로 저렴한 물고기
            if (!cheap.isEmpty()) {
                return cheap.get(rand.nextInt(cheap.size()));
            }
        } else if (randomValue < 80) {
            // 30% 확률로 중간 가격 물고기
            if (!medium.isEmpty()) {
                return medium.get(rand.nextInt(medium.size()));
            }
        } else {
            // 20% 확률로 비싼 물고기
            if (!expensive.isEmpty()) {
                return expensive.get(rand.nextInt(expensive.size()));
            }
        }
        
        // 해당 가격대에 물고기가 없으면 무작위 반환
        return getRandomFishPriceWord();
    }

    // fish_words.txt의 모든 물고기 이름 목록 반환
    public List<String> getFishWords() {
        return fishNames;
    }

    // 특정 물고기의 가격 반환 (없으면 0 반환)
    public Integer getFishPrice(String word) {
        return fishPrices.getOrDefault(word, 0);
    }

    // 물고기 가격 정보 전체 반환
    public Map<String, Integer> getFishPrices() {
        return fishPrices;
    }
}