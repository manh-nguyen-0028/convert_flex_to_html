import constants.Constants;

public class RegexExample {
    public static void main(String[] args) {
        String input = "Your script.btnLoginOnClickHandler() here";

        // Sử dụng regex để nhận diện và thay thế chuỗi
        String result = input.replaceAll("script\\.(.*?)\\(\\)", "#{" + "MG1001001_00_000" + Constants.CLASS_CONTROLLER + "." + "$1}");

        // In kết quả
        System.out.println(result);
    }
}