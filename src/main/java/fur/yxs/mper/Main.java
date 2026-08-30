package fur.yxs.mper;

import fur.yxs.mper.core.App;

public class Main {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:data.db");
            App app = new App();
            app.run(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}