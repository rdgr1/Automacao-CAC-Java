package  org.autocac.utils;
import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

public class LoadingUtils {

    /**
     * Exibe uma barra de progresso animada no console com cores.
     *
     * @param message  Mensagem inicial.
     * @param steps    Número de passos para completar.
     * @param interval Tempo (em milissegundos) entre os passos.
     */
    public static void showLoading(String message, int totalDuration, int interval) {
        String[] spinner = {"⣾", "⣽", "⣻", "⢿", "⡿", "⣟", "⣯", "⣷"};
        int steps = totalDuration / interval;
        // Definição de cores
        Attribute progressColor = Attribute.CYAN_TEXT();    // Cor da barra
        Attribute spinnerColor = Attribute.YELLOW_TEXT();   // Cor do spinner
        Attribute completeColor = Attribute.GREEN_TEXT();   // Cor de conclusão

        System.out.print(Ansi.colorize(message, Attribute.BOLD(), Attribute.BRIGHT_BLUE_TEXT()) + " [");

        for (int i = 0; i < steps; i++) {
            try {
                Thread.sleep(interval); // Aguarda pelo intervalo definido
            } catch (InterruptedException e) {
                System.out.println("\n" + Ansi.colorize("Loading interrompido.", Attribute.RED_TEXT()));
                return;
            }

            String progressBar = Ansi.colorize("#".repeat(i + 1), progressColor); // Barra preenchida
            String emptySpace = " ".repeat(steps - i - 1); // Espaços vazios
            String spinnerFrame = Ansi.colorize(spinner[i % spinner.length], spinnerColor); // Spinner animado

            System.out.print("\r" + Ansi.colorize(message, Attribute.BOLD(), Attribute.BRIGHT_BLUE_TEXT())
                    + " [" + progressBar + emptySpace + "] " + spinnerFrame);
        }

        System.out.println("\r" + Ansi.colorize(message, Attribute.BOLD(), Attribute.BRIGHT_BLUE_TEXT())
                + " [" + Ansi.colorize("#".repeat(steps), completeColor) + "] ✅ Completo!");
    }
}