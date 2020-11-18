package Mypackage;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.*;

public class VkApi {

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException, JSONException {
        try {
            System.out.print("Введите id группы: ");
            Scanner scanner = new Scanner(System.in);
            String groupID = scanner.next();

            Map<String, Integer> citiesMap = new HashMap<String, Integer>();
            int offset = 0;
            while (true) {
                URL url = new URL(String.format(
                        "https://api.vk.com/method/groups.getMembers?group_id=%s&fields=city&offset=%d&access_token=6a6f00e00e32891b7922d7c1ccb908a78de2c0f6b2abfe7a66ac9b634338b5b8c02848d4a1fe8f309d553&v=5.124",
                        groupID,
                        offset));
                URLConnection yc = url.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                yc.getInputStream()));
                String inputLine = readAll(in);
                in.close();

                JSONObject json = new JSONObject(inputLine);
                JSONArray members = (JSONArray) json.getJSONObject("response").get("items");

                if (members.length() <= 0) {
                    break;
                }

                for (int i = 0; i < members.length(); i++) {
                    JSONObject member = (JSONObject) members.get(i);
                    try {
                        String city = ((JSONObject) member.get("city")).get("title").toString();
                        if (citiesMap.containsKey(city)) {
                            citiesMap.put(city, citiesMap.get(city) + 1);
                        } else {
                            citiesMap.put(city, 1);
                        }
                    } catch (Exception e) {

                    }
                }

                System.out.println("Counted: " + (offset + members.length()));
                offset += 1000;
                if (offset % 5000 == 0) {
                    System.out.println("500ms waiting (DDOS protection)");
                    Thread.sleep(500);
                }
            }

            ArrayList<Map.Entry<String, Integer>> sortedCities = new ArrayList<>();
            int membersCount = 0;
            for (Map.Entry<String, Integer> entry : citiesMap.entrySet()) {
                sortedCities.add(entry);
                membersCount += entry.getValue();
            }
            sortedCities.sort(new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue() - o1.getValue();
                }
            });
            System.out.println("\n\n"+groupID);
            System.out.format("\nКоличество участников, у которых указан город: %d\n\n", membersCount);
            for (Map.Entry<String, Integer> entry : sortedCities) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                System.out.println(String.format("%-20s -> %-4s = %f%%", key, value, ((double) value / membersCount) * 100));
            }

        } catch (Exception e) {
            System.out.println("EXCEPTION - " + e);
        }
    }

}
