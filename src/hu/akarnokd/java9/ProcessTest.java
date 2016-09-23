package hu.akarnokd.java9;

public class ProcessTest {

    public static void main(String[] args) {
        ProcessHandle ph = ProcessHandle.current();

        System.out.println(ph.getPid());

        System.out.println(ph.info().commandLine());
    }
}
