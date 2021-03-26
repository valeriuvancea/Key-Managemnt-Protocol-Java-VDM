package test;

public class Main {
    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.unlockedMethod();
            System.out.println(test.lockedMethod());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("test");
    }
}
