import java.io.*;
import java.util.*;

public class TextSource {
    private List<String> fishNames = new ArrayList<>();  // fish_words.txt에서 가져온 물고기 이름 목록
    private Map<String, Integer> fishPrices = new HashMap<>();  // fish_prices.txt에서 가져온 물고기 가격 정보

    public TextSource() {
        loadFishNamesFromFile("fish_words.txt");  // fish_words.txt에서 물고기 이름을 로드
        loadFishPricesFromFile("fish_prices.txt");  // fish_prices.txt에서 물고기 가격을 로드
    }

    // fish_words.txt 파일에서 물고기 이름을 로드
    private void loadFishNamesFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                fishNames.add(line.trim());  // 물고기 이름 목록에 추가
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // fish_prices.txt 파일에서 물고기 가격 정보를 로드
    private void loadFishPricesFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String fishName = parts[0].trim();
                    int price = Integer.parseInt(parts[1].trim());
                    fishPrices.put(fishName, price);  // 물고기 가격을 fishPrices에 저장
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // fish_words.txt에서 랜덤 물고기 이름을 반환 (낚시 도구)
    public String getRandomFishWord() {
        Random rand = new Random();
        return fishNames.get(rand.nextInt(fishNames.size()));  // 랜덤으로 물고기 이름 반환
    }

    // fish_prices.txt에서 랜덤 물고기 이름을 반환 (기존 메서드 - 호환성 유지)
    public String getRandomFishPriceWord() {
        List<String> fishPriceWords = new ArrayList<>(fishPrices.keySet());
        Random rand = new Random();
        return fishPriceWords.get(rand.nextInt(fishPriceWords.size()));  // 랜덤으로 물고기 이름 반환
    }

    // 🎲 NEW! 가격에 따른 확률을 적용한 랜덤 물고기 반환
    public String getRandomFishPriceWordWithProbability() {
        Random rand = new Random();
        int randomValue = rand.nextInt(100); // 0-99 랜덤 값
        
        // 가격별로 물고기 분류
        List<String> cheap = new ArrayList<>();      // 50점 이하
        List<String> medium = new ArrayList<>();     // 51-90점
        List<String> expensive = new ArrayList<>();  // 91점 이상
        
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
        
        // 확률에 따라 선택
        if (randomValue < 50) {
            // 50% 확률 - 저렴한 물고기 (0-49)
            if (!cheap.isEmpty()) {
                return cheap.get(rand.nextInt(cheap.size()));
            }
        } else if (randomValue < 80) {
            // 30% 확률 - 중간 가격 물고기 (50-79)
            if (!medium.isEmpty()) {
                return medium.get(rand.nextInt(medium.size()));
            }
        } else {
            // 20% 확률 - 비싼 물고기 (80-99)
            if (!expensive.isEmpty()) {
                return expensive.get(rand.nextInt(expensive.size()));
            }
        }
        
        // 혹시 해당 가격대에 물고기가 없으면 아무거나 반환
        return getRandomFishPriceWord();
    }

    // fish_words.txt에 있는 모든 물고기 이름 반환
    public List<String> getFishWords() {
        return fishNames;
    }

    // fish_prices.txt에서 물고기 가격을 반환 (해당 단어가 없으면 0 반환)
    public Integer getFishPrice(String word) {
        return fishPrices.getOrDefault(word, 0);  // fish_prices.txt에 있는 물고기 가격 반환, 없으면 0
    }

    // 물고기 가격 정보 반환
    public Map<String, Integer> getFishPrices() {
        return fishPrices;
    }

    // 물고기 추가 메서드 (fish_prices.txt에만 적용)
    public void add(String fishName, int fishScore) {
        if (!fishPrices.containsKey(fishName)) {
            fishPrices.put(fishName, fishScore);  // 물고기 가격 추가
            appendFishToFile(fishName, fishScore);  // 파일에 추가된 물고기 정보 저장
        } else {
            // 물고기가 이미 존재하면 점수 업데이트
            fishPrices.put(fishName, fishScore);
            updateFishInFile(fishName, fishScore);  // 파일에서 가격을 업데이트
        }
    }

    // 물고기 정보를 파일에 추가하는 메서드
    private void appendFishToFile(String fishName, int fishScore) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("fish_prices.txt", true), "UTF-8")))) {
            writer.println(fishName + "," + fishScore);  // 파일에 물고기 이름과 가격 추가
            System.out.println("추가된 물고기: " + fishName + ", 가격: " + fishScore);  // 디버깅 로그
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 물고기 정보를 파일에서 업데이트하는 메서드 (예: 기존 물고기의 가격 변경)
    private void updateFishInFile(String fishName, int fishScore) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("fish_prices.txt"), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("fish_prices.txt"), "UTF-8")))) {
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].trim().equals(fishName)) {
                    writer.println(fishName + "," + fishScore);  // 가격 업데이트
                } else {
                    writer.println(line);  // 기존 내용 그대로
                }
            }
            System.out.println("파일 업데이트 완료: " + fishName + ", 가격: " + fishScore);  // 디버깅 로그
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}