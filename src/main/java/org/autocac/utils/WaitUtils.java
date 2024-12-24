package  org.autocac.utils;
import java.util.Random;

public class WaitUtils {
    public static int randomWait(int minSeconds, int maxSeconds) {
        Random random = new Random();
        int waitTime = random.nextInt(maxSeconds - minSeconds + 1) + minSeconds;
        System.out.println("Aguardando " + waitTime + " segundos...");
        try {
            Thread.sleep(waitTime * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Erro ao aguardar: " + e.getMessage());
        }
        return waitTime;
    }
}
